package ai.libs.jaicore.math.probability.pl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PLInferenceProblem {
	private final List<? extends List<?>> rankings;
	private final List<Object> comparedObjects;
	private final int n;

	public PLInferenceProblem(final Collection<? extends List<?>> rankings) {
		this(new ArrayList<>(rankings));
	}

	public PLInferenceProblem(final List<? extends List<?>> rankings) {
		super();
		this.rankings = rankings;
		Set<Object> comparedObjects = new HashSet<>();
		for (List<?> ranking : rankings) {
			comparedObjects.addAll(ranking);
		}
		this.comparedObjects = new ArrayList<>(comparedObjects);
		this.n = this.comparedObjects.size();
	}

	public List<? extends List<?>> getRankings() {
		return this.rankings;
	}

	public List<Object> getComparedObjects() {
		return this.comparedObjects;
	}

	public int getN() {
		return this.n;
	}
}
