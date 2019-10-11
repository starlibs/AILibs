package ai.libs.jaicore.ml.core.dataset.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericEncodingAttribute;

public abstract class AAttribute implements INumericEncodingAttribute {

	/**
	 *
	 */
	private static final long serialVersionUID = 8319254880082572177L;

	private String name;

	protected AAttribute(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

}
