package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IRankingAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IRankingAttributeValue;
import org.api4.java.ai.ml.ranking.IRanking;

public class LabelRankingAttributeValue implements IRankingAttributeValue<String> {

	private IRankingAttribute<String> attribute;
	private IRanking<String> value;

	public LabelRankingAttributeValue(final IRankingAttribute<String> attribute, final IRanking<String> value) {
		this.value = value;
		this.attribute = attribute;
	}

	@Override
	public IRanking<String> getValue() {
		return this.value;
	}

	@Override
	public IAttribute getAttribute() {
		return this.attribute;
	}

}
