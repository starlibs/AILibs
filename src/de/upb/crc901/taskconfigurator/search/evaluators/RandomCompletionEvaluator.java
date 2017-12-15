package de.upb.crc901.taskconfigurator.search.evaluators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.taskconfigurator.core.MLPipeline;
import de.upb.crc901.taskconfigurator.core.MLPipelineSolutionAnnotation;
import de.upb.crc901.taskconfigurator.core.MLUtil;
import de.upb.crc901.taskconfigurator.core.SolutionEvaluator;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.task.ceocstn.CEOCSTNUtil;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.ICancelableNodeEvaluator;
import jaicore.search.algorithms.standard.core.IGraphDependentNodeEvaluator;
import jaicore.search.algorithms.standard.core.ISolutionReportingNodeEvaluator;
import jaicore.search.algorithms.standard.core.SolutionEventBus;
import jaicore.search.algorithms.standard.core.SolutionFoundEvent;
import jaicore.search.algorithms.standard.rdfs.RandomizedDepthFirstSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;
import weka.core.Instances;

@SuppressWarnings("serial")
public class RandomCompletionEvaluator implements IGraphDependentNodeEvaluator<TFDNode, String, Integer>, DataDependentNodeEvaluator<TFDNode, Integer>,
		SerializableNodeEvaluator<TFDNode, Integer>, ISolutionReportingNodeEvaluator<TFDNode, Integer>, ICancelableNodeEvaluator {

	private final static Logger logger = LoggerFactory.getLogger(RandomCompletionEvaluator.class);
	private static Map<List<TFDNode>, List<TFDNode>> completions = new ConcurrentHashMap<>();
	private static Set<List<TFDNode>> postedSolutions = new HashSet<>();
	private static Set<List<CEOCAction>> unsuccessfulPlans = Collections.synchronizedSet(new HashSet<>());
	private static Map<List<CEOCAction>, Integer> pipelineScores = new ConcurrentHashMap<>();
	private static Map<List<CEOCAction>, Integer> pipelineScoreTimes = new ConcurrentHashMap<>();
	private static Map<Node<TFDNode, Integer>, Integer> fValues = new ConcurrentHashMap<>();
	private static final List<String> classifierRanking = Arrays.asList(new String[] { "IBk", "NaiveBayesMultinomial", "RandomTree", "NaiveBayes", "RandomForest", "SimpleLogistic",
			"MultiLayerPerceptron", "VotedPerceptron", "J48", "SMO", "Logistic" });

	private GraphGenerator<TFDNode, String> generator;
	private long timestampOfFirstEvaluation;
	private final Random random;
	private final int samples;
	private final SolutionEvaluator evaluator;
	private final SolutionEventBus<TFDNode> eventBus = new SolutionEventBus<>();
	private boolean dataSet = false;

	// private final Map<String,AttributeSelection> cachedFilters = new HashMap<>();
	// private final Map<String,Map<String,Integer>> solutionsPerTechnique = new HashMap<>();

	public RandomCompletionEvaluator(Random random, int samples, SolutionEvaluator evaluator) {
		super();
		if (random == null)
			throw new IllegalArgumentException("Random source must not be null!");
		if (samples <= 0)
			throw new IllegalArgumentException("Sample size must be greater than 0!");
		if (evaluator == null)
			throw new IllegalArgumentException("Evaluator must not be null!");
		this.random = random;
		this.samples = samples;
		this.evaluator = evaluator;
	}

	public Integer f(Node<TFDNode, Integer> n) throws Exception {
		if (timestampOfFirstEvaluation == 0)
			timestampOfFirstEvaluation = System.currentTimeMillis();
		logger.info("Received request for f-value of node {}", n);
		
		if (!fValues.containsKey(n)) {
			
			/* if we already have a value for this path, do not continue */
			if (generator == null)
				throw new IllegalStateException("Cannot compute f-values before the generator is set!");
			if (!n.getPoint().isGoal()) {
				List<TFDNode> path = n.externalPath();
				logger.info("This is an unknown node; computing score for path to node: {}", path);
				List<CEOCAction> partialPlan = CEOCSTNUtil.extractPlanFromSolutionPath(path);
				
				/* if we have an f-value belonging to this plan, store it (this can be if we get asked for the f of a node we generated internally but that does not even belong to the main search graph) */
				if (!pipelineScores.containsKey(partialPlan)) {
				
					/* check whether a filter and a classifier have been defined */
					List<String> currentProgram = Arrays.asList(MLUtil.getJavaCodeFromPlan(partialPlan).split("\n"));
					Optional<String> preprocessorLine = currentProgram.stream().filter(line -> line.contains("new") && line.contains("attributeSelection")).findAny();
					Optional<String> classifierLine = currentProgram.stream().filter(line -> line.contains("new") && line.contains("classifiers")).findAny();
					if (!classifierLine.isPresent()) {
						String nextLiteralName = n.getPoint().getRemainingTasks().get(0).getPropertyName();
						if (nextLiteralName.endsWith("__construct") && nextLiteralName.contains("classifiers")) {
							String classifierName = nextLiteralName.substring(nextLiteralName.lastIndexOf(".") + 1, nextLiteralName.indexOf(":"));
							if (!classifierRanking.contains(classifierName))
								return classifierRanking.size() * 2;
							return classifierRanking.indexOf(classifierName) + (preprocessorLine.isPresent() ? classifierRanking.size() : 0);
						}
						logger.info("No classifier defined yet, so returning 0.");
						return 0;
					}
	
					/* if the classifier has just been defined, check for the standard configuration */
					if (currentProgram.get(currentProgram.size() - 1).equals(classifierLine.get())) {
						logger.info("Classifier has just been chosen, now try its standard configuration.");
						Integer f = getFValueComputerForCompletePath(path);
						if (f != null) {
							fValues.put(n, f);
						} else {
							fValues.put(n, Integer.MAX_VALUE);
						}
					}
	
					/* if the space under this solution is overly searched, reject */
					// String filterName = getFilterName(partialPipeline);
					// String classifierName = getBaselearnerName(partialPipeline);
					// if (solutionsPerTechnique.containsKey(filterName) && solutionsPerTechnique.get(filterName).containsKey(classifierName) &&
					// solutionsPerTechnique.get(filterName).get(classifierName) > 10)
					// {
					// return Integer.MAX_VALUE;
					// }
	
					/* check if we have an f-value for exactly this node */
					if (!completions.containsKey(path)) {
						
						/* determine whether we have a solution path (found by the oracle) that goes over this node */
						/* only if we have no path to a solution over this node, we compute a new one */
						List<TFDNode> pathWhoseCompletionSubsumesCurrentPath = getSubsumingKnownPathCompletion(path);
						// List<TFDNode> pathWhoseCompletionSubsumesCurrentPath = null;
						if (pathWhoseCompletionSubsumesCurrentPath == null) {
							int best = Integer.MAX_VALUE;
							List<TFDNode> bestCompletion = null;
							for (int i = 0; i < samples; i++) {
	
								/* create randomized dfs searcher */
								BestFirst<TFDNode, String> completer = new RandomizedDepthFirstSearch<>(new GraphGenerator<TFDNode, String>() {
									public SingleRootGenerator<TFDNode> getRootGenerator() {
										return () -> n.getPoint();
									}
	
									public SuccessorGenerator<TFDNode, String> getSuccessorGenerator() {
										return generator.getSuccessorGenerator();
									}
	
									public GoalTester<TFDNode> getGoalTester() {
										return generator.getGoalTester();
									}
								}, random);
	
								/* now complete the current path by the dfs-solution */
								List<TFDNode> completedPath = new ArrayList<>(n.externalPath());
								List<TFDNode> pathCompletion = completer.nextSolution();
								if (pathCompletion == null)
									return Integer.MAX_VALUE;
								pathCompletion.remove(0);
								completedPath.addAll(pathCompletion);
	
								/* now evaluate this solution */
								Integer val = getFValueComputerForCompletePath(completedPath);
	
								if (val != null) {
									if (val < best) {
										best = val;
										bestCompletion = completedPath;
									}
								}
							}
							if (bestCompletion == null)
								return Integer.MAX_VALUE;
							assert bestCompletion == null || isSolutionPath(bestCompletion) : "Identified a completion that is no solution path!";
							completions.put(path, bestCompletion);
							pathWhoseCompletionSubsumesCurrentPath = path;
						} else {
							assert isSolutionPath(completions.get(pathWhoseCompletionSubsumesCurrentPath)) : "Identified a subsuming completion "
									+ pathWhoseCompletionSubsumesCurrentPath.stream().map(l -> l.toString() + "\n").collect(Collectors.toList()) + " that is no solution path!";
							completions.put(path, completions.get(pathWhoseCompletionSubsumesCurrentPath));
						}
					}
					fValues.put(n, getFValueComputerForCompletePath(completions.get(path)));
				}
				
				/* if this is a known plan, transfer the knowledge about its f to the node */
				else {
					fValues.put(n, pipelineScores.get(partialPlan));
				}
			}

			/* the node is a goal node */
			else {
				/* record that we found a new solution for this technique */
				// String filterName = getFilterName(pipeline);
				// String classifierName = getBaselearnerName(pipeline);
				// if (!solutionsPerTechnique.containsKey(filterName))
				// solutionsPerTechnique.put(filterName, new HashMap<>());
				// if (!solutionsPerTechnique.get(filterName).containsKey(classifierName))
				// solutionsPerTechnique.get(filterName).put(classifierName, 0);
				// solutionsPerTechnique.get(filterName).put(classifierName, solutionsPerTechnique.get(filterName).get(classifierName) + 1);
				fValues.put(n, getFValueComputerForCompletePath(n.externalPath()));
			}
		}
		int f = fValues.get(n);
		logger.info("Returning f-value: {}", f);
		return f;
	}

	private Integer getFValueComputerForCompletePath(List<TFDNode> path) throws Exception {
		if (!dataSet)
			throw new IllegalStateException("Cannot compute f-values if data have not been set!");
		assert isSolutionPath(path) : "Can only compute f-values for completed plans, but it is invoked with a plan that does not yield a goal node!";
		logger.info("Compute f-value for complete path {}", path);
		List<CEOCAction> plan = CEOCSTNUtil.extractPlanFromSolutionPath(path);
		if (!pipelineScores.containsKey(plan)) {
			if (unsuccessfulPlans.contains(plan)) {
				logger.info("Associated plan was evaluated unsuccessfully in a previous run; returning NULL: {}", plan); 
				return null;
			}
			logger.info("Associated plan is new. Compute f-value for complete plan {}", plan);
			
			long start = System.currentTimeMillis();
			Integer val = null;
			try {
			 val = evaluator.getSolutionScore(MLUtil.extractPipelineFromPlan(plan));
			}
			catch (Exception e) {
				unsuccessfulPlans.add(plan);
				throw e;
			}
			long duration = System.currentTimeMillis() - start;
			logger.info("Result: {}, Size: {}", val, pipelineScores.size());
			if (val == null) {
				unsuccessfulPlans.add(plan);
				return null;
			}
			pipelineScores.put(plan, val);
			pipelineScoreTimes.put(plan, (int) duration);
			postSolution(path);
		}
		logger.info("Determined value {} for pipeline {}.", pipelineScores.get(plan), plan);
		return pipelineScores.get(plan);
	}

	private boolean isSolutionPath(List<TFDNode> path) {
		return path.get(path.size() - 1).isGoal();
	}

	private List<TFDNode> getSubsumingKnownPathCompletion(List<TFDNode> path) throws InterruptedException {
		for (List<TFDNode> partialPath : completions.keySet()) {
			List<TFDNode> compl = completions.get(partialPath);
			if (compl.size() < path.size())
				continue;
			if (path.equals(compl))
				return compl;

			Map<ConstantParam, ConstantParam> map = new HashMap<>();
			boolean allUnifiable = true;
			for (int i = 0; i < path.size(); i++) {
				TFDNode current = path.get(i);
				TFDNode partner = compl.get(i);

				/* compute substitutions of new vars */
				Collection<ConstantParam> varsInCurrent = new HashSet<>(current.getState().getConstantParams());
				for (Literal l : current.getRemainingTasks())
					varsInCurrent.addAll(l.getConstantParams());
				Collection<ConstantParam> varsInPartner = new HashSet<>(partner.getState().getConstantParams());
				for (Literal l : partner.getRemainingTasks())
					varsInPartner.addAll(l.getConstantParams());
				Collection<ConstantParam> unboundVars = SetUtil.difference(varsInCurrent, map.keySet());
				Collection<ConstantParam> possibleTargets = SetUtil.difference(varsInPartner, map.values());
				for (ConstantParam p : new ArrayList<>(unboundVars)) {
					if (possibleTargets.contains(p)) {
						map.put(p, p);
						unboundVars.remove(p);
						possibleTargets.remove(p);
					}
				}

				/* if the relation between vars in the nodes is completely known, we can easily decide whether they are unifiable */
				if (unboundVars.isEmpty()) {
					if (getRenamedState(current.getState(), map).equals(partner.getState())
							&& getRenamedRemainingList(current.getRemainingTasks(), map).equals(partner.getRemainingTasks()))
						continue;
					else {
						allUnifiable = false;
						break;
					}
				}

				/* otherwise, we must check possible mappings between the still unbound vars */
				boolean unified = false;
				Collection<Map<ConstantParam, ConstantParam>> possibleMappingCompletions = SetUtil.allMappings(unboundVars, possibleTargets, true, true, true);
				for (Map<ConstantParam, ConstantParam> mappingCompletion : possibleMappingCompletions) {

					/* first check whether the state is equal */
					Monom copy = getRenamedState(current.getState(), mappingCompletion);
					if (!copy.equals(partner.getState()))
						continue;

					/* if this is the case, check whether the remaining tasks are equal */
					List<Literal> copyOfTasks = getRenamedRemainingList(current.getRemainingTasks(), mappingCompletion);
					if (!copyOfTasks.equals(partner.getRemainingTasks()))
						continue;

					/* now we know that this node can be unified. We add the respective map and quit the current node pair */
					map.putAll(mappingCompletion);
					unified = true;
					break;

				}
				if (!unified) {
					allUnifiable = false;
					break;
				}
			}

			/* if all nodes were unifiable, return this path */
			if (allUnifiable)
				return partialPath;
		}
		return null;

	}

	private void postSolution(List<TFDNode> solution) {
		if (postedSolutions.contains(solution))
			throw new IllegalArgumentException("Solution " + solution.toString() + " already posted!");
		postedSolutions.add(solution);
		int numberOfComputedFValues = pipelineScores.size();
		List<CEOCAction> plan = CEOCSTNUtil.extractPlanFromSolutionPath(solution);
		MLPipeline pl = MLUtil.extractPipelineFromPlan(plan);
		MLPipelineSolutionAnnotation<TFDNode, Integer> annotation = new MLPipelineSolutionAnnotation<TFDNode, Integer>() {

			@Override
			public Integer f() {
				return pipelineScores.get(plan);
			}

			@Override
			public int getTimeForFComputation() {
				return pipelineScoreTimes.get(plan);
			}

			@Override
			public int getTimeUntilSolutionWasFound() {
				return (int) (System.currentTimeMillis() - timestampOfFirstEvaluation);
			}

			@Override
			public int getGenerationNumberOfGoalNode() {
				return numberOfComputedFValues;
			}

			@Override
			public boolean isTunedSolution() {
				return solution.stream().filter(a -> a.getAppliedAction() != null).filter(a -> a.getAppliedAction().getOperation().getName().contains("setOptions")).findAny()
						.isPresent();
			}
			
			@Override
			public MLPipeline getPipeline() {
				return pl;
			}
		};
		eventBus.post(new SolutionFoundEvent<TFDNode, Integer>(solution, annotation));
	}

	private Monom getRenamedState(Monom state, Map<ConstantParam, ConstantParam> map) {
		Monom copy = new Monom(state, map);
		return copy;
	}

	private List<Literal> getRenamedRemainingList(List<Literal> remainingList, Map<ConstantParam, ConstantParam> map) {
		List<Literal> copyOfTasks = new ArrayList<>();
		for (Literal l : remainingList) {
			copyOfTasks.add(new Literal(l, map));
		}
		return copyOfTasks;
	}

	@Override
	public void setData(Instances data) {
		this.evaluator.setData(data);
		this.dataSet = true;
	}

	@Override
	public void setGenerator(GraphGenerator<TFDNode, String> generator) {
		this.generator = generator;
	}

	@Override
	public SolutionEventBus<TFDNode> getSolutionEventBus() {
		return this.eventBus;
	}

	@Override
	public void cancel() {
		logger.info("Receive cancel signal.");
		this.evaluator.cancel();
	}
}
