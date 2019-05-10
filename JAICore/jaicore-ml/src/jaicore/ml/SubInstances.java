package jaicore.ml;

import java.util.ArrayList;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

@SuppressWarnings("serial")
public class SubInstances extends Instances {

	private final Instances supset;
	private final int[] indices;

	public SubInstances(Instances dataset, int[] indices) {
		super(dataset.relationName(), new ArrayList<>(WekaUtil.getAttributes(dataset,true)), indices.length);
		this.supset = dataset;
		this.indices = indices;
		this.setClassIndex(dataset.classIndex());
		this.m_Instances = null;
	}

	@Override
	public boolean add(/* @non_null@ */Instance instance) {
		throwError();
		return false;
	}

	@Override
	public void add(int index, /* @non_null@ */Instance instance) {
		throwError();
	}

	public void delete() {
		throwError();
	}

	public void deleteAttributeAt(int position) {
		throwError();
	}

	@Override
	public/* @non_null pure@ */Instance firstInstance() {
		return supset.get(indices[0]);
	}
	
	@Override
	public/* @non_null pure@ */Instance instance(int index) {
		return supset.get(indices[index]);
	}

	@Override
	public/* @non_null pure@ */Instance get(int index) {

		return supset.get(indices[index]);
	}
	
	@Override
	public/* @pure@ */int numInstances() {

		return indices.length;
	}
	
	@Override
	public/* @pure@ */int size() {
		return indices.length;
	}

	@Override
	public Instance remove(int index) {
		throwError();
		return null;
	}

	@Override
	public Instance set(int index, /* @non_null@ */Instance instance) {
		throwError();
		return null;
	}

	@Override
	public void stratify(int numFolds) {
		throwError();
	}

	/**
	 * Returns the dataset as a string in ARFF format. Strings are quoted if they contain whitespace characters, or if they are a question mark.
	 * 
	 * @return the dataset in ARFF format as a string
	 */
	@Override
	public String toString() {

		StringBuffer text = new StringBuffer();

		text.append(ARFF_RELATION).append(" ").append(Utils.quote(m_RelationName)).append("\n\n");
		for (int i = 0; i < numAttributes(); i++) {
			text.append(attribute(i)).append("\n");
		}
		text.append("\n").append(ARFF_DATA).append("\n");

		text.append(stringWithoutHeader());
		return text.toString();
	}

	protected void stratStep(int numFolds) {
		throwError();
	}

	@Override
	public void swap(int i, int j) {
		throwError();
	}

	private void throwError() {
		throw new UnsupportedOperationException();
	}
}
