package jaicore.basic.algorithm;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import jaicore.basic.Cancelable;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;

/**
 * The algorithms should actually also be interruptible, but since this is often not the case,
 * we require the cancel method to ensure that the authors of the algorithms provide a mechanism
 * to stop the algorithm and free the used resources.
 *
 * Implementation of both Iterable and Iterator: We envision that an object of an algorithm really
 * only represents one run, so there is really no reason to have an additional object only to hold
 * the state.
 *
 * @author fmohr
 *
 * @param <I>
 *            class of which inputs stems from
 * @param <O>
 *            class of which solution candidates and the eventually returned result stem from
 */
public interface IAlgorithm<I, O> extends Iterable<AlgorithmEvent>, Iterator<AlgorithmEvent>, Callable<O>, Cancelable {

	/**
	 * @return The input that has been given to the algorithm.
	 */
	public I getInput();

	/**
	 * Registers a listener to the algorithm's event bus.
	 *
	 * @param listener
	 *            The listener to register.
	 */
	public void registerListener(Object listener);

	/**
	 * @return The number of cpus (the amount of parallelization) that is allowed to be used by the algorithm.
	 */
	public int getNumCPUs();

	/**
	 * @param numberOfCPUs
	 *            The numer of cpus that is allowed to be used by the algorithm.
	 */
	public void setNumCPUs(int numberOfCPUs);

	/**
	 * While setNumCPUs aims at telling the algorithm how many CPUs *should* be used for parallelization,
	 * this method can be used to define a strict constraint on the number of threads that must be spawned
	 * by the algorithm itself at most. The motivation for this functionality is that, on some systems,
	 * processes get killed if they consume too much CPU resources. This can usually be avoided if the
	 * number of threads is limited (e.g. to the number of cores that may be used). <code>setNumCPUs</code>
	 * does not put such a restriction, because this is unnecessarily limiting in environment where such
	 * constraints do not exist.
	 *
	 * If the value is set to 0, no own threads must be used at all. Note that this may not be possible for
	 * some algorithms that need observers in the background.
	 *
	 * If the value is set to -1, any restriction on the number of threads is removed.
	 *
	 * Note that different algorithms may have different default behaviors if this number is not set.
	 *
	 * @param maxNumberOfThreads
	 *            The maximum number of threads that may be spawned by the algorithm itself.
	 */
	public void setMaxNumThreads(int maxNumberOfThreads);

	/**
	 * Sets the timeout for the algorithm to the given value in the given time unit.
	 *
	 * @param timeout
	 *            The number of e.g. ms, seconds, minutes according to the given time unit.
	 * @param timeUnit
	 *            The time unit for which the timeout variable will be interpreted.
	 */
	public void setTimeout(long timeout, TimeUnit timeUnit);

	/**
	 * @param timeout
	 *            The timeout for the algorithm.
	 */
	public void setTimeout(TimeOut timeout);

	/**
	 * @return The timeout for the algorithm.
	 */
	public TimeOut getTimeout();

	/**
	 * Continues the execution of the algorithm until the next event is emitted.
	 *
	 * @return The next event occuring during the execution of the algorithm.
	 * @throws Exception
	 */
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException;

	/**
	 * @return The config interface to store parameters to the algorithm in.
	 */
	public IAlgorithmConfig getConfig();

	/**
	 * Overrides the call of Callable to restrict the set of allowed exceptions
	 */
	@Override
	public O call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException;

	/**
	 * globally unique identifier for the algorithm run
	 *
	 * @return
	 */
	public String getId();
}
