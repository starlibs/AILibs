package jaicore.graphvisualizer;

import jaicore.basic.MathExt;
import javafx.util.StringConverter;

public class IntegerAxisFormatter extends StringConverter<Number> {

	@Override
	public String toString(final Number object) {
		Double val = MathExt.round(object.doubleValue(), 8);
		if (val.intValue() == val) { // consider all numbers that are close to an integer by 10^-8 as ints
			String str = String.valueOf(val);
			str = str.substring(0, str.indexOf('.'));
			return str;
		} else {
			return "";
		}
	}

	@Override
	public Number fromString(final String string) {
		return null; // not needed
	}
}
