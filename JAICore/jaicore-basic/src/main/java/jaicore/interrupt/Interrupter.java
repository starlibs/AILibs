package jaicore.interrupt;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to conduct managed interrupts, which is essential for organized interrupts.
 *
 * In AILibs, it is generally forbidden to directly interrupt any thread.
 * Instead, the Interrupter should be used, because this enables the interrupted thread to decide how to proceed.
 *
 * Using the Interrupter has several advantages in debugging:
 *
 * 1. the Interrupter tells which thread caused the interrupt
 * 2. the Interrupter tells the time when the thread was interrupted
 * 3. the Interrupter provides a reason for the interrupt
 *
 * @author fmohr
 *
 */
public class Interrupter {

	/**
	 * The interrupter is a singleton and must not be created manually.
	 */
	private Interrupter () {
		super();
	}

	private static final Logger logger = LoggerFactory.getLogger(Interrupter.class);
	private static final Interrupter instance = new Interrupter();

	public static Interrupter get() {
		return instance;
	}
	
	private final List<Interrupt> openInterrupts = new LinkedList<>();

	public synchronized void interruptThread(final Thread t, final Object reason) {
		this.openInterrupts.add(new Interrupt(Thread.currentThread(), t, System.currentTimeMillis(), reason));
		logger.info("Interrupting {} on behalf of {} with reason {}", t, Thread.currentThread(), reason);
		t.interrupt();
	}

	public boolean hasCurrentThreadBeenInterruptedWithReason(final Object reason) {
		return this.hasThreadBeenInterruptedWithReason(Thread.currentThread(), reason);
	}

	public Optional<Interrupt> getInterruptOfCurrentThreadWithReason(final Object reason) {
		return this.getInterruptOfThreadWithReason(Thread.currentThread(), reason);
	}

	public Optional<Interrupt> getInterruptOfThreadWithReason(final Thread thread, final Object reason) {
		return this.openInterrupts.stream().filter(i -> i.getInterruptedThread() == thread && i.getReasonForInterruption().equals(reason)).findFirst();
	}

	public boolean hasThreadBeenInterruptedWithReason(final Thread thread, final Object reason) {
		boolean matches = this.openInterrupts.stream().anyMatch(i -> i.getInterruptedThread() == thread && i.getReasonForInterruption().equals(reason));
		logger.debug("Reasons for why thread {} has currently been interrupted: {}. Checked reason {} matched? {}", thread, openInterrupts.stream().filter(t -> t.getInterruptedThread() == thread).map(Interrupt::getReasonForInterruption).collect(Collectors.toList()), reason, matches);
		return matches;
	}

	public boolean hasCurrentThreadOpenInterrupts() {
		Thread currentThread = Thread.currentThread();
		return this.openInterrupts.stream().anyMatch(i -> i.getInterruptedThread() == currentThread);
	}

	public void markInterruptOnCurrentThreadAsResolved(final Object reason) throws InterruptedException {
		Thread currentThread = Thread.currentThread();
		if (!this.hasCurrentThreadBeenInterruptedWithReason(reason)) {
			throw new IllegalArgumentException("The thread " + currentThread + " has not been interrupted with reason " + reason);
		}
		this.openInterrupts.removeIf(i -> i.getInterruptedThread() == currentThread && i.getReasonForInterruption().equals(reason));
		if (this.hasCurrentThreadOpenInterrupts()) {
			Thread.interrupted(); // clear flag prior to throwing the InterruptedException
			throw new InterruptedException();
		}
	}
}
