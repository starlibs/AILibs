package jaicore.basic.algorithm;

public abstract class AAlgorithmFactory<I,O> implements IAlgorithmFactory<I, O> {
	
	private I input;
	
	@Override
	public <P> void setProblemInput(P problemInput, AlgorithmProblemTransformer<P, I> reducer) {
		this.setProblemInput(reducer.transform(problemInput));
	}

	@Override
	public void setProblemInput(I problemInput) {
		this.input = problemInput;
	}

	public I getInput() {
		return input;
	}
}
