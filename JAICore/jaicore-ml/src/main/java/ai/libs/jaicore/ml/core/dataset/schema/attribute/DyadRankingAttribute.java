package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Arrays;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IRankingAttributeValue;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.common.math.IVector;

import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;
import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.Ranking;

public class DyadRankingAttribute extends ARankingAttribute<IDyad> {

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
			return (((IRanking<?>) value).get(0) instanceof IDyad);
		}
		return (value instanceof DyadRankingAttributeValue);
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[DR] " + this.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IRankingAttributeValue<IDyad> getAsAttributeValue(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof DyadRankingAttributeValue) {
				return new DyadRankingAttributeValue(this, ((DyadRankingAttributeValue) object).getValue());
			} else {
				return new DyadRankingAttributeValue(this, (IRanking<IDyad>) object);
			}
		} else {
			throw new IllegalArgumentException("No valid value for this attribute");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IRanking<IDyad> getValueAsTypeInstance(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof DyadRankingAttributeValue) {
				return ((DyadRankingAttributeValue) object).getValue();
			} else {
				return (IRanking<IDyad>) object;
			}
		} else {
			throw new IllegalArgumentException("No valid value for this attribute");
		}
	}

	@Override
	public double toDouble(final Object object) {
		throw new UnsupportedOperationException("Not yet implemented in DyadRankingAttribute");
	}

	@Override
	public String serializeAttributeValue(final Object value) {
		return this.getValueAsTypeInstance(value).stream().map(x -> "(" + x.getContext() + ";" + x.getAlternative() + ")").reduce("", (a, b) -> a + (a.isEmpty() ? "" : ">") + b);
	}

	@Override
	public Object deserializeAttributeValue(final String string) {
		String[] split = string.split(">");
		IRanking<IDyad> ranking = new Ranking<>();
		Arrays.stream(split).map(x -> x.substring(1, x.length() - 1)).map(x -> new Dyad(this.parseVector(x.split(";")[0]), this.parseVector(x.split(",")[1]))).forEach(ranking::add);
		return ranking;
	}

	private IVector parseVector(final String vectorString) {
		String[] split = vectorString.substring(1, vectorString.length() - 1).split(",");
		return new DenseDoubleVector(Arrays.stream(split).mapToDouble(Double::parseDouble).toArray());
	}

}
