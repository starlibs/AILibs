package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IRankingAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IRankingAttributeValue;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;

public class DyadRankingAttributeValue implements IRankingAttributeValue<IDyad> {

	private final IRankingAttribute<IDyad> attribute;
	private final IRanking<IDyad> value;

	public DyadRankingAttributeValue(final IRankingAttribute<IDyad> attribute, final IRanking<IDyad> value) {
		this.attribute = attribute;
		this.value = value;
	}

	@Override
	public IRanking<IDyad> getValue() {
		return this.value;
	}

	@Override
	public IRankingAttribute<IDyad> getAttribute() {
		return null;
	}

}
