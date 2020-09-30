package ai.libs.jaicore.basic;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.stream.Collectors;

import org.api4.java.common.control.ILoggingCustomizable;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.concurrent.GlobalTimer;
import ai.libs.jaicore.concurrent.TrackableTimerTask;
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

	@BeforeEach
	public void checkThreadStatusBefore() throws InterruptedException {
		this.checkThreadStatus("before");
	}

	@AfterEach
	public void checkThreadStatusAfter() throws InterruptedException {
		this.checkThreadStatus("after");
	}

	public void checkThreadStatus(final String situation) throws InterruptedException {
		this.logger.info("Checking thread status: {}", situation);
		assert !Thread.currentThread().isInterrupted() : "Execution thread must not be interrupted " + situation + " test!";
		Collection<TrackableTimerTask> unresolvedTasks = GlobalTimer.getInstance().getActiveTasks();
		if (!unresolvedTasks.isEmpty()) {
			String msg = "Global Timer has " + unresolvedTasks.size() + " active jobs " + situation + " test: " + unresolvedTasks.stream().map(t -> "\n\t- " + t.toString()).collect(Collectors.joining());
			while (GlobalTimer.getInstance().getNumberOfActiveTasks() > 0) {
				this.logger.info("Waiting for timer to shutdown ...");
				Thread.sleep(100);
			}
			fail(msg);
		}
		assertTrue(unresolvedTasks.isEmpty());
	}
}
