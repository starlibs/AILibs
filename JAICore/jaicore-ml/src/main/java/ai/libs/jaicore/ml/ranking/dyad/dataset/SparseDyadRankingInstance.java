package ai.libs.jaicore.ml.ranking.dyad.dataset;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;

import ai.libs.jaicore.math.linearalgebra.IVector;
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

	/* The 'x' value for this instance */
	private IVector instance;

	/* The 'y' value for this instance */
	private List<IVector> alternatives;

	/**
	 * Construct a new sparse dyad ranking instance containing the given instance
	 * vector and ordering of alternatives.
	 *
	 * @param instance the instance for all of the alternatives
	 * @param alternatives the ordering of alternatives that, when combined with the
	 *            instances is an ordering of dyads
	 */
	public SparseDyadRankingInstance(final ILabeledInstanceSchema instanceSchema, final IVector instance, final List<IVector> alternatives) {
		super(instanceSchema);
		this.instance = instance;
		this.alternatives = Collections.unmodifiableList(alternatives);
	}

	@Override
	public Dyad getAttributeValue(final int position) {
		return new Dyad(this.instance, this.alternatives.get(position));
	}

	@Override
	public IRanking<IDyad> getLabel() {
		return new Ranking<>(IntStream.range(0, this.getNumAttributes()).mapToObj(x -> new Dyad(this.instance, this.alternatives.get(x))).collect(Collectors.toList()));
	}

	@Override
	public Iterator<IDyad> iterator() {
		return new Iterator<IDyad>() {

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

				return SparseDyadRankingInstance.this.getAttributeValue(this.index++);
			}
		};
	}

	@Override
	public int getNumAttributes() {
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

	@Override
	public double[] getPoint() {
		return null;
	}

	@Override
	public double getPointValue(final int pos) {
		return 0;
	}

}
