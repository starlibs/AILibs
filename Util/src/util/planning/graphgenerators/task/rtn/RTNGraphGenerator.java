package util.planning.graphgenerators.task.rtn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.basic.PerformanceLogger;
import util.basic.SetUtil;
import util.logic.ConstantParam;
import util.logic.Literal;
import util.logic.Monom;
import util.planning.graphgenerators.task.TaskPlannerUtil;
import util.planning.graphgenerators.task.tfd.TFDNodeUtil;
import util.planning.model.ceoc.CEOCAction;
import util.planning.model.ceoc.CEOCOperation;
import util.planning.model.core.Action;
import util.planning.model.core.Operation;
import util.planning.model.task.rtn.RTNPlanningProblem;
import util.planning.model.task.stn.Method;
import util.planning.model.task.stn.MethodInstance;
import util.search.core.NodeExpansionDescription;
import util.search.core.NodeType;
import util.search.core.OrNode;
import util.search.core.GraphGenerator;
import util.search.graphgenerator.GoalTester;
import util.search.graphgenerator.RootGenerator;
import util.search.graphgenerator.SuccessorGenerator;

public class RTNGraphGenerator implements GraphGenerator<RTNNode, RTNEdge> {

	private static final Logger logger = LoggerFactory.getLogger(RTNGraphGenerator.class);

	private final RTNPlanningProblem problem;
	private final Map<String, Operation> primitiveTasks = new HashMap<>();

	public RTNGraphGenerator(RTNPlanningProblem problem) {
		this.problem = problem;
		for (Operation op : problem.getDomain().getOperations())
			primitiveTasks.put(op.getName(), op);
	}

	@Override
	public RootGenerator<RTNNode> getRootGenerator() {
		return () -> Arrays.asList(new RTNNode[]{ new RTNNode(problem.getInit(), new ArrayList<>(TaskPlannerUtil.getTaskChainOfTotallyOrderedNetwork(problem.getNetwork())))});
	}

