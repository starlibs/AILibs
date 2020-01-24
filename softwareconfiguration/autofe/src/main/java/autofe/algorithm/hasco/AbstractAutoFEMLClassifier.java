package autofe.algorithm.hasco;

import org.api4.java.ai.ml.core.exception.PredictionException;
import org.nd4j.linalg.api.ndarray.INDArray;

import ai.libs.jaicore.db.sql.SQLAdapter;
import autofe.util.DataSet;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public abstract class AbstractAutoFEMLClassifier implements IFEMLClassifier {

	private static final String CLF_UNINITIALIZED_MESSAGE = "This classifier needs to be built first.";

	protected AutoFEWekaPipeline selectedPipeline;

	protected SQLAdapter adapter;
	protected int experimentID;
	protected String evalTable;

	public SQLAdapter getAdapter() {
		return this.adapter;
	}

	public void setAdapter(final SQLAdapter adapter) {
		this.adapter = adapter;
	}

	public int getExperimentID() {
		return this.experimentID;
	}

	public void setExperimentID(final int experimentID) {
		this.experimentID = experimentID;
	}

	public String getEvalTable() {
		return this.evalTable;
	}

	public void setEvalTable(final String evalTable) {
		this.evalTable = evalTable;
	}

	protected void setSelectedPipeline(final AutoFEWekaPipeline selectedPipeline) {
		this.selectedPipeline = selectedPipeline;
	}

	public AutoFEWekaPipeline getSelectedPipeline() {
		return this.selectedPipeline;
	}

	@Override
	public void buildClassifier(final Instances data) {
		throw new UnsupportedOperationException("This operation is not supported as the features have already been engineered.");
	}

	@Override
	public double classifyInstance(final Instance instance) throws PredictionException {
		if (this.selectedPipeline == null) {
			throw new IllegalArgumentException(CLF_UNINITIALIZED_MESSAGE);
		}
		return this.selectedPipeline.classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(final Instance arg0) throws PredictionException {
		if (this.selectedPipeline == null) {
			throw new PredictionException(CLF_UNINITIALIZED_MESSAGE);
		}
		return new double[] {};
	}

	@Override
	public Capabilities getCapabilities() {
		if (this.selectedPipeline == null) {
			return null;
		} else {
			return this.selectedPipeline.getCapabilities();
		}
	}

	@Override
	public double classifyInstance(final INDArray instance, final Instances refInstances) throws PredictionException {
		if (this.selectedPipeline == null) {
			throw new IllegalArgumentException(CLF_UNINITIALIZED_MESSAGE);
		}
		return this.selectedPipeline.classifyInstance(instance, refInstances);
	}

	@Override
	public double[] distributionForInstance(final INDArray instance, final Instances refInstances) throws PredictionException {
		if (this.selectedPipeline == null) {
			throw new IllegalArgumentException(CLF_UNINITIALIZED_MESSAGE);
		}
		return this.selectedPipeline.distributionForInstance(instance, refInstances);
	}

	@Override
	public Instances transformData(final DataSet data) throws InterruptedException {
		if (this.selectedPipeline == null) {
			throw new IllegalArgumentException(CLF_UNINITIALIZED_MESSAGE);
		}
		return this.selectedPipeline.transformData(data);
	}

	@Override
	public Instance transformData(final INDArray instance, final Instances refInstances) throws InterruptedException {
		if (this.selectedPipeline == null) {
			throw new IllegalArgumentException(CLF_UNINITIALIZED_MESSAGE);
		}
		return this.selectedPipeline.transformData(instance, refInstances);
	}

	public void setSQLAdapter(final SQLAdapter adapter, final int experimentID, final String evalTable) {
		this.adapter = adapter;
		this.experimentID = experimentID;
		this.evalTable = evalTable;
	}
}
