package autofe.util;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Instance;
import weka.core.Instances;

public class DataSet {
	private Instances instances;
	private List<INDArray> intermediateInstances;

	private static final Logger logger = LoggerFactory.getLogger(DataSet.class);

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
		copiedInstances.setClassIndex(copiedInstances.numAttributes() - 1);

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
		if (this.intermediateInstances != null && !this.intermediateInstances.isEmpty()) {
			this.instances = DataSetUtils.matricesToInstances(this.intermediateInstances, this.instances);
		} else {
			logger.debug("Could not update any instance due to lack of intermediate instances.");
		}
	}

	public void updateIntermediateInstances(final long[] refShape) {

		if (this.instances != null) {
			List<INDArray> newIntermediates = new ArrayList<>();
			for (final Instance inst : this.instances) {
				newIntermediates.add(DataSetUtils.instanceToMatrix(inst, refShape));
			}
			this.intermediateInstances = newIntermediates;
		}
	}
}
