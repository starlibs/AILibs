package ai.libs.jaicore.search.util;

import java.util.List;
import java.util.Stack;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public class GraphSanityChecker<N, A> extends AOptimalPathInORGraphSearch<GraphSearchInput<N, A>, N, A, Double> {

	private Logger logger = LoggerFactory.getLogger(GraphSanityChecker.class);
	private String loggerName;

	private SanityCheckResult sanityCheckResult;
	private final int maxNodesToExpand;
	private boolean detectCycles = true;
	private boolean detectDeadEnds = true;

	public GraphSanityChecker(final GraphSearchInput<N, A> problem, final int maxNodesToExpand) {
		super(problem);
		this.maxNodesToExpand = maxNodesToExpand;
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException {
		switch (this.getState()) {
		case CREATED:
			return this.activate();
		case ACTIVE:
			int expanded = 0;
			Stack<BackPointerPath<N, A, ?>> open = new Stack<>();
			N root = ((ISingleRootGenerator<N>) this.getGraphGenerator().getRootGenerator()).getRoot();
			IPathGoalTester<N, A> goalTester = this.getGoalTester();
			open.push(new BackPointerPath<>(null, root, null));
			this.post(new GraphInitializedEvent<N>(this, root));
			while (!open.isEmpty() && expanded < this.maxNodesToExpand) {
				BackPointerPath<N, A, ?> node = open.pop();
				if (!node.isGoal()) {
					this.post(new NodeTypeSwitchEvent<>(this, node, "or_closed"));
				}
				expanded++;
				List<INewNodeDescription<N, A>> successors = this.getGraphGenerator().getSuccessorGenerator().generateSuccessors(node.getHead());
				if (this.detectDeadEnds && successors.isEmpty() && !node.isGoal()) {
					this.sanityCheckResult = new DeadEndDetectedResult<N>(node.getHead());
					break;
				}
				for (INewNodeDescription<N, A> successor : successors) {
					if (this.detectCycles && node.getNodes().contains(successor.getTo())) {
						List<N> path = node.getNodes();
						path.add(successor.getTo());
						this.sanityCheckResult = new CycleDetectedResult<N>(path, node.getHead());
						break;
					}
					BackPointerPath<N, A, ?> newNode = new BackPointerPath<>(node, successor.getTo(), successor.getArcLabel());
					newNode.setGoal(goalTester.isGoal(newNode));
					open.add(newNode);
					this.post(new NodeAddedEvent<N>(this, node.getHead(), successor.getTo(), newNode.isGoal() ? "or_solution" : "or_open"));
				}
				if (this.sanityCheckResult != null) {
					break;
				}
				if (expanded % 100 == 0 || expanded == this.maxNodesToExpand) {
					this.logger.debug("Expanded {}/{} nodes.", expanded, this.maxNodesToExpand);
				}
			}
			this.shutdown();
			return this.terminate();

		default:
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
		}
	}

	public SanityCheckResult getSanityCheck() {
		return this.sanityCheckResult != null ? this.sanityCheckResult : new GraphSeemsSaneResult();
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		super.setLoggerName(this.loggerName + "._orgraphsearch");
	}

	public boolean isDetectCycles() {
		return this.detectCycles;
	}

	public void setDetectCycles(final boolean detectCycles) {
		this.detectCycles = detectCycles;
	}

	public boolean isDetectDeadEnds() {
		return this.detectDeadEnds;
	}

	public void setDetectDeadEnds(final boolean detectDeadEnds) {
		this.detectDeadEnds = detectDeadEnds;
	}
}