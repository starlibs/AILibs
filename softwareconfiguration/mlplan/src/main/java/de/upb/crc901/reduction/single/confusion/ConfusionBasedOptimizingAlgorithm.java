//package de.upb.crc901.reduction.single.confusion;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
//import jaicore.graphvisualizer.TooltipGenerator;
//import jaicore.ml.WekaUtil;
//import jaicore.ml.classification.multiclass.reduction.MCTreeNodeReD;
//import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
//import jaicore.ml.evaluation.MulticlassEvaluator;
//import jaicore.search.algorithms.standard.bestfirst.BestFirst;
//import jaicore.search.core.interfaces.GraphGenerator;
//import jaicore.search.model.travesaltree.Node;
//import jaicore.search.model.travesaltree.NodeExpansionDescription;
//import jaicore.search.model.travesaltree.NodeType;
//import jaicore.search.structure.graphgenerator.NodeGoalTester;
//import jaicore.search.structure.graphgenerator.SingleRootGenerator;
//import jaicore.search.structure.graphgenerator.SuccessorGenerator;
//import weka.classifiers.AbstractClassifier;
//import weka.classifiers.Classifier;
//import weka.classifiers.Evaluation;
//import weka.core.Instance;
//import weka.core.Instances;
//
//public class ConfusionBasedOptimizingAlgorithm {
//
//	private class BFNode {
//		private String leftClassifier;
//		private String rightClassifier;
//		private String innerClassifier;
//		Collection<Integer> itemsOnLeft;
//		Collection<Integer> itemsOnRight;
//
//		public BFNode(String leftClassifier, String rightClassifier, String innerClassifier, Collection<Integer> itemsOnLeft, Collection<Integer> itemsOnRight) {
//			super();
//			this.leftClassifier = leftClassifier;
//			this.rightClassifier = rightClassifier;
//			this.innerClassifier = innerClassifier;
//			this.itemsOnLeft = itemsOnLeft;
//			this.itemsOnRight = itemsOnRight;
//		}
//
//		@Override
//		public String toString() {
//			return "BFNode [leftClassifier=" + leftClassifier + ", rightClassifier=" + rightClassifier + ", innerClassifier=" + innerClassifier + ", itemsOnLeft=" + itemsOnLeft
//					+ ", itemsOnRight=" + itemsOnRight + "]";
//		}
//	}
//
//	private class BFGraphGenerator implements GraphGenerator<BFNode, String> {
//
//		private Collection<String> classifiers;
//		private int numClasses;
//
//		public BFGraphGenerator(Collection<String> classifiers, int numClasses) {
//			super();
//			this.classifiers = classifiers;
//			this.numClasses = numClasses;
//		}
//
//		@Override
//		public SingleRootGenerator<BFNode> getRootGenerator() {
//			return () -> new BFNode(null, null, null, null, null);
//		}
//
//		@Override
//		public SuccessorGenerator<BFNode, String> getSuccessorGenerator() {
//			return n -> {
//				List<NodeExpansionDescription<BFNode, String>> successors = new ArrayList<>();
//				if (n.leftClassifier == null) {
//					for (String classifier : classifiers) {
//						successors.add(new NodeExpansionDescription<>(n, new BFNode(classifier, null, null, null, null), "", NodeType.OR));
//					}
//				} else if (n.rightClassifier == null) {
//					for (String classifier : classifiers) {
//						successors.add(new NodeExpansionDescription<>(n, new BFNode(n.leftClassifier, classifier, null, null, null), "", NodeType.OR));
//					}
//				} else if (n.itemsOnLeft == null) {
//					Set<Integer> child = new HashSet<>();
//					child.add(0);
//					successors.add(new NodeExpansionDescription<>(n, new BFNode(n.leftClassifier, n.rightClassifier, null, child, new HashSet<>()), "", NodeType.OR));
//				} else if (n.itemsOnLeft.size() + n.itemsOnRight.size() != numClasses) {
//					int next = n.itemsOnLeft.size() + n.itemsOnRight.size();
//					Set<Integer> aLeft = new HashSet<>(n.itemsOnLeft);
//					aLeft.add(next);
//					Set<Integer> aRight = new HashSet<>(n.itemsOnRight);
//					aRight.add(next);
//					if (n.itemsOnLeft.size() + n.itemsOnRight.size() < numClasses - 1 || !n.itemsOnRight.isEmpty())
//						successors.add(new NodeExpansionDescription<>(n, new BFNode(n.leftClassifier, n.rightClassifier, null, aLeft, n.itemsOnRight), "", NodeType.OR));
//					successors.add(new NodeExpansionDescription<>(n, new BFNode(n.leftClassifier, n.rightClassifier, null, n.itemsOnLeft, aRight), "", NodeType.OR));
//
//				} else {
//					for (String classifier : classifiers) {
//						successors.add(
//								new NodeExpansionDescription<>(n, new BFNode(n.leftClassifier, n.rightClassifier, classifier, n.itemsOnLeft, n.itemsOnRight), "", NodeType.OR));
//					}
//				}
//				return successors;
//			};
//		}
//
//		@Override
//		public NodeGoalTester<BFNode> getGoalTester() {
//			return n -> n.innerClassifier != null;
//		}
//
//		@Override
//		public boolean isSelfContained() {
//			return true;
//		}
//
//		@Override
//		public void setNodeNumbering(boolean nodenumbering) {
//			throw new UnsupportedOperationException("No node numbering supported.");
//		}
//	}
//
//	public MCTreeNodeReD buildClassifier(Instances data, final Collection<String> pClassifierNames) throws Exception {
//
//		System.out.println("START: " + data.relationName());
//		int seed = 0;
//
//		Map<String, double[][]> confusionMatrices = new HashMap<>();
//		int numClasses = data.numClasses();
//		System.out.println("Computing confusion matrices ...");
//		int numOfEvaluatedInstancesTmp = 0;
//		Map<Integer,Integer> numOfEvaluatedInstancesPerClass = new HashMap<>();
//		for (int i = 0; i < 10; i++) {
//			List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(i), .7f);
//			for (Instance inst : split.get(1)) {
//				int cId = (int)inst.classValue();
//				int cur = numOfEvaluatedInstancesPerClass.containsKey(cId) ? numOfEvaluatedInstancesPerClass.get(cId) : 0;
//				numOfEvaluatedInstancesPerClass.put(cId, cur + 1);
//			}
//
//			/* compute confusion matrices for each classifier */
//			for (String classifier : pClassifierNames) {
//				try {
//					Classifier c = AbstractClassifier.forName(classifier, null);
//					c.buildClassifier(split.get(0));
//					Evaluation eval = new Evaluation(split.get(0));
//					eval.evaluateModel(c, split.get(1));
//					numOfEvaluatedInstancesTmp += split.get(1).size();
//					if (!confusionMatrices.containsKey(classifier))
//						confusionMatrices.put(classifier, new double[numClasses][numClasses]);
//
//					double[][] currentCM = confusionMatrices.get(classifier);
//					double[][] addedCM = eval.confusionMatrix();
//
//					for (int j = 0; j < numClasses; j++) {
//						for (int k = 0; k < numClasses; k++) {
//							currentCM[j][k] += addedCM[j][k];
//						}
//					}
//				} catch (Throwable e) {
//					// System.err.println(e.getClass().getName() + ": " + e.getMessage());
//				}
//			}
//		}
//		final int numOfEvaluatedInstances = numOfEvaluatedInstancesTmp;
//		System.out.println(numOfEvaluatedInstancesPerClass);
//
//		Set<Integer> items = new HashSet<>();
//		for (int i = 0; i < numClasses; i++)
//			items.add(i);
//
//		BestFirst<BFNode, String, Double> search = new BestFirst<>(new BFGraphGenerator(pClassifierNames, numClasses), n -> {
//
//			BFNode p = n.getPoint();
//			if (p.itemsOnRight == null)
//				return 0.0;
//
//			/* compute CURRENT cost of the two separate problems */
//			double[][] cm1 = confusionMatrices.get(p.leftClassifier);
//			double[][] cm2 = confusionMatrices.get(p.rightClassifier);
////			double p1 = getExpectedMisclassificationRate(p.itemsOnLeft, cm1);
////			double p2 = getExpectedMisclassificationRate(p.itemsOnRight, cm2);
//			
//			/* if the inner classifier has been set, train the respective model and check the */
//			if (p.innerClassifier != null) {
//
////				Map<String, String> classMap = new HashMap<>();
////				for (int i1 : p.itemsOnLeft) {
////					classMap.put(data.classAttribute().value(i1), "l");
////				}
////				for (int i2 : p.itemsOnRight) {
////					classMap.put(data.classAttribute().value(i2), "r");
////				}
//
//				/* verify low confusions */
//				// Instances leftData = new Instances(data);
//				// System.out.println(p.itemsOnLeft);
//				// leftData.removeIf(i -> !p.itemsOnLeft.contains((int)i.classValue()));
//				// System.out.println(leftData.size());
//				// Classifier cLeft = AbstractClassifier.forName(p.leftClassifier, null);
//				// List<Instances> split = WekaUtil.getStratifiedSplit(leftData, new Random(123), .7f);
//				// cLeft.buildClassifier(split.get(0));
//				// Evaluation lEval = new Evaluation(leftData);
//				// lEval.evaluateModel(cLeft, split.get(1));
//				// double errorRate = lEval.errorRate();
//				// System.out.println("Left: " + errorRate);
//
//				/* train inner classifier and compute estimate for overall error */
//				try {
////					Instances newData = WekaUtil.getRefactoredInstances(data, classMap);
////					List<Instances> binaryInnerSplit = WekaUtil.getStratifiedSplit(newData, new Random(seed), .7f);
////					Classifier c = AbstractClassifier.forName(p.innerClassifier, null);
////					c.buildClassifier(binaryInnerSplit.get(0));
////					Evaluation eval = new Evaluation(newData);
////					eval.evaluateModel(c, binaryInnerSplit.get(1));
////					double[][] cm = eval.confusionMatrix();
////					double pErrorLeft = getProbabilityToMisclassifyObjectOfClass(cm, 0);
////					double pErrorRight = getProbabilityToMisclassifyObjectOfClass(cm, 1);
////					double shareOfItemsForLeft = newData.stream().filter(i -> i.classValue() == 0).count() * 1f / newData.size();
////					double shareOfItemsForRight = newData.stream().filter(i -> i.classValue() == 1).count() * 1f / newData.size();
////					double overallForLeftItems = pErrorLeft + (1 - pErrorLeft) * p1;
////					double overallForRightItems = pErrorRight + (1 - pErrorRight) * p2;
////					double overallExpectedError = shareOfItemsForLeft * overallForLeftItems + shareOfItemsForRight * overallForRightItems;
//					MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(seed)), 1, data, .7f);
//					MCTreeNodeReD tree = getMCTreeNode(p, data);
//					return mccv.evaluate(tree);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//			/* compute lower bound on the errors that will be made by the left and the right classifier respectively */
//			int currentMistakesLeft = getMistakesOfCluster(p.itemsOnLeft, cm1);
//			int currentMistakesRight = getMistakesOfCluster(p.itemsOnRight, cm2);
//			int lowerBoundForMistakesLeft = currentMistakesLeft;
//			int lowerBoundForMistakesRight = currentMistakesRight;
//			Collection<Integer> extendedLeft = new HashSet<>(p.itemsOnLeft);
//			Collection<Integer> extendedRight = new HashSet<>(p.itemsOnRight);
//			for (int i = 0; i < numClasses; i++) {
//				if (p.itemsOnLeft.contains(i) || p.itemsOnRight.contains(i))
//					continue;
//				Collection<Integer> aLeft = new HashSet<>(p.itemsOnLeft);
//				aLeft.add(i);
//				double pm1 = getMistakesOfCluster(aLeft, cm1);
//				Collection<Integer> aRight = new HashSet<>(p.itemsOnRight);
//				aRight.add(i);
//				double pm2 = getMistakesOfCluster(aRight, cm2);
//				if (pm1 < pm2) {
//					extendedLeft.add(i);
//					lowerBoundForMistakesLeft += (pm1 - currentMistakesLeft);
//				}
//				else {
//					extendedRight.add(i);
//					lowerBoundForMistakesRight += (pm2 - currentMistakesRight);
//				}
//			}
//			double lowerBoundForErrorLeft = lowerBoundForMistakesLeft * 1f / getNumberOfEvaluatedInstancesForCluster(extendedLeft, numOfEvaluatedInstancesPerClass);
//			double lowerBoundForErrorRight = lowerBoundForMistakesLeft * 1f / getNumberOfEvaluatedInstancesForCluster(extendedRight, numOfEvaluatedInstancesPerClass);
//
//			/* compute lower bound on the error that will be made by the parent */
//			/* to this end, first create a binary problem ONLY of the currently fixed classes */
//			double minimumError = Double.MAX_VALUE;
//			if (!p.itemsOnLeft.isEmpty() && !p.itemsOnRight.isEmpty()) {
//				Map<String, String> classMap = new HashMap<>();
//				for (int i1 : p.itemsOnLeft) {
//					classMap.put(data.classAttribute().value(i1), "l");
//				}
//				for (int i2 : p.itemsOnRight) {
//					classMap.put(data.classAttribute().value(i2), "r");
//				}
//				Instances newData = WekaUtil.getRefactoredInstances(data, classMap);
//				List<Instances> binaryInnerSplit = WekaUtil.getStratifiedSplit(newData, new Random(seed), .7f);
//				for (String classifier : pClassifierNames) {
//
//					/* train inner classifier and compute estimate for overall error */
//					try {
//						Classifier c = AbstractClassifier.forName(classifier, null);
//						c.buildClassifier(binaryInnerSplit.get(0));
//						Evaluation eval = new Evaluation(newData);
//						eval.evaluateModel(c, binaryInnerSplit.get(1));
//						double[][] cm = eval.confusionMatrix();
//						double pErrorLeft = getProbabilityToMisclassifyObjectOfClass(cm, 0);
//						double pErrorRight = getProbabilityToMisclassifyObjectOfClass(cm, 1);
//						double shareOfItemsForLeft = newData.stream().filter(i -> i.classValue() == 0).count() * 1f / newData.size();
//						double shareOfItemsForRight = newData.stream().filter(i -> i.classValue() == 1).count() * 1f / newData.size();
//						double overallForLeftItems = pErrorLeft + (1 - pErrorLeft) * lowerBoundForErrorLeft;
//						double overallForRightItems = pErrorRight + (1 - pErrorRight) * lowerBoundForErrorRight;
//						double lowerBoundForOverallExpectedError = shareOfItemsForLeft * overallForLeftItems + shareOfItemsForRight * overallForRightItems;
//						if (lowerBoundForOverallExpectedError < minimumError) {
//							minimumError = lowerBoundForOverallExpectedError;
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			/* now assume that the parent makes no mistake and compute the resulting performance */
//			else
//				minimumError = (double) (lowerBoundForMistakesLeft + lowerBoundForMistakesRight) / numOfEvaluatedInstances;
//
//
//			return minimumError;
//
//		});
//
//		new SimpleGraphVisualizationWindow<>(search).getPanel().setTooltipGenerator(new TooltipGenerator<Object>() {
//
//			@Override
//			public String getTooltip(Object node) {
//				@SuppressWarnings("unchecked")
//				Node<BFNode, Double> n = (Node<BFNode, Double>) node;
//				return n.getPoint().toString() + "<br />" + n.getInternalLabel();
//			}
//		});
//		;
//		List<BFNode> solution = search.nextSolutionThatDominatesOpen();
//		BFNode goalNode = solution.get(solution.size() - 1);
//		System.out.println("Believed error is: " + search.getFOfReturnedSolution(solution));
//
//		MCTreeNodeReD tree = getMCTreeNode(goalNode, data);
//		tree.buildClassifier(data);
//		return tree;
//	}
//	
//	private MCTreeNodeReD getMCTreeNode(BFNode node, Instances data) throws Exception {
//		Collection<String> classesLeft = node.itemsOnLeft.stream().map(c -> data.classAttribute().value(c)).collect(Collectors.toList());
//		Collection<String> classesRight = node.itemsOnRight.stream().map(c -> data.classAttribute().value(c)).collect(Collectors.toList());
//		MCTreeNodeReD tree = new MCTreeNodeReD(node.innerClassifier, classesLeft, node.leftClassifier, classesRight, node.rightClassifier);
//		return tree;
//	}
//	
//	private int getNumberOfEvaluatedInstancesForCluster(Collection<Integer> cluster, Map<Integer,Integer> counter) {
//		int sum = 0;
//		for (int i : cluster)
//			sum += counter.get(i);
//		return sum;
//	}
//
//	private double getProbabilityToMisclassifyObjectOfClass(double[][] cm, int c) {
//		int occurrencesOfC = 0;
//		int mistakes = 0;
//		for (int i = 0; i < cm.length; i++) {
//			occurrencesOfC += cm[c][i];
//			if (c != i)
//				mistakes += cm[c][i];
//		}
//		return mistakes * 1f / occurrencesOfC;
//	}
//
//	private int getMistakesOfCluster(Collection<Integer> cluster, double[][] confusionMatrix) {
//		int sum = 0;
//		for (int i : cluster) {
//			for (int j : cluster) {
//				if (i != j)
//					sum += confusionMatrix[i][j];
//			}
//		}
//		return sum;
//	}
//}
