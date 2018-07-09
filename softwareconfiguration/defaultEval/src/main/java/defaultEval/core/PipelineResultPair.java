package defaultEval.core;

import de.upb.crc901.automl.pipeline.service.MLServicePipeline;

public class PipelineResultPair{
	public MLServicePipeline pipeline;
	public double result = -1;
	
	public PipelineResultPair(MLServicePipeline value0) {
		this.pipeline = value0;
	}
}
