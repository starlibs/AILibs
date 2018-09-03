package autofe.algorithm.hasco;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.nd4j.linalg.api.ndarray.INDArray;

import com.google.common.eventbus.Subscribe;

import autofe.util.DataSet;
import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import hasco.events.HASCOSolutionEvent;
import jaicore.basic.SQLAdapter;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public abstract class AbstractAutoFEMLClassifier implements IFEMLClassifier {

	private AutoFEWekaPipeline selectedPipeline;

	private SQLAdapter adapter;
	private int experimentID;
	private String evalTable;

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
	public double classifyInstance(final Instance instance) throws Exception {
		if (this.selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return this.selectedPipeline.classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(final Instance arg0) throws Exception {
		if (this.selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return null;
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
	public double classifyInstance(final INDArray instance, final Instances refInstances) throws Exception {
		if (this.selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return this.selectedPipeline.classifyInstance(instance, refInstances);
	}

	@Override
	public double[] distributionForInstance(final INDArray instance, final Instances refInstances) throws Exception {
		if (this.selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return this.selectedPipeline.distributionForInstance(instance, refInstances);
	}

	@Override
	public Instances transformData(final DataSet data) throws InterruptedException {
		if (this.selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return this.selectedPipeline.transformData(data);
	}

	@Override
	public Instance transformData(final INDArray instance, final Instances refInstances) throws InterruptedException {
		if (this.selectedPipeline == null) {
			throw new IllegalArgumentException("This classifier needs to be built first.");
		}
		return this.selectedPipeline.transformData(instance, refInstances);
	}

	public abstract void enableVisualization(boolean enableVisualization);

	public void setSQLAdapter(final SQLAdapter adapter, final int experimentID, final String evalTable) {
		this.adapter = adapter;
		this.experimentID = experimentID;
		this.evalTable = evalTable;
	}

	@Subscribe
	public void rcvHASCOSolutionEvent(final HASCOSolutionEvent<ForwardDecompositionSolution, MLPipeline, Double> e) {
		if (this.adapter != null) {
			Map<String, Object> eval = new HashMap<>();
			eval.put("run_id", this.experimentID);
			eval.put("preprocessor", "-");
			eval.put("classifier", e.getSolution().getSolution().toString());
			eval.put("errorRate", e.getSolution().getScore());
			eval.put("time_train", e.getSolution().getTimeToComputeScore());
			eval.put("time_predict", -1);
			try {
				this.adapter.insert(this.evalTable, eval);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

}
