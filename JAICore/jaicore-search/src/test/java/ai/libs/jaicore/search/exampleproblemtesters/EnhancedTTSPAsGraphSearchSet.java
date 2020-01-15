package ai.libs.jaicore.search.exampleproblemtesters;

import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.basic.algorithm.ReductionBasedAlgorithmTestProblemSet;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPState;
import ai.libs.jaicore.problemsets.enhancedttsp.EnhancedTTSPProblemSet;
import ai.libs.jaicore.search.exampleproblems.enhancedttsp.EnhancedTTSPToSimpleGraphSearchReducer;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPAsGraphSearchSet
extends ReductionBasedAlgorithmTestProblemSet<GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPState, String, Double>, IPath<EnhancedTTSPState, String>, EnhancedTTSP, ShortList> {

	public EnhancedTTSPAsGraphSearchSet() {
		super("Enhanced TTSP as graph search", new EnhancedTTSPProblemSet(), new EnhancedTTSPToSimpleGraphSearchReducer());
	}
}