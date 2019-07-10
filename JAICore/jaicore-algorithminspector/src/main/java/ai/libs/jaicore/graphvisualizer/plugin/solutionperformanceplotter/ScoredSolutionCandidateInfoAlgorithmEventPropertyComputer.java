package ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter;

import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.events.ScoredSolutionCandidateFoundEvent;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

public class ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer implements AlgorithmEventPropertyComputer {

	public static final String SCORED_SOLUTION_CANDIDATE_INFO_PROPERTY_NAME = "scored_solution_candidate_info";

	private SolutionCandidateRepresenter solutionCandidateRepresenter;

	public ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer(final SolutionCandidateRepresenter solutionCandidateRepresenter) {
		this.solutionCandidateRepresenter = solutionCandidateRepresenter;
	}

	public ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer() {
		this(null);
	}

	@Override
	public Object computeAlgorithmEventProperty(final AlgorithmEvent algorithmEvent) throws PropertyComputationFailedException {
		if (algorithmEvent instanceof ScoredSolutionCandidateFoundEvent) {
			ScoredSolutionCandidateFoundEvent<?, ?> solutionCandidateFoundEvent = (ScoredSolutionCandidateFoundEvent<?, ?>) algorithmEvent;
			String solutionCandidateRepresentation = this.getStringRepresentationOfSolutionCandidate(solutionCandidateFoundEvent.getSolutionCandidate());
			String score = solutionCandidateFoundEvent.getScore().toString();
			return new ScoredSolutionCandidateInfo(solutionCandidateRepresentation, score);
		}
		return null;
	}

	private String getStringRepresentationOfSolutionCandidate(final Object solutionCandidate) {
		if (this.solutionCandidateRepresenter == null) {
			return solutionCandidate.toString();
		}
		return this.solutionCandidateRepresenter.getStringRepresentationOfSolutionCandidate(solutionCandidate);
	}

	@Override
	public String getPropertyName() {
		return SCORED_SOLUTION_CANDIDATE_INFO_PROPERTY_NAME;
	}

}