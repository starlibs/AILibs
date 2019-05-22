package jaicore.ml.dyadranking.activelearning;

import java.util.Collection;
import java.util.Set;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.activelearning.IActiveLearningPoolProvider;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Interface for an active learning pool provider in the context of dyad
 * ranking. It offers access to the pool of dyads both by instance features and
 * alternative features.
 * 
 * @author Jonas Hanselle
 *
 */
public interface IDyadRankingPoolProvider extends IActiveLearningPoolProvider<IDyadRankingInstance> {

	/**
	 * Returns the set of all {@link Dyad}s with the given {@link Vector} of
	 * instance features.
	 * 
	 * @param instanceFeatures {@link Vector} of instance features.
	 * @return {@link Set} of dyads with the given {@link Vector} of instance
	 *         features.
	 */
	public Set<Dyad> getDyadsByInstance(Vector instanceFeatures);

	/**
	 * Returns the set of all {@link Dyad}s with the given {@link Vector} of
	 * alternative features.
	 * 
	 * @param alternativeFeatures {@link Vector} of alternative features.
	 * @return {@link Set} of dyads with the given {@link Vector} of alternative
	 *         features.
	 */
	public Set<Dyad> getDyadsByAlternative(Vector alternativeFeatures);

	/**
	 * Returns a {@link Collection} that contains all instance features contained in
	 * the pool.
	 * 
	 * @return A {@link Collection} that contains all instance features contained in
	 *         the pool.
	 */
	public Collection<Vector> getInstanceFeatures();
	
	public void setRemoveDyadsWhenQueried(boolean flag);
	
	public int getPoolSize();
	
	public DyadRankingDataset getQueriedRankings();
}
