package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public abstract class NumericRangeOptionPredicate extends OptionsPredicate  {
	private List<Object> values = null;
	
	protected abstract double getMin();
	
	protected abstract double getMax();
	
	/**
	 * Number steps takes between min and max.
	 * If below 0 no option will be added to the list.
	 */
	protected abstract int getSteps();
	
	/**
	 * Return true if the options needs to be integers.
	 */
	protected abstract boolean needsIntegers();
	
	/**
	 * Override to append additional values
	 */
	protected List<? extends Object> additionalValues() {
		return null;
	}
	
	/**
	 * Override and set to false to enable logarithmic scale.
	 */
	protected boolean isLinear() {
		return true;
	}
	
	/**
	 * Fills the list if the list is null.
	 */
	private void fillList() {
		double max = getMax();
		double min = getMin();
		int steps = getSteps();
		if(steps < 0) {
			return;
		}
		double stepSize = (max - min) / (steps + 1);
		boolean needsIntegers = needsIntegers();
		Set<Object> numericalValues = new TreeSet<>();	

//		numericalValues.add(min);
//		numericalValues.add(max);
		
		// Pre-compute  constants for logarithmic scale if necessary
		double expCoeff;
		double scale;
		if(isLinear()) {
			// dont need these values
			expCoeff = -1;
			scale = -1; 
		} else {

			// For any point x on the scale between min and max,
			// the corresponding logarithmic value, y, is:
			// y = a exp b*x
			// for b = log (max/min) / (max-min)
			if(min <= 0.00001) {
				min = 0.00001;  // set a lower bound
			}
			expCoeff = isLinear() ? -1 : Math.log(max/min) / (max-min); // b
			scale = isLinear() ? -1 :  max / Math.exp(expCoeff  * max); // a
		}
			
		for (int i = 0; i <= getSteps()+1; i++) {
			// linear value:
			double value = min + i * stepSize;
			
			// if it is logarithmic scale recalculate value:
			if(!isLinear()) {
				// y = a exp b*x
				value = scale * Math.exp( expCoeff  * value);
			}
			
			if(needsIntegers) {
				numericalValues.add(Math.round(value)); // round
			} else {
				numericalValues.add(value); // maybe need to round here to
			}
		}
		if(!numericalValues.isEmpty()) {
			values.addAll(numericalValues);
		}
	}
	
	public NumericRangeOptionPredicate() {
		if(values == null) {
			values = new ArrayList<>();
			fillList();
			List<? extends Object> additionalValues = this.additionalValues();
			if(additionalValues != null) {
				values.addAll(additionalValues);
			}
		}
	}
	
	public final List<? extends Object> getValidValues() {
		return values;
	}
	
	public static void main(String[] args) {
		NumericRangeOptionPredicate test = new NumericRangeOptionPredicate() {
			
			@Override
			protected boolean needsIntegers() {
				return true;
			}
			

	        @Override
	        protected double getMin() {
	            return 1;
	        }

	        @Override
	        protected double getMax() {
	            return 1;
	        }

	        @Override
	        protected int getSteps() {
	            return -1;
	        }
			
			protected boolean isLinear() {
				return false;
			}
		};
		System.out.println(test.getValidValues());
	}
}
