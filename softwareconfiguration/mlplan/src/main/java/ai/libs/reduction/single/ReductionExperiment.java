package ai.libs.reduction.single;

public class ReductionExperiment {
	private final int seed;
	private final String dataset;
	private final String nameOfLeftClassifier;
	private final String nameOfInnerClassifier;
	private final String nameOfRightClassifier;
	private String exceptionLeft;
	private String exceptionInner;
	private String exceptionRight;

	public ReductionExperiment(final int seed, final String dataset, final String nameOfLeftClassifier, final String nameOfInnerClassifier, final String nameOfRightClassifier) {
		super();
		this.seed = seed;
		this.dataset = dataset;
		this.nameOfLeftClassifier = nameOfLeftClassifier;
		this.nameOfInnerClassifier = nameOfInnerClassifier;
		this.nameOfRightClassifier = nameOfRightClassifier;
	}

	public ReductionExperiment(final int seed, final String dataset, final String nameOfLeftClassifier, final String nameOfInnerClassifier, final String nameOfRightClassifier, final String exceptionLeft, final String exceptionInner, final String exceptionRight) {
		this(seed,dataset,nameOfLeftClassifier,nameOfInnerClassifier,nameOfRightClassifier);
		this.exceptionLeft = exceptionLeft;
		this.exceptionInner = exceptionInner;
		this.exceptionRight = exceptionRight;
	}

	public int getSeed() {
		return this.seed;
	}

	public String getDataset() {
		return this.dataset;
	}

	public String getNameOfLeftClassifier() {
		return this.nameOfLeftClassifier;
	}

	public String getNameOfInnerClassifier() {
		return this.nameOfInnerClassifier;
	}

	public String getNameOfRightClassifier() {
		return this.nameOfRightClassifier;
	}

	public String getExceptionLeft() {
		return this.exceptionLeft;
	}

	public void setExceptionLeft(final String exceptionLeft) {
		this.exceptionLeft = exceptionLeft;
	}

	public String getExceptionInner() {
		return this.exceptionInner;
	}

	public void setExceptionInner(final String exceptionInner) {
		this.exceptionInner = exceptionInner;
	}

	public String getExceptionRight() {
		return this.exceptionRight;
	}

	public void setExceptionRight(final String exceptionRight) {
		this.exceptionRight = exceptionRight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.dataset == null) ? 0 : this.dataset.hashCode());
		result = prime * result + ((this.exceptionInner == null) ? 0 : this.exceptionInner.hashCode());
		result = prime * result + ((this.exceptionLeft == null) ? 0 : this.exceptionLeft.hashCode());
		result = prime * result + ((this.exceptionRight == null) ? 0 : this.exceptionRight.hashCode());
		result = prime * result + ((this.nameOfInnerClassifier == null) ? 0 : this.nameOfInnerClassifier.hashCode());
		result = prime * result + ((this.nameOfLeftClassifier == null) ? 0 : this.nameOfLeftClassifier.hashCode());
		result = prime * result + ((this.nameOfRightClassifier == null) ? 0 : this.nameOfRightClassifier.hashCode());
		result = prime * result + this.seed;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		ReductionExperiment other = (ReductionExperiment) obj;
		if (this.dataset == null) {
			if (other.dataset != null) {
				return false;
			}
		} else if (!this.dataset.equals(other.dataset)) {
			return false;
		}
		if (this.exceptionInner == null) {
			if (other.exceptionInner != null) {
				return false;
			}
		} else if (!this.exceptionInner.equals(other.exceptionInner)) {
			return false;
		}
		if (this.exceptionLeft == null) {
			if (other.exceptionLeft != null) {
				return false;
			}
		} else if (!this.exceptionLeft.equals(other.exceptionLeft)) {
			return false;
		}
		if (this.exceptionRight == null) {
			if (other.exceptionRight != null) {
				return false;
			}
		} else if (!this.exceptionRight.equals(other.exceptionRight)) {
			return false;
		}
		if (this.nameOfInnerClassifier == null) {
			if (other.nameOfInnerClassifier != null) {
				return false;
			}
		} else if (!this.nameOfInnerClassifier.equals(other.nameOfInnerClassifier)) {
			return false;
		}
		if (this.nameOfLeftClassifier == null) {
			if (other.nameOfLeftClassifier != null) {
				return false;
			}
		} else if (!this.nameOfLeftClassifier.equals(other.nameOfLeftClassifier)) {
			return false;
		}
		if (this.nameOfRightClassifier == null) {
			if (other.nameOfRightClassifier != null) {
				return false;
			}
		} else if (!this.nameOfRightClassifier.equals(other.nameOfRightClassifier)) {
			return false;
		}
		return this.seed == other.seed;
	}

	@Override
	public String toString() {
		return "ReductionExperiment [seed=" + this.seed + ", dataset=" + this.dataset + ", nameOfLeftClassifier=" + this.nameOfLeftClassifier + ", nameOfInnerClassifier=" + this.nameOfInnerClassifier
				+ ", nameOfRightClassifier=" + this.nameOfRightClassifier + ", exceptionLeft=" + this.exceptionLeft + ", exceptionInner="
				+ this.exceptionInner + ", exceptionRight=" + this.exceptionRight + "]";
	}
}
