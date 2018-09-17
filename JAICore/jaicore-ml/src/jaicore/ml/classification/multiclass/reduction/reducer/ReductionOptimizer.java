package jaicore.ml.classification.multiclass.reduction.reducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jaicore.graphvisualizer.gui.VisualizationWindow;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import jaicore.basic.MathExt;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.ml.WekaUtil;
import jaicore.ml.classification.multiclass.reduction.EMCNodeType;
import jaicore.ml.classification.multiclass.reduction.MCTreeNode;
import jaicore.ml.classification.multiclass.reduction.MCTreeNodeLeaf;
import jaicore.search.algorithms.standard.bestfirst.BestFirstEpsilon;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;
import jaicore.search.model.travesaltree.Node;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.OneR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class ReductionOptimizer implements Classifier {

	private final Random rand;
	private MCTreeNode root;

	public ReductionOptimizer(Random rand) {
		super();
		this.rand = rand;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		List<Instances> dataSplit = WekaUtil.getStratifiedSplit(data, rand, .6f);
		Instances train = dataSplit.get(0);
		Instances validate = dataSplit.get(1);
		BestFirstEpsilon<RestProblem, Decision, Double> search = new BestFirstEpsilon<RestProblem, Decision, Double>(new GeneralEvaluatedTraversalTree<>(new ReductionGraphGenerator(rand, train), n -> getLossForClassifier(getTreeFromSolution(n.externalPath(), data, false), data) * 1.0), n -> n.path().size() * -1.0
		, 0.1, false);

		VisualizationWindow<Node<RestProblem, Double>,Decision> window = new VisualizationWindow<>(search);
		window.setTooltipGenerator(new TooltipGenerator<Node<RestProblem, Double>>() {

			@Override
			public String getTooltip(Node<RestProblem, Double> node) {
				return search.getFValue(node) + "<pre>" + getTreeFromSolution(node.externalPath(), data, false).toStringWithOffset() + "</pre>";
			}
		});

		/* get best 20 solutions */
		int i = 0;
		Collection<EvaluatedSearchGraphPath<RestProblem,Decision,Double>> solutions = new ArrayList<>();
		EvaluatedSearchGraphPath<RestProblem,Decision,Double> solution;
		while ((solution = search.nextSolution()) != null) {
			solutions.add(solution);
			if (i++ > 100)
				break;
		}
		System.out.println(solutions.size());

		/* select */
		EvaluatedSearchGraphPath<RestProblem,Decision,Double> bestSolution = solutions.stream().min((s1, s2) -> s1.getScore().compareTo(s2.getScore())).get();
		root = getTreeFromSolution(bestSolution.getNodes(), data, true);
		root.buildClassifier(data);
		System.out.println(root.toStringWithOffset());
		System.out.println(bestSolution.getScore());
	}

	@Override
	public double classifyInstance(Instance instance) throws Exception {
		return root.classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		return root.distributionForInstance(instance);
	}

	@Override
	public Capabilities getCapabilities() {
		return null;
	}

	private void completeTree(MCTreeNode tree) {

		/* if the tree is not ready yet, complete it. The completion strategy is now just to set the node to "direct" with a random forest */
		if (!tree.isCompletelyConfigured()) {
			for (MCTreeNode node : tree) {
				if (!node.getChildren().isEmpty())
					continue;
				if (node.getContainedClasses().size() == 1)
					continue;
				node.setNodeType(EMCNodeType.DIRECT);
				node.setBaseClassifier(new OneR());
				for (int openClass : node.getContainedClasses()) {
					try {
						node.addChild(new MCTreeNodeLeaf(openClass));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private int getLossForClassifier(MCTreeNode tree, Instances data) {

		completeTree(tree);

		synchronized (this) {
			/* now eval the tree */
			try {
				DescriptiveStatistics stats = new DescriptiveStatistics();
				for (int i = 0; i < 2; i++) {
					List<Instances> split = (WekaUtil.getStratifiedSplit(data, rand, .6f));
					tree.buildClassifier(split.get(0));

					Evaluation eval = new Evaluation(data);
					eval.evaluateModel(tree, split.get(1));
					stats.addValue(eval.pctIncorrect());
				}
				int score = (int) Math.round((stats.getMean() * 100));
				System.out.println(score);
				return score;

			} catch (Exception e) {
				e.printStackTrace();
				return Integer.MAX_VALUE;
			}
		}

	}

	private MCTreeNode getTreeFromSolution(List<RestProblem> solution, Instances data, boolean mustBeComplete) {
		List<Decision> decisions = solution.stream().filter(n -> n.getEdgeToParent() != null).map(n -> n.getEdgeToParent()).collect(Collectors.toList());
		Stack<MCTreeNode> open = new Stack<>();
		Attribute classAttribute = data.classAttribute();
		MCTreeNode root = new MCTreeNode(IntStream.range(0, classAttribute.numValues()).mapToObj(i -> i).collect(Collectors.toList()));
		open.push(root);
		for (Decision decision : decisions) {
			MCTreeNode nodeToRefine = open.pop(); // by construction of the search space, this node should belong to the decision
			if (nodeToRefine == null)
				throw new IllegalStateException("No node to apply the decision to! Apparently, there are more decisions for nodes than there are inner nodes.");

			/* insert decision to the node */
			nodeToRefine.setNodeType(decision.getClassificationType());
			nodeToRefine.setBaseClassifier(decision.getBaseClassifier());

			boolean isCutOff = !(decision.getLft() != null && decision.getRgt() != null);

			if (isCutOff) {
				for (Integer c : nodeToRefine.getContainedClasses()) {
					try {
						nodeToRefine.addChild(new MCTreeNodeLeaf(c));
					} catch (Exception e) {
						e.printStackTrace();
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
						e.printStackTrace();
					}
				} else {
					MCTreeNode lft = new MCTreeNode(classesLft.stream().map(c -> classAttribute.indexOfValue(c)).collect(Collectors.toList()));
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
						e.printStackTrace();
					}
				} else {
					MCTreeNode rgt = new MCTreeNode(classesRgt.stream().map(c -> classAttribute.indexOfValue(c)).collect(Collectors.toList()));
					nodeToRefine.addChild(rgt);
					if (addedLeftChild) {
						MCTreeNode lft = open.pop();
						open.push(rgt);
						open.push(lft);
					} else
						open.push(rgt);
				}
			}
		}

		if (mustBeComplete && !open.isEmpty())
			throw new IllegalStateException("Not all nodes have been equipped with decisions!");
		return root;
	}

	private double getAccuracy(Classifier c, Instances test) throws Exception {
		int mistakes = 0;
		for (Instance i : test) {
			if (c.classifyInstance(i) != i.classValue())
				mistakes++;
		}
		return MathExt.round(100 * (1 - mistakes * 1f / test.size()), 2);
	}
}
