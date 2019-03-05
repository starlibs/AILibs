package jaicore.basic.algorithm;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jaicore.basic.Cancelable;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;

/**
 * The algorithms should actually also be interruptible, but since this is often not the case, we require the cancel method to ensure that the authors of the algorithms provide a mechanism to stop the algorithm and free the used resources.
 *
 * Implementation of both Iterable and Iterator: We envision that an object of an algorithm really only represents one run, so there is really no reason to have an additional object only to hold the state.
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
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException;

	/**
	 * @return The config interface to store parameters to the algorithm in.
	 */
	public IAlgorithmConfig getConfig();
	
	/**
	 * Overrides the call of Callable to restrict the set of allowed exceptions
	 */
	public O call() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException;
	
	/**
	 * globally unique identifier for the algorithm run
	 * 
	 * @return
	 */
	public String getId();
}
