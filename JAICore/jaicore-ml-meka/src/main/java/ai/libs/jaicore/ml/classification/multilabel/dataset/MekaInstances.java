package ai.libs.jaicore.ml.classification.multilabel.dataset;

import static ai.libs.jaicore.ml.classification.multilabel.dataset.MekaInstancesUtil.extractSchema;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.common.attributedobjects.IListDecorator;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.api4.java.common.reconstruction.IReconstructionPlan;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.basic.reconstruction.ReconstructionPlan;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstance;
import weka.core.Instance;
import weka.core.Instances;

public class MekaInstances implements IMekaInstances, IListDecorator<Instances, Instance, IMekaInstance>, IReconstructible {

	private static final long serialVersionUID = -1980814429448333405L;

	private ILabeledInstanceSchema schema;

	private final List<IReconstructionInstruction> reconstructionInstructions;

	private Instances dataset;

	public MekaInstances(final Instances dataset) {
		this(dataset, extractSchema(dataset));
		this.reconstructionInstructions.add(new ReconstructionInstruction("Instances", "__construct", new Class<?>[] {}, new Object[] {}));

	}

	public MekaInstances(final Instances dataset, final ILabeledInstanceSchema schema) {
		this.schema = schema;
		this.dataset = dataset;
		this.reconstructionInstructions = new ArrayList<>();
	}

	public MekaInstances(final ILabeledDataset<? extends ILabeledInstance> dataset) {
		this.schema = dataset.getInstanceSchema();
		if (dataset instanceof MekaInstances) {
			this.dataset = new Instances(((MekaInstances) dataset).dataset);
		} else {
			try {
				this.dataset = MekaInstancesUtil.datasetToWekaInstances(dataset);
			} catch (UnsupportedAttributeTypeException e) {
				throw new IllegalArgumentException("Could not convert dataset to weka's Instances.", e);
			}
		}
		if (this.dataset.numAttributes() != dataset.getNumAttributes() + this.dataset.classIndex()) {
			throw new IllegalStateException("Number of attributes in the MekaInstances do not coincide. We have " + this.dataset.numAttributes() + " while given dataset had " + dataset.getNumAttributes()
			+ ". There should be a difference of " + this.dataset.classIndex() + ", because WEKA counts the labels as an attribute each.");
		}
		this.reconstructionInstructions = (dataset instanceof IReconstructible) ? ((ReconstructionPlan) ((IReconstructible) dataset).getConstructionPlan()).getInstructions() : null;
	}

	@Override
	public Instances getInstances() {
		return this.dataset;
	}

	@Override
	public void removeColumn(final int columnPos) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public IMekaInstances createEmptyCopy() throws DatasetCreationException {
		return new MekaInstances(new Instances(this.dataset, 0));
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hb = new HashCodeBuilder();
		for (IMekaInstance inst : this) {
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
		MekaInstances other = (MekaInstances) obj;
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
	public Class<IMekaInstance> getTypeOfDecoratingItems() {
		return IMekaInstance.class;
	}

	@Override
	public Class<Instance> getTypeOfDecoratedItems() {
		return Instance.class;
	}

	@Override
	public Constructor<? extends IMekaInstance> getConstructorForDecoratingItems() {
		try {
			return MekaInstance.class.getConstructor(this.getTypeOfDecoratedItems());
		} catch (Exception e) {
			throw new IllegalArgumentException("The constructor of the list class could not be invoked.");
		}
	}

	@Override
	public Instances getList() {
		return this.dataset;
	}

	@Override
	public IDataset<IMekaInstance> createCopy() throws DatasetCreationException, InterruptedException {
		return new MekaInstances(this);
	}

	@Override
	public Object[] getLabelVector() {
		return WekaUtil.getClassesAsList(this.dataset).toArray();
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		return this.schema;
	}

	@Override
	public Object[][] getFeatureMatrix() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeColumn(final String columnName) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void removeColumn(final IAttribute attribute) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IReconstructionPlan getConstructionPlan() {
		return new ReconstructionPlan(this.reconstructionInstructions.stream().map(i -> (ReconstructionInstruction) i).collect(Collectors.toList()));
	}

	@Override
	public void addInstruction(final IReconstructionInstruction instruction) {
		this.reconstructionInstructions.add(instruction);
	}

}
