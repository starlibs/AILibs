package ai.libs.jaicore.ml.weka.classification.pipeline;

import java.io.Serializable;

import ai.libs.jaicore.ml.weka.WekaUtil;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.core.Instance;
import weka.core.Instances;

@SuppressWarnings("serial")
public class SupervisedFilterSelector implements Serializable, FeaturePreprocessor {
	private final ASSearch searcher;
	private final ASEvaluation evaluator;
	private final AttributeSelection selector;
	private boolean prepared;

	public SupervisedFilterSelector(final ASSearch searcher, final ASEvaluation evaluator) {
		super();
		this.searcher = searcher;
		this.evaluator = evaluator;
		this.selector = new AttributeSelection();
		this.selector.setSearch(searcher);
		this.selector.setEvaluator(evaluator);
	}

	public SupervisedFilterSelector(final ASSearch searcher, final ASEvaluation evaluator, final AttributeSelection selector) {
		super();
		this.searcher = searcher;
		this.evaluator = evaluator;
		this.selector = selector;
	}

	public ASSearch getSearcher() {
		return this.searcher;
	}

	public ASEvaluation getEvaluator() {
		return this.evaluator;
	}

	public AttributeSelection getSelector() {
		return this.selector;
	}

	@Override
	public void prepare(final Instances data) throws PreprocessingException {
		try {
			this.selector.SelectAttributes(data);
		} catch (Exception e) {
			throw new PreprocessingException(e);
		}
		this.prepared = true;
	}

	@Override
	public Instance apply(final Instance data) throws PreprocessingException {
		if (!this.prepared) {
			throw new IllegalStateException("Cannot apply preprocessor before it has been prepared!");
		}
		try {
			return this.selector.reduceDimensionality(data);
		} catch (Exception e) {
			throw new PreprocessingException(e);
		}
	}

	@Override
	public Instances apply(final Instances data) throws PreprocessingException {
		if (!this.prepared) {
			throw new IllegalStateException("Cannot apply preprocessor before it has been prepared!");
		}
		try {
			return this.selector.reduceDimensionality(data);
		} catch (Exception e) {
			throw new PreprocessingException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.evaluator == null) ? 0 : this.evaluator.hashCode());
		result = prime * result + ((this.searcher == null) ? 0 : this.searcher.hashCode());
		result = prime * result + ((this.selector == null) ? 0 : this.selector.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SupervisedFilterSelector other = (SupervisedFilterSelector) obj;
		if (this.evaluator == null) {
			if (other.evaluator != null) {
				return false;
			}
		} else if (!this.evaluator.equals(other.evaluator)) {
			return false;
		}
		if (this.searcher == null) {
			if (other.searcher != null) {
				return false;
			}
		} else if (!this.searcher.equals(other.searcher)) {
			return false;
		}
		if (this.selector == null) {
			if (other.selector != null) {
				return false;
			}
		} else if (!this.selector.equals(other.selector)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isPrepared() {
		return this.prepared;
	}

	public void setPrepared(final boolean prepared) {
		this.prepared = prepared;
	}

	@Override
	public String toString() {
		return "SupervisedFilterSelector [searcher=" + WekaUtil.getPreprocessorDescriptor(this.searcher) + ", evaluator=" + WekaUtil.getPreprocessorDescriptor(this.evaluator) + "]";
	}
}
