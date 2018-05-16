package de.upb.crc901.reduction.single;

public class ReductionExperiment {
	private final int seed;
	private final String dataset;
	private final String nameOfClassifierForRPNDSplit, nameOfLeftClassifier, nameOfInnerClassifier, nameOfRightClassifier;
	private String exceptionRPND, exceptionLeft, exceptionInner, exceptionRight;

	public ReductionExperiment(int seed, String dataset, String nameOfClassifierForRPNDSplit, String nameOfLeftClassifier, String nameOfInnerClassifier, String nameOfRightClassifier) {
		super();
		this.seed = seed;
		this.dataset = dataset;
		this.nameOfClassifierForRPNDSplit = nameOfClassifierForRPNDSplit;
		this.nameOfLeftClassifier = nameOfLeftClassifier;
		this.nameOfInnerClassifier = nameOfInnerClassifier;
		this.nameOfRightClassifier = nameOfRightClassifier;
	}
	
	public ReductionExperiment(int seed, String dataset, String nameOfClassifierForRPNDSplit, String nameOfLeftClassifier, String nameOfInnerClassifier, String nameOfRightClassifier, String exceptionRPND, String exceptionLeft, String exceptionInner, String exceptionRight) {
		this(seed,dataset,nameOfClassifierForRPNDSplit,nameOfLeftClassifier,nameOfInnerClassifier,nameOfRightClassifier);
		this.exceptionRPND = exceptionRPND;
		this.exceptionLeft = exceptionLeft;
		this.exceptionInner = exceptionInner;
		this.exceptionRight = exceptionRight;
	}

	public int getSeed() {
		return seed;
	}

	public String getDataset() {
		return dataset;
	}

	public String getNameOfClassifierForRPNDSplit() {
		return nameOfClassifierForRPNDSplit;
	}

	public String getNameOfLeftClassifier() {
		return nameOfLeftClassifier;
	}

	public String getNameOfInnerClassifier() {
		return nameOfInnerClassifier;
	}

	public String getNameOfRightClassifier() {
		return nameOfRightClassifier;
	}

	public String getExceptionRPND() {
		return exceptionRPND;
	}

	public void setExceptionRPND(String exceptionRPND) {
		this.exceptionRPND = exceptionRPND;
	}

	public String getExceptionLeft() {
		return exceptionLeft;
	}

	public void setExceptionLeft(String exceptionLeft) {
		this.exceptionLeft = exceptionLeft;
	}

	public String getExceptionInner() {
		return exceptionInner;
	}

	public void setExceptionInner(String exceptionInner) {
		this.exceptionInner = exceptionInner;
	}

	public String getExceptionRight() {
		return exceptionRight;
	}

	public void setExceptionRight(String exceptionRight) {
		this.exceptionRight = exceptionRight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
		result = prime * result + ((nameOfClassifierForRPNDSplit == null) ? 0 : nameOfClassifierForRPNDSplit.hashCode());
		result = prime * result + ((nameOfInnerClassifier == null) ? 0 : nameOfInnerClassifier.hashCode());
		result = prime * result + ((nameOfLeftClassifier == null) ? 0 : nameOfLeftClassifier.hashCode());
		result = prime * result + ((nameOfRightClassifier == null) ? 0 : nameOfRightClassifier.hashCode());
		result = prime * result + seed;
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
		ReductionExperiment other = (ReductionExperiment) obj;
		if (dataset == null) {
			if (other.dataset != null)
				return false;
		} else if (!dataset.equals(other.dataset))
			return false;
		if (nameOfClassifierForRPNDSplit == null) {
			if (other.nameOfClassifierForRPNDSplit != null)
				return false;
		} else if (!nameOfClassifierForRPNDSplit.equals(other.nameOfClassifierForRPNDSplit))
			return false;
		if (nameOfInnerClassifier == null) {
			if (other.nameOfInnerClassifier != null)
				return false;
		} else if (!nameOfInnerClassifier.equals(other.nameOfInnerClassifier))
			return false;
		if (nameOfLeftClassifier == null) {
			if (other.nameOfLeftClassifier != null)
				return false;
		} else if (!nameOfLeftClassifier.equals(other.nameOfLeftClassifier))
			return false;
		if (nameOfRightClassifier == null) {
			if (other.nameOfRightClassifier != null)
				return false;
		} else if (!nameOfRightClassifier.equals(other.nameOfRightClassifier))
			return false;
		if (seed != other.seed)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReductionExperiment [seed=" + seed + ", dataset=" + dataset + ", nameOfClassifierForRPNDSplit=" + nameOfClassifierForRPNDSplit + ", nameOfLeftClassifier="
				+ nameOfLeftClassifier + ", nameOfInnerClassifier=" + nameOfInnerClassifier + ", nameOfRightClassifier=" + nameOfRightClassifier + "]";
	}
}
