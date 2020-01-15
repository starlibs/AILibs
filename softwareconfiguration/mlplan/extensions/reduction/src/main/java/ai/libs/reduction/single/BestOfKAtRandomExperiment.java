package ai.libs.reduction.single;

public class BestOfKAtRandomExperiment extends ReductionExperiment {

	private final int k;
	private final int mccvRepeats;

	public BestOfKAtRandomExperiment(final int seed, final String dataset, final String nameOfLeftClassifier, final String nameOfInnerClassifier, final String nameOfRightClassifier, final int k, final int mccvRepeats) {
		super(seed, dataset, nameOfLeftClassifier, nameOfInnerClassifier, nameOfRightClassifier);
		this.k = k;
		this.mccvRepeats = mccvRepeats;
	}

	public int getK() {
		return this.k;
	}

	public int getMccvRepeats() {
		return this.mccvRepeats;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.k;
		result = prime * result + this.mccvRepeats;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		BestOfKAtRandomExperiment other = (BestOfKAtRandomExperiment) obj;
		if (this.k != other.k) {
			return false;
		}
		return this.mccvRepeats == other.mccvRepeats;
	}

	@Override
	public String toString() {
		return "BestOfKAtRandomExperiment [k=" + this.k + ", mccvRepeats=" + this.mccvRepeats + "]";
	}

}
