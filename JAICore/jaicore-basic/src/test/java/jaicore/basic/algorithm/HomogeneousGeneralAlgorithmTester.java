package jaicore.basic.algorithm;

/**
 * 
 * This is just a simplification class for the case that no problem reduction is involved.
 * 
 * @author fmohr
 *
 * @param <I> class of algorithm input
 * @param <O> class of algorithm output
 */
public abstract class HomogeneousGeneralAlgorithmTester<I,O> extends GeneralAlgorithmTester<I, I, O> {
	
	@Override
	public AlgorithmProblemTransformer<I, I> getProblemReducer() {
		return r -> r;
	}

}
