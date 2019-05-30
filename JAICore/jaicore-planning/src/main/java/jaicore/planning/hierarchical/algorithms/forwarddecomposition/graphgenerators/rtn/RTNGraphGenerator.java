package jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.rtn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.classical.algorithms.strips.forward.StripsUtil;
import jaicore.planning.classical.problems.ceoc.CEOCAction;
import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.classical.problems.strips.Operation;
import jaicore.planning.core.Action;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.TaskPlannerUtil;
import jaicore.planning.hierarchical.problems.rtn.RTNPlanningProblem;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.planning.hierarchical.problems.stn.MethodInstance;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class RTNGraphGenerator implements GraphGenerator<RTNNode, RTNEdge> {

	private static final Logger logger = LoggerFactory.getLogger(RTNGraphGenerator.class);

	private final RTNPlanningProblem problem;
	private final Map<String, Operation> primitiveTasks = new HashMap<>();
	private final TaskPlannerUtil util = new TaskPlannerUtil(null);

	public RTNGraphGenerator(RTNPlanningProblem problem) {
		this.problem = problem;
		for (Operation op : problem.getDomain().getOperations())
			primitiveTasks.put(op.getName(), op);
	}

	@Override
	public SingleRootGenerator<RTNNode> getRootGenerator() {
		return () -> new RTNNode(false, problem.getInit(), new ArrayList<>(util.getTaskChainOfTotallyOrderedNetwork(problem.getNetwork())));
	}

	@Override
	public SuccessorGenerator<RTNNode, RTNEdge> getSuccessorGenerator() {
		return l -> {
			final List<NodeExpansionDescription<RTNNode, RTNEdge>> successors = new ArrayList<>();
			final Monom state = l.getState();
			final List<Literal> currentlyRemainingTasks = l.getRemainingTasks();
			final Literal nextTaskTmp = currentlyRemainingTasks.get(0);
			if (nextTaskTmp == null)
				return successors;
			final Literal nextTask = new Literal(nextTaskTmp.getPropertyName().substring(nextTaskTmp.getPropertyName().indexOf("-") + 1, nextTaskTmp.getPropertyName().length()),
					nextTaskTmp.getParameters());
			final String actualTaskName = nextTask.getPropertyName();
			
			/* if this is an or-node, perform the split as always */
			if (!l.isAndNode()) {

				/* if the task is primitive */

				if (primitiveTasks.containsKey(actualTaskName)) {

					logger.info("Computing successors for PRIMITIVE task {} in state {}", nextTask, state);

					final Collection<Action> applicableActions = util.getActionsForPrimitiveTaskThatAreApplicableInState(null, primitiveTasks.get(actualTaskName), nextTask,
							state);
					for (Action applicableAction : applicableActions) {
						logger.info("Adding successor for PRIMITIVE task {} in state {}: {}", nextTask, state, applicableAction.getEncoding());

						assert state.containsAll(applicableAction.getPrecondition().stream().filter(lit -> lit.isPositive()).collect(Collectors.toList()))
								&& SetUtil.disjoint(state, applicableAction.getPrecondition().stream().filter(lit -> lit.isNegated()).collect(Collectors.toList())) : ("Action "
										+ applicableAction + " is supposed to be aplpicable in state " + state + " but it is not!");

						/* if the depth is % k == 0, then compute the rest problem explicitly */
						final Monom updatedState = new Monom(state, false);
						final CEOCOperation op = (CEOCOperation) applicableAction.getOperation();
						final CEOCAction relevantAction = new CEOCAction(op, applicableAction.getGrounding());
						try {
							StripsUtil.updateState(updatedState, applicableAction);
						} catch (Exception e) {
							System.out.println("apply " + applicableAction.getEncoding() + " to state: " + state);
							System.out.println("addlists: " + relevantAction.getAddLists());
							e.printStackTrace();
							System.exit(1);
						}
						final List<Literal> remainingTasks = new ArrayList<>(currentlyRemainingTasks);
						remainingTasks.remove(0);
						boolean isAndNode = remainingTasksInitializeANDNode(remainingTasks);
						successors.add(new NodeExpansionDescription<>(l, new RTNNode(isAndNode, updatedState, remainingTasks), new RTNEdge(null, null, relevantAction), null));
					}
					assert checkDoubleNodes(successors);
					logger.info("Computed {} successors", successors.size());
				}

				/* otherwise determine methods for the task */
				else {

					logger.info("Computing successors for COMPLEX task {} in state {}", nextTask, state);
					final Set<Method> usedMethods = new HashSet<>();

					/* if this is an OR-Node */
					final Collection<MethodInstance> instances = util.getMethodInstancesForTaskThatAreApplicableInState(null, this.problem.getDomain().getMethods(), nextTask,
							state, currentlyRemainingTasks);
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

						final List<Literal> remainingTasks = new ArrayList<>(util.getTaskChainOfTotallyOrderedNetwork(instance.getNetwork()));

						final int indexForRemoval = remainingTasks.size();
						remainingTasks.addAll(currentlyRemainingTasks);
						remainingTasks.remove(indexForRemoval); // remove the first literal of the 2ndly appended list

						/* hard code the and-or-stuff for a moment */
						boolean isAndNode = remainingTasksInitializeANDNode(remainingTasks);
						successors.add(new NodeExpansionDescription<>(l, new RTNNode(isAndNode, state, remainingTasks), new RTNEdge(null, instance, null), null));
					}
				}
			}

			/* if this is an AND-node, create one successor for each refine action and the first after them */
			else {

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
					for (String c : RTNUtil.getClassesThatExistInState(l)) {
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
					successors.add(new NodeExpansionDescription<>(l, new RTNNode(false, reducedState, remainingTask), new RTNEdge(mapping, null, null), null));
				}

				/* now create one node for the remaining tasks */
				if (!tasksForLastNode.isEmpty())
					successors.add(new NodeExpansionDescription<>(l, new RTNNode(false, state, tasksForLastNode), new RTNEdge(null, null, null), null));
			}

			logger.info("Computed {} successors", successors.size());
			return successors;
		};
	}

	@Override
	public NodeGoalTester<RTNNode> getGoalTester() {
		return p -> p.getRemainingTasks().isEmpty();
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

	@Override
	public boolean isSelfContained() {
		return false;
	}

	@Override
	public void setNodeNumbering(boolean nodenumbering) {
		// TODO Auto-generated method stub
		
	}
}
