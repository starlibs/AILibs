package jaicore.search.algorithms.parallel.parallelexploration.distributed.clustertest;

import java.io.Serializable;

class TestNode implements Serializable {
	private static final long serialVersionUID = 793618120417152627L;
	final int min, max;

	public TestNode(int min, int max) {
		super();
		this.min = min;
		this.max = max;
	}

	public String toString() {
		return min + "/" + max;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + max;
		result = prime * result + min;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestNode other = (TestNode) obj;
		if (max != other.max)
			return false;
		if (min != other.min)
			return false;
		return true;
	}
}