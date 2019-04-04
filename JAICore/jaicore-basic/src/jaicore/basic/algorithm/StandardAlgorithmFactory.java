package jaicore.basic.algorithm;

public abstract class StandardAlgorithmFactory<I, O> implements IAlgorithmFactory<I, O> {
	private I problemInput;

	public I getProblemInput() {
		return problemInput;
	}

	@Override
	public void setProblemInput(I problemInput) {
		this.problemInput = problemInput;
	}
	
	@Override
	public <P> void setProblemInput(P problemInput, AlgorithmProblemTransformer<P, I> reducer) {
		this.problemInput = reducer.transform(problemInput);
	}
}
