package ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes;

import java.util.ArrayList;
import java.util.Collection;

import org.api4.java.ai.ml.IRanking;

public class Ranking<O> extends ArrayList<O> implements IRanking<O> {
	/**
	 *
	 */
	private static final long serialVersionUID = 6925500382758165610L;

	public Ranking(final Collection<O> items) {
		super(items);
	}
}
