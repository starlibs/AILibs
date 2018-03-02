package jaicore.ml.experiments;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class Experiment {
	private final String dataset;
	private final String algorithm;
	private final String algorithmMode;
	private final int seed;
	private final int timeoutInSeconds;
	private final int cpus;
	private final int memoryInMB;
	private final String performanceMeasure;

	public Experiment(String dataset, String algorithm, String algorithmMode, int seed, int timeoutInSeconds, int cpus, int memoryInMB,
			String performanceMeasure) {
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
		return dataset;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getAlgorithmMode() {
		return algorithmMode;
	}

	public int getSeed() {
		return seed;
	}

	public int getTimeoutInSeconds() {
		return timeoutInSeconds;
	}

	public int getCpus() {
		return cpus;
	}

	public int getMemoryInMB() {
		return memoryInMB;
	}

	public String getPerformanceMeasure() {
		return performanceMeasure;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
		result = prime * result + ((algorithmMode == null) ? 0 : algorithmMode.hashCode());
		result = prime * result + cpus;
		result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
		result = prime * result + memoryInMB;
		result = prime * result + ((performanceMeasure == null) ? 0 : performanceMeasure.hashCode());
		result = prime * result + seed;
		result = prime * result + timeoutInSeconds;
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
		Experiment other = (Experiment) obj;
		if (algorithm == null) {
			if (other.algorithm != null)
				return false;
		} else if (!algorithm.equals(other.algorithm))
			return false;
		if (algorithmMode == null) {
			if (other.algorithmMode != null)
				return false;
		} else if (!algorithmMode.equals(other.algorithmMode))
			return false;
		if (cpus != other.cpus)
			return false;
		if (dataset == null) {
			if (other.dataset != null)
				return false;
		} else if (!dataset.equals(other.dataset))
			return false;
		if (memoryInMB != other.memoryInMB)
			return false;
		if (performanceMeasure == null) {
			if (other.performanceMeasure != null)
				return false;
		} else if (!performanceMeasure.equals(other.performanceMeasure))
			return false;
		if (seed != other.seed)
			return false;
		if (timeoutInSeconds != other.timeoutInSeconds)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Experiment [dataset=" + dataset + ", algorithm=" + algorithm + ", algorithmMode=" + algorithmMode + ", seed=" + seed
				+ ", timeoutInSeconds=" + timeoutInSeconds + ", cpus=" + cpus + ", memoryInMB=" + memoryInMB + ", performanceMeasure=" + performanceMeasure + "]";
	}
}
