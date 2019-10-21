package ai.libs.jaicore.ml.core.newdataset.reader.arff;

public enum EArffAttributeTypes {

	NOMINAL("nominal"), NUMERIC("numeric");

	private final String name;

	private EArffAttributeTypes(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
