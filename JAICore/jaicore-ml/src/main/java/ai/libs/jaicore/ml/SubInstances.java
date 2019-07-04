package ai.libs.jaicore.ml;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

@SuppressWarnings("serial")
public class SubInstances extends Instances {

	private final Instances supset;
	private final int[] indices;

	public SubInstances(final Instances dataset, final int[] indices) {
		super(dataset.relationName(), new ArrayList<>(WekaUtil.getAttributes(dataset,true)), indices.length);
		this.supset = dataset;
		this.indices = indices;
		this.setClassIndex(dataset.classIndex());
		this.m_Instances = null;
	}

	@Override
	public boolean add(/* @non_null@ */final Instance instance) {
		this.throwError();
		return false;
	}

	@Override
	public void add(final int index, /* @non_null@ */final Instance instance) {
		this.throwError();
	}

	@Override
	public void delete() {
		this.throwError();
	}

	@Override
	public void deleteAttributeAt(final int position) {
		this.throwError();
	}

	@Override
	public/* @non_null pure@ */Instance firstInstance() {
		return this.supset.get(this.indices[0]);
	}

	@Override
	public/* @non_null pure@ */Instance instance(final int index) {
		return this.supset.get(this.indices[index]);
	}

	@Override
	public/* @non_null pure@ */Instance get(final int index) {

		return this.supset.get(this.indices[index]);
	}

	@Override
	public/* @pure@ */int numInstances() {

		return this.indices.length;
	}

	@Override
	public/* @pure@ */int size() {
		return this.indices.length;
	}

	@Override
	public Instance remove(final int index) {
		this.throwError();
		return null;
	}

	@Override
	public Instance set(final int index, /* @non_null@ */final Instance instance) {
		this.throwError();
		return null;
	}

	@Override
	public void stratify(final int numFolds) {
		this.throwError();
	}

	/**
	 * Returns the dataset as a string in ARFF format. Strings are quoted if they contain whitespace characters, or if they are a question mark.
	 *
	 * @return the dataset in ARFF format as a string
	 */
	@Override
	public String toString() {

		StringBuilder text = new StringBuilder();

		text.append(ARFF_RELATION).append(" ").append(Utils.quote(this.m_RelationName)).append("\n\n");
		for (int i = 0; i < this.numAttributes(); i++) {
			text.append(this.attribute(i)).append("\n");
		}
		text.append("\n").append(ARFF_DATA).append("\n");

		text.append(this.stringWithoutHeader());
		return text.toString();
	}

	@Override
	protected void stratStep(final int numFolds) {
		this.throwError();
	}

	@Override
	public void swap(final int i, final int j) {
		this.throwError();
	}

	private void throwError() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(super.hashCode()).append(this.supset).append(this.indices).toHashCode();
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
		SubInstances other = (SubInstances) obj;
		return new EqualsBuilder().append(other.indices, this.indices).append(other.supset, this.supset).isEquals();
	}
}
