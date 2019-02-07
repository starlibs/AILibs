package de.upb.crc901.mlplan.metamining.dyadranking;

import java.util.Collection;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.WEKAPipelineCharacterizer;
import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import hasco.core.Util;
import hasco.model.Component;
import jaicore.ml.dyadranking.search.ADyadRankedNodeQueue;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
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
	 * the characterizer for characterizing pipelines
	 */
	private WEKAPipelineCharacterizer characterizer;

	/**
	 * Construct a new WEKA dyad ranked node queue that ranks WEKA pipelines
	 * constructed from the given components in the given context.
	 * 
	 * @param contextCharacterization
	 *            the characterization of the context
	 * @param components
	 *            the components
	 */
	public WEKADyadRankedNodeQueue(Vector contextCharacterization, Collection<Component> components) {
		super(contextCharacterization);
		this.components = components;
		// TODO init pipeline characterizer
	}

	@Override
	protected Vector characterize(Node<TFDNode, Double> node) {
		return new DenseDoubleVector(characterizer.characterize(
				Util.getComponentInstanceFromState(components, node.getPoint().getState(), "solution", true)));
	}
}
