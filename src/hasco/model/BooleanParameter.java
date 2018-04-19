package hasco.model;

public class BooleanParameter extends CategoricalParameter {
	public BooleanParameter(String name, boolean defaultValue) {
		super(name, new String[] {"true", "false"}, defaultValue ? "true" : "false");
	}
}
