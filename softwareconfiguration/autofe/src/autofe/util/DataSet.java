package autofe.util;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

import weka.core.Instances;

public class DataSet {
	private Instances instances;
	private List<INDArray> intermediateInstances;

	public DataSet(final Instances instances, final List<INDArray> intermediateInstances) {
		this.instances = instances;
		this.intermediateInstances = intermediateInstances;
	}

	public Instances getInstances() {
		return instances;
	}

	public void setInstances(Instances instances) {
		this.instances = instances;
	}

	public List<INDArray> getIntermediateInstances() {
		return intermediateInstances;
	}

	public void setIntermediateInstances(List<INDArray> intermediateInstances) {
		this.intermediateInstances = intermediateInstances;
	}

	public DataSet copy() {
		Instances copiedInstances = new Instances(this.instances);
		List<INDArray> copiedIntermediates = null;
		if (this.intermediateInstances != null) {
			copiedIntermediates = new ArrayList<>();
			for (INDArray array : this.intermediateInstances) {
				copiedIntermediates.add(array.dup());
			}
		}
		return new DataSet(copiedInstances, copiedIntermediates);
	}

	public void updateInstances() {

		if (this.intermediateInstances != null) {
			System.out.println("Updating instances...");
			System.out.println("Num intermediate matrices: " + this.intermediateInstances.size());
			this.instances = DataSetUtils.matricesToInstances(this.intermediateInstances, this.instances);
			System.out.println("Num instances new: " + this.instances.numInstances());
		} else
			System.out.println("No updates");
	}
}
