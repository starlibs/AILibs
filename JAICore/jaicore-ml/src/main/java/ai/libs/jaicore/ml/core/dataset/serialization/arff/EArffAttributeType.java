package ai.libs.jaicore.ml.core.dataset.serialization.arff;

public enum EArffAttributeType {

	NOMINAL("nominal"), NUMERIC("numeric"), INTEGER("integer"), REAL("real"), STRING("string");

	private final String name;

	private EArffAttributeType(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
