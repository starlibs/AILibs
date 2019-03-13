package de.upb.crc901.mlplan.metamining.dyadranking;

import java.util.Collection;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.ComponentInstanceVectorFeatureGenerator;
import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.IPipelineCharacterizer;
import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.ml.dyadranking.algorithm.ADyadRanker;
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
	 *            the characterization of the context
	 * @param components
	 *            the components
	 */
	public WEKADyadRankedNodeQueue(Vector contextCharacterization, Collection<Component> components, ADyadRanker ranker,
			AbstractDyadScaler scaler) {
		super(contextCharacterization, ranker, scaler);
		this.components = components;
		this.characterizer = new ComponentInstanceVectorFeatureGenerator(components);
	}

	@Override
	protected Vector characterize(Node<TFDNode, Double> node) {
		ComponentInstance cI = Util.getComponentInstanceFromState(components, node.getPoint().getState(), "solution",
				true);
		if (cI != null) {
			return new DenseDoubleVector(characterizer.characterize(cI));
		} else {
			return new DenseDoubleVector(characterizer.getLengthOfCharacterization(), 0);
		}
		
	}
}
