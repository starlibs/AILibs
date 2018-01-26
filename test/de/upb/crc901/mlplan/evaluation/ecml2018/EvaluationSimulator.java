package de.upb.crc901.mlplan.evaluation.ecml2018;

public class EvaluationSimulator {

	public static void main(String[] args) throws Exception {
		
		for (int i = 0; i < 12; i++) {
			int runId = 265 + i * 2;
			PipelineEvaluation.main(new String[] { "testrsc/autowekasets/", "" + runId});
		}
	}

}
