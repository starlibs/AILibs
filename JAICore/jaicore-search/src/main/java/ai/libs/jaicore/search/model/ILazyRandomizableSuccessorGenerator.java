package ai.libs.jaicore.search.model;

import java.util.Iterator;
import java.util.Random;

import org.api4.java.datastructure.graph.implicit.ILazySuccessorGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;

/**
 * This allows to ensure that the received iterator is randomized in the desired way.
 *
 * @author Felix Mohr
 *
 * @param <N>
 * @param <A>
 */
public interface ILazyRandomizableSuccessorGenerator<N, A> extends ILazySuccessorGenerator<N, A> {

	public Iterator<INewNodeDescription<N, A>> getIterativeGenerator(N node, Random random);
}
