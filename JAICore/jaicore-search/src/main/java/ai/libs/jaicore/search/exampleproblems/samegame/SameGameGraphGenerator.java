package ai.libs.jaicore.search.exampleproblems.samegame;


import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.problems.samegame.SameGameCell;
import ai.libs.jaicore.problems.samegame.SameGameState;
import ai.libs.jaicore.search.core.interfaces.LazySuccessorGenerator;

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
	public SingleRootGenerator<SameGameNode> getRootGenerator() {
		return () -> this.rootNode;
	}

	@Override
	public LazySuccessorGenerator<SameGameNode, SameGameCell> getSuccessorGenerator() {
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
