package ai.libs.jaicore.ml.core.dataset.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IRankingAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IRankingAttributeValue;
import org.api4.java.ai.ml.ranking.dataset.IRanking;

import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;

public class DyadRankingAttributeValue implements IRankingAttributeValue<Dyad> {

	private final IRankingAttribute<Dyad> attribute;
	private final IRanking<Dyad> value;

	public DyadRankingAttributeValue(final IRankingAttribute<Dyad> attribute, final IRanking<Dyad> value) {
		this.attribute = attribute;
		this.value = value;
	}

	@Override
	public IRanking<Dyad> getValue() {
		return this.value;
	}

	@Override
	public IRankingAttribute<Dyad> getAttribute() {
		return null;
	}

}
