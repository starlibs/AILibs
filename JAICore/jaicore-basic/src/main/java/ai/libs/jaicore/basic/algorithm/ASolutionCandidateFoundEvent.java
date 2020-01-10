package ai.libs.jaicore.basic.algorithm;

import java.util.concurrent.atomic.AtomicInteger;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;

/**
 * This is to notify listeners that an algorithm has found a solution candidate.
 * Note that this is generally not the answer returned by the algorithm but only one possible candidate.
 *
 * @author Felix Mohr
 *
 * @param <O>
 *            class from which solution elements stem from
 */

public class ASolutionCandidateFoundEvent<O> extends AAlgorithmEvent implements ISolutionCandidateFoundEvent<O> {

	private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

	private final O solutionCandidate;
	private final int gid;

	public ASolutionCandidateFoundEvent(final IAlgorithm<?, ?> algorithm, final O solutionCandidate) {
		super(algorithm);
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
