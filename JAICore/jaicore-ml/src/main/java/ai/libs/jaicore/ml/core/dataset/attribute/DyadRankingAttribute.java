package ai.libs.jaicore.ml.core.dataset.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IRankingAttributeValue;
import org.api4.java.ai.ml.ranking.dataset.IRanking;

import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;

public class DyadRankingAttribute extends ARankingAttribute<Dyad> {

	/**
	 *
	 */
	private static final long serialVersionUID = -7427433693910952078L;

	public DyadRankingAttribute(final String name) {
		super(name);
	}

	@Override
	public boolean isValidValue(final Object value) {
		if (value instanceof IRanking) {
			return (((IRanking<?>) value).get(0) instanceof Dyad);
		}
		return (value instanceof DyadRankingAttributeValue);
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[DR] " + this.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IRankingAttributeValue<Dyad> getAsAttributeValue(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof DyadRankingAttributeValue) {
				return new DyadRankingAttributeValue(this, ((DyadRankingAttributeValue) object).getValue());
			} else {
				return new DyadRankingAttributeValue(this, (IRanking<Dyad>) object);
			}
		} else {
			throw new IllegalArgumentException("No valid value for this attribute");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IRanking<Dyad> getValueAsTypeInstance(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof DyadRankingAttributeValue) {
				return ((DyadRankingAttributeValue) object).getValue();
			} else {
				return (IRanking<Dyad>) object;
			}
		} else {
			throw new IllegalArgumentException("No valid value for this attribute");
		}
	}

}
