package ai.libs.jaicore.basic;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.Collection;
import java.util.stream.Collectors;

import org.api4.java.common.control.ILoggingCustomizable;
import org.awaitility.Awaitility;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.concurrent.CancellationTimerTask;
import ai.libs.jaicore.concurrent.GlobalTimer;
import ai.libs.jaicore.concurrent.NamedTimerTask;
import ai.libs.jaicore.concurrent.TrackableTimerTask;
import ai.libs.jaicore.interrupt.InterruptionTimerTask;
import ai.libs.jaicore.logging.LoggerUtil;

public abstract class ATest implements ILoggingCustomizable {

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
		Collection<TrackableTimerTask> unresolvedTasks = GlobalTimer.getInstance().getActiveTasks().stream().filter(t -> t != GlobalTimer.INIT_TASK).collect(Collectors.toList());
		if (!unresolvedTasks.isEmpty()) {
			String msg = "Global Timer has " + unresolvedTasks.size() + " active jobs " + situation + " test: " + unresolvedTasks.stream().map(t -> {
				StringBuilder sb = new StringBuilder();
				sb.append(t.getClass().getName());
				sb.append(" (" + (t.hasBeenExecuted() ? "" : "not ")  + "executed, " + (t.isFinished() ? "" : "not ") + "finished)");
				if (t instanceof NamedTimerTask) {
					sb.append(": ");
					sb.append(((NamedTimerTask) t).getDescriptor());
					if (t instanceof CancellationTimerTask) {
						sb.append(" - to cancel " + ((CancellationTimerTask)t).getCancelable());
					}
					if (t instanceof InterruptionTimerTask) {
						InterruptionTimerTask it = (InterruptionTimerTask)t;
						sb.append(" - to interrupt " + it.getThreadToBeInterrupted() + " with reason " + it.getReason() + " (" + (it.isTriggered() ? "" : "not ")  +"triggered)");
					}
				}
				return "\n\t- " + sb.toString();
			}).collect(Collectors.joining());
			while (GlobalTimer.getInstance().getNumberOfActiveTasks() > 0) {
				this.logger.info("Waiting for timer to shutdown ...");
				Awaitility.await().atLeast(Duration.ofMillis(100));
			}
			fail(msg);
		}
		assertTrue(unresolvedTasks.isEmpty());
	}
}
