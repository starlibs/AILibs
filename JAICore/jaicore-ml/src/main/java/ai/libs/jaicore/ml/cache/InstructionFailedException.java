package ai.libs.jaicore.ml.cache;

public class InstructionFailedException extends Exception {

	private static final long serialVersionUID = -3672656226527248715L;

	public InstructionFailedException(final Exception e) {
		super(e);
	}

}
