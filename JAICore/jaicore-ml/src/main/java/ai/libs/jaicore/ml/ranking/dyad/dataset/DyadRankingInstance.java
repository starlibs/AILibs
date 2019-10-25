package ai.libs.jaicore.ml.ranking.dyad.dataset;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;

import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.Ranking;

/**
 * A general implementation of a dyad ranking instance that contains an
 * immutable list of dyad to represent the ordering of dyads.
 *
 * The order is assumed to represent the ground truth (i.e., the label of the instance)
 *
 * @author Helena Graf
 *
 */
public class DyadRankingInstance extends ADyadRankingInstance {

	/* the ordering of dyads kept by this instance */
	private List<IDyad> dyads;

	/**
	 * Construct a new dyad ranking instance that saves the given ordering of dyads
	 * immutably.
	 *
	 * @param dyads the ordering of dyads to be stored in this instance
	 */
	public DyadRankingInstance(final ILabeledInstanceSchema instanceSchema, final List<IDyad> dyads) {
		super(instanceSchema);
		this.dyads = Collections.unmodifiableList(dyads);
	}

	@Override
	public Iterator<IDyad> iterator() {
		return this.dyads.iterator();
	}

	@Override
	public IDyad getAttributeValue(final int pos) {
		return this.dyads.get(pos);
	}

	@Override
	public IRanking<IDyad> getLabel() {
		return new Ranking<>(this.dyads);
	}

	@Override
	public int getNumAttributes() {
		return this.dyads.size();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof IDyadRankingInstance)) {
			return false;
		}

		IDyadRankingInstance drInstance = (IDyadRankingInstance) o;

		for (int i = 0; i < drInstance.getNumAttributes(); i++) {
			if (!(drInstance.getAttributeValue(i)).equals(this.getAttributeValue(i))) {
				return false;
			}
		}

		return true;
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
	public double[] getPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getPointValue(final int pos) {
		return 0;
	}

}
