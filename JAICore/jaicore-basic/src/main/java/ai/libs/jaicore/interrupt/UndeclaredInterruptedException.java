package ai.libs.jaicore.interrupt;

public class UndeclaredInterruptedException extends RuntimeException {

	private static final long serialVersionUID = 1387159071444885644L;

	public UndeclaredInterruptedException(final String msg) {
		super(msg);
	}

	public UndeclaredInterruptedException(final String msg, final InterruptedException e) {
		super(msg, e);
	}

	public UndeclaredInterruptedException(final InterruptedException e) {
		super(e);
	}
}
