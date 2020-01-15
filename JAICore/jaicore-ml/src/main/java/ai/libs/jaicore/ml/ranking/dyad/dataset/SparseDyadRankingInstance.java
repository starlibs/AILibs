package ai.libs.jaicore.ml.ranking.dyad.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.common.math.IVector;

import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;
import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.Ranking;

/**
 * A dyad ranking instance implementation that assumes the same instance for all
 * dyads contained in its ordering. It saves the instance and alternatives
 * separately and contstructs dyads from them on request.
 *
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class SparseDyadRankingInstance extends ADyadRankingInstance {

	private IVector context;
	private Set<IVector> alternatives;

	private Ranking<IVector> rankedAlternatives;

	public SparseDyadRankingInstance(final IVector context, final Set<IVector> alternatives) {
		this.context = context;
		this.alternatives = new HashSet<>(alternatives);
		this.rankedAlternatives = new Ranking<>();
	}

	public SparseDyadRankingInstance(final IVector context, final List<IVector> alternatives) {
		this.context = context;
		this.alternatives = new HashSet<>(alternatives);
		this.rankedAlternatives = new Ranking<>(alternatives);
	}

	@Override
	public Set<IDyad> getAttributeValue(final int position) {
		if (position == 0) {
			return new HashSet<>(this.alternatives.stream().map(y -> new Dyad(this.context, y)).collect(Collectors.toList()));
		}
		throw new IllegalArgumentException("No attribute at position " + position + ".");
	}

	@Override
	public IRanking<IDyad> getLabel() {
		return new Ranking<>(this.rankedAlternatives.stream().map(y -> new Dyad(this.context, y)).collect(Collectors.toList()));
	}

	@Override
	public Iterator<IDyad> iterator() {
		return new Iterator<IDyad>() {

			private int index = 0;
			private List<IDyad> dyads = new ArrayList<>(SparseDyadRankingInstance.this.getAttributeValue(0));

			@Override
			public boolean hasNext() {
				return this.index < SparseDyadRankingInstance.this.getNumberOfRankedElements();
			}

			@Override
			public IDyad next() {
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}

				return this.dyads.get(this.index++);
			}
		};
	}

	public IVector getContext() {
		return this.context;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SparseDyadRankingInstance: ");
		builder.append(System.lineSeparator());
		builder.append("Instance: ");
		builder.append(this.context);
		builder.append(System.lineSeparator());
		builder.append("Alternatives: ");
		builder.append(this.alternatives);
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.alternatives == null) ? 0 : this.alternatives.hashCode());
		result = prime * result + ((this.context == null) ? 0 : this.context.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SparseDyadRankingInstance other = (SparseDyadRankingInstance) obj;
		if (this.alternatives == null) {
			if (other.alternatives != null) {
				return false;
			}
		} else if (!this.alternatives.equals(other.alternatives)) {
			return false;
		}
		if (this.context == null) {
			if (other.context != null) {
				return false;
			}
		} else if (!this.context.equals(other.context)) {
			return false;
		}
		return true;
	}

	@Override
	public int getNumberOfRankedElements() {
		return this.alternatives.size();
	}

	@Override
	public void setDyads(final Set<IDyad> dyads) {
		this.assertThatAllContextsAreIdentical(dyads);
		this.context = dyads.iterator().next().getContext();
		this.alternatives = dyads.stream().map(IDyad::getAlternative).collect(Collectors.toSet());
	}

	@Override
	public void setRanking(final Ranking<IDyad> ranking) {
		this.assertThatAllContextsAreIdentical(ranking);
	}

	private void assertThatAllContextsAreIdentical(final Collection<IDyad> dyads) {
		IDyad anyDyad = dyads.iterator().next();
		boolean allContextsIdentical = dyads.stream().allMatch(d -> d.getContext().equals(anyDyad.getContext()));
		if (!allContextsIdentical) {
			throw new IllegalArgumentException("For a sparse dyad ranking instance, all contexts have to be identical.");
		}
	}

}
