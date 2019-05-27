package jaicore.ml.experiments;

public class MLExperiment {
	private final String dataset;
	private final String algorithm;
	private final String algorithmMode;
	private final int seed;
	private final int timeoutInSeconds;
	private final int cpus;
	private final int memoryInMB;
	private final String performanceMeasure;

	public MLExperiment(final String dataset, final String algorithm, final String algorithmMode, final int seed, final int timeoutInSeconds, final int cpus, final int memoryInMB,
			final String performanceMeasure) {
		super();
		this.dataset = dataset;
		this.algorithm = algorithm;
		this.algorithmMode = algorithmMode;
		this.seed = seed;
		this.timeoutInSeconds = timeoutInSeconds;
		this.cpus = cpus;
		this.memoryInMB = memoryInMB;
		this.performanceMeasure = performanceMeasure;
	}

	public String getDataset() {
		return this.dataset;
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	public String getAlgorithmMode() {
		return this.algorithmMode;
	}

	public int getSeed() {
		return this.seed;
	}

	public int getTimeoutInSeconds() {
		return this.timeoutInSeconds;
	}

	public int getCpus() {
		return this.cpus;
	}

	public int getMemoryInMB() {
		return this.memoryInMB;
	}

	public String getPerformanceMeasure() {
		return this.performanceMeasure;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.algorithm == null) ? 0 : this.algorithm.hashCode());
		result = prime * result + ((this.algorithmMode == null) ? 0 : this.algorithmMode.hashCode());
		result = prime * result + this.cpus;
		result = prime * result + ((this.dataset == null) ? 0 : this.dataset.hashCode());
		result = prime * result + this.memoryInMB;
		result = prime * result + ((this.performanceMeasure == null) ? 0 : this.performanceMeasure.hashCode());
		result = prime * result + this.seed;
		result = prime * result + this.timeoutInSeconds;
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
		MLExperiment other = (MLExperiment) obj;
		if (this.algorithm == null) {
			if (other.algorithm != null) {
				return false;
			}
		} else if (!this.algorithm.equals(other.algorithm)) {
			return false;
		}
		if (this.algorithmMode == null) {
			if (other.algorithmMode != null) {
				return false;
			}
		} else if (!this.algorithmMode.equals(other.algorithmMode)) {
			return false;
		}
		if (this.cpus != other.cpus) {
			return false;
		}
		if (this.dataset == null) {
			if (other.dataset != null) {
				return false;
			}
		} else if (!this.dataset.equals(other.dataset)) {
			return false;
		}
		if (this.memoryInMB != other.memoryInMB) {
			return false;
		}
		if (this.performanceMeasure == null) {
			if (other.performanceMeasure != null) {
				return false;
			}
		} else if (!this.performanceMeasure.equals(other.performanceMeasure)) {
			return false;
		}
		if (this.seed != other.seed) {
			return false;
		}
		return this.timeoutInSeconds == other.timeoutInSeconds;
	}

	@Override
	public String toString() {
		return "Experiment [dataset=" + this.dataset + ", algorithm=" + this.algorithm + ", algorithmMode=" + this.algorithmMode + ", seed=" + this.seed
				+ ", timeoutInSeconds=" + this.timeoutInSeconds + ", cpus=" + this.cpus + ", memoryInMB=" + this.memoryInMB + ", performanceMeasure=" + this.performanceMeasure + "]";
	}
}
