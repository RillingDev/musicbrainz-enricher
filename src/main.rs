use anyhow::Ok;
use clap::{Parser, Subcommand};
use log::{error, info};
use reqwest::Url;
use tokio_postgres::{Client, NoTls};

use crate::musicbrainz::{MusicbrainzClient, MusicbrainzCredentials};
use crate::sql::{init_schema, select_merged_results};

mod musicbrainz;
mod sql;

#[derive(Parser)]
#[command(version, about, long_about = None)]
struct Cli {
	#[command(flatten)]
	verbosity: clap_verbosity_flag::Verbosity<clap_verbosity_flag::InfoLevel>,

	#[command(subcommand)]
	command: Command,
}

#[derive(Subcommand)]
#[command()]
enum Command {
	#[command(about = "Submit gathered data")]
	Submit {
		#[arg(
			long = "musicbrainz-url",
			required = true,
			help = "Musicbrainz base URL, e.g. `https://musicbrainz.org` or `https://test.musicbrainz.org`"
		)]
		musicbrainz_url: Url,

		#[arg(
			long = "musicbrainz-username",
			required = true,
			help = "Musicbrainz bot account username"
		)]
		musicbrainz_username: String,

		#[arg(
			long = "musicbrainz-password",
			required = true,
			help = "Musicbrainz bot account password"
		)]
		musicbrainz_password: String,
	},
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
	let args = Cli::parse();
	env_logger::builder()
		.filter_level(args.verbosity.into())
		.init();

	let (mut db_client, db_connection) = tokio_postgres::connect(
		// This is hardcoded as the application is supposed to run against a local copy of the musicbrainz mirror database
		"host=localhost port=5432 user=musicbrainz password=musicbrainz dbname=musicbrainz_db",
		NoTls,
	)
	.await?;
	// The connection object performs the actual communication with the database,
	// so spawn it off to run on its own.
	tokio::spawn(async move {
		if let Err(e) = db_connection.await {
			error!("Connection error: {e}");
		}
	});
	init_schema(&mut db_client).await?;

	match args.command {
		Command::Submit {
			musicbrainz_url,
			musicbrainz_username,
			musicbrainz_password,
		} => {
			run_submit(
				db_client,
				musicbrainz_url,
				MusicbrainzCredentials {
					username: musicbrainz_username,
					password: musicbrainz_password,
				},
			)
			.await
		}
	}
}

const TAG_SUBMISSION_CHUNK_SIZE: u32 = 25;

async fn run_submit(
	mut db_client: Client,
	musicbrainz_url: Url,
	musicbrainz_credentials: MusicbrainzCredentials,
) -> anyhow::Result<()> {
	let mb_client = MusicbrainzClient::new(musicbrainz_url, musicbrainz_credentials)?;

	let mut offset: u32 = 0;
	let mut results;

	loop {
		results = select_merged_results(&mut db_client, TAG_SUBMISSION_CHUNK_SIZE, offset).await?;
		let result_len: u32 = results.len().try_into()?;

		if result_len == 0 {
			break;
		}

		info!("Selected {result_len} results for submission.");
		mb_client.submit_tags(results).await?;

		offset += result_len;
	}

	Ok(())
}
