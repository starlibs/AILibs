package de.upb.crc901.reduction.ensemble.simple;

public class EnsembleOfSimpleOneStepReductionsExperiment {
	private final int seed;
	private final String dataset;
	private final String nameOfClassifier;
	private final int numberOfStumps;
	private double errorRate;
	private String exception;

	public EnsembleOfSimpleOneStepReductionsExperiment(int seed, String dataset, String nameOfClassifier, int numberOfStumps) {
		super();
		this.seed = seed;
		this.dataset = dataset;
		this.nameOfClassifier = nameOfClassifier;
		this.numberOfStumps = numberOfStumps;
	}

	public EnsembleOfSimpleOneStepReductionsExperiment(int seed, String dataset, String nameOfClassifier, int numberOfStumps, double errorRate, String exception) {
		this(seed, dataset, nameOfClassifier, numberOfStumps);
		this.errorRate = errorRate;
		this.exception = exception;
	}

	public int getSeed() {
		return seed;
	}

	public String getDataset() {
		return dataset;
	}

	public double getErrorRate() {
		return errorRate;
	}

	public void setErrorRate(double errorRate) {
		this.errorRate = errorRate;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public String getNameOfClassifier() {
		return nameOfClassifier;
	}

	public int getNumberOfStumps() {
		return numberOfStumps;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
		result = prime * result + ((nameOfClassifier == null) ? 0 : nameOfClassifier.hashCode());
		result = prime * result + numberOfStumps;
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
		EnsembleOfSimpleOneStepReductionsExperiment other = (EnsembleOfSimpleOneStepReductionsExperiment) obj;
		if (dataset == null) {
			if (other.dataset != null)
				return false;
		} else if (!dataset.equals(other.dataset))
			return false;
		if (nameOfClassifier == null) {
			if (other.nameOfClassifier != null)
				return false;
		} else if (!nameOfClassifier.equals(other.nameOfClassifier))
			return false;
		if (numberOfStumps != other.numberOfStumps)
			return false;
		if (seed != other.seed)
			return false;
		return true;
	}
}
