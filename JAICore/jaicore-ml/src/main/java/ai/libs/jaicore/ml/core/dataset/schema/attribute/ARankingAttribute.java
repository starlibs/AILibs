package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IRankingAttribute;
import org.api4.java.ai.ml.ranking.IRanking;

public abstract class ARankingAttribute<O> extends AGenericObjectAttribute<IRanking<O>> implements IRankingAttribute<O> {

	/**
	 *
	 */
	private static final long serialVersionUID = -9045962289246750137L;

	protected ARankingAttribute(final String name) {
		super(name);
	}

}
