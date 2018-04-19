package hasco.model;

import java.util.Collection;

public class CategoricalParameter extends Parameter {
	private final String[] values;

	public CategoricalParameter(String name, String[] values, String defaultValue) {
		super(name, defaultValue);
		this.values = values;
	}
	
	public CategoricalParameter(String name, Collection<String> values, String defaultValue) {
		this(name, values.toArray(new String[] {}), defaultValue);
	}

	public String[] getValues() {
		return values;
	}
}
