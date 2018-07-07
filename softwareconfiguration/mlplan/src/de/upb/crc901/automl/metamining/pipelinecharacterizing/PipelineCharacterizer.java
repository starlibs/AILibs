package de.upb.crc901.automl.metamining.pipelinecharacterizing;

import de.upb.crc901.automl.pipeline.basic.MLPipeline;

public class PipelineCharacterizer implements IPipelineCharacterizer {
	
	private IPipelinePatternRecognitionAlgorithm patternRecognizer = new PipelinePatternRecognizer();
	private IOntologyConnector ontologyConnector = new OntologyConnector();

	@Override
	public void build() {
		// TODO Auto-generated method stub

	}

	@Override
	public double[] characterize(MLPipeline pipeline) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[][] getCharacterizationsOfTrainingExamples() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the patternRecognizer
	 */
	public IPipelinePatternRecognitionAlgorithm getPatternRecognizer() {
		return patternRecognizer;
	}

	/**
	 * @param patternRecognizer the patternRecognizer to set
	 */
	public void setPatternRecognizer(IPipelinePatternRecognitionAlgorithm patternRecognizer) {
		this.patternRecognizer = patternRecognizer;
	}

	/**
	 * @return the ontologyConnector
	 */
	public IOntologyConnector getOntologyConnector() {
		return ontologyConnector;
	}

	/**
	 * @param ontologyConnector the ontologyConnector to set
	 */
	public void setOntologyConnector(IOntologyConnector ontologyConnector) {
		this.ontologyConnector = ontologyConnector;
	}

}
