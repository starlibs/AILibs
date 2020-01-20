package ai.libs.jaicore.search.exampleproblems.samegame;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.problems.samegame.SameGameCell;
import ai.libs.jaicore.problems.samegame.SameGameState;
import ai.libs.jaicore.search.core.interfaces.ISuccessorGenerationRelevantRemovalNode;

public class SameGameNode implements ISuccessorGenerationRelevantRemovalNode {
	private final SameGameNode parent;
	private SameGameState state;
	private final SameGameCell selectedCell;
	private final short score;
	private final boolean isGoalState;
	private final boolean keepInMemory;
	private final int hashOfPathToRoot;

	public SameGameNode(final SameGameState state) {
		super();
		this.state = state;
		this.parent = null;
		this.isGoalState = !this.state.isMovePossible();
		this.score = state.getScore();
		this.selectedCell = null;
		this.keepInMemory = true;
		this.hashOfPathToRoot = this.getDecisionPathToRoot().hashCode();
	}

	public SameGameNode(final SameGameNode parent, final SameGameCell selectedCell) {
		this.selectedCell = selectedCell;
		this.parent = parent;
		SameGameState stateAfterMove= parent.getState().getStateAfterMove(selectedCell.getRow(), selectedCell.getCol());
		this.isGoalState = !stateAfterMove.isMovePossible();
		this.score = stateAfterMove.getScore();
		List<SameGameCell> pathToRoot = this.getDecisionPathToRoot();
		this.keepInMemory =  parent.getDecisionPathToRoot().size() % 2 == 0;
		if (this.keepInMemory) {
			this.state = stateAfterMove;
		}
		this.hashOfPathToRoot = pathToRoot.hashCode();
	}

	@Override
	public void eraseGenes() {
		if (this.keepInMemory) {
			throw new IllegalStateException("Cannot erase genes of node that is configured to keep its genes!");
		}
		this.state = null;
	}

	@Override
	public void recoverGenes() {
		if (this.parent == null) {
			return;
		}
		boolean eraseGenesInParentAfterwards = false;
		if (this.parent.state == null) {
			this.parent.recoverGenes();
			eraseGenesInParentAfterwards = true;
		}
		SameGameState parentState = this.parent.getState();
		if (eraseGenesInParentAfterwards) {
			this.parent.eraseGenes();
		}
		this.state = parentState.getStateAfterMove(this.selectedCell.getRow(), this.selectedCell.getCol());
	}

	public boolean isGoalState() {
		return this.isGoalState;
	}

	public SameGameState getState() {
		return this.state;
	}

	public int getScore() {
		return this.score;
	}

	public List<SameGameNode> getPath() {
		if (this.parent == null) {
			List<SameGameNode> rootList = new ArrayList<>();
			rootList.add(this);
			return rootList;
		}
		List<SameGameNode> path = this.parent.getPath();
		path.add(this);
		return path;
	}

	public List<SameGameCell> getDecisionPathToRoot() {
		if (this.parent == null) {
			return new ArrayList<>();
		}
		List<SameGameCell> path = this.parent.getDecisionPathToRoot();
		path.add(this.selectedCell);
		return path;
	}

	@Override
	public int hashCode() {
		return this.hashOfPathToRoot;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SameGameNode other = (SameGameNode) obj;
		return this.getDecisionPathToRoot().equals(other.getDecisionPathToRoot());
	}

	public boolean isKeepInMemory() {
		return this.keepInMemory;
	}

	@Override
	public boolean allowsGeneErasure() {
		return !this.keepInMemory;
	}

	public SameGameCell getSelectedCell() {
		return this.selectedCell;
	}

	@Override
	public String toString() {
		return "SameGameNode [selectedCell=" + this.selectedCell + ", score=" + this.score + "]";
	}
}
