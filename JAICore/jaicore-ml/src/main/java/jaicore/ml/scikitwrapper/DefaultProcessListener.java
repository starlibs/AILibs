package jaicore.ml.scikitwrapper;

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

	/**
	 * Constructor to initialize the DefaultProcessListener.
	 * @param verbose Flag whether standard outputs are forwarded to the logger.
	 */
	public DefaultProcessListener(final boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public void handleError(final String error) {
		L.error(">>> {}", error);
	}

	@Override
	public void handleInput(final String input) throws IOException, InterruptedException {
		if (this.verbose) {
			L.info(">>> {}", input);
		}
	}

}
