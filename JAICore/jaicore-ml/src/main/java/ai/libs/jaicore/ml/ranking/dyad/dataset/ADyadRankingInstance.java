package ai.libs.jaicore.ml.ranking.dyad.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.Ranking;

public abstract class ADyadRankingInstance implements INDArrayDyadRankingInstance {

	@Override
	public INDArray toMatrix() {
		List<INDArray> dyadList = new ArrayList<>(this.getNumberOfRankedElements());
		for (IDyad dyad : this) {
			INDArray dyadVector = Nd4j.create(dyad.toDoubleVector());
			dyadList.add(dyadVector);
		}
		INDArray dyadMatrix;
		dyadMatrix = Nd4j.vstack(dyadList);
		return dyadMatrix;
	}

	@Override
	public double[] getPoint() {
		throw new UnsupportedOperationException("Cannot create vector representation of ranking instance.");
	}

	@Override
	public void removeColumn(int columnPos) {
		throw new UnsupportedOperationException("Cannot create vector representation of ranking instance.");
	}

	@Override
	public Object[] getAttributes() {
		return new Object[] { getAttributeValue(0) };
	}

	protected void assertOnlyDyadsInCollection(Collection<?> collection) {
		boolean noneDyadInCollection = collection.stream().anyMatch(o -> !(o instanceof IDyad));
		if (noneDyadInCollection) {
			throw new IllegalArgumentException("Cannot set collection with non-dyad element as attribute value.");
		}
	}

	protected void assertNonEmptyCollection(Collection<?> collection) {
		if (collection.isEmpty()) {
			throw new IllegalArgumentException("Cannot work with empty collection.");
		}
	}

	@Override
	public abstract Set<IDyad> getAttributeValue(final int pos);

	public Set<IDyad> getDyads() {
		return getAttributeValue(0);
	}

	public abstract void setDyads(Set<IDyad> dyads);

	public abstract void setRanking(Ranking<IDyad> ranking);

	@Override
	public void setLabel(Object obj) {
		if (!(obj instanceof List)) {
			throw new IllegalArgumentException("Label " + obj + " is not of type List.");
		}
		List<?> list = (List<?>) obj;
		assertNonEmptyCollection(list);
		assertOnlyDyadsInCollection(list);

		Set<IDyad> dyads = getDyads();

		boolean unknownDyadInList = list.stream().anyMatch(o -> !(dyads.contains(o)));
		if (unknownDyadInList) {
			throw new IllegalArgumentException("Cannot set list with unknown dyad element as label.");
		}

		Ranking<IDyad> ranking = new Ranking<>(list.stream().map(o -> (IDyad) o).collect(Collectors.toList()));
		setRanking(ranking);
	}

	@Override
	public void setAttributeValue(int pos, Object value) {
		if (!(value instanceof Collection)) {
			throw new IllegalArgumentException("Attribute value " + value + " is not of type Collection.");
		}
		Collection<?> collection = (Collection<?>) value;
		assertNonEmptyCollection(collection);
		assertOnlyDyadsInCollection(collection);

		Set<IDyad> dyads = collection.stream().map(o -> (IDyad) o).collect(Collectors.toSet());
		setDyads(dyads);
	}
}
