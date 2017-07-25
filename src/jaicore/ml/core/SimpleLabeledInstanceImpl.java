package jaicore.ml.core;

import java.util.ArrayList;

import jaicore.ml.interfaces.LabeledInstance;

@SuppressWarnings("serial")
public class SimpleLabeledInstanceImpl<L> extends ArrayList<Double> implements LabeledInstance<L> {

	private L label;
	
	public SimpleLabeledInstanceImpl() {
		super();
	}
	
	public SimpleLabeledInstanceImpl(L label) {
		super();
		this.label = label;
	}

	@Override
	public String toJson() {
		return null;
	}

	@Override
	public int getNumberOfColumns() {
		return this.size();
	}

	@Override
	public void setLabel(L label) {
		this.label = label;
	}

	@Override
	public L getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return "{data=" + super.toString() + ", label=" + label + "}";
	}
}
