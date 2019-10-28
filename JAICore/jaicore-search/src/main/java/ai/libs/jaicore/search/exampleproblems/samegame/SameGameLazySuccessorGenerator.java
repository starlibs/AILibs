package ai.libs.jaicore.search.exampleproblems.samegame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.NodeType;

import ai.libs.jaicore.problems.samegame.SameGameCell;
import ai.libs.jaicore.problems.samegame.SameGameState;
import ai.libs.jaicore.search.core.interfaces.LazySuccessorGenerator;

public class SameGameLazySuccessorGenerator implements LazySuccessorGenerator<SameGameNode, SameGameCell> {

	// List<NodeExpansionDescription<SameGameNode, SameGameCell>> succ = new ArrayList<>();
	// if (!n.isKeepInMemory()) {
	// if (n.getState() != null) {
	// throw new IllegalStateException("The state of the expanded node should not be there (for memory efficiency)!");
	// }
	// n.recoverGenes(); // create state again
	// }
	// SameGameState state = n.getState();
	// if (state == null) {
	// throw new IllegalStateException("Gene recover failed!");
	// }
	// int i = 0;
	// for (Collection<SameGameCell> block : state.getBlocksOfPieces()) {
	// if (block.size() > 1) {
	// SameGameCell move = block.iterator().next();
	// this.logger.debug("Considering move {} representing block {}", move, block);
	// succ.add(new NodeExpansionDescription<>(new SameGameNode(n, move), move, NodeType.OR));
	// i++;
	// }
	// }
	// if (succ.size() != i) {
	// throw new IllegalStateException();
	// }
	//
	// /* erase state again */
	// if (!n.isKeepInMemory()) {
	// n.eraseGenes();
	// if (n.getState() != null) {
	// throw new IllegalStateException("Erasure failed!");
	// }
	// }
	//
	@Override
	public List<NodeExpansionDescription<SameGameNode, SameGameCell>> generateSuccessors(final SameGameNode node) throws InterruptedException {
		List<NodeExpansionDescription<SameGameNode, SameGameCell>> succ = new ArrayList<>();
		Iterator<NodeExpansionDescription<SameGameNode, SameGameCell>> it = this.getSuccessorIterator(node);
		while (it.hasNext()) {
			succ.add(it.next());
		}
		return succ;
	}

	@Override
	public Iterator<NodeExpansionDescription<SameGameNode, SameGameCell>> getSuccessorIterator(final SameGameNode n) {

		return new Iterator<NodeExpansionDescription<SameGameNode, SameGameCell>>() {

			private final List<SameGameCell> unselectedCells;
			private final Random random = new Random();

			{
				if (n.getState() == null) {
					n.recoverGenes();
				}
				this.unselectedCells = n.getState().getBlocksOfPieces().stream().filter(b -> b.size() > 1).map(b -> b.iterator().next()).collect(Collectors.toList());
				Collections.shuffle(this.unselectedCells, this.random);
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
				return new NodeExpansionDescription<>(node, nextCell, NodeType.OR);
			}
		};
	}

	@Override
	public NodeExpansionDescription<SameGameNode, SameGameCell> getRandomSuccessor(final SameGameNode node, final Random random) {
		if (node.getState() == null) {
			node.recoverGenes();
		}
		SameGameState state = node.getState();
		byte row = -1;
		byte col = -1;
		int numRows = state.getNumRows();
		int numCols = state.getNumCols();
		do {
			row = (byte)random.nextInt(numRows);
			col = (byte)random.nextInt(numCols);
		}
		while (!state.canCellBeSelected(row, col));

		SameGameCell cell = new SameGameCell(row, col);
		SameGameNode succ = new SameGameNode(node, cell);

		if (!node.isKeepInMemory()) {
			node.eraseGenes();
		}
		return new NodeExpansionDescription<>(succ, cell, NodeType.OR);
	}
}
