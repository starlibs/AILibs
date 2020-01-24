package ai.libs.jaicore.ml.weka.classification.pipeline;

public class PreprocessingException extends Exception {

	private static final long serialVersionUID = -5454710107294165881L;

	public PreprocessingException(final Throwable t) {
		super(t);
	}

	public PreprocessingException(final String message) {
		super(message);
	}

	public PreprocessingException(final String message, final Throwable t) {
		super(message, t);
	}
}
