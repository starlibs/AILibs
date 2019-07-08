package ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter;

public class ScoredSolutionCandidateInfo {

	private String solutionCandidateRepresentation;
	private String score;

	protected ScoredSolutionCandidateInfo() {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((score == null) ? 0 : score.hashCode());
		result = prime * result + ((solutionCandidateRepresentation == null) ? 0 : solutionCandidateRepresentation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ScoredSolutionCandidateInfo other = (ScoredSolutionCandidateInfo) obj;
		if (score == null) {
			if (other.score != null) {
				return false;
			}
		} else if (!score.equals(other.score)) {
			return false;
		}
		if (solutionCandidateRepresentation == null) {
			if (other.solutionCandidateRepresentation != null) {
				return false;
			}
		} else if (!solutionCandidateRepresentation.equals(other.solutionCandidateRepresentation)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ScoredSolutionCandidateInfo [solutionCandidateRepresentation=" + solutionCandidateRepresentation + ", score=" + score + "]";
	}

}
