package jaicore.basic.algorithm.events;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is to notify listeners that an algorithm has found a solution candidate. Note that this is generally not the answer returned by the algorithm but only one possible candidate.
 *
 * @author fmohr
 *
 * @param <O>
 *            class from which solution elements stem from
 */

public class ASolutionCandidateFoundEvent<O> extends AAlgorithmEvent implements SolutionCandidateFoundEvent<O> {

	private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

	private final O solutionCandidate;
	private final int gid;

	public ASolutionCandidateFoundEvent(final String algorithmId, final O solutionCandidate) {
		super(algorithmId);
		this.solutionCandidate = solutionCandidate;
		this.gid = ID_COUNTER.getAndIncrement();
	}

	@Override
	public O getSolutionCandidate() {
		return this.solutionCandidate;
	}

	public int getGID() {
		return this.gid;
	}
}
