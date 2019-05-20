package jaicore.graphvisualizer.plugin.solutionperformanceplotter;

public class ScoredSolutionCandidateInfo {

	private String solutionCandidateRepresentation;
	private String score;

	@SuppressWarnings("unused")
	private ScoredSolutionCandidateInfo() {
		// for serialization purposes
	}

	public ScoredSolutionCandidateInfo(String solutionCandidateRepresentation, String score) {
		super();
		this.solutionCandidateRepresentation = solutionCandidateRepresentation;
		this.score = score;
	}

	public String getSolutionCandidateRepresentation() {
		return solutionCandidateRepresentation;
	}

	public String getScore() {
		return score;
	}

}
