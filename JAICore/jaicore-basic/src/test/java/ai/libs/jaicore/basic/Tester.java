package ai.libs.jaicore.basic;

import org.api4.java.common.control.ILoggingCustomizable;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.logging.LoggerUtil;

public abstract class Tester implements ILoggingCustomizable {

	protected final static Logger LOGGER = LoggerFactory.getLogger(LoggerUtil.LOGGER_NAME_TESTER);

	protected Logger logger = LoggerFactory.getLogger(LoggerUtil.LOGGER_NAME_TESTER + "." + this.getClass().getSimpleName().toLowerCase());

	@Rule
	public Timeout globalTimeout = Timeout.seconds(120); // no test should run longer than two minutes

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	protected Logger getLogger() {
		return this.logger;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger name from {} to {}.", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger name to {}.", name);
	}
}
