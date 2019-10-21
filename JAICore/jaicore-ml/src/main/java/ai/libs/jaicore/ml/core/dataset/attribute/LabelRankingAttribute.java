package ai.libs.jaicore.ml.core.dataset.attribute;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IRankingAttributeValue;
import org.api4.java.ai.ml.ranking.dataset.IRanking;

public class LabelRankingAttribute extends ARankingAttribute<String> {

	/**
	 *
	 */
	private static final long serialVersionUID = -7357189772771718391L;

	private final Collection<String> labels;

	public LabelRankingAttribute(final String name, final Collection<String> labels) {
		super(name);
		this.labels = labels;
	}

	@Override
	public boolean isValidValue(final Object value) {
		if (value instanceof IRanking) {
			IRanking<?> ranking = (IRanking<?>) value;
			Set<String> labelsInRanking = new HashSet<>();
			for (Object rankedObject : ranking) {
				if (rankedObject instanceof String) {
					labelsInRanking.add((String) rankedObject);
				} else {
					return false;
				}
			}
			return this.labels.containsAll(labelsInRanking);
		}
		return (value instanceof LabelRankingAttributeValue);
	}

	public Collection<String> getLabels() {
		return this.labels;
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[LR] " + this.getName() + " " + this.labels;
	}

	@Override
	public IRankingAttributeValue<String> getAsAttributeValue(final Object object) {
		return new LabelRankingAttributeValue(this, this.getValueAsTypeInstance(object));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IRanking<String> getValueAsTypeInstance(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof LabelRankingAttributeValue) {
				return ((LabelRankingAttributeValue) object).getValue();
			} else {
				return (IRanking<String>) object;
			}
		} else {
			throw new IllegalArgumentException("No valid value of this attribute");
		}
	}

	@Override
	public double toDouble(final Object object) {
		throw new UnsupportedOperationException("Not yet implemented in LabelRankingAttribute"); // TODO
	}

	@Override
	public String serializeAttributeValue(final Object value) {
		throw new UnsupportedOperationException("Not yet implemented.");// TODO
	}

	@Override
	public Object deserializeAttributeValue(final String string) {
		throw new UnsupportedOperationException("Not yet implemented.");// TODO
	}

}
