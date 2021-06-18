package ai.libs.jaicore.ml.core.dataset.serialization.arff;

/**
 * Enum of supported attribute types
 *
 */
public enum EArffAttributeType {

	NOMINAL("nominal"), //
	NUMERIC("numeric"), //
	INTEGER("integer"), //
	REAL("real"), //
	STRING("string"), //
	TIMESERIES("timeseries"), //
	MULTIDIMENSIONAL("multidimensional");

	private final String name;

	private EArffAttributeType(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
