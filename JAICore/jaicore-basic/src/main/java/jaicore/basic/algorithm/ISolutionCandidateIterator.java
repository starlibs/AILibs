package jaicore.basic.algorithm;

import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;

public interface ISolutionCandidateIterator<I, O> extends IAlgorithm<I, O> {
	public O nextSolutionCandidate() throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException, AlgorithmException;
	public SolutionCandidateFoundEvent<O> nextSolutionCandidateEvent() throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException, AlgorithmException;
}
