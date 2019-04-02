package de.upb.crc901.mlplan.metamining.dyadranking;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.IPipelineCharacterizer;
import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.ml.dyadranking.algorithm.IDyadRanker;
import jaicore.ml.dyadranking.search.ADyadRankedNodeQueue;
import jaicore.ml.dyadranking.util.AbstractDyadScaler;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.model.travesaltree.Node;

/**
 * A queue that uses a dyad ranker to rank WEKA pipelines.
 * 
 * @author Helena Graf
 *
 */
public class WEKADyadRankedNodeQueue extends ADyadRankedNodeQueue<TFDNode, Double> {

	private Logger logger = LoggerFactory.getLogger(ADyadRankedNodeQueue.class);

	/**
	 * the allowed components of the pipelines
	 */
	private Collection<Component> components;

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
	public WEKADyadRankedNodeQueue(Vector contextCharacterization, Collection<Component> components, IDyadRanker ranker,
			AbstractDyadScaler scaler, IPipelineCharacterizer characterizer) {
		super(contextCharacterization, ranker, scaler);
		this.components = components;
		this.characterizer = characterizer;
	}

	@Override
	protected Vector characterize(Node<TFDNode, Double> node) {
		ComponentInstance cI = Util.getComponentInstanceFromState(components, node.getPoint().getState(), "solution",
				true);
		if (cI != null) {
			logger.debug("Characterizing new node.");
			return new DenseDoubleVector(characterizer.characterize(cI));
		} else {
			logger.debug("CI from node for characterization is null.");
			return new DenseDoubleVector(characterizer.getLengthOfCharacterization(), 0);
		}

	}
}
