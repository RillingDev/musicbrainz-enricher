use clap::{Parser, Subcommand};
use log::error;
use reqwest::Url;
use tokio_postgres::NoTls;

use crate::{
	gather::{gatherer, run_gather},
	sql::init_schema,
	submit::{MusicbrainzCredentials, run_submit},
};

mod gather;
mod http;
mod sql;
mod submit;

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
	#[command(about = "Gathered data from relations")]
	Gather {
		#[arg(long = "discogs-token", required = false, help = "Discogs API token")]
		discogs_token: Option<String>,
	},

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
			short = 'u',
			required = true,
			help = "Musicbrainz bot account username"
		)]
		musicbrainz_username: String,

		#[arg(
			long = "musicbrainz-password",
			short = 'p',
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

	let (db_client, db_connection) = tokio_postgres::connect(
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
	init_schema(&db_client).await?;

	match args.command {
		Command::Gather { discogs_token } => {
			run_gather(
				db_client,
				discogs_token.map(|token| gatherer::discogs::DiscogsCredentials { token }),
			)
			.await
		}
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
