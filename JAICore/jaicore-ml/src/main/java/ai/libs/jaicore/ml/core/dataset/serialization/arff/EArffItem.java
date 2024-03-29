package ai.libs.jaicore.ml.core.dataset.serialization.arff;

/**
 * enum of possible arff declarations
 *
 */
public enum EArffItem {

	RELATION("@relation"), ATTRIBUTE("@attribute"), DATA("@data");

	private final String value;

	private EArffItem(final String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
