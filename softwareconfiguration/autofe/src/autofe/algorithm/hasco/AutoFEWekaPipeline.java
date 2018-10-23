package autofe.algorithm.hasco;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class AutoFEWekaPipeline implements IFEMLClassifier, Serializable, Cloneable {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutoFEWekaPipeline.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 1847043351230983666L;

	private FilterPipeline filterPipeline;
	private Classifier mlPipeline;

	public AutoFEWekaPipeline(final FilterPipeline filterPipeline, final Classifier mlPipeline) {
		this.filterPipeline = filterPipeline;
		this.mlPipeline = mlPipeline;
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		this.mlPipeline.buildClassifier(data);
	}

	@Override
	public void buildClassifier(final DataSet data) throws Exception {
		this.mlPipeline.buildClassifier(this.transformData(data));
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		return this.mlPipeline.classifyInstance(instance);
	}

	@Override
	public double classifyInstance(final INDArray instance, final Instances refInstances) throws Exception {
		return this.classifyInstance(this.transformData(instance, refInstances));
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		return this.mlPipeline.distributionForInstance(instance);
	}

	@Override
	public double[] distributionForInstance(final INDArray instance, final Instances refInstances) throws Exception {
		return this.mlPipeline.distributionForInstance(this.transformData(instance, refInstances));
	}

	@Override
	public Capabilities getCapabilities() {
		return this.mlPipeline.getCapabilities();
	}

	@Override
	public Instances transformData(final DataSet data) throws InterruptedException {
		DataSet intermediateData = data;
		if (this.filterPipeline != null && this.filterPipeline.getFilters() != null
				&& !this.filterPipeline.getFilters().isEmpty()) {
			intermediateData = this.filterPipeline.applyFilter(intermediateData, true);
		}
		return DataSetUtils.matricesToInstances(intermediateData);
	}

	@Override
	public Instance transformData(final INDArray instance, final Instances refInstances) throws InterruptedException {
		List<INDArray> data = new LinkedList<>();
		data.add(instance);
		Instances wekaData = this.transformData(new DataSet(refInstances, data));
		return wekaData.get(0);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.filterPipeline);
		sb.append("=>");

		if (this.mlPipeline instanceof MLPipeline) {
			sb.append(this.mlPipeline);
		} else {
			sb.append(WekaUtil.getClassifierDescriptor(this.mlPipeline));
		}

		return sb.toString();
	}

	public FilterPipeline getFilterPipeline() {
		return this.filterPipeline;
	}

	public Classifier getMLPipeline() {
		return this.mlPipeline;
	}

	@Override
	public AutoFEWekaPipeline clone() throws CloneNotSupportedException {
		try {
			return new AutoFEWekaPipeline(this.filterPipeline.clone(), WekaUtil.cloneClassifier(this.mlPipeline));
		} catch (Exception e) {
			LOGGER.error("Could not clone AutoFEWekaPipeline due to '" + e.getMessage()
					+ "'. Returning null object instead.");
			return null;
		}
	}
}
