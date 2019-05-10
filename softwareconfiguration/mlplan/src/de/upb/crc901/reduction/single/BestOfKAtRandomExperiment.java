package de.upb.crc901.reduction.single;

public class BestOfKAtRandomExperiment extends ReductionExperiment {

	private final int k, mccvRepeats;

	public BestOfKAtRandomExperiment(int seed, String dataset, String nameOfLeftClassifier, String nameOfInnerClassifier, String nameOfRightClassifier, int k, int mccvRepeats) {
		super(seed, dataset, nameOfLeftClassifier, nameOfInnerClassifier, nameOfRightClassifier);
		this.k = k;
		this.mccvRepeats = mccvRepeats;
	}

	public int getK() {
		return k;
	}

	public int getMccvRepeats() {
		return mccvRepeats;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + k;
		result = prime * result + mccvRepeats;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BestOfKAtRandomExperiment other = (BestOfKAtRandomExperiment) obj;
		if (k != other.k)
			return false;
		if (mccvRepeats != other.mccvRepeats)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BestOfKAtRandomExperiment [k=" + k + ", mccvRepeats=" + mccvRepeats + "]";
	}

}
