package ai.libs.jaicore.search.exampleproblems.samegame;


import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.ILazySuccessorGenerator;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.problems.samegame.SameGameCell;
import ai.libs.jaicore.problems.samegame.SameGameState;

public class SameGameGraphGenerator implements IGraphGenerator<SameGameNode, SameGameCell>, ILoggingCustomizable {

	private final SameGameState initState;
	private final SameGameNode rootNode;
	private Logger logger = LoggerFactory.getLogger(SameGameGraphGenerator.class);

	public SameGameGraphGenerator(final SameGameState initState) {
		super();
		this.initState = initState;
		this.rootNode = new SameGameNode(this.initState);
	}

	@Override
	public ISingleRootGenerator<SameGameNode> getRootGenerator() {
		return () -> this.rootNode;
	}

	@Override
	public ILazySuccessorGenerator<SameGameNode, SameGameCell> getSuccessorGenerator() {
		return new SameGameLazySuccessorGenerator();
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
