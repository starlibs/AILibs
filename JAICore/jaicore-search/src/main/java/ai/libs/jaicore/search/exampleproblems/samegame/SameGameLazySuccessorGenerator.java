package ai.libs.jaicore.search.exampleproblems.samegame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

import org.api4.java.common.control.IRandomConfigurable;
import org.api4.java.datastructure.graph.implicit.ILazySuccessorGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;

import ai.libs.jaicore.problems.samegame.SameGameCell;
import ai.libs.jaicore.problems.samegame.SameGameState;
import ai.libs.jaicore.search.model.NodeExpansionDescription;

public class SameGameLazySuccessorGenerator implements ILazySuccessorGenerator<SameGameNode, SameGameCell>, IRandomConfigurable {

	private Random random = new Random(0);

	@Override
	public List<INewNodeDescription<SameGameNode, SameGameCell>> generateSuccessors(final SameGameNode node) throws InterruptedException {
		List<INewNodeDescription<SameGameNode, SameGameCell>> succ = new ArrayList<>();
		Iterator<INewNodeDescription<SameGameNode, SameGameCell>> it = this.getIterativeGenerator(node);
		while (it.hasNext()) {
			succ.add(it.next());
		}
		return succ;
	}

	@Override
	public Iterator<INewNodeDescription<SameGameNode, SameGameCell>> getIterativeGenerator(final SameGameNode n) {

		return new Iterator<INewNodeDescription<SameGameNode, SameGameCell>>() {

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
				return new NodeExpansionDescription<>(node, nextCell);
			}
		};
	}

	public INewNodeDescription<SameGameNode, SameGameCell> getRandomSuccessor(final SameGameNode node) {
		if (node.getState() == null) {
			node.recoverGenes();
		}
		SameGameState state = node.getState();
		byte row = -1;
		byte col = -1;
		int numRows = state.getNumRows();
		int numCols = state.getNumCols();
		do {
			row = (byte)this.random.nextInt(numRows);
			col = (byte)this.random.nextInt(numCols);
		}
		while (!state.canCellBeSelected(row, col));

		SameGameCell cell = new SameGameCell(row, col);
		SameGameNode succ = new SameGameNode(node, cell);

		if (!node.isKeepInMemory()) {
			node.eraseGenes();
		}
		return new NodeExpansionDescription<>(succ, cell);
	}

	@Override
	public void setRandom(final Random random) {
		this.random = random;
	}
}
