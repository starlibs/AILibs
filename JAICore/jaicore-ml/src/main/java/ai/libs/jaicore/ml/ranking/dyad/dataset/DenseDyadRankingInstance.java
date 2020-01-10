package ai.libs.jaicore.ml.ranking.dyad.dataset;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;

import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.Ranking;

/**
 *
 *
 * @author Helena Graf
 * @author Alexander Tornede
 *
 */
public class DenseDyadRankingInstance extends ADyadRankingInstance {

	private Set<IDyad> dyads;

	private Ranking<IDyad> rankingOverDyads;

	public DenseDyadRankingInstance(final Set<IDyad> dyads) {
		this.dyads = new HashSet<>(dyads);
		this.rankingOverDyads = new Ranking<>();
	}

	public DenseDyadRankingInstance(final List<IDyad> dyads) {
		this.dyads = new HashSet<>(dyads);
		this.rankingOverDyads = new Ranking<>(dyads);
	}

	@Override
	public Iterator<IDyad> iterator() {
		return this.dyads.iterator();
	}

	@Override
	public Set<IDyad> getAttributeValue(final int pos) {
		if (pos == 0) {
			return Collections.unmodifiableSet(this.dyads);
		}
		throw new IllegalArgumentException("No attribute at position " + pos + ".");
	}

	@Override
	public IRanking<IDyad> getLabel() {
		return this.rankingOverDyads;
	}

	@Override
	public int getNumberOfRankedElements() {
		return this.dyads.size();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof IDyadRankingInstance)) {
			return false;
		}

		IDyadRankingInstance drInstance = (IDyadRankingInstance) o;

		for (int i = 0; i < drInstance.getNumberOfRankedElements(); i++) {
			if (!(drInstance.getAttributeValue(i)).equals(this.getAttributeValue(i))) {
				return false;
			}
		}

		return drInstance.getLabel().equals(this.getLabel());
	}

	@Override
	public int hashCode() {
		int result = 42;
		result = result * 31 + this.dyads.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DyadRankingInstance: ");
		builder.append(this.dyads);
		return builder.toString();
	}

	@Override
	public void setDyads(final Set<IDyad> dyads) {
		this.dyads = dyads;
	}

	@Override
	public void setRanking(final Ranking<IDyad> ranking) {
		this.rankingOverDyads = new Ranking<>(ranking);
	}
}
