package ai.libs.jaicore.experiments;

public interface IExperimentDecoder<I, A> {
	public I getProblem(Experiment experiment);

	public A getAlgorithm(Experiment experiment);
}
