package de.upb.crc901.mlplan.metamining.dyadranking;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.openml.webapplication.fantail.dc.Characterizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.ComponentInstanceVectorFeatureGenerator;
import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.IPipelineCharacterizer;
import de.upb.isys.linearalgebra.DenseDoubleVector;
import hasco.model.Component;
import jaicore.ml.dyadranking.search.ADyadRankedNodeQueueConfig;
import jaicore.ml.metafeatures.DatasetCharacterizerInitializationFailedException;
import jaicore.ml.metafeatures.LandmarkerCharacterizer;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import weka.core.Instances;

/**
 * A configuration class that contains configurable variables for using ML-Plan
 * with best-first search and a dyad-ranked OPEN list instead of random
 * completions.
 * 
 * @author Helena Graf
 *
 */
public class WEKADyadRankedNodeQueueConfig extends ADyadRankedNodeQueueConfig<TFDNode> {

	private Logger logger = LoggerFactory.getLogger(WEKADyadRankedNodeQueueConfig.class);

	/**
	 * the characterizer used to characterize new datasets, must produce dataset
	 * meta data of the same format the dyad ranker is trained with
	 */
	private Characterizer datasetCharacterizer;

	/**
	 * the characterizer used to characterize new pipelines; must produce pipeline
	 * meta features of the same format the dyad ranker is trained with
	 */
	private IPipelineCharacterizer pipelineCharacterizer;

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
	 * @throws IOException
	 *             if the default ranker or scaler cannot be loaded
	 * @throws ClassNotFoundException
	 *             if the default ranker or scaler cannot be instantiated
	 * @throws DatasetCharacterizerInitializationFailedException
	 *             if the default dataset characterizer cannot be instantiated
	 */
	public WEKADyadRankedNodeQueueConfig()
			throws ClassNotFoundException, IOException, DatasetCharacterizerInitializationFailedException {
		super();
		this.datasetCharacterizer = new LandmarkerCharacterizer();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void configureBestFirst(BestFirst bestFirst) {
		logger.trace("Configuring OPEN list of BF");
		bestFirst.setOpen(new WEKADyadRankedNodeQueue(new DenseDoubleVector(contextCharacterization), components,
				ranker, scaler, pipelineCharacterizer));
	}

	/**
	 * Configure the data in the context of whose metafeatures the dyad ranker ranks
	 * the pipelines.
	 * 
	 * @param data
	 *            the data to use
	 */
	public void setData(Instances data) {
		logger.trace("Setting data to instances of size {}", data.size());
		contextCharacterization = datasetCharacterizer.characterize(data).entrySet().stream()
				.mapToDouble(Map.Entry::getValue).toArray();
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

		if (this.pipelineCharacterizer == null) {
			this.pipelineCharacterizer = new ComponentInstanceVectorFeatureGenerator(components);
		}
	}

	/**
	 * Set the dataset characterizer to be used. must produce dataset
	 * meta data of the same format the dyad ranker is trained with.
	 * 
	 * @param datasetCharacterizer
	 */
	public void setDatasetCharacterizer(Characterizer datasetCharacterizer) {
		this.datasetCharacterizer = datasetCharacterizer;
	}

	/**
	 * Set the pipeline characterizer to be used, must produce pipeline
	 * meta features of the same format the dyad ranker is trained with.
	 * 
	 * @param pipelineCharacterizer the pipeline characterizer to use
	 */
	public void setPipelineCharacterizer(IPipelineCharacterizer pipelineCharacterizer) {
		this.pipelineCharacterizer = pipelineCharacterizer;
	}
}