	@Override
	public SuccessorGenerator<RTNNode, RTNEdge> getSuccessorGenerator() {
		return l -> {
			PerformanceLogger.logStart("successor computation init");
			final List<NodeExpansionDescription<RTNNode, RTNEdge>> successors = new ArrayList<>();
			final Monom state = l.getPoint().getState();
			final List<Literal> currentlyRemainingTasks = l.getPoint().getRemainingTasks();
			final Literal nextTaskTmp = currentlyRemainingTasks.get(0);
			if (nextTaskTmp == null)
				return successors;
			final Literal nextTask = new Literal(nextTaskTmp.getPropertyName().substring(nextTaskTmp.getPropertyName().indexOf("-") + 1, nextTaskTmp.getPropertyName().length()),
					nextTaskTmp.getParameters());
			final String actualTaskName = nextTask.getPropertyName();
			PerformanceLogger.logEnd("successor computation init");
			
			/* if this is an or-node, perform the split as always */
			if (l instanceof OrNode) {
				PerformanceLogger.logStart("successor computation: Computing OR-Nodes");

				/* if the task is primitive */

				if (primitiveTasks.containsKey(actualTaskName)) {

					logger.info("Computing successors for PRIMITIVE task {} in state {}", nextTask, state);

					PerformanceLogger.logStart("Compute applicable actions");
					final Collection<Action> applicableActions = TaskPlannerUtil.getActionsForPrimitiveTaskThatAreApplicableInState(null, primitiveTasks.get(actualTaskName), nextTask,
							state);
					PerformanceLogger.logEnd("Compute applicable actions");
					PerformanceLogger.logStart("Generate nodes for applicable actions");
					for (Action applicableAction : applicableActions) {
						logger.info("Adding successor for PRIMITIVE task {} in state {}: {}", nextTask, state, applicableAction.getEncoding());

						assert state.containsAll(applicableAction.getPrecondition().stream().filter(lit -> lit.isPositive()).collect(Collectors.toList()))
								&& SetUtil.disjoint(state, applicableAction.getPrecondition().stream().filter(lit -> lit.isNegated()).collect(Collectors.toList())) : ("Action "
										+ applicableAction + " is supposed to be aplpicable in state " + state + " but it is not!");

						/* if the depth is % k == 0, then compute the rest problem explicitly */
						PerformanceLogger.logStart("copy state");
						final Monom updatedState = new Monom(state, false);
						PerformanceLogger.logEnd("copy state");
						PerformanceLogger.logStart("explicit cast");
						final CEOCOperation op = (CEOCOperation) applicableAction.getOperation();
						PerformanceLogger.logEnd("explicit cast");
						PerformanceLogger.logStart("copy action");
						final CEOCAction relevantAction = new CEOCAction(op, applicableAction.getGrounding());
						PerformanceLogger.logEnd("copy action");
						try {
							PerformanceLogger.logStart("update state");
							TFDNodeUtil.updateState(updatedState, applicableAction);
							PerformanceLogger.logEnd("update state");
						} catch (Exception e) {
							System.out.println("apply " + applicableAction.getEncoding() + " to state: " + state);
							System.out.println("addlists: " + relevantAction.getAddLists());
							e.printStackTrace();
							System.exit(1);
						}
						final List<Literal> remainingTasks = new ArrayList<>(currentlyRemainingTasks);
						remainingTasks.remove(0);
						PerformanceLogger.logStart("insert node");
						successors.add(new NodeExpansionDescription<>(l.getPoint(), new RTNNode(updatedState, remainingTasks), new RTNEdge(null, null, relevantAction),
								remainingTasksInitializeANDNode(remainingTasks) ? NodeType.AND : NodeType.OR));
						PerformanceLogger.logEnd("insert node");
					}
					assert checkDoubleNodes(successors);
					PerformanceLogger.logEnd("Generate nodes for applicable actions");
					logger.info("Computed {} successors", successors.size());
				}

				/* otherwise determine methods for the task */
				else {

					logger.info("Computing successors for COMPLEX task {} in state {}", nextTask, state);
					final Set<Method> usedMethods = new HashSet<>();

					/* if this is an OR-Node */
					PerformanceLogger.logStart("Compute applicable method instances");
					final Collection<MethodInstance> instances = TaskPlannerUtil.getMethodInstancesForTaskThatAreApplicableInState(null, this.problem.getDomain().getMethods(), nextTask,
							state);
					PerformanceLogger.logEnd("Compute applicable method instances");
					PerformanceLogger.logStart("Generate nodes for applicable method instances");
					for (MethodInstance instance : instances) {

						/* skip this instance if the method is lonely and we already used it */
						if (!usedMethods.contains(instance.getMethod())) {
							usedMethods.add(instance.getMethod());
						} else if (instance.getMethod().isLonely()) {
							continue;
						}

						assert state.containsAll(instance.getPrecondition().stream().filter(lit -> lit.isPositive()).collect(Collectors.toList()))
								&& SetUtil.disjoint(state, instance.getPrecondition().stream().filter(lit -> lit.isNegated()).collect(Collectors.toList())) : ("Instance "
										+ instance + " is supposed to be aplpicable in state " + state + " but it is not!");

						logger.info("Adding successor {}", instance);

						final List<Literal> remainingTasks = new ArrayList<>(TaskPlannerUtil.getTaskChainOfTotallyOrderedNetwork(instance.getNetwork()));

						final int indexForRemoval = remainingTasks.size();
						remainingTasks.addAll(currentlyRemainingTasks);
						remainingTasks.remove(indexForRemoval); // remove the first literal of the 2ndly appended list

						/* hard code the and-or-stuff for a moment */
						successors.add(new NodeExpansionDescription<>(l.getPoint(), new RTNNode(state, remainingTasks), new RTNEdge(null, instance, null),
								remainingTasksInitializeANDNode(remainingTasks) ? NodeType.AND : NodeType.OR));
					}
					PerformanceLogger.logEnd("Generate nodes for applicable method instances");
				}
				PerformanceLogger.logEnd("successor computation: Computing OR-Nodes");
			}

			/* if this is an AND-node, create one successor for each refine action and the first after them */
			else {

				PerformanceLogger.logStart("successor computation: Computing AND-Nodes");

				/* determine the next k tasks that are parallelizable */
				final List<Literal> parallelizableTasks = new ArrayList<>();
				final List<Literal> tasksForLastNode = new ArrayList<>(currentlyRemainingTasks);
				for (int i = 0; i < currentlyRemainingTasks.size(); i++) {
					Literal task = currentlyRemainingTasks.get(i);
					if (task.getPropertyName().contains("refine")) {
						parallelizableTasks.add(task);
						tasksForLastNode.remove(0);
					}
				}

				/* now create one successor for each of the refine-statements */
				for (Literal task : parallelizableTasks) {

					/* compute the reduced state */
					final Monom reducedState = new Monom();
					final Set<ConstantParam> relevantConstants = new HashSet<>(task.getConstantParams());
					for (String c : RTNUtil.getClassesThatExistInState(l.getPoint())) {
						relevantConstants.add(new ConstantParam(c));
					}
					for (Literal lit : state) {
						if (relevantConstants.containsAll(lit.getConstantParams()))
							reducedState.add(lit);
					}
					
					/* rename clusters in reduced state */
					Map<String, Collection<String>> clusters = new HashMap<>();
					for (Literal lit : reducedState) {
						if (lit.getPropertyName().equals("in")) {
							String item = lit.getConstantParams().get(0).getName();
							String cluster = lit.getConstantParams().get(1).getName();
							if (!clusters.containsKey(cluster))
								clusters.put(cluster, new ArrayList<>());
							clusters.get(cluster).add(item);
						}
					}
					for (String cluster : clusters.keySet()) {
						clusters.put(cluster, clusters.get(cluster).stream().sorted().collect(Collectors.toList()));
					}
					final List<Literal> toRemove = new ArrayList<>();
					final List<Literal> toInsert = new ArrayList<>();
					for (Literal lit : reducedState) {
						if (lit.getPropertyName().equals("biggest")) {
							toRemove.add(lit);
							continue;
						} else if (!SetUtil.intersection(lit.getConstantParams().stream().map(p -> p.getName()).collect(Collectors.toList()), clusters.keySet()).isEmpty()) {
							toRemove.add(lit);
							final List<ConstantParam> params = new ArrayList<>();
							for (ConstantParam p : lit.getConstantParams()) {
								params.add(clusters.containsKey(p.getName()) ? new ConstantParam(clusters.get(p.getName()).toString()) : p);
							}
							toInsert.add(new Literal(lit.getPropertyName(), params));
						}
					}
					reducedState.removeAll(toRemove);
					reducedState.addAll(toInsert);

					/* add the ground knowledge to the state (even about objects that are not there anymore) */
					for (Literal lit : state) {
						if (lit.getPropertyName().equals("bigger") && !reducedState.contains(lit))
							reducedState.add(lit);
					}

					/* define the remaining task */
					final List<Literal> remainingTask = new ArrayList<>();
					final List<ConstantParam> paramsForTask = new ArrayList<>();
					for (ConstantParam p : task.getConstantParams()) {
						paramsForTask.add(clusters.containsKey(p.getName()) ? new ConstantParam(clusters.get(p.getName()).toString()) : p);
					}

					/* define mapping for renaiming the subproblem solutions afterwards */
					Map<ConstantParam, ConstantParam> mapping = new HashMap<>();
					for (String cluster : clusters.keySet()) {
						mapping.put(new ConstantParam(clusters.get(cluster).toString()), new ConstantParam(cluster));
					}

					remainingTask.add(new Literal(task.getPropertyName().substring(task.getPropertyName().indexOf("-") + 1), paramsForTask));
					successors.add(new NodeExpansionDescription<>(l.getPoint(), new RTNNode(reducedState, remainingTask), new RTNEdge(mapping, null, null), NodeType.OR));
				}

				/* now create one node for the remaining tasks */
				if (!tasksForLastNode.isEmpty())
					successors.add(new NodeExpansionDescription<>(l.getPoint(), new RTNNode(state, tasksForLastNode), new RTNEdge(null, null, null), NodeType.OR));
				PerformanceLogger.logEnd("successor computation: Computing AND-Nodes");
			}

			logger.info("Computed {} successors", successors.size());
			return successors;
		};
	}

	@Override
	public GoalTester<RTNNode> getGoalTester() {
		return p -> p.getPoint().getRemainingTasks().isEmpty();
	}

	private boolean remainingTasksInitializeANDNode(List<Literal> tasks) {
		if (tasks.isEmpty())
			return false;
		Literal followingTask = tasks.get(0);
		return followingTask.getPropertyName().contains("refine");
	}

	private static boolean checkDoubleNodes(List<NodeExpansionDescription<RTNNode, RTNEdge>> successors) {
		if (successors.size() != new HashSet<>(successors).size()) {
			System.err.println("Doppelte Knoten im Nachfolger!");
			return false;
		}
		return true;
	}
}
