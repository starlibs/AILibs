package jaicore.ml.scikitwrapper;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultProcessListener extends AProcessListener {
	private static final Logger L = LoggerFactory.getLogger(DefaultProcessListener.class);

	protected final boolean verbose;

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
