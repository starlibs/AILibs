package de.upb.crc901.mlplan.multilabel;

import java.io.Serializable;
import java.util.Enumeration;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;

/**
 * 
 * @author Felix Mohr
 *
 */
@SuppressWarnings("serial")
public class MultilabelMLPipeline implements MultiLabelClassifier, Serializable {

	private final MLPipeline actualPipeline;
	
	private MultilabelMLPipeline(MLPipeline pl) {
		actualPipeline = pl;
	}

	public MultilabelMLPipeline(ASSearch search, ASEvaluation evaluation, MultiLabelClassifier classifier) {
		super();
		this.actualPipeline = new MLPipeline(search, evaluation, classifier);
	}

	@Override
	public void buildClassifier(Instances trainingSet) throws Exception {
		actualPipeline.buildClassifier(trainingSet);;
	}

	@Override
	public double[] distributionForInstance(Instance i) throws Exception {
		return actualPipeline.distributionForInstance(i);
	}
	
	public MultilabelMLPipeline clone() {
		return new MultilabelMLPipeline(actualPipeline.clone());
	}

	@Override
	public double classifyInstance(Instance arg0) throws Exception {
		return actualPipeline.classifyInstance(arg0);
	}

	@Override
	public Capabilities getCapabilities() {
		return actualPipeline.getCapabilities();
	}

	@Override
	public String[] getOptions() {
		return null;
	}

	@Override
	public Enumeration<Option> listOptions() {
		return null;
	}

	@Override
	public void setOptions(String[] arg0) throws Exception {
		
	}

	@Override
	public void setDebug(boolean debug) {
		
	}

	@Override
	public boolean getDebug() {
		return false;
	}

	@Override
	public String debugTipText() {
		return null;
	}

	@Override
	public String getModel() {
		return null;
	}

	public MLPipeline getActualPipeline() {
		return actualPipeline;
	}
}
