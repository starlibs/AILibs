package ai.libs.jaicore.basic.algorithm;

import ai.libs.jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;

public interface ISolutionCandidateIterator<I, O> extends IAlgorithm<I, O> {
	public O nextSolutionCandidate() throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException, AlgorithmException;
	public SolutionCandidateFoundEvent<O> nextSolutionCandidateEvent() throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException, AlgorithmException;
}
