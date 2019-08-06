package ai.libs.jaicore.ml.core.dataset.attribute.nominal;

import java.util.List;

import org.api4.java.ai.ml.dataset.attribute.nominal.INominalAttributeType;
import org.api4.java.ai.ml.dataset.attribute.nominal.INominalAttributeValue;

import ai.libs.jaicore.ml.core.dataset.attribute.AAttributeType;

public class NominalAttributeType extends AAttributeType implements INominalAttributeType {

	/**
	 *
	 */
	private static final long serialVersionUID = 3727153881173459843L;

	private final List<String> domain;

	public NominalAttributeType(final String name, final List<String> domain) {
		super(name);
		this.domain = domain;
	}

	@Override
	public boolean isValidValue(final Object value) {
		return this.domain.contains(value);
	}

	@Override
	public INominalAttributeValue buildAttributeValue(final Object value) {
		if (!this.isValidValue(value)) {
			throw new IllegalArgumentException("Not a valid value for this nominal attribute");
		}
		return new NominalAttributeValue((String) value);
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[Nom] " + this.getName() + " " + this.domain;
	}

	@Override
	public List<String> getValues() {
		return this.domain;
	}

	@Override
	public boolean isBinary() {
		return this.getValues().size() == 2;
	}

	@Override
	public String decodeToString(final double label) {
		return this.domain.get((int) label);
	}

	@Override
	public double encodeToDouble(final String value) {
		return this.domain.indexOf(value);
	}

}
