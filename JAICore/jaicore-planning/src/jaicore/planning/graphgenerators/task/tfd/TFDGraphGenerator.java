package jaicore.planning.graphgenerators.task.tfd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.TaskPlannerUtil;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.core.PlannerUtil;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;
import jaicore.planning.model.task.stn.MethodInstance;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.core.interfaces.PathUnifyingGraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

@SuppressWarnings("serial")
public class TFDGraphGenerator<O extends Operation, M extends Method, A extends Action> implements SerializableGraphGenerator<TFDNode, String>, PathUnifyingGraphGenerator<TFDNode, String> {

	protected TaskPlannerUtil util = new TaskPlannerUtil(null);
	protected final IHTNPlanningProblem<O, M, A> problem;
	protected final Map<String, Operation> primitiveTasks = new HashMap<>();

	public TFDGraphGenerator(IHTNPlanningProblem<O, M, A> problem) {
		this.problem = problem;
		for (Operation op : problem.getDomain().getOperations())
			primitiveTasks.put(op.getName(), op);
	}

	protected Collection<TFDNode> getSuccessorsResultingFromResolvingPrimitiveTask(Monom state, Literal taskToBeResolved, List<Literal> remainingOtherTasks) {
		Collection<TFDNode> successors = new ArrayList<>();
		for (Action applicableAction : util.getActionsForPrimitiveTaskThatAreApplicableInState(null, primitiveTasks.get(taskToBeResolved.getPropertyName()), taskToBeResolved, state)) {
			Monom stateCopy = new Monom(state);
			PlannerUtil.updateState(stateCopy, applicableAction);
			successors.add(postProcessPrimitiveTaskNode(new TFDNode(stateCopy, remainingOtherTasks, null, applicableAction)));
		}
		return successors;
	}

	protected Collection<TFDNode> getSuccessorsResultingFromResolvingComplexTask(Monom state, Literal taskToBeResolved, List<Literal> remainingOtherTasks) {
		Collection<TFDNode> successors = new ArrayList<>();
		for (MethodInstance instance : util.getMethodInstancesForTaskThatAreApplicableInState(null, this.problem.getDomain().getMethods(), taskToBeResolved, state, remainingOtherTasks)) {

			/* derive remaining network for this instance */
			List<Literal> remainingTasks = stripTNPrefixes(util.getTaskChainOfTotallyOrderedNetwork(instance.getNetwork()));
			remainingTasks.addAll(remainingOtherTasks);
			successors.add(postProcessComplexTaskNode(new TFDNode(state, remainingTasks, instance, null)));
		}
		return successors;
	}

	protected List<Literal> stripTNPrefixes(List<Literal> taskList) {
		return taskList.stream().map(l -> {
			String taskName = l.getPropertyName().substring(l.getPropertyName().indexOf("-") + 1, l.getPropertyName().length());
			return new Literal(taskName, l.getParameters(), l.isPositive());
		}).collect(Collectors.toList());
	}

	/**
	 * A hook for extending classes that can be used to change the nodes before they are attached
	 * 
	 * @param node
	 * @return
	 */
	protected TFDNode postProcessPrimitiveTaskNode(TFDNode node) {
		return node;
	}

	/**
	 * A hook for extending classes that can be used to change the nodes before they are attached
	 * 
	 * @param node
	 * @return
	 */
	protected TFDNode postProcessComplexTaskNode(TFDNode node) {
		return node;
	}

	@Override
	public SingleRootGenerator<TFDNode> getRootGenerator() {
		TaskPlannerUtil util = new TaskPlannerUtil(null);
		return () -> new TFDNode(problem.getInit(), stripTNPrefixes(util.getTaskChainOfTotallyOrderedNetwork(problem.getNetwork())));
	}

	@Override
	public SuccessorGenerator<TFDNode, String> getSuccessorGenerator() {
		return l -> {
			Monom state = l.getState();
			List<Literal> currentlyRemainingTasks = new ArrayList<>(l.getRemainingTasks());
			if (currentlyRemainingTasks.isEmpty())
				return new ArrayList<>();
			Literal nextTaskTmp = currentlyRemainingTasks.get(0);
			currentlyRemainingTasks.remove(0);
			String nextTaskName = nextTaskTmp.getPropertyName();
			Literal nextTask = new Literal(nextTaskName, nextTaskTmp.getParameters());

			/* get the child nodes */
			Collection<TFDNode> successors = primitiveTasks.containsKey(nextTask.getPropertyName()) ? getSuccessorsResultingFromResolvingPrimitiveTask(state, nextTask, currentlyRemainingTasks)
					: getSuccessorsResultingFromResolvingComplexTask(state, nextTask, currentlyRemainingTasks);

			/* change order in remaining tasks based on numbered prefixes */
			successors = successors.stream().map(s -> orderRemainingTasksByPriority(s)).collect(Collectors.toList());

			/* derive successor descriptions from the nodes */
			return successors.stream().map(n -> new NodeExpansionDescription<TFDNode, String>(l, n, "", NodeType.OR)).collect(Collectors.toList());
		};
	}

	public TFDNode orderRemainingTasksByPriority(TFDNode node) {

		/* determine order of tasks based on the prefixes */
		Pattern p = Pattern.compile("(\\d+)_");
		List<Literal> unorderedLiterals = new ArrayList<>();
		Map<Integer, List<Literal>> orderedLiterals = new HashMap<>();
		node.getRemainingTasks().forEach(t -> {
			Matcher m = p.matcher(t.getPropertyName());
			if (m.find()) {
				int order = Integer.valueOf(m.group(1));
				if (!orderedLiterals.containsKey(order))
					orderedLiterals.put(order, new ArrayList<>());
				List<Literal> tasksWithorder = orderedLiterals.get(order);
				tasksWithorder.add(t);
			} else
				unorderedLiterals.add(t);
		});

		/* reorganize task network */
		List<Literal> newLiteralList = new ArrayList<>();
		orderedLiterals.keySet().stream().sorted().forEach(order -> newLiteralList.addAll(orderedLiterals.get(order)));
		newLiteralList.addAll(unorderedLiterals);
		return new TFDNode(node.getState(), newLiteralList, node.getAppliedMethodInstance(), node.getAppliedAction());
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

	}

	@Override
	public boolean isPathSemanticallySubsumed(List<TFDNode> path, List<TFDNode> potentialSuperPath) throws InterruptedException {
		int n = path.size();
		for (int i = 0; i < n; i++)
			if (!path.get(i).equals(potentialSuperPath.get(i)))
				return false;
		return true;
	}
}
