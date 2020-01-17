package ai.libs.jaicore.search.exampleproblems.openshop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

import ai.libs.jaicore.problems.scheduling.openshop.Machine;
import ai.libs.jaicore.problems.scheduling.openshop.OpenShopProblem;
import ai.libs.jaicore.problems.scheduling.openshop.Operation;
import ai.libs.jaicore.search.model.NodeExpansionDescription;

public class OpenShopGraphGenerator implements IGraphGenerator<OpenShopState, String> {

	private final OpenShopProblem problem;
	private final boolean pruneInactiveNodes;

	public OpenShopGraphGenerator(final OpenShopProblem problem) {
		this(problem, false);
	}

	public OpenShopGraphGenerator(final OpenShopProblem problem, final boolean pruneInactiveNodes) {
		super();
		this.problem = problem;
		this.pruneInactiveNodes = pruneInactiveNodes;
	}

	@Override
	public ISingleRootGenerator<OpenShopState> getRootGenerator() {
		return () -> new OpenShopOperationSelectionState(this.problem, null, null, this.problem.getOperations().values().stream().map(Operation::getName).collect(Collectors.toList()));
	}

	@Override
	public ISuccessorGenerator<OpenShopState, String> getSuccessorGenerator() {
		return n -> {
			List<INewNodeDescription<OpenShopState, String>> succ = new ArrayList<>();

			/* for actives schedules, continue as usual */
			if (n instanceof OpenShopOperationSelectionState) {
				for (String opName : ((OpenShopOperationSelectionState) n).getUnselectedOperations()) {
					OpenShopMachineSelectionState successor = new OpenShopMachineSelectionState(this.problem, (OpenShopOperationSelectionState)n, this.problem.getOperation(opName));

					/* add successor only if the solution can still be active! */
					if (!this.pruneInactiveNodes || successor.getSchedule().isActive()) {
						succ.add(new NodeExpansionDescription<>(successor, opName));
					}
				}
			}
			else if (n instanceof OpenShopMachineSelectionState) {

				/* identify assignable machines */
				Collection<Machine> possibleMachines = ((OpenShopMachineSelectionState) n).getOperationSelectedInParent().getWorkcenter().getMachines();
				for (Machine m : possibleMachines) {
					List<String> possibleOps = new ArrayList<>(((OpenShopMachineSelectionState) n).getParent().getUnselectedOperations());
					String removeOp = ((OpenShopMachineSelectionState) n).getOperationSelectedInParent().getName();
					boolean removed = possibleOps.remove(removeOp);
					if (!removed) {
						throw new IllegalStateException("Agenda has not been reduced. Operation that was supposed to be removed: " + removeOp);
					}
					OpenShopOperationSelectionState successor = new OpenShopOperationSelectionState(this.problem, (OpenShopMachineSelectionState)n, m, possibleOps);

					/* add successor only if the solution can still be active! */
					if (!this.pruneInactiveNodes || successor.getSchedule().isActive()) {
						succ.add(new NodeExpansionDescription<>(successor, m.getMachineID()));
					}
				}
			}
			else {
				throw new IllegalArgumentException("Unsupported type " + n);
			}
			return succ;
		};
	}

}
