package jaicore.search.gui.plugins.rollouthistograms;

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

}
