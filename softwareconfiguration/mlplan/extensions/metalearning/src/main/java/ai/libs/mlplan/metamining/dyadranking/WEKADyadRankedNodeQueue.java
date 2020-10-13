package ai.libs.mlplan.metamining.dyadranking;

import java.util.ArrayList;
import java.util.Collection;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.common.math.IVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.HASCOUtil;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.search.ADyadRankedNodeQueue;
import ai.libs.jaicore.ml.ranking.dyad.learner.util.AbstractDyadScaler;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.mlplan.metamining.pipelinecharacterizing.IPipelineCharacterizer;

/**
 * A queue that uses a dyad ranker to rank WEKA pipelines.
 *
 * @author Helena Graf
 *
 */
public class WEKADyadRankedNodeQueue extends ADyadRankedNodeQueue<TFDNode, Double> {

	private Logger logger = LoggerFactory.getLogger(WEKADyadRankedNodeQueue.class);

	/**
	 * the allowed components of the pipelines
	 */
	private Collection<IComponent> components;

	/**
	 * the characterizer for characterizing (partial) pipelines
	 */
	private IPipelineCharacterizer characterizer;

	/**
	 * Construct a new WEKA dyad ranked node queue that ranks WEKA pipelines
	 * constructed from the given components in the given context.
	 *
	 * @param contextCharacterization
	 *            the characterization of the dataset (the context)
	 * @param components
	 *            the search space components
	 * @param ranker
	 *            the ranker to use to rank the dyads - must be pre-trained
	 * @param scaler
	 *            the scaler to use to scale the dataset - must have been fit to
	 *            data already
	 */
	public WEKADyadRankedNodeQueue(final IVector contextCharacterization, final Collection<? extends IComponent> components, final IDyadRanker ranker, final AbstractDyadScaler scaler, final IPipelineCharacterizer characterizer) {
		super(contextCharacterization, ranker, scaler);
		this.components = new ArrayList<>(components);
		this.characterizer = characterizer;
	}

	@Override
	protected IVector characterize(final IEvaluatedPath<TFDNode, ?, Double> path) {
		ComponentInstance cI = HASCOUtil.getComponentInstanceFromState(this.components, path.getHead().getState(), "solution", true);
		if (cI != null) {
			this.logger.debug("Characterizing new node.");
			return new DenseDoubleVector(this.characterizer.characterize(cI));
		} else {
			this.logger.debug("CI from node for characterization is null.");
			return new DenseDoubleVector(this.characterizer.getLengthOfCharacterization(), 0);
		}

	}
}
