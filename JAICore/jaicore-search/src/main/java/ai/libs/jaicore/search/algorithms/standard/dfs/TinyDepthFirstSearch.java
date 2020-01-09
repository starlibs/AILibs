package ai.libs.jaicore.search.algorithms.standard.dfs;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public class TinyDepthFirstSearch<N, A> {
	private final List<SearchGraphPath<N, A>> solutionPaths = new LinkedList<>();
	private final ISuccessorGenerator<N, A> successorGenerator;
	private final INodeGoalTester<N, A> goalTester;
	private final N root;
	private final Deque<N> nodes = new LinkedList<>();
	private final Deque<A> edges = new LinkedList<>();

	public TinyDepthFirstSearch(final GraphSearchInput<N, A> problem) {
		super();
		this.root = ((ISingleRootGenerator<N>) problem.getGraphGenerator().getRootGenerator()).getRoot();
		this.goalTester = (INodeGoalTester<N, A>) problem.getGoalTester();
		this.successorGenerator = problem.getGraphGenerator().getSuccessorGenerator();
		this.nodes.add(this.root);
	}

	public void run() throws InterruptedException {
		this.dfs(this.root);
	}

	public void dfs(final N head) throws InterruptedException {
		if (this.goalTester.isGoal(head)) {
			this.solutionPaths.add(new SearchGraphPath<>(new ArrayList<>(this.nodes), new ArrayList<>(this.edges)));
		}
		else {

			/* expand node and invoke dfs for each child in order */
			List<INewNodeDescription<N,A>> successors = this.successorGenerator.generateSuccessors(head);
			for (INewNodeDescription<N,A> succ : successors) {
				N to = succ.getTo();
				A label = succ.getArcLabel();
				this.nodes.addFirst(to);
				this.edges.addFirst(label);
				this.dfs(to);
				N removed = this.nodes.removeFirst();
				this.edges.removeFirst();
				assert removed == to : "Expected " + to + " but removed " + removed;
			}
		}
	}

	public List<SearchGraphPath<N, A>> getSolutionPaths() {
		return this.solutionPaths;
	}
}
