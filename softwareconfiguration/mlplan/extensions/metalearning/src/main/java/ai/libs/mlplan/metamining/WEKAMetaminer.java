package ai.libs.mlplan.metamining;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.metamining.IMetaMiner;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfigurationMap;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.mlplan.metamining.pipelinecharacterizing.IPipelineCharacterizer;
import ai.libs.mlplan.metamining.pipelinecharacterizing.WEKAPipelineCharacterizer;
import ai.libs.mlplan.metamining.similaritymeasures.F3Optimizer;
import ai.libs.mlplan.metamining.similaritymeasures.IHeterogenousSimilarityMeasureComputer;
import ai.libs.mlplan.metamining.similaritymeasures.IRelativeRankMatrixComputer;
import ai.libs.mlplan.metamining.similaritymeasures.RelativeRankMatricComputer;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * An implementation of the meta miner for pipelines consisting exclusively of WEKA components.
 *
 * @author Helena Graf
 *
 */
public class WEKAMetaminer implements IMetaMiner {

	private Logger logger = LoggerFactory.getLogger(WEKAMetaminer.class);

	private boolean hasBeenBuilt = false;
	private INDArray datasetMetafeatures;
	private Enumeration<Attribute> dataSetMetaFeaturesAttributes;

	private IHeterogenousSimilarityMeasureComputer similarityMeasure = new F3Optimizer(0.1);
	private IRelativeRankMatrixComputer similarityComputer = new RelativeRankMatricComputer();
	private IPipelineCharacterizer pipelineCharacterizer;

	public WEKAMetaminer(final INumericParameterRefinementConfigurationMap paramConfigs) {
		this.pipelineCharacterizer = new WEKAPipelineCharacterizer(paramConfigs);
	}

	@Override
	public double score(final ComponentInstance componentInstance) {
		// Check if has been trained
		if (!this.hasBeenBuilt) {
			throw new WEKAMetaminerRuntimeException("Metaminer has not been built!");
		}
		if (this.dataSetMetaFeaturesAttributes == null) {
			throw new WEKAMetaminerRuntimeException("Metaminer has not been given a data set characterization!");
		}

		// Characterize pipeline and compute similarity with data set
		double[] pipelineMetafeatures = this.pipelineCharacterizer.characterize(componentInstance);
		return this.similarityMeasure.computeSimilarity(this.datasetMetafeatures, Nd4j.create(pipelineMetafeatures));
	}

	public void build(final List<? extends IComponentInstance> distinctPipelines, final Instances metaFeatureInformation,
			final double[][][] performanceValues) throws AlgorithmException, InterruptedException {
		// Check whether has been built
		if (this.hasBeenBuilt) {
			throw new AlgorithmException("MetaMiner has already been built!");
		}

		// ----- Data set Characterization -----

		this.dataSetMetaFeaturesAttributes = metaFeatureInformation.enumerateAttributes();

		// Convert to matrix (Matrix X with rows representing data sets)
		INDArray datasetsMetafeatures = Nd4j.create(metaFeatureInformation.size(),
				metaFeatureInformation.numAttributes());
		for (int i = 0; i < metaFeatureInformation.size(); i++) {
			datasetsMetafeatures.putRow(i, Nd4j.create(metaFeatureInformation.get(i).toDoubleArray()));
		}
		this.logger.debug("Dataset metafeatures: {} x {}",datasetsMetafeatures.rows(),datasetsMetafeatures.columns());

		// ----- Pipeline Characterization -----

		// Compute relative performance ranks of pipelines on data sets
		this.logger.info("Computing relative performance Matrix.");
		INDArray rankMatrix = this.similarityComputer.computeRelativeRankMatrix(performanceValues);
		this.logger.info("Rank matrix: {} x {}",rankMatrix.rows(),rankMatrix.columns());
		this.logger.debug("Rank Matrix: {}",rankMatrix);

		// Initialize PipelineCharacterizer with list of distinct pipelines
		this.logger.info("WEKAMetaminer: Initializing pipeline characterizer.");
		this.pipelineCharacterizer.build(distinctPipelines);

		// Get Characterization of base pipelines from PipelineCharacterizer (Matrix W)
		INDArray pipelinesMetafeatures = Nd4j.create(this.pipelineCharacterizer.getCharacterizationsOfTrainingExamples());
		this.logger.debug("WEKAMetaminer: Pipeline Metafeatures: {} x {}",pipelinesMetafeatures.rows(),pipelinesMetafeatures.columns());

		// Initialize HeterogenousSimilarityMeasures
		this.logger.info("WEKAMetaminer: Create similarity measure.");
		this.similarityMeasure.build(datasetsMetafeatures, pipelinesMetafeatures, rankMatrix);

		// Building is finished
		this.hasBeenBuilt = true;
	}

	public void setDataSetCharacterization(final Map<String, Double> datasetCharacterization) {
		// Characterize the given data set with characterizer (set x)
		this.datasetMetafeatures = Nd4j.create(datasetCharacterization.size());
		List<Attribute> attributes = Collections.list(this.dataSetMetaFeaturesAttributes);
		for (int i = 0; i < attributes.size(); i++) {
			this.datasetMetafeatures.putScalar(i, datasetCharacterization.get(attributes.get(i).name()));
		}
	}

	/**
	 * Get the similarity measure used to determine the similarities of s
	 *
	 * @return
	 */
	public IHeterogenousSimilarityMeasureComputer getSimilarityMeasure() {
		return this.similarityMeasure;
	}

	public void setSimilarityMeasure(final IHeterogenousSimilarityMeasureComputer similarityMeasure) {
		this.similarityMeasure = similarityMeasure;
	}

	public IPipelineCharacterizer getPipelineCharacterizer() {
		return this.pipelineCharacterizer;
	}

	public void setPipelineCharacterizer(final IPipelineCharacterizer pipelineCharacterizer) {
		this.pipelineCharacterizer = pipelineCharacterizer;
	}

}
