package ai.libs.mlplan.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

public abstract class PipelineValidityCheckingNodeEvaluator implements IPathEvaluator<TFDNode, String, Double> {

	private ILabeledDataset<?> data;
	private List<IComponent> components;

	/* the predicates of the dataset */
	protected boolean propertiesDetermined;
	protected boolean binaryClass;
	protected boolean multiClass;
	protected boolean regression;
	protected boolean multiValuedNominalAttributes;
	protected boolean containsNegativeValues;


	public PipelineValidityCheckingNodeEvaluator() {

	}

	public PipelineValidityCheckingNodeEvaluator(final Collection<? extends IComponent> components, final ILabeledDataset<?> data) {
		this.setComponents(components);
		this.setData(data);
		this.extractDatasetProperties();
	}

	protected synchronized void extractDatasetProperties() {
		if (!this.propertiesDetermined) {
			if (this.getComponents() == null) {
				throw new IllegalStateException("Components not defined!");
			}

			/* compute binary class predicate */
			this.binaryClass = this.getData().getLabelAttribute() instanceof ICategoricalAttribute && ((ICategoricalAttribute) this.getData().getLabelAttribute()).getNumberOfCategories() == 2;
			this.multiClass = this.getData().getLabelAttribute() instanceof ICategoricalAttribute && ((ICategoricalAttribute) this.getData().getLabelAttribute()).getNumberOfCategories() > 2;
			this.regression = this.getData().getLabelAttribute() instanceof INumericAttribute;

			/* determine whether the dataset is multi-valued nominal */
			this.multiValuedNominalAttributes = false;
			for (IAttribute att : this.getData().getListOfAttributes()) {
				if (att instanceof ICategoricalAttribute && ((ICategoricalAttribute) att).getNumberOfCategories() > 2) {
					this.multiValuedNominalAttributes = true;
					break;
				}
			}

			/* determine whether dataset contains negative attribute values */
			this.containsNegativeValues = false;
			for (ILabeledInstance i : this.getData()) {
				this.containsNegativeValues = this.containsNegativeValues || Arrays.stream(i.getPoint()).anyMatch(x -> x < 0);
				if (this.containsNegativeValues) {
					break;
				}
			}

			this.propertiesDetermined = true;
		}
	}

	public void setData(final ILabeledDataset<?> data) {
		Objects.requireNonNull(data);
		this.data = data;
	}

	public void setComponents(final Collection<? extends IComponent> components) {
		Objects.requireNonNull(components);
		this.components = new ArrayList<>(components);
	}

	public ILabeledDataset<?> getData() {
		return this.data;
	}

	public Collection<IComponent> getComponents() {
		return this.components;
	}
}
