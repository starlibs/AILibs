package ai.libs.jaicore.ml.core.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.api4.java.common.reconstruction.IReconstructionPlan;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.basic.reconstruction.ReconstructionPlan;

public class Dataset extends ArrayList<ILabeledInstance> implements ILabeledDataset<ILabeledInstance>, IReconstructible {

	/**
	 *
	 */
	private static final long serialVersionUID = -3643080541896274181L;

	private final List<ReconstructionInstruction> instructions = new ArrayList<>();
	private final transient ILabeledInstanceSchema schema;

	public Dataset(final ILabeledInstanceSchema schema) {
		this.schema = schema;
	}

	public Dataset(final ILabeledInstanceSchema schema, final List<ILabeledInstance> instances) {
		this(schema);
		this.addAll(instances);
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		return this.schema;
	}

	@Override
	public Dataset createEmptyCopy() throws DatasetCreationException, InterruptedException {
		return new Dataset(this.schema);
	}

	@Override
	public Object[][] getFeatureMatrix() {
		return (Object[][]) IntStream.range(0, this.size()).mapToObj(x -> this.get(x).getAttributes()).toArray();
	}

	@Override
	public Object[] getLabelVector() {
		return IntStream.range(0, this.size()).mapToObj(x -> this.get(x).getLabel()).toArray();
	}

	@Override
	public void removeColumn(final int columnPos) {
		this.schema.removeAttribute(columnPos);
		this.stream().forEach(x -> x.removeColumn(columnPos));
	}

	@Override
	public void removeColumn(final String columnName) {
		Optional<IAttribute> att = this.schema.getAttributeList().stream().filter(x -> x.getName().equals(columnName)).findFirst();
		if (att.isPresent()) {
			this.removeColumn(this.schema.getAttributeList().indexOf(att.get()));
		} else {
			throw new IllegalArgumentException("There is no such attribute with name " + columnName + " to remove.");
		}
	}

	@Override
	public void removeColumn(final IAttribute attribute) {
		int index = this.schema.getAttributeList().indexOf(attribute);
		if (index >= 0) {
			this.removeColumn(index);
		} else {
			throw new IllegalArgumentException("There is no such attribute with name " + attribute.getName() + " to remove.");
		}
	}

	@Override
	public Dataset createCopy() throws DatasetCreationException, InterruptedException {
		Dataset ds = this.createEmptyCopy();
		for (ILabeledInstance i : this) {
			ds.add(i);
		}
		return ds;
	}

	@Override
	public IReconstructionPlan getConstructionPlan() {
		return new ReconstructionPlan(this.instructions);
	}

	public String getInstancesAsString() {
		StringBuilder sb = new StringBuilder();
		this.stream().map(ILabeledInstance::toString).forEach(x -> sb.append(x + "\n"));
		return sb.toString();
	}

	@Override
	public void addInstruction(final IReconstructionInstruction instruction) {
		this.instructions.add((ReconstructionInstruction) instruction);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.schema == null) ? 0 : this.schema.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Dataset other = (Dataset) obj;
		if (this.schema == null) {
			if (other.schema != null) {
				return false;
			}
		} else if (!this.schema.equals(other.schema)) {
			return false;
		}
		return true;
	}

}
