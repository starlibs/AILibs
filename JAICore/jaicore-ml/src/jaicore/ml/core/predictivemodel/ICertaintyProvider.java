package jaicore.ml.core.predictivemodel;

import jaicore.ml.core.dataset.IInstance;

/**
 * The {@link ICertaintyProvider} models a {@link IPredictiveModel} that
 * provides uncertainty information for queries in form of {@link IInstance}s.
 * 
 * @author Jonas Hanselle
 * 
 * @param <TARGET> The type of the target that this {@link ICertaintyProvider}
 *        provides certainty for.
 */
public interface ICertaintyProvider<TARGET> extends IPredictiveModel<TARGET> {

	/**
	 * Returns the certainty for a given {@link IInstance}.
	 * 
	 * @param queryInstance {@link IInstance} for which certainty shall be obtained.
	 * @return Certainty of the model for the given {@link IInstance}
	 */
	public double getCertainty(IInstance queryInstance);

}
