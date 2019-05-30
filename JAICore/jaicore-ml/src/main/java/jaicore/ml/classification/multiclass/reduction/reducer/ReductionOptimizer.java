package jaicore.ml.classification.multiclass.reduction.reducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.logging.LoggerUtil;
import jaicore.ml.WekaUtil;
import jaicore.ml.classification.multiclass.reduction.EMCNodeType;
import jaicore.ml.classification.multiclass.reduction.MCTreeNode;
import jaicore.ml.classification.multiclass.reduction.MCTreeNodeLeaf;
import jaicore.search.algorithms.standard.bestfirst.BestFirstEpsilon;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.OneR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class ReductionOptimizer implements Classifier {

	private final long seed;
	private MCTreeNode root;
	private transient Logger logger = LoggerFactory.getLogger(ReductionOptimizer.class);

	public ReductionOptimizer(final long seed) {
		super();
		this.seed = seed;
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		List<Instances> dataSplit = WekaUtil.getStratifiedSplit(data, this.seed, .6f);
		Instances train = dataSplit.get(0);
		BestFirstEpsilon<RestProblem, Decision, Double> search = new BestFirstEpsilon<>(new GraphSearchWithSubpathEvaluationsInput<>(new ReductionGraphGenerator(new Random(this.seed), train), n -> this.getLossForClassifier(this.getTreeFromSolution(n.externalPath(), data, false), data) * 1.0), n -> n.path().size() * -1.0
				, 0.1, false);

		/* get best 20 solutions */
		int i = 0;
		Collection<EvaluatedSearchGraphPath<RestProblem,Decision,Double>> solutions = new ArrayList<>();
		EvaluatedSearchGraphPath<RestProblem,Decision,Double> solution;
		while ((solution = search.nextSolutionCandidate()) != null) {
			solutions.add(solution);
			if (i++ > 100) {
				break;
			}
		}

		/* select */
		Optional<EvaluatedSearchGraphPath<RestProblem,Decision,Double>> bestSolution = solutions.stream().min((s1, s2) -> s1.getScore().compareTo(s2.getScore()));
		if (!bestSolution.isPresent()) {
			this.logger.error("No solution found");
			return;
		}
		this.root = this.getTreeFromSolution(bestSolution.get().getNodes(), data, true);
		this.root.buildClassifier(data);
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		return this.root.classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		return this.root.distributionForInstance(instance);
	}

	@Override
	public Capabilities getCapabilities() {
		return null;
	}

	private void completeTree(final MCTreeNode tree) {

		/* if the tree is not ready yet, complete it. The completion strategy is now just to set the node to "direct" with a random forest */
		if (!tree.isCompletelyConfigured()) {
			for (MCTreeNode node : tree) {
				if (!node.getChildren().isEmpty()) {
					continue;
				}
				if (node.getContainedClasses().size() == 1) {
					continue;
				}
				node.setNodeType(EMCNodeType.DIRECT);
				node.setBaseClassifier(new OneR());
				for (int openClass : node.getContainedClasses()) {
					try {
						node.addChild(new MCTreeNodeLeaf(openClass));
					} catch (Exception e) {
						this.logger.error(LoggerUtil.getExceptionInfo(e));
					}
				}
			}
		}
	}

	private int getLossForClassifier(final MCTreeNode tree, final Instances data) {

		this.completeTree(tree);

		synchronized (this) {
			/* now eval the tree */
			try {
				DescriptiveStatistics stats = new DescriptiveStatistics();
				for (int i = 0; i < 2; i++) {
					List<Instances> split = (WekaUtil.getStratifiedSplit(data, this.seed + i, .6f));
					tree.buildClassifier(split.get(0));

					Evaluation eval = new Evaluation(data);
					eval.evaluateModel(tree, split.get(1));
					stats.addValue(eval.pctIncorrect());
				}
				return (int) Math.round((stats.getMean() * 100));

			} catch (Exception e) {
				this.logger.error(LoggerUtil.getExceptionInfo(e));
				return Integer.MAX_VALUE;
			}
		}

	}

	private MCTreeNode getTreeFromSolution(final List<RestProblem> solution, final Instances data, final boolean mustBeComplete) {
		List<Decision> decisions = solution.stream().filter(n -> n.getEdgeToParent() != null).map(RestProblem::getEdgeToParent).collect(Collectors.toList());
		Deque<MCTreeNode> open = new LinkedList<>();
		Attribute classAttribute = data.classAttribute();
		MCTreeNode localRoot = new MCTreeNode(IntStream.range(0, classAttribute.numValues()).mapToObj(i -> i).collect(Collectors.toList()));
		open.addFirst(localRoot);
		for (Decision decision : decisions) {
			MCTreeNode nodeToRefine = open.removeFirst(); // by construction of the search space, this node should belong to the decision
			if (nodeToRefine == null) {
				throw new IllegalStateException("No node to apply the decision to! Apparently, there are more decisions for nodes than there are inner nodes.");
			}

			/* insert decision to the node */
			nodeToRefine.setNodeType(decision.getClassificationType());
			nodeToRefine.setBaseClassifier(decision.getBaseClassifier());

			boolean isCutOff = !(decision.getLft() != null && decision.getRgt() != null);

			if (isCutOff) {
				for (Integer c : nodeToRefine.getContainedClasses()) {
					try {
						nodeToRefine.addChild(new MCTreeNodeLeaf(c));
					} catch (Exception e) {
						this.logger.error(LoggerUtil.getExceptionInfo(e));
					}
				}
			} else {

				/* set left child */
				boolean addedLeftChild = false;
				List<String> classesLft = new ArrayList<>(decision.getLft());
				if (classesLft.size() == 1) {
					try {
						nodeToRefine.addChild(new MCTreeNodeLeaf(classAttribute.indexOfValue(classesLft.get(0))));
					} catch (Exception e) {
						this.logger.error(LoggerUtil.getExceptionInfo(e));
					}
				} else {
					MCTreeNode lft = new MCTreeNode(classesLft.stream().map(classAttribute::indexOfValue).collect(Collectors.toList()));
					nodeToRefine.addChild(lft);
					addedLeftChild = true;
					open.push(lft);
				}

				/* set right child */
				List<String> classesRgt = new ArrayList<>(decision.getRgt());
				if (classesRgt.size() == 1) {
					try {
						nodeToRefine.addChild(new MCTreeNodeLeaf(data.classAttribute().indexOfValue(classesRgt.get(0))));
					} catch (Exception e) {
						this.logger.error(LoggerUtil.getExceptionInfo(e));
					}
				} else {
					MCTreeNode rgt = new MCTreeNode(classesRgt.stream().map(classAttribute::indexOfValue).collect(Collectors.toList()));
					nodeToRefine.addChild(rgt);
					if (addedLeftChild) {
						MCTreeNode lft = open.pop();
						open.push(rgt);
						open.push(lft);
					} else {
						open.push(rgt);
					}
				}
			}
		}

		if (mustBeComplete && !open.isEmpty()) {
			throw new IllegalStateException("Not all nodes have been equipped with decisions!");
		}
		return localRoot;
	}
}