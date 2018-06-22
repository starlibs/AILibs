package autofe.util;

import java.util.List;

import weka.core.Instance;

public class DataSet<T> {
	private List<Instance> instances;
	private List<T> intermediateInstances;

	public DataSet(final List<Instance> instances, final List<T> intermediateInstances) {
		this.instances = instances;
		this.intermediateInstances = intermediateInstances;
	}

	public List<Instance> getInstances() {
		return instances;
	}

	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}

	public List<T> getIntermediateInstances() {
		return intermediateInstances;
	}

	public void setIntermediateInstances(List<T> intermediateInstances) {
		this.intermediateInstances = intermediateInstances;
	}
}
