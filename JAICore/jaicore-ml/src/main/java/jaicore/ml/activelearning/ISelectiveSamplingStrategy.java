package jaicore.ml.activelearning;


/**
 * A strategy for selective sampling.
 * @author Jonas Hanselle
 *
 */
public interface ISelectiveSamplingStrategy<I> {
	
	/**
	 * Chooses the {@link IInstance} to query next.
	 * @return {@link IInstance} to query next.
	 */
	public I nextQueryInstance();

}
