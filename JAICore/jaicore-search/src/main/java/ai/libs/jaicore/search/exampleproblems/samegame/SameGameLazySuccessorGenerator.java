package ai.libs.jaicore.search.exampleproblems.samegame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.api4.java.datastructure.graph.implicit.INewNodeDescription;

import ai.libs.jaicore.problems.samegame.SameGameCell;
import ai.libs.jaicore.search.model.ILazyRandomizableSuccessorGenerator;
import ai.libs.jaicore.search.model.NodeExpansionDescription;

public class SameGameLazySuccessorGenerator implements ILazyRandomizableSuccessorGenerator<SameGameNode, SameGameCell>{

	private Random random = new Random(0);

	@Override
	public List<INewNodeDescription<SameGameNode, SameGameCell>> generateSuccessors(final SameGameNode node) throws InterruptedException {
		Objects.requireNonNull(node, "The given node must not be null.");
		List<INewNodeDescription<SameGameNode, SameGameCell>> succ = new ArrayList<>();
		Iterator<INewNodeDescription<SameGameNode, SameGameCell>> it = this.getIterativeGenerator(node);
		while (it.hasNext()) {
			succ.add(it.next());
		}

		assert !(node.getState() != null && node.getState().isMovePossible()) || !succ.isEmpty() : "No successors have been generated, but there is a move possible!";
		return succ;
	}

	@Override
	public Iterator<INewNodeDescription<SameGameNode, SameGameCell>> getIterativeGenerator(final SameGameNode n) {
		return this.getIterativeGenerator(n, this.random);
	}

	@Override
	public Iterator<INewNodeDescription<SameGameNode, SameGameCell>> getIterativeGenerator(final SameGameNode n, final Random random) {

		return new Iterator<INewNodeDescription<SameGameNode, SameGameCell>>() {

			private final List<SameGameCell> unselectedCells;

			{
				if (n.getState() == null) {
					n.recoverGenes();
				}
				this.unselectedCells = n.getState().getBlocksOfPieces().stream().filter(b -> b.size() > 1).map(b -> b.iterator().next()).collect(Collectors.toList());
				if (this.unselectedCells.isEmpty() && n.getState().isMovePossible()) {
					throw new IllegalStateException("Moves possible, but no block can be selected. Here is the board: " + n.getState().getBoardAsString() + "\nand are the blocks of pieces: " + n.getState().getBlocksOfPieces().stream().map(b -> "\n\t" + b.toString()).collect(Collectors.joining()));
				}
				Collections.shuffle(this.unselectedCells, random);
				if (n.allowsGeneErasure()) {
					n.eraseGenes();
				}
			}

			@Override
			public boolean hasNext() {
				return !this.unselectedCells.isEmpty();
			}

			@Override
			public NodeExpansionDescription<SameGameNode, SameGameCell> next() {
				if (this.unselectedCells.isEmpty()) {
					throw new NoSuchElementException("Set of unselected cells is empty!");
				}
				SameGameCell nextCell = this.unselectedCells.remove(0);
				if (!n.isKeepInMemory()) {
					n.recoverGenes();
				}
				SameGameNode node = new SameGameNode(n, nextCell);
				if (!n.isKeepInMemory()) {
					n.eraseGenes();
				}
				return new NodeExpansionDescription<>(node, nextCell);
			}
		};
	}
}
