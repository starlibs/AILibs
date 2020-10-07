package ai.libs.jaicore.concurrent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.interrupt.InterruptionTimerTask;
import ai.libs.jaicore.test.MediumTest;

public class TrackableTimerTest extends ATest {

	public TrackableTimerTask getEmptyTrackableTask() {
		return new TrackableTimerTask() {

			@Override
			public void exec() {
				/* do nothing */
			}

		};
	}

	public TimerTask getEmptyTask() {
		return new TimerTask() {

			@Override
			public void run() {
				/* do nothing */
			}

		};
	}

	@MediumTest
	@Test
	public void testIndividualTrackableTask() throws InterruptedException {
		TrackableTimer tt = new TrackableTimer();

		/* conduct several tests on a task that will be executed in an instant */
		TrackableTimerTask t1 = this.getEmptyTrackableTask();
		tt.schedule(t1, 100);
		assertFalse(t1.isCanceled());
		assertFalse(tt.hasTaskBeenExecutedInPast(t1)); // task should not have been executed
		assertTrue(tt.willTaskBeExecutedInFuture(t1));
		assertTrue(tt.hasOpenTasks());
		Thread.sleep(1000);
		assertFalse(t1.isCanceled());
		assertTrue(tt.hasTaskBeenExecutedInPast(t1)); // task should have been executed
		assertFalse(tt.willTaskBeExecutedInFuture(t1)); // task should not be executed anymore
		assertFalse(tt.hasOpenTasks());

		/* conduct several tests on a task that will be executed in a remote future */
		TrackableTimerTask t2 = this.getEmptyTrackableTask();
		tt.schedule(t2, 1000000);
		assertFalse(t2.isCanceled());
		assertFalse(tt.hasTaskBeenExecutedInPast(t2)); // task should not have been executed
		assertTrue(tt.willTaskBeExecutedInFuture(t2));
		assertTrue(tt.hasOpenTasks());
		t2.cancel();
		assertTrue(t2.isCanceled());
		assertFalse(tt.willTaskBeExecutedInFuture(t2));
		assertFalse(tt.hasOpenTasks());
	}

	@MediumTest
	@Test
	public void testReocurringTrackableTask() throws InterruptedException {
		TrackableTimer tt = new TrackableTimer();
		TrackableTimerTask t = this.getEmptyTrackableTask();

		/* conduct several tests on execution */
		tt.schedule(t, 100, 100);
		assertFalse(t.isCanceled());
		assertFalse(tt.hasTaskBeenExecutedInPast(t)); // task should not have been executed
		assertTrue(tt.willTaskBeExecutedInFuture(t));
		assertTrue(tt.hasOpenTasks());
		Thread.sleep(1000);
		assertTrue(tt.hasTaskBeenExecutedInPast(t)); // task should have been executed
		assertTrue(tt.willTaskBeExecutedInFuture(t)); // task should still be executed
		assertTrue(tt.hasOpenTasks());
		t.cancel();
		assertTrue(t.isCanceled());
		assertFalse(tt.willTaskBeExecutedInFuture(t));
		assertFalse(tt.hasOpenTasks());
	}

	@MediumTest
	public void testIndividualUntrackableTask() throws InterruptedException {
		TrackableTimer tt = new TrackableTimer();

		/* conduct several tests on a task that will be executed in an instant */
		TrackableTimerTask t1 = TrackableTimerTask.get(this.getEmptyTask());
		tt.schedule(t1, 1000);
		assertFalse(t1.isCanceled());
		assertFalse(t1.isCanceled()); // this check occurs twice here on purpose, because we want to test whether the check changes the field, which it shouldn't
		assertFalse(tt.hasTaskBeenExecutedInPast(t1)); // task should not have been executed
		assertTrue(tt.willTaskBeExecutedInFuture(t1));
		assertTrue(tt.hasOpenTasks());
		Thread.sleep(2000);
		assertFalse(t1.isCanceled());
		assertTrue(tt.hasTaskBeenExecutedInPast(t1)); // task should have been executed
		assertFalse(tt.willTaskBeExecutedInFuture(t1)); // task should not be executed anymore
		assertFalse(tt.hasOpenTasks());

		/* conduct several tests on a task that will be executed in a remote future */
		TrackableTimerTask t2 = TrackableTimerTask.get(this.getEmptyTask());
		tt.schedule(t2, 1000000);
		assertFalse(t2.isCanceled());
		assertFalse(tt.hasTaskBeenExecutedInPast(t2)); // task should not have been executed
		assertTrue(tt.willTaskBeExecutedInFuture(t2));
		assertTrue(tt.hasOpenTasks());
		t2.cancel();
		assertTrue(t2.isCanceled());
		assertFalse(tt.willTaskBeExecutedInFuture(t2));
		assertFalse(tt.hasOpenTasks());
	}

	@MediumTest
	public void testReocurringUntrackableTask() throws InterruptedException {
		TrackableTimer tt = new TrackableTimer();
		TrackableTimerTask t = TrackableTimerTask.get(this.getEmptyTask());

		/* conduct several tests on execution */
		tt.schedule(t, 1000, 100);
		assertFalse(t.isCanceled());
		assertFalse(tt.hasTaskBeenExecutedInPast(t)); // task should not have been executed
		assertTrue(tt.willTaskBeExecutedInFuture(t));
		assertTrue(tt.hasOpenTasks());
		Thread.sleep(2000);
		assertTrue(tt.hasTaskBeenExecutedInPast(t)); // task should have been executed
		assertTrue(tt.willTaskBeExecutedInFuture(t)); // task should still be executed
		assertTrue(tt.hasOpenTasks());
		t.cancel();
		assertTrue(t.isCanceled());
		assertFalse(tt.willTaskBeExecutedInFuture(t));
		assertFalse(tt.hasOpenTasks());
	}

	@MediumTest
	public void testInterruptTask() throws InterruptedException {
		TrackableTimer tt = new TrackableTimer();
		Thread t = new Thread() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					Awaitility.await().atLeast(100, TimeUnit.MILLISECONDS);
				}
			}
		};
		TrackableTimerTask it = new InterruptionTimerTask("test interrupt", t);
		tt.schedule(it, 1000);
		assertFalse(tt.hasTaskBeenExecutedInPast(it));
		assertTrue(tt.willTaskBeExecutedInFuture(it));
		assertTrue(tt.hasOpenTasks());

		/* conduct several tests on execution */
		Thread.sleep(1500);
		assertFalse(it.isCanceled());
		assertTrue(tt.hasTaskBeenExecutedInPast(it));
		assertFalse(tt.willTaskBeExecutedInFuture(it));
		assertFalse(tt.hasOpenTasks());
	}
}
