package jaicore.search.util;

import java.util.List;

public class CycleDetectedResult<N> extends SanityCheckResult {
	private final List<N> wholePath;
	private final N duplicateNode;

	public CycleDetectedResult(List<N> wholePath, N duplicateNode) {
		super();
		this.wholePath = wholePath;
		this.duplicateNode = duplicateNode;
	}

	public List<N> getWholePath() {
		return wholePath;
	}

	public N getDuplicateNode() {
		return duplicateNode;
	}

	@Override
	public String toString() {
		return "CycleDetectedResult [wholePath=" + wholePath + ", duplicateNode=" + duplicateNode + "]";
	}
}
