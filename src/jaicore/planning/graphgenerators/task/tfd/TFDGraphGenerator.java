package jaicore.planning.graphgenerators.task.tfd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.TaskPlannerUtil;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.stn.MethodInstance;
import jaicore.planning.model.task.stn.STNPlanningProblem;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class TFDGraphGenerator implements GraphGenerator<TFDNode,String> {

	private final STNPlanningProblem problem;
	private final Map<String,Operation> primitiveTasks = new HashMap<>();
	private final TaskPlannerUtil util = new TaskPlannerUtil(null);
	private final TFDNodeUtil tfdUtil = new TFDNodeUtil(null);
	
	public TFDGraphGenerator(STNPlanningProblem problem) {
		this.problem = problem;
		for (Operation op : problem.getDomain().getOperations())
			primitiveTasks.put(op.getName(), op);
	}

	@Override
	public SingleRootGenerator<TFDNode> getRootGenerator() {
		return () -> new TFDNode(problem.getInit(), util.getTaskChainOfTotallyOrderedNetwork(problem.getNetwork()));
	}

	@Override
	public SuccessorGenerator<TFDNode,String> getSuccessorGenerator() {
		return l -> {
			List<NodeExpansionDescription<TFDNode,String>> successors = new ArrayList<>();
			Monom state = l.getState();
			List<Literal> currentlyRemainingTasks = new ArrayList<>(l.getRemainingTasks());
			Literal nextTaskTmp = currentlyRemainingTasks.get(0);
			currentlyRemainingTasks.remove(0);
			String nextTaskName = nextTaskTmp.getPropertyName().substring(nextTaskTmp.getPropertyName().indexOf("-") + 1, nextTaskTmp.getPropertyName().length());
			Literal nextTask = new Literal(nextTaskName, nextTaskTmp.getParameters());
			
			/* if the task is primitive */
			if (primitiveTasks.containsKey(nextTask.getPropertyName())) {
				
				for (Action applicableAction : util.getActionsForPrimitiveTaskThatAreApplicableInState(null, primitiveTasks.get(nextTask.getPropertyName()), nextTask, state)) {
					Monom stateCopy = new Monom(state);
					tfdUtil.updateState(stateCopy, applicableAction);
					successors.add(new NodeExpansionDescription<>(l, new TFDNode(stateCopy, currentlyRemainingTasks, null, applicableAction), "edge label", NodeType.OR));
				}
			}
			
			/* otherwise determine methods for the task */
			else {
				for (MethodInstance instance : util.getMethodInstancesForTaskThatAreApplicableInState(null, this.problem.getDomain().getMethods(), nextTask, state, currentlyRemainingTasks)) {
					
					/* derive remaining network for this instance */
					List<Literal> remainingTasks = util.getTaskChainOfTotallyOrderedNetwork(instance.getNetwork());
					remainingTasks.addAll(currentlyRemainingTasks);
					successors.add(new NodeExpansionDescription<>(l, new TFDNode(state, remainingTasks, instance, null), "edge label", NodeType.OR));
				}
			}
			return successors;
		};
	}

	@Override
	public NodeGoalTester<TFDNode> getGoalTester() {
		return l -> l.getRemainingTasks().isEmpty();
	}
	
	@Override
	public boolean isSelfContained() {
		return false;
	}

	@Override
	public void setNodeNumbering(boolean nodenumbering) {
		// TODO Auto-generated method stub
		
	}
}
