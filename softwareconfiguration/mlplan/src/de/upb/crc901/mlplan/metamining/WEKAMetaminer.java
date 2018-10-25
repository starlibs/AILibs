package de.upb.crc901.mlplan.metamining;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.IPipelineCharacterizer;
import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.WEKAPipelineCharacterizer;
import de.upb.crc901.mlplan.metamining.similaritymeasures.F3Optimizer;
import de.upb.crc901.mlplan.metamining.similaritymeasures.IHeterogenousSimilarityMeasureComputer;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import hasco.metamining.IMetaMiner;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import weka.core.Attribute;
import weka.core.Instances;

public class WEKAMetaminer implements IMetaMiner {

	private boolean hasBeenBuilt = false;
	private WEKAPipelineFactory wekaPipelineFactory = new WEKAPipelineFactory();

	private Instances dataset;
	private INDArray datasetMetafeatures;

	private String datasetSet = "all";
	private String metafeatureSet = "all";

	private IHeterogenousSimilarityMeasureComputer similarityMeasure = new F3Optimizer(0.1);
	private IPipelineCharacterizer pipelineCharacterizer = new WEKAPipelineCharacterizer();
	private ComponentLoader componentLoader;

	public WEKAMetaminer(Instances dataset) {
		this.dataset = dataset;
	}

	@Override
	public double score(ComponentInstance componentInstance) {
		if (!hasBeenBuilt) {
			throw new RuntimeException("Metaminer has not been built!");
		}
		try {
			MLPipeline pipeline = wekaPipelineFactory.getComponentInstantiation(componentInstance);
			double[] pipelineMetafeatures = pipelineCharacterizer.characterize(pipeline);
			return similarityMeasure.computeSimilarity(datasetMetafeatures, Nd4j.create(pipelineMetafeatures));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void build() throws Exception {
		// Check whether has been built
		if (hasBeenBuilt) {
			throw new Exception("MetaMiner has already been built!");
		}

		// ----- Data set Characterization -----
		// Get training data for given datasetSet and metaFeatureSet from DB connection
		// TODO import the db connection - will result in this info
		Instances metaFeatureInformation = null;

		// Convert to matrix (Matrix X with rows representing data sets) with ascending
		// data set indices (data set index itself excluded)
		INDArray datasetsMetafeatures = Nd4j.create(metaFeatureInformation.size() - 1,
				metaFeatureInformation.numAttributes());
		for (int i = 1; i < metaFeatureInformation.size(); i++) {
			datasetsMetafeatures.putRow(i - 1, Nd4j.create(metaFeatureInformation.get(i).toDoubleArray()));
		}

		// Characterize the given data set with characterizer (set x)
		// TODO import data set characterizer - will result in this info
		HashMap<String, Double> datasetCharacterization = null;

		// Convert the characterization to a vector of double (ensure same order of
		// attributes as training data)
		datasetMetafeatures = Nd4j.create(datasetCharacterization.size());
		int i = 0;
		for (Enumeration<Attribute> attributes = metaFeatureInformation.enumerateAttributes(); attributes
				.hasMoreElements(); i++) {
			datasetMetafeatures.putScalar(i, datasetCharacterization.get(attributes.nextElement().name()));
		}
		;

		// ----- Pipeline Characterization -----
		// Get PerformanceSamples from knowledge base according to given data set set
		// TODO import knowledge base
		// Compute Rank Matrix R
		INDArray rankMatrix = null;
		// Extract list of distinct pipelines from that (or purposefully get a only
		// samples for a list of pipelines before!)
		ArrayList<MLPipeline> distinctPipelines = new ArrayList<MLPipeline>();

		// Initialize PipelineCharacterizer with list of distinct pipelines
		pipelineCharacterizer.build(distinctPipelines);

		// Get Characterization of base pipelines from PipelineCharacterizer (Matrix W)
		INDArray pipelinesMetafeatures = Nd4j.create(pipelineCharacterizer.getCharacterizationsOfTrainingExamples());

		// Initialize HeterogenousSimilarityMeasures
		similarityMeasure.build(datasetsMetafeatures, pipelinesMetafeatures, rankMatrix);

		// building is finished
		hasBeenBuilt = true;
	}

	public Instances getDataset() {
		return dataset;
	}

	/**
	 * @return the datasetSet
	 */
	public String getDatasetSet() {
		return datasetSet;
	}

	/**
	 * @param datasetSet
	 *            the datasetSet to set
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
	 * @param metafeatureSet
	 *            the metafeatureSet to set
	 */
	public void setMetafeatureSet(String metafeatureSet) {
		this.metafeatureSet = metafeatureSet;
	}

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
