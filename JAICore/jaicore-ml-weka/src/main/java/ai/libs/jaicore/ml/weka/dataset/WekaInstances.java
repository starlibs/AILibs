package ai.libs.jaicore.ml.weka.dataset;

import static ai.libs.jaicore.ml.weka.dataset.WekaInstancesUtil.extractSchema;

import java.lang.reflect.Constructor;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.common.attributedobjects.IListDecorator;

import ai.libs.jaicore.ml.core.dataset.ADataset;
import weka.core.Instance;
import weka.core.Instances;

public class WekaInstances extends ADataset<IWekaInstance> implements IWekaInstances, IListDecorator<Instances, Instance, IWekaInstance> {

	/**
	 *
	 */
	private static final long serialVersionUID = -1980814429448333405L;

	private Instances dataset;

	public WekaInstances(final Instances dataset) {
		this(dataset, extractSchema(dataset));
	}

	public WekaInstances(final Instances dataset, final ILabeledInstanceSchema schema) {
		super(schema);
		this.dataset = dataset;
	}

	public WekaInstances(final ILabeledDataset<? extends ILabeledInstance> dataset) {
		super(dataset.getInstanceSchema());
		if (dataset instanceof WekaInstances) {
			this.dataset = ((WekaInstances) dataset).dataset;
		} else {
			try {
				this.dataset = WekaInstancesUtil.datasetToWekaInstances(dataset);
			} catch (UnsupportedAttributeTypeException e) {
				throw new IllegalArgumentException("Could not convert dataset to weka's Instances.", e);
			}
		}
	}

	public Instances getInstances() {
		return this.dataset;
	}

	@Override
	public void removeColumn(final int columnPos) {

	}

	@Override
	public IWekaInstances createEmptyCopy() throws DatasetCreationException {
		return new WekaInstances(new Instances(this.dataset, 0));
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hb = new HashCodeBuilder();
		for (IWekaInstance inst : this) {
			hb.append(inst.hashCode());
		}
		return hb.toHashCode();
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
		WekaInstances other = (WekaInstances) obj;
		int n = this.size();
		for (int i = 0; i < n; i++) {
			if (!this.get(i).equals(other.get(i))) {
				return false;
			}
		}
		return true;
	}

	public int getFrequency(final IWekaInstance instance) {
		return (int) this.stream().filter(instance::equals).count();
	}

	@Override
	public String toString() {
		return "WekaInstances [schema=" + this.getInstanceSchema() + "]\n" + this.dataset;
	}

	@Override
	public Class<IWekaInstance> getTypeOfDecoratingItems() {
		return IWekaInstance.class;
	}

	@Override
	public Class<Instance> getTypeOfDecoratedItems() {
		return Instance.class;
	}

	@Override
	public Constructor<? extends IWekaInstance> getConstructorForDecoratedItems() {
		try {
			return WekaInstance.class.getConstructor(this.getTypeOfDecoratedItems());
		} catch (Exception e) {
			throw new IllegalArgumentException("The constructor of the list class could not be invoked.");
		}
	}

	@Override
	public Instances getList() {
		return this.dataset;
	}

}
