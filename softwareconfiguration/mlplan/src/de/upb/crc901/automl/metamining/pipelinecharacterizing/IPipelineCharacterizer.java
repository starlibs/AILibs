package de.upb.crc901.automl.metamining.pipelinecharacterizing;

import de.upb.crc901.automl.pipeline.basic.MLPipeline;

public interface IPipelineCharacterizer {
	public void build();
	public double[] characterize(MLPipeline pipeline);
	public double[][] getCharacterizationsOfTrainingExamples();
}
