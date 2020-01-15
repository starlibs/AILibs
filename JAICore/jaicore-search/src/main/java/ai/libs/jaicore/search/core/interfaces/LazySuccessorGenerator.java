package ai.libs.jaicore.search.core.interfaces;

import java.util.Iterator;
import java.util.Random;

import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

public interface LazySuccessorGenerator<N, A> extends ISuccessorGenerator<N, A> {
	public Iterator<INewNodeDescription<N, A>> getSuccessorIterator(N node);

	public INewNodeDescription<N, A> getRandomSuccessor(N node, Random random);
}
