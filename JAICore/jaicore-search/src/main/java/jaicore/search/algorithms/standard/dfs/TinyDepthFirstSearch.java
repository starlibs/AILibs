package jaicore.search.algorithms.standard.dfs;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class TinyDepthFirstSearch<N, A> {
	private final List<SearchGraphPath<N, A>> solutionPaths = new LinkedList<>();
	private final SuccessorGenerator<N, A> successorGenerator;
	private final NodeGoalTester<N> goalTester;
	private final N root;
	private final Deque<N> path = new LinkedList<>();

	public TinyDepthFirstSearch(final GraphSearchInput<N, A> problem) {
		super();
		this.root = ((SingleRootGenerator<N>) problem.getGraphGenerator().getRootGenerator()).getRoot();
		this.goalTester = (NodeGoalTester<N>) problem.getGraphGenerator().getGoalTester();
		this.successorGenerator = problem.getGraphGenerator().getSuccessorGenerator();
		this.path.add(this.root);
	}

	public void run() throws InterruptedException {
		this.dfs(this.root);
	}

	public void dfs(final N head) throws InterruptedException {
		if (this.goalTester.isGoal(head)) {
			this.solutionPaths.add(new SearchGraphPath<>(new ArrayList<>(this.path)));
		}
		else {

			/* expand node and invoke dfs for each child in order */
			List<NodeExpansionDescription<N,A>> successors = this.successorGenerator.generateSuccessors(head);
			for (NodeExpansionDescription<N,A> succ : successors) {
				N to = succ.getTo();
				this.path.addFirst(to);
				this.dfs(to);
				N removed = this.path.removeFirst();
				assert removed == to : "Expected " + to + " but removed " + removed;
			}
		}
	}

	public List<SearchGraphPath<N, A>> getSolutionPaths() {
		return this.solutionPaths;
	}
}
