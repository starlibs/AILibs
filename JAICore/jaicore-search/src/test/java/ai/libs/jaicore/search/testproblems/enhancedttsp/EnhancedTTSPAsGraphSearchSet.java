package ai.libs.jaicore.search.testproblems.enhancedttsp;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPath;

import ai.libs.jaicore.basic.algorithm.ReductionBasedAlgorithmTestProblemSet;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSPNode;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSPProblemSet;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPAsGraphSearchSet
extends ReductionBasedAlgorithmTestProblemSet<GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double>, IPath<EnhancedTTSPNode, String>, EnhancedTTSP, ShortList> {

	public EnhancedTTSPAsGraphSearchSet() {
		super("Enhanced TTSP as graph search", new EnhancedTTSPProblemSet(), new EnhancedTTSPToGraphSearchReducer());
	}
}
