package de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model;

import java.io.Serializable;

import de.upb.crc901.mlplan.multiclass.wekamlplan.sophisticated.FeaturePreprocessor;
import jaicore.ml.WekaUtil;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

@SuppressWarnings("serial")
public class SuvervisedFilterPreprocessor implements Serializable, FeaturePreprocessor {
	private final ASSearch searcher;
	private final ASEvaluation evaluator;
	private final AttributeSelection selector;
	private boolean prepared;

	public SuvervisedFilterPreprocessor(ASSearch searcher, ASEvaluation evaluator) {
		super();
		this.searcher = searcher;
		this.evaluator = evaluator;
		this.selector = new AttributeSelection();
		this.selector.setSearch(searcher);
		this.selector.setEvaluator(evaluator);
	}
	
	public SuvervisedFilterPreprocessor(ASSearch searcher, ASEvaluation evaluator, AttributeSelection selector) {
		super();
		this.searcher = searcher;
		this.evaluator = evaluator;
		this.selector = selector;
	}

	public ASSearch getSearcher() {
		return searcher;
	}

	public ASEvaluation getEvaluator() {
		return evaluator;
	}

	public AttributeSelection getSelector() {
		return selector;
	}
	
	public void prepare(Instances data) throws Exception {
		selector.SelectAttributes(data);
		prepared = true;
	}
	
	public Instance apply(Instance data) throws Exception {
		if (!prepared)
			throw new IllegalStateException("Cannot apply preprocessor before it has been prepared!");
		Instance inst = selector.reduceDimensionality(data);
		if (inst.dataset().classIndex() >= 0)
			inst = WekaUtil.removeClassAttribute(inst);
		for (int i = 0; i < inst.dataset().numAttributes(); i++) {
			Attribute a = inst.dataset().attribute(i);
			inst.dataset().renameAttribute(a, this.getClass().getSimpleName() + "_" + a.name());
		}
		return inst;
	}
	
	public Instances apply(Instances data) throws Exception {
		if (!prepared)
			throw new IllegalStateException("Cannot apply preprocessor before it has been prepared!");
		Instances inst = selector.reduceDimensionality(data);
		if (inst.classIndex() >= 0)
			inst = WekaUtil.removeClassAttribute(inst);
		for (int i = 0; i < inst.numAttributes(); i++) {
			Attribute a = inst.attribute(i);
			inst.renameAttribute(a, this.getClass().getSimpleName() + "_" + a.name());
		}
		return inst;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((evaluator == null) ? 0 : evaluator.hashCode());
		result = prime * result + ((searcher == null) ? 0 : searcher.hashCode());
		result = prime * result + ((selector == null) ? 0 : selector.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SuvervisedFilterPreprocessor other = (SuvervisedFilterPreprocessor) obj;
		if (evaluator == null) {
			if (other.evaluator != null)
				return false;
		} else if (!evaluator.equals(other.evaluator))
			return false;
		if (searcher == null) {
			if (other.searcher != null)
				return false;
		} else if (!searcher.equals(other.searcher))
			return false;
		if (selector == null) {
			if (other.selector != null)
				return false;
		} else if (!selector.equals(other.selector))
			return false;
		return true;
	}

	public boolean isPrepared() {
		return prepared;
	}

	public void setPrepared(boolean prepared) {
		this.prepared = prepared;
	}
}
