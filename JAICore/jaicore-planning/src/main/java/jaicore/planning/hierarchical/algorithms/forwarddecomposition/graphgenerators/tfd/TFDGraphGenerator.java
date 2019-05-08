package jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.logging.ToJSONStringUtil;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.classical.algorithms.strips.forward.StripsUtil;
import jaicore.planning.classical.problems.strips.Operation;
import jaicore.planning.core.Action;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.TaskPlannerUtil;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.planning.hierarchical.problems.stn.MethodInstance;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.core.interfaces.PathUnifyingGraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

@SuppressWarnings("serial")
public class TFDGraphGenerator implements SerializableGraphGenerator<TFDNode, String>, PathUnifyingGraphGenerator<TFDNode, String> {

	private static Logger logger = LoggerFactory.getLogger(TFDGraphGenerator.class);
	protected TaskPlannerUtil util = new TaskPlannerUtil(null);
	protected final IHTNPlanningProblem problem;
	protected final Map<String, Operation> primitiveTasks = new HashMap<>();

	public TFDGraphGenerator(final IHTNPlanningProblem problem) {
		this.problem = problem;
		for (Operation op : problem.getDomain().getOperations()) {
			this.primitiveTasks.put(op.getName(), op);
		}
	}

	protected Collection<TFDNode> getSuccessorsResultingFromResolvingPrimitiveTask(final Monom state, final Literal taskToBeResolved, final List<Literal> remainingOtherTasks) throws InterruptedException {
		Collection<TFDNode> successors = new ArrayList<>();
		for (Action applicableAction : this.util.getActionsForPrimitiveTaskThatAreApplicableInState(null, this.primitiveTasks.get(taskToBeResolved.getPropertyName()), taskToBeResolved, state)) {
			Monom stateCopy = new Monom(state);
			StripsUtil.updateState(stateCopy, applicableAction);
			successors.add(this.postProcessPrimitiveTaskNode(new TFDNode(stateCopy, remainingOtherTasks, null, applicableAction)));
		}
		return successors;
	}

	protected Collection<TFDNode> getSuccessorsResultingFromResolvingComplexTask(final Monom state, final Literal taskToBeResolved, final List<Literal> remainingOtherTasks) throws InterruptedException {
		Collection<TFDNode> successors = new ArrayList<>();
		Collection<MethodInstance> applicableMethodInstances = this.util.getMethodInstancesForTaskThatAreApplicableInState(null, this.problem.getDomain().getMethods(), taskToBeResolved, state, remainingOtherTasks);
		assert this.areLonelyMethodsContainedAtMostOnce(applicableMethodInstances);
		for (MethodInstance instance : applicableMethodInstances) {

			/* derive remaining network for this instance */
			List<Literal> remainingTasks = this.stripTNPrefixes(this.util.getTaskChainOfTotallyOrderedNetwork(instance.getNetwork()));
			remainingTasks.addAll(remainingOtherTasks);
			successors.add(this.postProcessComplexTaskNode(new TFDNode(state, remainingTasks, instance, null)));
		}
		return successors;
	}

	private boolean areLonelyMethodsContainedAtMostOnce(final Collection<MethodInstance> instances) {
		List<Method> usedMethods = new ArrayList<>();
		for (MethodInstance mi : instances) {
			if (!mi.getMethod().isLonely()) {
				continue;
			}
			boolean doubleUseDetected = usedMethods.contains(mi.getMethod());
			assert !doubleUseDetected : "Lonely method " + mi.getMethod() + " has been generated several times as being applicable!";
			if (doubleUseDetected) {
				return false;
			}
			usedMethods.add(mi.getMethod());
		}
		return true;
	}

	protected List<Literal> stripTNPrefixes(final List<Literal> taskList) {
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
	protected TFDNode postProcessPrimitiveTaskNode(final TFDNode node) {
		return node;
	}

	/**
	 * A hook for extending classes that can be used to change the nodes before they are attached
	 *
	 * @param node
	 * @return
	 */
	protected TFDNode postProcessComplexTaskNode(final TFDNode node) {
		return node;
	}

	@Override
	public SingleRootGenerator<TFDNode> getRootGenerator() {
		TaskPlannerUtil util = new TaskPlannerUtil(null);
		return () -> new TFDNode(this.problem.getInit(), this.stripTNPrefixes(util.getTaskChainOfTotallyOrderedNetwork(this.problem.getNetwork())));
	}

	@Override
	public SuccessorGenerator<TFDNode, String> getSuccessorGenerator() {
		return l -> {
			Monom state = l.getState();
			List<Literal> currentlyRemainingTasks = new ArrayList<>(l.getRemainingTasks());
			if (currentlyRemainingTasks.isEmpty()) {
				return new ArrayList<>();
			}
			Literal nextTaskTmp = currentlyRemainingTasks.get(0);
			currentlyRemainingTasks.remove(0);
			String nextTaskName = nextTaskTmp.getPropertyName();
			Literal nextTask = new Literal(nextTaskName, nextTaskTmp.getParameters());

			/* get the child nodes */
			long creationStartTime = System.currentTimeMillis();
			Collection<TFDNode> successors = this.primitiveTasks.containsKey(nextTask.getPropertyName()) ? this.getSuccessorsResultingFromResolvingPrimitiveTask(state, nextTask, currentlyRemainingTasks)
					: this.getSuccessorsResultingFromResolvingComplexTask(state, nextTask, currentlyRemainingTasks);
			logger.info("Node generation finished and took {}ms", System.currentTimeMillis() - creationStartTime);

			/* change order in remaining tasks based on numbered prefixes */
			successors = successors.stream().map(s -> this.orderRemainingTasksByPriority(s)).collect(Collectors.toList());

			/* derive successor descriptions from the nodes */
			return successors.stream().map(n -> new NodeExpansionDescription<TFDNode, String>(l, n, n.getAppliedAction() != null ? n.getAppliedAction().getEncoding() : n.getAppliedMethodInstance().getEncoding(), NodeType.OR)).collect(Collectors.toList());
		};
	}

	public TFDNode orderRemainingTasksByPriority(final TFDNode node) {

		/* determine order of tasks based on the prefixes */
		Pattern p = Pattern.compile("(\\d+)_");
		List<Literal> unorderedLiterals = new ArrayList<>();
		Map<Integer, List<Literal>> orderedLiterals = new HashMap<>();
		node.getRemainingTasks().forEach(t -> {
			Matcher m = p.matcher(t.getPropertyName());
			if (m.find()) {
				int order = Integer.valueOf(m.group(1));
				if (!orderedLiterals.containsKey(order)) {
					orderedLiterals.put(order, new ArrayList<>());
				}
				List<Literal> tasksWithorder = orderedLiterals.get(order);
				tasksWithorder.add(t);
			} else {
				unorderedLiterals.add(t);
			}
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
	public void setNodeNumbering(final boolean nodenumbering) {

	}

	@Override
	public boolean isPathSemanticallySubsumed(final List<TFDNode> path, final List<TFDNode> potentialSuperPath) throws InterruptedException {
		int n = path.size();
		for (int i = 0; i < n; i++) {
			if (!path.get(i).equals(potentialSuperPath.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("util", this.util);
		fields.put("problem", this.problem);
		fields.put("primitiveTasks", this.primitiveTasks);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
