package ai.libs.jaicore.search.exampleproblems.openshop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.problems.scheduling.openshop.Machine;
import ai.libs.jaicore.problems.scheduling.openshop.OpenShopProblem;
import ai.libs.jaicore.problems.scheduling.openshop.Operation;

public class OpenShopOperationSelectionState extends OpenShopState {
	private final OpenShopMachineSelectionState parent;
	private final Machine machineSelectedInParent;
	private final Collection<String> unselectedOperations;

	public OpenShopOperationSelectionState(final OpenShopProblem problem, final OpenShopMachineSelectionState parent, final Machine machineSelectedInParent, final Collection<String> unselectedOperations) {
		super(problem);
		this.parent = parent;
		this.machineSelectedInParent = machineSelectedInParent;
		this.unselectedOperations = unselectedOperations;
	}

	public OpenShopMachineSelectionState getParent() {
		return this.parent;
	}

	public Machine getMachineSelectedInParent() {
		return this.machineSelectedInParent;
	}

	public Collection<String> getUnselectedOperations() {
		return this.unselectedOperations;
	}

	@Override
	public List<Pair<Operation, Machine>> getPartialAssignment() {
		if (this.parent == null) {
			return new ArrayList<>();
		}
		List<Pair<Operation, Machine>> assignmentAtLastNode = this.parent.getParent().getPartialAssignment();
		Pair<Operation, Machine> nextAssignment = new Pair<>(this.parent.getOperationSelectedInParent(), this.machineSelectedInParent);
		assignmentAtLastNode.add(nextAssignment);
		return assignmentAtLastNode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.machineSelectedInParent == null) ? 0 : this.machineSelectedInParent.hashCode());
		result = prime * result + ((this.parent == null) ? 0 : this.parent.hashCode());
		result = prime * result + ((this.unselectedOperations == null) ? 0 : this.unselectedOperations.hashCode());
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
		OpenShopOperationSelectionState other = (OpenShopOperationSelectionState) obj;
		if (this.machineSelectedInParent == null) {
			if (other.machineSelectedInParent != null) {
				return false;
			}
		} else if (!this.machineSelectedInParent.equals(other.machineSelectedInParent)) {
			return false;
		}
		if (this.parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!this.parent.equals(other.parent)) {
			return false;
		}
		if (this.unselectedOperations == null) {
			if (other.unselectedOperations != null) {
				return false;
			}
		} else if (!this.unselectedOperations.equals(other.unselectedOperations)) {
			return false;
		}
		return true;
	}
}
