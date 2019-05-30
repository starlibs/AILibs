package jaicore.search.testproblems.enhancedttsp;

import it.unimi.dsi.fastutil.shorts.ShortList;
import jaicore.basic.algorithm.ReductionBasedAlgorithmTestProblemSet;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.testproblems.enhancedttsp.EnhancedTTSP;
import jaicore.testproblems.enhancedttsp.EnhancedTTSPNode;
import jaicore.testproblems.enhancedttsp.EnhancedTTSPProblemSet;

public class EnhancedTTSPAsGraphSearchSet
extends ReductionBasedAlgorithmTestProblemSet<GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double>, SearchGraphPath<EnhancedTTSPNode, String>, EnhancedTTSP, ShortList> {

	public EnhancedTTSPAsGraphSearchSet() {
		super("Enhanced TTSP as graph search", new EnhancedTTSPProblemSet(), new EnhancedTTSPToGraphSearchReducer());
	}
}
