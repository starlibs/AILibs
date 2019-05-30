package jaicore.ml.classification.multiclass.reduction.reducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jaicore.ml.WekaUtil;
import jaicore.ml.classification.multiclass.reduction.EMCNodeType;
import jaicore.ml.classification.multiclass.reduction.splitters.ISplitter;
import jaicore.ml.classification.multiclass.reduction.splitters.RPNDSplitter;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ReductionGraphGenerator implements GraphGenerator<RestProblem, Decision> {

	private final Random rand;
	private final Instances data;

	public ReductionGraphGenerator(final Random rand, final Instances data) {
		super();
		this.rand = rand;
		this.data = data;
	}

	@Override
	public SingleRootGenerator<RestProblem> getRootGenerator() {
		return () -> {
			RestProblem root = new RestProblem(null);
			root.add(new HashSet<>(WekaUtil.getClassesActuallyContainedInDataset(this.data)));
			return root;
		};
	}

	@Override
	public SuccessorGenerator<RestProblem, Decision> getSuccessorGenerator() {
		return n -> {
			List<NodeExpansionDescription<RestProblem, Decision>> restProblems = new ArrayList<>();
			try {
				List<String> set = new ArrayList<>(n.get(0));
				if (set.size() < 2) {
					throw new UnsupportedOperationException("Cannot create successor where rest problem consists of only one class.");
				}

				/* add remaining open problems to node */
				List<Set<String>> remainingProblems = new ArrayList<>();
				for (int j = 1; j < n.size(); j++) {
					remainingProblems.add(n.get(j));
				}

				/* iterate over all considered classifiers */
				String[] portfolio = new String[] { "weka.classifiers.trees.RandomForest", "weka.classifiers.functions.SMO", "weka.classifiers.lazy.IBk" };
				for (String classifier : portfolio) {

					/* add the simplest option, namely to solve the nodes at once */
					for (EMCNodeType nodeType : EMCNodeType.values()) {
						if (nodeType == EMCNodeType.MERGE) {
							continue;
						}
						if (this.data.classAttribute().numValues() > 12 && this.data.size() > 1000 && nodeType == EMCNodeType.ALLPAIRS) {
							continue;
						}
						RestProblem rp = new RestProblem(new Decision(null, null, nodeType, AbstractClassifier.forName(classifier, null)));
						rp.addAll(remainingProblems);
						restProblems.add(new NodeExpansionDescription<>(n, rp, rp.getEdgeToParent(), NodeType.OR));
					}

					/* now go for splits (here we always apply direct) */
					List<ISplitter> splitters = new ArrayList<>();
					Map<ISplitter,Classifier> classifiers = new HashMap<>();
					for (int i = 0; i < 1; i++) {
						Classifier c = AbstractClassifier.forName(classifier, null);
						ISplitter splitter = new RPNDSplitter(this.rand, c);
						classifiers.put(splitter,c);
						splitters.add(splitter);
					}
					for (ISplitter splitter : splitters) {
						Collection<Collection<String>> split = splitter.split(this.data);
						Iterator<Collection<String>> iterator = split.iterator();

						Set<String> c1 = new HashSet<>(iterator.next());
						Set<String> c2 = new HashSet<>(iterator.next());
						RestProblem rp = new RestProblem(new Decision(c1, c2, EMCNodeType.DIRECT, classifiers.get(splitter)));
						if (c1.size() > 1) {
							rp.add(c1);
						}
						if (c2.size() > 1) {
							rp.add(c2);
						}
						rp.addAll(remainingProblems);

						/* add rest problem */
						restProblems.add(new NodeExpansionDescription<>(n, rp, rp.getEdgeToParent(), NodeType.OR));
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
				if (open.size() > 1) {
					return false;
				}
			}
			return true;
		};
	}

	@Override
	public boolean isSelfContained() {
		return false;
	}

	@Override
	public void setNodeNumbering(final boolean nodenumbering) {
		/* do nothing, irrelevant */
	}
}
