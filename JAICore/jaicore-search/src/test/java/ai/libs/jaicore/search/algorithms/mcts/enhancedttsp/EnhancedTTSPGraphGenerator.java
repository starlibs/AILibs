package ai.libs.jaicore.search.algorithms.mcts.enhancedttsp;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPState;
import ai.libs.jaicore.search.exampleproblems.enhancedttsp.EnhancedTTSPSuccessorGenerator;

public class EnhancedTTSPGraphGenerator implements IGraphGenerator<EnhancedTTSPState, String>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(EnhancedTTSPGraphGenerator.class);
	private final EnhancedTTSP problem;
	private final EnhancedTTSPSuccessorGenerator succGen;

	public EnhancedTTSPGraphGenerator(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
		this.succGen = new EnhancedTTSPSuccessorGenerator(problem);
	}

	@Override
	public ISingleRootGenerator<EnhancedTTSPState> getRootGenerator() {
		return () -> this.problem.getInitalState();
	}

	@Override
	public ISuccessorGenerator<EnhancedTTSPState, String> getSuccessorGenerator() {
		return this.succGen;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.succGen.setLoggerName(name + ".sg");
	}
}
