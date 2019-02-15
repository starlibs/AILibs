package jaicore.graphvisualizer;

import javafx.util.StringConverter;

public class IntegerAxisFormatter extends StringConverter<Number> {

	@Override
	public String toString(Number object) {
		if (object.intValue() == object.doubleValue()) {
			String str = object.toString();
			str = str.substring(0, str.indexOf("."));
			return str;
		}
		else
			return "";
	}

	@Override
	public Number fromString(String string) {
		return null; // not needed
	}
}
