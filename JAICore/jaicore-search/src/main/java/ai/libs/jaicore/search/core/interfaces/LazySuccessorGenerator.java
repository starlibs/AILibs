package ai.libs.jaicore.search.core.interfaces;

import java.util.Iterator;
import java.util.Random;

import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;

public interface LazySuccessorGenerator<N, A> extends SuccessorGenerator<N, A> {
	public Iterator<NodeExpansionDescription<N, A>> getSuccessorIterator(N node);

	public NodeExpansionDescription<N, A> getRandomSuccessor(N node, Random random);
}
