package de.upb.crc901.mlplan.classifiers;

import java.util.Enumeration;

import de.upb.crc901.mlplan.search.algorithms.GraphBasedPipelineSearcher;
import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;

public class MultiLabelGraphBasedPipelineSearcher<T, A, V extends Comparable<V>> implements MultiLabelClassifier {

	private final GraphBasedPipelineSearcher<T, A, V> searcher;

	public MultiLabelGraphBasedPipelineSearcher(GraphBasedPipelineSearcher<T, A, V> searcher) {
		super();
		this.searcher = searcher;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		this.searcher.buildClassifier(data);
		Classifier c = searcher.getSelectedModel();
		if (c != null && !(c instanceof MultiLabelClassifier))
			throw new IllegalStateException("Determined non-multilabel classifier.");
	}

	@Override
	public double classifyInstance(Instance instance) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		return searcher.distributionForInstance(instance);
	}

	@Override
	public Capabilities getCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Option> listOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDebug(boolean debug) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getDebug() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String debugTipText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	public GraphBasedPipelineSearcher<T, A, V> getSearcher() {
		return searcher;
	}
}
