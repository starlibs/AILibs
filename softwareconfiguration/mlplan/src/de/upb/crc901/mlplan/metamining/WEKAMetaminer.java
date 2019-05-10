package de.upb.crc901.mlplan.metamining;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.IPipelineCharacterizer;
import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.WEKAPipelineCharacterizer;
import de.upb.crc901.mlplan.metamining.similaritymeasures.F3Optimizer;
import de.upb.crc901.mlplan.metamining.similaritymeasures.IHeterogenousSimilarityMeasureComputer;
import de.upb.crc901.mlplan.metamining.similaritymeasures.IRelativeRankMatrixComputer;
import de.upb.crc901.mlplan.metamining.similaritymeasures.RelativeRankMatricComputer;
import hasco.metamining.IMetaMiner;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * An implementation of the meta miner for pipelines consisting exclusively of WEKA components.
 * 
 * @author Helena Graf
 *
 */
public class WEKAMetaminer implements IMetaMiner {

	private boolean hasBeenBuilt = false;
	private INDArray datasetMetafeatures;
	private Enumeration<Attribute> dataSetMetaFeaturesAttributes;

	private IHeterogenousSimilarityMeasureComputer similarityMeasure = new F3Optimizer(0.1);
	private IRelativeRankMatrixComputer similarityComputer = new RelativeRankMatricComputer();
	private IPipelineCharacterizer pipelineCharacterizer;

	public WEKAMetaminer(Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramConfigs) {
		this.pipelineCharacterizer = new WEKAPipelineCharacterizer(paramConfigs);
	}

	@Override
	public double score(ComponentInstance componentInstance) {
		// Check if has been trained
		if (!hasBeenBuilt) {
			throw new RuntimeException("Metaminer has not been built!");
		}
		if (dataSetMetaFeaturesAttributes == null) {
			throw new RuntimeException("Metaminer has not been given a data set characterization!");
		}

		// Characterize pipeline and compute similarity with data set
		double[] pipelineMetafeatures = pipelineCharacterizer.characterize(componentInstance);
		return similarityMeasure.computeSimilarity(datasetMetafeatures, Nd4j.create(pipelineMetafeatures));
	}

	public void build(List<ComponentInstance> distinctPipelines, Instances metaFeatureInformation,
			double[][][] performanceValues) throws Exception {
		// Check whether has been built
		if (hasBeenBuilt) {
			throw new Exception("MetaMiner has already been built!");
		}

		// ----- Data set Characterization -----

		dataSetMetaFeaturesAttributes = metaFeatureInformation.enumerateAttributes();		

		// Convert to matrix (Matrix X with rows representing data sets)
		INDArray datasetsMetafeatures = Nd4j.create(metaFeatureInformation.size(),
				metaFeatureInformation.numAttributes());
		for (int i = 0; i < metaFeatureInformation.size(); i++) {
			datasetsMetafeatures.putRow(i, Nd4j.create(metaFeatureInformation.get(i).toDoubleArray()));
		}
		System.out.println("WEKAMetaminer: dataset metafeatures: " + datasetsMetafeatures.rows() + " x " + datasetsMetafeatures.columns());

		// ----- Pipeline Characterization -----

		// Compute relative performance ranks of pipelines on data sets
		System.out.println("WEKAMetaminer: Computing relative performance Matrix.");
		INDArray rankMatrix = similarityComputer.computeRelativeRankMatrix(performanceValues);
		System.out.println("WEKAMetaminer: rank matrix: " + rankMatrix.rows() + " x " + rankMatrix.columns());
		System.out.println(rankMatrix);

		// Initialize PipelineCharacterizer with list of distinct pipelines
		System.out.println("WEKAMetaminer: Initializing pipeline characterizer.");
		pipelineCharacterizer.build(distinctPipelines);

		// Get Characterization of base pipelines from PipelineCharacterizer (Matrix W)
		INDArray pipelinesMetafeatures = Nd4j.create(pipelineCharacterizer.getCharacterizationsOfTrainingExamples());
		System.out.println("WEKAMetaminer: Pipeline Metafeatures: " + pipelinesMetafeatures.rows() + " x " + pipelinesMetafeatures.columns());

		// Initialize HeterogenousSimilarityMeasures
		System.out.println("WEKAMetaminer: Create similarity measure.");
		similarityMeasure.build(datasetsMetafeatures, pipelinesMetafeatures, rankMatrix);

		// Building is finished
		hasBeenBuilt = true;
	}

	public void setDataSetCharacterization(Map<String, Double> datasetCharacterization) {
		// Characterize the given data set with characterizer (set x)
		datasetMetafeatures = Nd4j.create(datasetCharacterization.size());		
		List<Attribute> attributes = Collections.list(dataSetMetaFeaturesAttributes);
		for (int i = 0; i < attributes.size(); i++) {
			datasetMetafeatures.putScalar(i, datasetCharacterization.get(attributes.get(i).name()));
		}
	}

	/**
	 * Get the similarity measure used to determine the similarities of s
	 * 
	 * @return
	 */
	public IHeterogenousSimilarityMeasureComputer getSimilarityMeasure() {
		return similarityMeasure;
	}

	public void setSimilarityMeasure(IHeterogenousSimilarityMeasureComputer similarityMeasure) {
		this.similarityMeasure = similarityMeasure;
	}

	public IPipelineCharacterizer getPipelineCharacterizer() {
		return pipelineCharacterizer;
	}

	public void setPipelineCharacterizer(IPipelineCharacterizer pipelineCharacterizer) {
		this.pipelineCharacterizer = pipelineCharacterizer;
	}

}
