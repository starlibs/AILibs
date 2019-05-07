package jaicore.interrupt;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
	private Interrupter() {
		super();
	}

	private static final Logger logger = LoggerFactory.getLogger(Interrupter.class);
	private static final Interrupter instance = new Interrupter();

	public static Interrupter get() {
		return instance;
	}

	private final Map<Thread, Set<Object>> blackListedInterruptReasons = new HashMap<>(); // interrupts to avoid when they arrive
	private final List<Interrupt> openInterrupts = new LinkedList<>();

	public synchronized void interruptThread(final Thread t, final Object reason) {
		if (this.blackListedInterruptReasons.containsKey(t) && this.blackListedInterruptReasons.get(t).contains(reason)) {
			this.blackListedInterruptReasons.get(t).remove(reason);
			logger.info("Thread {} is not interrupted, because it has been marked to be avoided for reason {}. Removing the entry from the black list.", t, reason);
			return;
		}
		this.openInterrupts.add(new Interrupt(Thread.currentThread(), t, System.currentTimeMillis(), reason));
		logger.info("Interrupting {} on behalf of {} with reason {}", t, Thread.currentThread(), reason);
		t.interrupt();
		logger.info("Interrupt accomplished. Interrupt flag of {}: {}", t, t.isInterrupted());
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

	public void avoidInterrupt(final Thread t, final Object reason) {
		if (!this.blackListedInterruptReasons.containsKey(t)) {
			this.blackListedInterruptReasons.put(t, new HashSet<>());
		}
		this.blackListedInterruptReasons.get(t).add(reason);
	}

	public boolean hasThreadBeenInterruptedWithReason(final Thread thread, final Object reason) {
		boolean matches = this.openInterrupts.stream().anyMatch(i -> i.getInterruptedThread() == thread && i.getReasonForInterruption().equals(reason));
		if (logger.isDebugEnabled()) {
			if (matches) {
				logger.debug("Reasons for why thread {} has currently been interrupted: {}. Checked reason {} matched? {}", thread,
						this.openInterrupts.stream().filter(t -> t.getInterruptedThread() == thread).map(Interrupt::getReasonForInterruption).collect(Collectors.toList()), reason, matches);
			} else {
				logger.debug("Thread {} is currently not interrupted. In particular, it is not interrupted with reason {}", thread, reason);
			}
		}
		return matches;
	}

	public Collection<Interrupt> getAllUnresolvedInterrupts() {
		return this.openInterrupts;
	}

	public Collection<Interrupt> getAllUnresolvedInterruptsOfThread(final Thread thread) {
		return this.openInterrupts.stream().filter(i -> i.getInterruptedThread() == thread).collect(Collectors.toList());
	}

	public Optional<Interrupt> getLatestUnresolvedInterruptOfThread(final Thread thread) {
		return this.getAllUnresolvedInterruptsOfThread(thread).stream().sorted((i1, i2) -> Long.compare(i1.getTimestampOfInterruption(), i2.getTimestampOfInterruption())).findFirst();
	}

	public Optional<Interrupt> getLatestUnresolvedInterruptOfCurrentThread() {
		return this.getLatestUnresolvedInterruptOfThread(Thread.currentThread());
	}

	public boolean hasCurrentThreadOpenInterrupts() {
		Thread currentThread = Thread.currentThread();
		return this.openInterrupts.stream().anyMatch(i -> i.getInterruptedThread() == currentThread);
	}

	public synchronized void markInterruptOnCurrentThreadAsResolved(final Object reason) throws InterruptedException {
		Thread ct = Thread.currentThread();
		this.markInterruptAsResolved(ct, reason);
		if (this.hasCurrentThreadOpenInterrupts()) {
			Thread.interrupted(); // clear flag prior to throwing the InterruptedException
			logger.info("Throwing a new InterruptedException after having resolved the current interrupt, because the thread still has open interrupts! The reasons for these are: {}",
					this.getAllUnresolvedInterruptsOfThread(ct).stream().map(Interrupt::getReasonForInterruption).collect(Collectors.toList()));
			throw new InterruptedException();
		}
	}

	public synchronized void markInterruptAsResolved(final Thread t, final Object reason) {
		if (!this.hasThreadBeenInterruptedWithReason(t, reason)) {
			throw new IllegalArgumentException("The thread " + t + " has not been interrupted with reason " + reason + ". Reasons for which it has been interrupted: "
					+ Interrupter.get().getAllUnresolvedInterruptsOfThread(Thread.currentThread()).stream().map(Interrupt::getReasonForInterruption).collect(Collectors.toList()));
		}
		logger.debug("Removing interrupt with reason {} from list of open interrupts for thread {}", reason, t);
		this.openInterrupts.removeIf(i -> i.getInterruptedThread() == t && i.getReasonForInterruption().equals(reason));
		if (this.getAllUnresolvedInterruptsOfThread(t).contains(reason)) {
			throw new IllegalStateException("The interrupt should have been resolved, but it has not!");
		}
	}
}
