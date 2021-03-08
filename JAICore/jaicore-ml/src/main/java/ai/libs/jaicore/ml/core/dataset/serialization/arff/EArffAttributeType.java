package ai.libs.jaicore.ml.core.dataset.serialization.arff;

/**
 * enum of differnet Attributes that are implemented in the parser. They are mainly definied in rg.api4.java.ai.ml.core.dataset.schema
 * documentation by Lukas Fehring
 *
 */
public enum EArffAttributeType {

	NOMINAL("nominal"), NUMERIC("numeric"), INTEGER("integer"), REAL("real"), STRING("string"), TIMESERIES("timeseries"), MULTIDIMENSIONAL("multidimensional");

	private final String name;

	private EArffAttributeType(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
