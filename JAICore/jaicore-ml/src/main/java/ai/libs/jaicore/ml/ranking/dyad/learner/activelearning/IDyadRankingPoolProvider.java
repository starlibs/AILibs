package ai.libs.jaicore.ml.ranking.dyad.learner.activelearning;

import java.util.Collection;
import java.util.Set;

import org.api4.java.ai.ml.core.learner.active.IActiveLearningPoolProvider;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;

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
	 * Returns the set of all {@link Dyad}s with the given {@link IVector} of
	 * instance features.
	 *
	 * @param instanceFeatures {@link IVector} of instance features.
	 * @return {@link Set} of dyads with the given {@link IVector} of instance
	 *         features.
	 */
	public Set<IDyad> getDyadsByInstance(IVector instanceFeatures);

	/**
	 * Returns the set of all {@link Dyad}s with the given {@link IVector} of
	 * alternative features.
	 *
	 * @param alternativeFeatures {@link IVector} of alternative features.
	 * @return {@link Set} of dyads with the given {@link IVector} of alternative
	 *         features.
	 */
	public Set<IDyad> getDyadsByAlternative(IVector alternativeFeatures);

	/**
	 * Returns a {@link Collection} that contains all instance features contained in
	 * the pool.
	 *
	 * @return A {@link Collection} that contains all instance features contained in
	 *         the pool.
	 */
	public Collection<IVector> getInstanceFeatures();

	public void setRemoveDyadsWhenQueried(boolean flag);

	public int getPoolSize();

	public DyadRankingDataset getQueriedRankings();
}
