package ai.libs.jaicore.search.gui.plugins.mcts.rollouthistograms;

import java.util.List;

public class RolloutInfo {

	private List<String> path;
	private Object score;

	@SuppressWarnings("unused")
	private RolloutInfo() {
		// for serialization purposes
	}

	public RolloutInfo(List<String> path, Object score) {
		this.path = path;
		this.score = score;
	}

	public List<String> getPath() {
		return path;
	}

	public Object getScore() {
		return score;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((score == null) ? 0 : score.hashCode());
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
		RolloutInfo other = (RolloutInfo) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (score == null) {
			if (other.score != null) {
				return false;
			}
		} else if (!score.equals(other.score)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RolloutInfo [path=" + path + ", score=" + score + "]";
	}

}
