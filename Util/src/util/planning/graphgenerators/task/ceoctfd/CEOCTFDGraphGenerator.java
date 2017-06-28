package util.planning.graphgenerators.task.ceoctfd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.basic.PerformanceLogger;
import util.logic.CNFFormula;
import util.logic.Literal;
import util.logic.Monom;
import util.planning.graphgenerators.task.TaskPlannerUtil;
import util.planning.graphgenerators.task.tfd.TFDNode;
import util.planning.graphgenerators.task.tfd.TFDNodeUtil;
import util.planning.graphgenerators.task.tfd.TFDRestProblem;
import util.planning.model.ceoc.CEOCAction;
import util.planning.model.ceoc.CEOCOperation;
import util.planning.model.conditional.CEAction;
import util.planning.model.conditional.CEOperation;
import util.planning.model.core.Action;
import util.planning.model.core.Operation;
import util.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import util.planning.model.task.stn.Method;
import util.planning.model.task.stn.MethodInstance;
import util.search.core.GraphGenerator;
import util.search.core.NodeExpansionDescription;
import util.search.core.NodeType;
import util.search.graphgenerator.GoalTester;
import util.search.graphgenerator.RootGenerator;
import util.search.graphgenerator.SuccessorGenerator;

public class CEOCTFDGraphGenerator implements GraphGenerator<TFDNode,String> {

	private static final Logger logger = LoggerFactory.getLogger(CEOCTFDGraphGenerator.class);
	private static final int checkpointDistance = 1;
	private final CEOCSTNPlanningProblem problem;
	private final CNFFormula knowledge;
	private final Map<String, Operation> primitiveTasks = new HashMap<>();

	public CEOCTFDGraphGenerator(CEOCSTNPlanningProblem problem) {
		this.problem = problem;
		this.knowledge = problem.getKnowledge();
		for (Operation op : problem.getDomain().getOperations())
			primitiveTasks.put(op.getName(), op);
	}

	@Override
	public RootGenerator<TFDNode> getRootGenerator() {
		return () -> Arrays.asList(new TFDNode[]{ new TFDNode(problem.getInit(), TaskPlannerUtil.getTaskChainOfTotallyOrderedNetwork(problem.getNetwork()))});
	}

	@Override
	public SuccessorGenerator<TFDNode,String> getSuccessorGenerator() {
		return l -> {
			List<NodeExpansionDescription<TFDNode,String>> successors = new ArrayList<>();
				
			TFDRestProblem rp = l.getPoint().getProblem();
			if (rp == null)
				rp = TFDNodeUtil.getRestProblem(l.externalPath());
			Monom state = rp.getState();
			List<Literal> currentlyRemainingTasks = rp.getRemainingTasks();
			Literal nextTaskTmp = currentlyRemainingTasks.get(0);
			if (nextTaskTmp == null)
				return successors;
			String nextTaskName = nextTaskTmp.getPropertyName().substring(nextTaskTmp.getPropertyName().indexOf("-") + 1, nextTaskTmp.getPropertyName().length());
			Literal nextTask = new Literal(nextTaskName, nextTaskTmp.getParameters());
			int depth = l.path().size();
			
			/* if the task is primitive */
			if (primitiveTasks.containsKey(nextTask.getPropertyName())) {

				logger.info("Computing successors for PRIMITIVE task {} in state {}", nextTask, state);
				for (Action applicableAction : TaskPlannerUtil.getActionsForPrimitiveTaskThatAreApplicableInState(knowledge, primitiveTasks.get(nextTask.getPropertyName()), nextTask, state)) {
					logger.info("Adding successor for PRIMITIVE task {} in state {}: {}", nextTask, state, applicableAction.getEncoding());
					
					/* if the depth is % k == 0, then compute the rest problem explicitly */
					Monom updatedState = new Monom(state, false);
					TFDNodeUtil.updateState(updatedState, applicableAction);
					List<Literal> remainingTasks = new ArrayList<>(currentlyRemainingTasks);
					remainingTasks.remove(0);
					TFDNode node = null;
					if (depth % checkpointDistance == 0)
						node = new TFDNode(updatedState, remainingTasks, null, new CEOCAction((CEOCOperation) applicableAction.getOperation(), applicableAction.getGrounding()));
					else
						node = new TFDNode(new CEAction((CEOperation) applicableAction.getOperation(), applicableAction.getGrounding()), remainingTasks.isEmpty());
					successors.add(new NodeExpansionDescription<>(l.getPoint(), node, "edge label", NodeType.OR));
				}
				if (successors.size() != new HashSet<>(successors).size()) {
					System.err.println("Doppelte Knoten im Nachfolger!");
					System.exit(1);
				}
				logger.info("Computed {} successors", successors.size());
			}

			/* otherwise determine methods for the task */
			else {

				logger.info("Computing successors for COMPLEX task {} in state {}", nextTask, state);
				Set<Method> usedMethods = new HashSet<>();
				PerformanceLogger.logStart("compute instances");
				Collection<MethodInstance> instances = TaskPlannerUtil.getMethodInstancesForTaskThatAreApplicableInState(knowledge, this.problem.getDomain().getMethods(), nextTask, state);
				PerformanceLogger.logEnd("compute instances");
				for (MethodInstance instance : instances) {

					/* skip this instance if the method is lonely and we already used it */
					if (!usedMethods.contains(instance.getMethod())) {
						usedMethods.add(instance.getMethod());
					} else if (instance.getMethod().isLonely()) {
						continue;
					}

					logger.info("Adding successor {}", instance);
					
					/* if the depth is % k == 0, then compute the rest problem explicitly */
					
					List<Literal> prependedTasks = TaskPlannerUtil.getTaskChainOfTotallyOrderedNetwork(instance.getNetwork());
					List<Literal> remainingTasks = new ArrayList<>(prependedTasks);
					remainingTasks.addAll(currentlyRemainingTasks);
					remainingTasks.remove(prependedTasks.size()); // remove the first literal of the 2ndly appended list
					TFDNode node = null;
					if (depth % checkpointDistance == 0)
						node = new TFDNode(new Monom(state, false), remainingTasks, instance, null);
					else
						node = new TFDNode(instance, remainingTasks.isEmpty());
					successors.add(new NodeExpansionDescription<>(l.getPoint(),  node, "edge label", NodeType.OR));
				}
				
				logger.info("Computed {} successors", successors.size());
			}
//			l.getPoint().clear();
			return successors;
		};
	}
	
	@Override
	public GoalTester<TFDNode> getGoalTester() {
		return p -> p.getPoint().isGoal();
	}

	public CEOCSTNPlanningProblem getProblem() {
		return problem;
	}
	
}
