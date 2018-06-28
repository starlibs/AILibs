package autofe.util;

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
	
	public void updateInstances() {
		if(this.intermediateInstances != null)
			this.instances = DataSetUtils.matricesToInstances(this.intermediateInstances, this.instances);
		
		// TODO
//		throw new UnsupportedOperationException("Not implemented yet.");
//		if(this.intermediateInstances != null && this.intermediateInstances.get(0) instanceof FastBitmap)
//			this.instances = DataSetUtils.bitmapsToInstances((List<FastBitmap>) getIntermediateInstances(), this.instances);
//		else
//			throw new UnsupportedOperationException("Not implemented yet.");
	}
}
