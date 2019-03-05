package de.upb.crc901.mlplan.metamining.dyadranking;

import java.util.Collection;
import java.util.Map;

import org.openml.webapplication.fantail.dc.Characterizer;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import hasco.model.Component;
import jaicore.ml.dyadranking.search.ADyadRankedNodeQueueConfig;
import jaicore.ml.metafeatures.GlobalCharacterizer;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import weka.core.Instances;

/**
 * A configuration class that contains configurable variables for using ML-Plan
 * with best-first search and a dyad-ranked OPEN list instead of random
 * completions. Also is used to configure ML-Plan to behave this way.
 * 
 * @author Helena Graf
 *
 */
public class WEKADyadRankedNodeQueueConfig extends ADyadRankedNodeQueueConfig<TFDNode> {

	/**
	 * the characterizer used to characterize new datasets, must produce dataset
	 * meta data of the same format the dyad ranker is trained with
	 */
	private Characterizer characterizer;

	/**
	 * characterization of the dataset the WEKA classifiers are applied to
	 */
	private double[] contextCharacterization;

	/**
	 * components used during the search necessary so that the pipeline
	 * characterizer can translate nodes to components instances
	 */
	private Collection<Component> components;

	/**
	 * Create a new configuration for a WEAK dyad ranked node queue.
	 * 
	 * @throws Exception
	 */
	public WEKADyadRankedNodeQueueConfig() throws Exception {
		super();
		this.characterizer = new GlobalCharacterizer();
	}

	/**
	 * Configure the data in the context of whose metafeatures the dyad ranker ranks
	 * the pipelines.
	 * 
	 * @param data
	 *            the data to use
	 */
	public void setData(Instances data) {
		contextCharacterization = characterizer.characterize(data).entrySet().stream().mapToDouble(Map.Entry::getValue)
				.toArray();
	}

	/**
	 * Configure the dyad ranked node queue to use the given components for the
	 * pipeline characterizer to transform nodes to component instances.
	 * 
	 * @param components
	 *            the components to use for the pipeline characterizer
	 */
	public void setComponents(Collection<Component> components) {
		this.components = components;
	}

	@Override
	public void configureBestFirst(BestFirst bestFirst) {
		bestFirst.setOpen(new WEKADyadRankedNodeQueue(new DenseDoubleVector(contextCharacterization), components));
	}
}
