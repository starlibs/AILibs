package ai.libs.jaicore.ml.weka.classification.learner.reduction.reducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.classification.learner.reduction.EMCNodeType;
import ai.libs.jaicore.ml.weka.classification.learner.reduction.splitter.ISplitter;
import ai.libs.jaicore.ml.weka.classification.learner.reduction.splitter.RPNDSplitter;
import ai.libs.jaicore.search.model.NodeExpansionDescription;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ReductionGraphGenerator implements IGraphGenerator<RestProblem, Decision> {

	private final Logger logger = LoggerFactory.getLogger(ReductionGraphGenerator.class);
	private final Random rand;
	private final Instances data;

	public ReductionGraphGenerator(final Random rand, final Instances data) {
		super();
		this.rand = rand;
		this.data = data;
	}

	@Override
	public ISingleRootGenerator<RestProblem> getRootGenerator() {
		return () -> {
			RestProblem root = new RestProblem(null);
			root.add(new HashSet<>(WekaUtil.getClassesActuallyContainedInDataset(this.data)));
			return root;
		};
	}

	@Override
	public ISuccessorGenerator<RestProblem, Decision> getSuccessorGenerator() {
		return n -> {
			List<INewNodeDescription<RestProblem, Decision>> restProblems = new ArrayList<>();
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
						restProblems.add(new NodeExpansionDescription<>(rp, rp.getEdgeToParent()));
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
						restProblems.add(new NodeExpansionDescription<>(rp, rp.getEdgeToParent()));
					}
				}
			} catch (Exception e) {
				this.logger.error("Encountered error: {}", e);
			}
			return restProblems;
		};
	}
}
