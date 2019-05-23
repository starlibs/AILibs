package autofe.algorithm.hasco;

import org.nd4j.linalg.api.ndarray.INDArray;

import com.google.common.eventbus.Subscribe;

import autofe.util.DataSet;
import hasco.core.HASCOSolutionCandidate;
import jaicore.basic.SQLAdapter;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public abstract class AbstractAutoFEMLClassifier implements IFEMLClassifier {

	protected AutoFEWekaPipeline selectedPipeline;

	protected SQLAdapter adapter;
	protected int experimentID;
	protected String evalTable;

	public SQLAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(final SQLAdapter adapter) {
		this.adapter = adapter;
	}

	public int getExperimentID() {
		return experimentID;
	}

	public void setExperimentID(final int experimentID) {
		this.experimentID = experimentID;
	}

	public String getEvalTable() {
		return evalTable;
	}

	public void setEvalTable(final String evalTable) {
		this.evalTable = evalTable;
	}

	protected void setSelectedPipeline(final AutoFEWekaPipeline selectedPipeline) {
		this.selectedPipeline = selectedPipeline;
	}

	public AutoFEWekaPipeline getSelectedPipeline() {
		return selectedPipeline;
	}

	@Override
	public void buildClassifier(final Instances data) {
		throw new UnsupportedOperationException(
				"This operation is not supported as the features have already been engineered.");
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		if (selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return selectedPipeline.classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(final Instance arg0) throws Exception {
		if (selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return null;
	}

	@Override
	public Capabilities getCapabilities() {
		if (selectedPipeline == null) {
			return null;
		} else {
			return selectedPipeline.getCapabilities();
		}
	}

	@Override
	public double classifyInstance(final INDArray instance, final Instances refInstances) throws Exception {
		if (selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return selectedPipeline.classifyInstance(instance, refInstances);
	}

	@Override
	public double[] distributionForInstance(final INDArray instance, final Instances refInstances) throws Exception {
		if (selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return selectedPipeline.distributionForInstance(instance, refInstances);
	}

	@Override
	public Instances transformData(final DataSet data) throws InterruptedException {
		if (selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return selectedPipeline.transformData(data);
	}

	@Override
	public Instance transformData(final INDArray instance, final Instances refInstances) throws InterruptedException {
		if (selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return selectedPipeline.transformData(instance, refInstances);
	}

	public void setSQLAdapter(final SQLAdapter adapter, final int experimentID, final String evalTable) {
		this.adapter = adapter;
		this.experimentID = experimentID;
		this.evalTable = evalTable;
	}

	// @Subscribe
	// public void rcvHASCOSolutionEvent(final
	// SolutionCandidateFoundEvent<HASCOSolutionCandidate<Double>> e) {
	// if (this.adapter != null) {
	// new
	// AutoFEWeka.getComponentInstantiation(e.getSolutionCandidate().getComponentInstance());
	// Map<String, Object> eval = new HashMap<>();
	// eval.put("run_id", this.experimentID);
	// eval.put("preprocessor", "-");
	// eval.put("classifier",
	// WekaUtil.getClassifierDescriptor(pl.getBaseClassifier()));
	// eval.put("errorRate", e.getSolutionCandidate().getScore());
	// eval.put("time_train", e.getSolution().getTimeToComputeScore());
	// eval.put("time_predict", -1);
	// try {
	// this.adapter.insert(this.evalTable, eval);
	// } catch (SQLException e1) {
	// e1.printStackTrace();
	// }
	// }
	// }

	@Subscribe
	public abstract void rcvHASCOSolutionEvent(final SolutionCandidateFoundEvent<HASCOSolutionCandidate<Double>> e)
			throws Exception;

}
