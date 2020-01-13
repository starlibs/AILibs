package ai.libs.jaicore.basic.kvstore;

public class ParseKVStoreFromDescriptionFailedException extends RuntimeException {

	public ParseKVStoreFromDescriptionFailedException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public ParseKVStoreFromDescriptionFailedException(final String msg) {
		super(msg);
	}

	public ParseKVStoreFromDescriptionFailedException(final Throwable cause) {
		super(cause);
	}
}
