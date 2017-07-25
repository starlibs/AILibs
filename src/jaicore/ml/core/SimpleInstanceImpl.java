package jaicore.ml.core;

import java.util.ArrayList;

import jaicore.ml.interfaces.Instance;

@SuppressWarnings("serial")
public class SimpleInstanceImpl extends ArrayList<Double> implements Instance {

	@Override
	public String toJson() {
		return null;
	}

	@Override
	public int getNumberOfColumns() {
		return this.size();
	}
}
