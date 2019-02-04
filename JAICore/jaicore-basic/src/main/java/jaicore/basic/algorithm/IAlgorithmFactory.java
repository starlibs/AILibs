package jaicore.basic.algorithm;

public interface IAlgorithmFactory<I, O> {
	
	public <P> void setProblemInput(P problemInput, AlgorithmProblemTransformer<P, I> reducer);
	
	public void setProblemInput(I problemInput);
	
	public IAlgorithm<I, O> getAlgorithm();
}
