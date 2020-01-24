package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IRankingAttributeValue;
import org.api4.java.ai.ml.ranking.IRanking;

public class LabelRankingAttribute extends ARankingAttribute<String> {

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
		throw new UnsupportedOperationException("Not yet implemented in LabelRankingAttribute");
	}

	@Override
	public String serializeAttributeValue(final Object value) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public Object deserializeAttributeValue(final String string) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.labels == null) ? 0 : this.labels.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		LabelRankingAttribute other = (LabelRankingAttribute) obj;
		if (this.labels == null) {
			if (other.labels != null) {
				return false;
			}
		} else if (!this.labels.equals(other.labels)) {
			return false;
		}
		return true;
	}
}
