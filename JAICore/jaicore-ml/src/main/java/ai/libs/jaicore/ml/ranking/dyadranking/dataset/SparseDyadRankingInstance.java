package ai.libs.jaicore.ml.ranking.dyadranking.dataset;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.IRanking;

import ai.libs.jaicore.math.linearalgebra.Vector;
import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.Ranking;
import ai.libs.jaicore.ml.ranking.dyadranking.Dyad;

/**
 * A dyad ranking instance implementation that assumes the same instance for all
 * dyads contained in its ordering. It saves the instance and alternatives
 * separately and contstructs dyads from them on request.
 *
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class SparseDyadRankingInstance extends ADyadRankingInstance {

	/* The 'x' value for this instance */
	private Vector instance;

	/* The 'y' value for this instance */
	private List<Vector> alternatives;

	/**
	 * Construct a new sparse dyad ranking instance containing the given instance
	 * vector and ordering of alternatives.
	 *
	 * @param instance the instance for all of the alternatives
	 * @param alternatives the ordering of alternatives that, when combined with the
	 *            instances is an ordering of dyads
	 */
	public SparseDyadRankingInstance(final Vector instance, final List<Vector> alternatives) {
		this.instance = instance;
		this.alternatives = Collections.unmodifiableList(alternatives);
	}

	@Override
	public Dyad get(final int position) {
		return new Dyad(this.instance, this.alternatives.get(position));
	}

	@Override
	public IRanking<Dyad> getLabel() {
		return new Ranking<>(IntStream.range(0, this.getNumFeatures()).mapToObj(x -> new Dyad(this.instance, this.alternatives.get(x))).collect(Collectors.toList()));
	}

	@Override
	public Iterator<Dyad> iterator() {
		return new Iterator<Dyad>() {

			private int index = 0;

			@Override
			public boolean hasNext() {
				return this.index < SparseDyadRankingInstance.this.alternatives.size();
			}

			@Override
			public Dyad next() {
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}

				return SparseDyadRankingInstance.this.get(this.index++);
			}
		};
	}

	@Override
	public int getNumFeatures() {
		return this.alternatives.size();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SparseDyadRankingInstance: ");
		builder.append(System.lineSeparator());
		builder.append("Instance: ");
		builder.append(this.instance);
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
		result = prime * result + ((this.instance == null) ? 0 : this.instance.hashCode());
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
		if (this.instance == null) {
			if (other.instance != null) {
				return false;
			}
		} else if (!this.instance.equals(other.instance)) {
			return false;
		}
		return true;
	}

}
