package de.upb.crc901.mlplan.evaluablepredicates.mlplan;

import java.util.ArrayList;
import java.util.List;

public abstract class LinearOptionPredicate extends OptionsPredicate  {
	List<Double> values = new ArrayList<>();
	
	protected abstract double getMin();
	protected abstract double getMax();
	protected abstract int getSteps();
	
	public LinearOptionPredicate() {
		double max = getMax();
		double min = getMin();
		double stepSize = (max - min) / getSteps();
		for (int i = 0; i < getSteps(); i++) {
			values.add(min + i * stepSize);
		}
	}
	
	public final List<? extends Object> getValidValues() {
		return values;
	}
}
