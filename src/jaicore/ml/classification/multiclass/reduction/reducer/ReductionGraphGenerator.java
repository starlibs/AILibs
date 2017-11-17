package jaicore.ml.classification.multiclass.reduction.reducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import jaicore.ml.WekaUtil;
import jaicore.ml.classification.multiclass.reduction.EMCNodeType;
import jaicore.ml.classification.multiclass.reduction.splitters.ISplitter;
import jaicore.ml.classification.multiclass.reduction.splitters.RPNDSplitter;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ReductionGraphGenerator implements GraphGenerator<RestProblem, Decision> {

	private final Random rand;
	private final Instances data;

	public ReductionGraphGenerator(Random rand, Instances data) {
		super();
		this.rand = rand;
		this.data = data;
	}

	@Override
	public RootGenerator<RestProblem> getRootGenerator() {
		return () -> {
			Collection<RestProblem> roots = new ArrayList<>();
			RestProblem root = new RestProblem(null);
			root.add(new HashSet<>(WekaUtil.getClassesActuallyContainedInDataset(data)));
			roots.add(root);
			return roots;
		};
	}

	@Override
	public SuccessorGenerator<RestProblem, Decision> getSuccessorGenerator() {
		return n -> {
			List<NodeExpansionDescription<RestProblem, Decision>> restProblems = new ArrayList<>();
			try {
				List<String> set = new ArrayList<>(n.getPoint().get(0));
				if (set.size() < 2)
					throw new UnsupportedOperationException("Cannot create successor where rest problem consists of only one class.");

				/* add remaining open problems to node */
				List<Set<String>> remainingProblems = new ArrayList<>();
				for (int j = 1; j < n.getPoint().size(); j++)
					remainingProblems.add(n.getPoint().get(j));

				/* iterate over all considered classifiers */
				String[] portfolio = new String[] { "weka.classifiers.trees.RandomForest", "weka.classifiers.functions.SMO", "weka.classifiers.lazy.IBk" };
				for (String classifier : portfolio) {

					/* add the simplest option, namely to solve the nodes at once */
					for (EMCNodeType nodeType : EMCNodeType.values()) {
						if (nodeType == EMCNodeType.MERGE)
							continue;
						if (data.classAttribute().numValues() > 12 && data.size() > 1000) {
							if (nodeType == EMCNodeType.ALLPAIRS) {
								System.out.println("Skipping " + nodeType + " with " + classifier + " due to complexity constraints.");
								continue;
							}
						}
						RestProblem rp = new RestProblem(new Decision(null, null, nodeType, AbstractClassifier.forName(classifier, null)));
						rp.addAll(remainingProblems);
						restProblems.add(new NodeExpansionDescription<>(n.getPoint(), rp, rp.getEdgeToParent(), NodeType.OR));
					}

					/* now go for splits (here we always apply direct) */
					List<ISplitter> splitters = new ArrayList<>();
					for (int i = 0; i < 1; i++) {
						splitters.add(new RPNDSplitter(data, rand));
						// splitters.add(new GreedySplitter(train));
					}
					for (ISplitter splitter : splitters) {
						Classifier c = AbstractClassifier.forName(classifier, null);
						assert c != null : "No classifier selected!";
						Collection<Collection<String>> split = splitter.split(set, c);
						Iterator<Collection<String>> iterator = split.iterator();

						Set<String> c1 = new HashSet<>(iterator.next());
						Set<String> c2 = new HashSet<>(iterator.next());
						RestProblem rp = new RestProblem(new Decision(c1, c2, EMCNodeType.DIRECT, c));
						if (c1.size() > 1)
							rp.add(c1);
						if (c2.size() > 1)
							rp.add(c2);
						rp.addAll(remainingProblems);

						/* add rest problem */
						restProblems.add(new NodeExpansionDescription<>(n.getPoint(), rp, rp.getEdgeToParent(), NodeType.OR));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return restProblems;
		};
	}

	@Override
	public NodeGoalTester<RestProblem> getGoalTester() {
		return n -> {
			for (Set<String> open : n) {
				if (open.size() > 1)
					return false;
			}
			return true;
		};
	}
}
