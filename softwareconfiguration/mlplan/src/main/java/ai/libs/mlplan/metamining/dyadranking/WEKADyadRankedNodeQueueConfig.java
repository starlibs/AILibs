package ai.libs.mlplan.metamining.dyadranking;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.openml.webapplication.fantail.dc.Characterizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.Component;
import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.ml.dyadranking.search.ADyadRankedNodeQueueConfig;
import ai.libs.jaicore.ml.metafeatures.DatasetCharacterizerInitializationFailedException;
import ai.libs.jaicore.ml.metafeatures.LandmarkerCharacterizer;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.mlplan.metamining.pipelinecharacterizing.ComponentInstanceVectorFeatureGenerator;
import ai.libs.mlplan.metamining.pipelinecharacterizing.IPipelineCharacterizer;
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
	public void configureBestFirst(final BestFirst bestFirst) {
		this.logger.trace("Configuring OPEN list of BF");
		bestFirst.setOpen(new WEKADyadRankedNodeQueue(new DenseDoubleVector(this.contextCharacterization), this.components,
				this.ranker, this.scaler, this.pipelineCharacterizer));
	}

	/**
	 * Configure the data in the context of whose metafeatures the dyad ranker ranks
	 * the pipelines.
	 *
	 * @param data
	 *            the data to use
	 */
	public void setData(final Instances data) {
		this.logger.trace("Setting data to instances of size {}", data.size());
		this.contextCharacterization = this.datasetCharacterizer.characterize(data).entrySet().stream()
				.mapToDouble(Map.Entry::getValue).toArray();
	}

	/**
	 * Configure the dyad ranked node queue to use the given components for the
	 * pipeline characterizer to transform nodes to component instances.
	 *
	 * @param components
	 *            the components to use for the pipeline characterizer
	 */
	public void setComponents(final Collection<Component> components) {
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
	public void setDatasetCharacterizer(final Characterizer datasetCharacterizer) {
		this.datasetCharacterizer = datasetCharacterizer;
	}

	/**
	 * Set the pipeline characterizer to be used, must produce pipeline
	 * meta features of the same format the dyad ranker is trained with.
	 *
	 * @param pipelineCharacterizer the pipeline characterizer to use
	 */
	public void setPipelineCharacterizer(final IPipelineCharacterizer pipelineCharacterizer) {
		this.pipelineCharacterizer = pipelineCharacterizer;
	}
}
