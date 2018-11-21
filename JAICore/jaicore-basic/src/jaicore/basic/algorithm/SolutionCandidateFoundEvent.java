package jaicore.basic.algorithm;

/**
 * This is to notify listeners that an algorithm has found a solution candidate.
 * Note that this is generally not the answer returned by the algorithm but only
 * one possible candidate.
 * 
 * @author fmohr
 *
 * @param <O> class from which solution elements stem from
 */
public class SolutionCandidateFoundEvent<O> implements AlgorithmEvent {
	private final O solutionCandidate;
	
	public SolutionCandidateFoundEvent(O solutionCandidate) {
		super();
		this.solutionCandidate = solutionCandidate;
	}

	public O getSolutionCandidate() {
		return solutionCandidate;
	}
}
