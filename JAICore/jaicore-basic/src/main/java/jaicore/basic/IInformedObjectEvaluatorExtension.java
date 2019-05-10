package jaicore.basic;


/**
 * To be used with {@link IObjectEvaluator} if the evaluation depends on the the best value seen so far
 * for other evaluations.
 * 
 * @author jnowack
 *
 */
public interface IInformedObjectEvaluatorExtension<V extends Comparable<V>>{
	public void updateBestScore(V bestScore);
}