package jaicore.graphvisualizer.plugin.solutionperformanceplotter;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.ScoredSolutionCandidateFoundEvent;
import jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

public class ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer implements AlgorithmEventPropertyComputer {

	public static final String SCORED_SOLUTION_CANDIDATE_INFO_PROPERTY_NAME = "scored_solution_candidate_info";

	private SolutionCandidateRepresenter solutionCandidateRepresenter;

	public ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer(SolutionCandidateRepresenter solutionCandidateRepresenter) {
		this.solutionCandidateRepresenter = solutionCandidateRepresenter;
	}

	public ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer() {
		this(null);
	}

	@Override
	public Object computeAlgorithmEventProperty(AlgorithmEvent algorithmEvent) throws PropertyComputationFailedException {
		if (algorithmEvent instanceof ScoredSolutionCandidateFoundEvent) {
			ScoredSolutionCandidateFoundEvent<?, ?> solutionCandidateFoundEvent = (ScoredSolutionCandidateFoundEvent<?, ?>) algorithmEvent;
			String solutionCandidateRepresentation = getStringRepresentationOfSolutionCandidate(solutionCandidateFoundEvent.getSolutionCandidate());
			String score = solutionCandidateFoundEvent.getScore().toString();
			return new ScoredSolutionCandidateInfo(solutionCandidateRepresentation, score);
		}
		return null;
	}

	private String getStringRepresentationOfSolutionCandidate(Object solutionCandidate) {
		if (solutionCandidateRepresenter == null) {
			return solutionCandidate.toString();
		}
		return solutionCandidateRepresenter.getStringRepresentationOfSolutionCandidate(solutionCandidate);
	}

	@Override
	public String getPropertyName() {
		return SCORED_SOLUTION_CANDIDATE_INFO_PROPERTY_NAME;
	}

}
