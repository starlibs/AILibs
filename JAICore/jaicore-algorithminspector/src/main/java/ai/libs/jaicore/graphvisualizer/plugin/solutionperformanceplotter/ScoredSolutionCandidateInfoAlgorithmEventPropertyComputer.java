package ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.IScoredSolutionCandidateFoundEvent;

import ai.libs.jaicore.graphvisualizer.events.recorder.AIndependentAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

public class ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer extends AIndependentAlgorithmEventPropertyComputer {

	public static final String SCORED_SOLUTION_CANDIDATE_INFO_PROPERTY_NAME = "scored_solution_candidate_info";

	private SolutionCandidateRepresenter solutionCandidateRepresenter;

	public ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer(final SolutionCandidateRepresenter solutionCandidateRepresenter) {
		this.solutionCandidateRepresenter = solutionCandidateRepresenter;
	}

	public ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer() {
		this(null);
	}

	@Override
	public Object computeAlgorithmEventProperty(final IAlgorithmEvent algorithmEvent) throws PropertyComputationFailedException {
		if (algorithmEvent instanceof IScoredSolutionCandidateFoundEvent) {
			IScoredSolutionCandidateFoundEvent<?, ?> solutionCandidateFoundEvent = (IScoredSolutionCandidateFoundEvent<?, ?>) algorithmEvent;
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
