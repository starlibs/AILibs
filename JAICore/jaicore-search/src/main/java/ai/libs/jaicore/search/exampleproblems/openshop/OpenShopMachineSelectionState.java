package ai.libs.jaicore.search.exampleproblems.openshop;

import java.util.List;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.problems.scheduling.openshop.Machine;
import ai.libs.jaicore.problems.scheduling.openshop.OpenShopProblem;
import ai.libs.jaicore.problems.scheduling.openshop.Operation;

public class OpenShopMachineSelectionState extends OpenShopState {
	private final OpenShopOperationSelectionState parent;
	private final Operation operationSelectedInParent;

	public OpenShopMachineSelectionState(final OpenShopProblem problem, final OpenShopOperationSelectionState parent, final Operation operationSelectedInParent) {
		super(problem);
		this.parent = parent;
		this.operationSelectedInParent = operationSelectedInParent;
	}

	public OpenShopOperationSelectionState getParent() {
		return this.parent;
	}

	public Operation getOperationSelectedInParent() {
		return this.operationSelectedInParent;
	}

	@Override
	public List<Pair<Operation, Machine>> getPartialAssignment() {
		return this.parent.getPartialAssignment();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.operationSelectedInParent == null) ? 0 : this.operationSelectedInParent.hashCode());
		result = prime * result + ((this.parent == null) ? 0 : this.parent.hashCode());
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
		OpenShopMachineSelectionState other = (OpenShopMachineSelectionState) obj;
		if (this.operationSelectedInParent == null) {
			if (other.operationSelectedInParent != null) {
				return false;
			}
		} else if (!this.operationSelectedInParent.equals(other.operationSelectedInParent)) {
			return false;
		}
		if (this.parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!this.parent.equals(other.parent)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "OpenShopMachineSelectionState [parent=" + this.parent + ", operationSelectedInParent=" + this.operationSelectedInParent + "]";
	}


}
