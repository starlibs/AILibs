package ai.libs.jaicore.ml.ranking.dyadranking;

/**
 *
 * @author mwever
 *
 * @param <R> Internal representation of one part of the dyad.
 * @param <V> Vectorial representation of the dyad.
 */
public interface IDyad<R, V> {

	public R getInstance();

	public R getAlternative();

	public V toVector();

}
