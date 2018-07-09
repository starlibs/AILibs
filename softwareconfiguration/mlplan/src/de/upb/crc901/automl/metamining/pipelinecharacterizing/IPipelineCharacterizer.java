package de.upb.crc901.automl.metamining.pipelinecharacterizing;

import java.util.List;

import de.upb.crc901.automl.pipeline.basic.MLPipeline;

public interface IPipelineCharacterizer {
	public void build(List<MLPipeline> pipelines);
	public double[] characterize(MLPipeline pipeline);
	public double[][] getCharacterizationsOfTrainingExamples();
}
