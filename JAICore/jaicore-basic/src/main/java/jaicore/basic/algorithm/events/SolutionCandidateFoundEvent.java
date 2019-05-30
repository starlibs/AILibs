package jaicore.basic.algorithm.events;

public interface SolutionCandidateFoundEvent<O> extends AlgorithmEvent {
	
	public O getSolutionCandidate();
}
