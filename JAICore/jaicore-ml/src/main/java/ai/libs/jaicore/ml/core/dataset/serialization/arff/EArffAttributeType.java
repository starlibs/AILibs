package ai.libs.jaicore.ml.core.dataset.serialization.arff;

public enum EArffAttributeType {

	NOMINAL("nominal"), NUMERIC("numeric"), REAL("real");

	private final String name;

	private EArffAttributeType(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
