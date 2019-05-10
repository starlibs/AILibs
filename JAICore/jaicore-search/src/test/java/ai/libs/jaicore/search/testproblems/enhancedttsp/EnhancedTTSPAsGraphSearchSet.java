package ai.libs.jaicore.search.testproblems.enhancedttsp;

import ai.libs.jaicore.basic.algorithm.ReductionBasedAlgorithmTestProblemSet;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSPNode;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSPProblemSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class EnhancedTTSPAsGraphSearchSet
extends ReductionBasedAlgorithmTestProblemSet<GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double>, SearchGraphPath<EnhancedTTSPNode, String>, EnhancedTTSP, ShortList> {

	public EnhancedTTSPAsGraphSearchSet() {
		super("Enhanced TTSP as graph search", new EnhancedTTSPProblemSet(), new EnhancedTTSPToGraphSearchReducer());
	}
}
