package ai.libs.jaicore.ml.core.dataset.attribute;

import org.api4.java.ai.ml.dataset.attribute.IAttributeType;

public abstract class AAttributeType implements IAttributeType {

	/**
	 *
	 */
	private static final long serialVersionUID = 8319254880082572177L;

	private String name;

	protected AAttributeType(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
