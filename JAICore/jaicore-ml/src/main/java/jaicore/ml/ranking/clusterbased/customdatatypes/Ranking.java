package jaicore.ml.ranking.clusterbased.customdatatypes;

import java.util.ArrayList;
import java.util.Collection;

public class Ranking<S> extends ArrayList<S> {
	public Ranking(final Collection<S> items) {
		super(items);
	}
}
