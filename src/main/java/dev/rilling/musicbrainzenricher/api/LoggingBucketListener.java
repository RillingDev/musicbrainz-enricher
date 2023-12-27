package dev.rilling.musicbrainzenricher.api;

import io.github.bucket4j.BucketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingBucketListener implements BucketListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingBucketListener.class);

	private final String bucketName;

	public LoggingBucketListener(String bucketName) {
		this.bucketName = bucketName;
	}

	@Override
	public void onConsumed(long tokens) {
		LOGGER.trace("Consumed {} token(s) from bucket '{}'.", tokens, bucketName);
	}

	@Override
	public void onRejected(long tokens) {
		LOGGER.trace("Rejected consumption of {} token(s) from bucket '{}'.", tokens, bucketName);
	}

	@Override
	public void onParked(long nanos) {
		// noop
	}

	@Override
	public void onInterrupted(InterruptedException e) {
		LOGGER.trace("Interrupted waiting for bucket '{}'.", bucketName);
	}

	@Override
	public void onDelayed(long nanos) {
		// noop
	}
}
