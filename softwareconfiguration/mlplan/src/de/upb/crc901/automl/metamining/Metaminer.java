package de.upb.crc901.automl.metamining;

import java.util.Enumeration;
import java.util.HashMap;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import de.upb.crc901.automl.hascowekaml.WEKAPipelineFactory;
import de.upb.crc901.automl.metamining.pipelinecharacterizing.IPipelineCharacterizer;
import de.upb.crc901.automl.metamining.pipelinecharacterizing.PipelineCharacterizer;
import de.upb.crc901.automl.metamining.similaritymeasures.AlternatingGradientDescent;
import de.upb.crc901.automl.metamining.similaritymeasures.IHeterogenousSimilarityMeasureComputer;
import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import hasco.model.ComponentInstance;
import weka.core.Attribute;
import weka.core.Instances;

public class Metaminer implements IMetaMiner {
	
	private boolean hasBeenBuilt=false;
	private WEKAPipelineFactory wekaPipelineFactory = new WEKAPipelineFactory();
	
	private Instances dataset;
	private RealVector datasetMetafeatures;
	
	private String datasetSet="all";
	private String metafeatureSet="all";
	
	private IHeterogenousSimilarityMeasureComputer similarityMeasure = new AlternatingGradientDescent();
	private IPipelineCharacterizer pipelineCharacterizer = new PipelineCharacterizer();
	private IRankMatrixComputer rankMatrixComputer = new RankMatrixComputer();
	
	public Metaminer(Instances dataset) {
		this.setDataset(dataset);
	}

	@Override
	public double score(ComponentInstance componentInstance) {
		if (!hasBeenBuilt) {
			throw new RuntimeException("Metaminer has not been built!");
		}
		try {
			MLPipeline pipeline = wekaPipelineFactory.getComponentInstantiation(componentInstance);
			double [] pipelineMetafeatures = pipelineCharacterizer.characterize(pipeline);
			return similarityMeasure.computeSimilarity(datasetMetafeatures, new ArrayRealVector(pipelineMetafeatures));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void build() throws Exception{
		// Check whether has been built
		if (hasBeenBuilt) {
			throw new Exception("MetaMiner has already been built!");
		} 
		
		// ----- Data set Characterization -----
		// Get training data for given datasetSet and metaFeatureSet from DB connection
		// TODO import the db connection - will result in this info
		Instances metaFeatureInformation = null;
		
		// Convert to matrix (Matrix X with rows representing data sets)

		RealMatrix datasetsMetafeatures = new Array2DRowRealMatrix(metaFeatureInformation.size(), metaFeatureInformation.numAttributes());
		for (int i = 0; i < metaFeatureInformation.size(); i++) {
			datasetsMetafeatures.setRow(i, metaFeatureInformation.get(i).toDoubleArray());
		}
		
		// Characterize the given data set with characterizer (set x)
		//TODO import data set characterizer - will result in this info
		HashMap<String,Double> datasetCharacterization = null;
		
		// Convert the characterization to a vector of double
		datasetMetafeatures = new ArrayRealVector(datasetCharacterization.size());
		int i = 0;
		for (Enumeration<Attribute> attributes = metaFeatureInformation.enumerateAttributes(); attributes.hasMoreElements(); i++) {
			datasetMetafeatures.setEntry(i,datasetCharacterization.get(attributes.nextElement().name()));
		};

		// ----- Pipeline Characterization -----
		// Get PerformanceSamples from knowledge base according to given data set set
		// TODO import knowledge base
		
		// Initialize PipelineCharacterizer with PerformanceSamples (or more, more concrete implementations of PerformanceSamples)
		// TODO import performance samples and knowledge base
		
		// Get Characterization of base pipelines from PipelineCharacterizer (Matrix W)
		RealMatrix pipelinesMetafeatures = new Array2DRowRealMatrix(pipelineCharacterizer.getCharacterizationsOfTrainingExamples());
		
		// Compute Rank Matrix R
		RealMatrix rankMatrix = rankMatrixComputer.computeRankMatrix(datasetsMetafeatures, pipelinesMetafeatures);
		
		// Initialize HeterogenousSimilarityMeasures
		similarityMeasure.build(datasetsMetafeatures, pipelinesMetafeatures, rankMatrix);
		
		// building is finished
		hasBeenBuilt=true;
	}

	public Instances getDataset() {
		return dataset;
	}

	public void setDataset(Instances dataset) {
		this.dataset = dataset;
	}

	/**
	 * @return the datasetSet
	 */
	public String getDatasetSet() {
		return datasetSet;
	}

	/**
	 * @param datasetSet the datasetSet to set
	 */
	public void setDatasetSet(String datasetSet) {
		this.datasetSet = datasetSet;
	}

	/**
	 * @return the metafeatureSet
	 */
	public String getMetafeatureSet() {
		return metafeatureSet;
	}

	/**
	 * @param metafeatureSet the metafeatureSet to set
	 */
	public void setMetafeatureSet(String metafeatureSet) {
		this.metafeatureSet = metafeatureSet;
	}

}
