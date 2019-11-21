package ai.libs.jaicore.experiments;

import ai.libs.jaicore.experiments.exceptions.ExperimentDecodingException;

public interface IExperimentDecoder<I, A> {
	public I getProblem(Experiment experiment) throws ExperimentDecodingException;

	public A getAlgorithm(Experiment experiment) throws ExperimentDecodingException;
}
