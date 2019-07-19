package ai.libs.jaicore.ml.ranking;

import java.util.List;

/**
 * A ranking is a function mapping assigning each object of a set of objects a rank, i.e. a
 * number between 1 and the total number of objects. For convenience, we assume the ranking
 * to be stored in a list. On top of this, we define ranking specific methods to ease the
 * handling of rankings and access ranking-specific information directly.
 *
 * @author mwever
 *
 * @param <S> The type of objects which is ranked.
 */
public interface IRanking<S> extends List<S> {

	/**
	 * Retrieves the rank for the given object from this ranking.
	 * @param object The object to retrieve the rank for.
	 * @return The rank of the requested object.
	 */
	default int getRankOfObject(final S object) {
		return this.indexOf(object) + 1;
	}

	/**
	 * Retrieves the object for the given rank from this ranking.
	 * @param rank The rank to retrieve the object for.
	 * @return The object with the requested rank.
	 */
	default S getObjectForRank(final int rank) {
		return this.get(rank - 1);
	}

}
