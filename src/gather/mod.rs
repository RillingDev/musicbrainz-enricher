use tokio_postgres::Client;

mod genre_matcher;

pub async fn run_gather(db_client: Client) -> anyhow::Result<()> {
	Ok(())
}
