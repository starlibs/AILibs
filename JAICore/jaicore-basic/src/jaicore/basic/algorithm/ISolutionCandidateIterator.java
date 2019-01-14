package jaicore.basic.algorithm;

import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;

public interface ISolutionCandidateIterator<O> {
	public O nextSolutionCandidate() throws TimeoutException, AlgorithmExecutionCanceledException, InterruptedException, AlgorithmException;
	public SolutionCandidateFoundEvent<O> nextSolutionCandidateEvent() throws TimeoutException, AlgorithmExecutionCanceledException, InterruptedException, AlgorithmException;
}
