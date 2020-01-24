package ai.libs.jaicore.math.probability.pl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortSet;

public class PLInferenceProblem {
	private final List<ShortList> rankings;
	private final int n;

	public PLInferenceProblem(final Collection<ShortList> rankings) {
		this(new ArrayList<>(rankings));
	}

	public PLInferenceProblem(final List<ShortList> rankings) {
		super();
		this.rankings = rankings;
		ShortSet comparedObjects = new ShortArraySet();
		for (ShortList ranking : rankings) {
			comparedObjects.addAll(ranking);
		}
		this.n = comparedObjects.size();
	}

	public List<ShortList> getRankings() {
		return this.rankings;
	}

	public int getNumObjects() {
		return this.n;
	}
}
