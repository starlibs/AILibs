package ai.libs.jaicore.ml.core.dataset.serialization.arff;

import ai.libs.jaicore.ml.core.dataset.schema.InstanceSchema;

/**
 * Enum of different Attributes that are implemented in the parser. They are mainly defined in
 * {@link InstanceSchema}
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
