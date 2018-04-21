package jaicore.basic;

/**
 * The algorithms should actually also be interruptible, but since this is often not the case, we require the cancel method to
 * ensure that the authors of the algorithms provide a mechanism to stop the algorithm and free the used resources.
 * 
 * @author fmohr
 *
 * @param <S>
 */
public interface IIterableAlgorithm<S> extends Iterable<S> {
	
	public void cancel();
}
