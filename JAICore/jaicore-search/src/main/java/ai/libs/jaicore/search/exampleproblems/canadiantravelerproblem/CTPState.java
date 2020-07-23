package ai.libs.jaicore.search.exampleproblems.canadiantravelerproblem;

import java.util.Map;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.search.exampleproblems.lake.ECTPEdgeKnowledge;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class CTPState {
	private final ShortList currentTour;
	private final Map<Pair<Short, Short>, ECTPEdgeKnowledge> edgeKnowledge;

	public CTPState(final ShortList currentTour, final Map<Pair<Short, Short>, ECTPEdgeKnowledge> edgeKnowledge) {
		super();
		this.currentTour = currentTour;
		this.edgeKnowledge = edgeKnowledge;
	}

	public ShortList getCurrentTour() {
		return this.currentTour;
	}

	public Map<Pair<Short, Short>, ECTPEdgeKnowledge> getEdgeKnowledge() {
		return this.edgeKnowledge;
	}

	public short getPosition() {
		return this.currentTour.getShort(this.currentTour.size() - 1);
	}

	@Override
	public String toString() {
		return this.currentTour.toString() + " - " + this.edgeKnowledge;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.currentTour == null) ? 0 : this.currentTour.hashCode());
		result = prime * result + ((this.edgeKnowledge == null) ? 0 : this.edgeKnowledge.hashCode());
		return result;
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
		CTPState other = (CTPState) obj;
		if (this.currentTour == null) {
			if (other.currentTour != null) {
				return false;
			}
		} else if (!this.currentTour.equals(other.currentTour)) {
			return false;
		}
		if (this.edgeKnowledge == null) {
			if (other.edgeKnowledge != null) {
				return false;
			}
		} else if (!this.edgeKnowledge.equals(other.edgeKnowledge)) {
			return false;
		}
		return true;
	}
}
