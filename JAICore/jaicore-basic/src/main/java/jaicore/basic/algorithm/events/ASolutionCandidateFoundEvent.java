package jaicore.basic.algorithm.events;

/**
 * This is to notify listeners that an algorithm has found a solution candidate. Note that this is generally not the answer returned by the algorithm but only one possible candidate.
 * 
 * @author fmohr
 *
 * @param <O>
 *            class from which solution elements stem from
 */

public class ASolutionCandidateFoundEvent<O> extends AAlgorithmEvent {

	private final O solutionCandidate;

	public ASolutionCandidateFoundEvent(String algorithmId, O solutionCandidate) {
			super(algorithmId);
			this.solutionCandidate = solutionCandidate;
		}

	public O getSolutionCandidate() {
		return solutionCandidate;
	}
}
