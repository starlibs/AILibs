package ai.libs.jaicore.ml.scikitwrapper;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DefaultProcessListener might be used to forward any type of outputs of a process to a logger.
 *
 * @author wever
 */
public class DefaultProcessListener extends AProcessListener {

	/* Logging */
	private static final Logger L = LoggerFactory.getLogger(DefaultProcessListener.class);

	/**
	 * Flag whether standard outputs are forwarded to the logger.
	 */
	protected final boolean verbose;

	private StringBuilder errorSB;
	private StringBuilder defaultSB;

	/**
	 * Constructor to initialize the DefaultProcessListener.
	 *
	 * @param verbose Flag whether standard outputs are forwarded to the logger.
	 */
	public DefaultProcessListener(final boolean verbose) {
		super();
		this.verbose = verbose;
		this.errorSB = new StringBuilder();
		this.defaultSB = new StringBuilder();
	}

	/**
	 * Constructor to initialize the DefaultProcessListener.
	 *
	 * @param verbose Flag whether standard outputs are forwarded to the logger.
	 */
	public DefaultProcessListener(final boolean verbose, final boolean listenToPIDFromProcess) {
		super(listenToPIDFromProcess);
		this.verbose = verbose;
		this.errorSB = new StringBuilder();
		this.defaultSB = new StringBuilder();
	}

	@Override
	public void handleError(final String error) {
		this.errorSB.append(error + "\n");
		if (this.verbose) {
			L.error(">>> {}", error);
		}
	}

	@Override
	public void handleInput(final String input) throws IOException, InterruptedException {
		this.defaultSB.append(input + "\n");
		if (this.verbose) {
			L.info(">>> {}", input);
		}
	}

	public String getErrorOutput() {
		return this.errorSB.toString();
	}

	public String getDefaultOutput() {
		return this.defaultSB.toString();
	}

}
