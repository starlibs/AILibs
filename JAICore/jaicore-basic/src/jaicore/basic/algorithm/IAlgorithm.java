package jaicore.basic.algorithm;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import jaicore.basic.Cancelable;

/**
 * The algorithms should actually also be interruptible, but since this is often not the case, we require the cancel method to
 * ensure that the authors of the algorithms provide a mechanism to stop the algorithm and free the used resources.
 * 
 * Implementation of both Iterable and Iterator: We envision that an object of an algorithm really only represents one run, so
 * there is really no reason to have an additional object only to hold the state.
 * 
 * @author fmohr
 *
 * @param <I> class of which inputs stems from
 * @param <O> class of which solution candidates and the eventually returned result stem from
 */
public interface IAlgorithm<I,O> extends Iterable<AlgorithmEvent>, Iterator<AlgorithmEvent>, Callable<O>, Cancelable {
	
	public I getInput();
	public void registerListener(Object listener);
	public void setNumCPUs(int numberOfCPUs);
	public int getNumCPUs();
	public void setTimeout(int timeout, TimeUnit timeUnit);
	public int getTimeout();
	public TimeUnit getTimeoutUnit();
	
	public AlgorithmEvent nextWithException() throws Exception;
}
