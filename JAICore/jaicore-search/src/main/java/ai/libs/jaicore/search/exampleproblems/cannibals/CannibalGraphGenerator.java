package ai.libs.jaicore.search.exampleproblems.cannibals;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.IRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

import ai.libs.jaicore.problems.cannibals.CannibalProblem;
import ai.libs.jaicore.search.model.NodeExpansionDescription;

public class CannibalGraphGenerator implements IGraphGenerator<CannibalProblem, String> {

	private final CannibalProblem initState;

	public CannibalGraphGenerator(final CannibalProblem initState) {
		super();
		this.initState = initState;
	}

	@Override
	public IRootGenerator<CannibalProblem> getRootGenerator() {
		return new ISingleRootGenerator<CannibalProblem>() {

			@Override
			public CannibalProblem getRoot() {
				return CannibalGraphGenerator.this.initState;
			}
		};
	}

	@Override
	public ISuccessorGenerator<CannibalProblem, String> getSuccessorGenerator() {
		return new ISuccessorGenerator<CannibalProblem, String>() {

			@Override
			public List<INewNodeDescription<CannibalProblem, String>> generateSuccessors(final CannibalProblem node) throws InterruptedException {
				List<INewNodeDescription<CannibalProblem, String>> successors = new ArrayList<>();
				int ml = node.getMissionariesOnLeft();
				int mr = node.getMissionariesOnRight();
				int cl = node.getCannibalsOnLeft();
				int cr = node.getCannibalsOnRight();

				/* first consider the case that the boat is on the left */
				if (node.isBoatOnLeft()) {
					if (ml >= 2) {
						CannibalProblem candidate = new CannibalProblem(false, ml - 2, cl, mr + 2, cr);
						CannibalGraphGenerator.this.checkThatNumberOfPeopleHasNotChanged(node, candidate);
						if (!candidate.isLost()) {
							successors.add(new NodeExpansionDescription<>(candidate, "2m->"));
						}
					}
					if (ml >= 1) {
						CannibalProblem candidate = new CannibalProblem(false, ml - 1, cl, mr + 1, cr);
						CannibalGraphGenerator.this.checkThatNumberOfPeopleHasNotChanged(node, candidate);
						if (!candidate.isLost()) {
							successors.add(new NodeExpansionDescription<>(candidate, "1m->"));
						}
					}
					if (cl >= 1) {
						CannibalProblem candidate = new CannibalProblem(false, ml, cl - 1, mr, cr + 1);
						CannibalGraphGenerator.this.checkThatNumberOfPeopleHasNotChanged(node, candidate);
						if (!candidate.isLost()) {
							successors.add(new NodeExpansionDescription<>(candidate, "1c->"));
						}
					}
					if (ml >= 1 && cl >= 1) {
						CannibalProblem candidate = new CannibalProblem(false, ml - 1, cl - 1, mr + 1, cr + 1);
						CannibalGraphGenerator.this.checkThatNumberOfPeopleHasNotChanged(node, candidate);
						if (!candidate.isLost()) {
							successors.add(new NodeExpansionDescription<>(candidate, "1m1c->"));
						}
					}
					if (cl >= 2) {
						CannibalProblem candidate = new CannibalProblem(false, ml, cl - 2, mr, cr + 2);
						CannibalGraphGenerator.this.checkThatNumberOfPeopleHasNotChanged(node, candidate);
						if (!candidate.isLost()) {
							successors.add(new NodeExpansionDescription<>(candidate, "2c->"));
						}
					}
				}

				/* now consider the cases that the boat is on the right */
				else {
					if (mr >= 2) {
						CannibalProblem candidate = new CannibalProblem(true, ml + 2, cl, mr - 2, cr);
						CannibalGraphGenerator.this.checkThatNumberOfPeopleHasNotChanged(node, candidate);
						if (!candidate.isLost()) {
							successors.add(new NodeExpansionDescription<>(candidate, "2m<-"));
						}
					}
					if (mr >= 1) {
						CannibalProblem candidate = new CannibalProblem(true, ml + 1, cl, mr - 1, cr);
						CannibalGraphGenerator.this.checkThatNumberOfPeopleHasNotChanged(node, candidate);
						if (!candidate.isLost()) {
							successors.add(new NodeExpansionDescription<>(candidate, "1m<-"));
						}
					}
					if (cr >= 1) {
						CannibalProblem candidate = new CannibalProblem(true, ml, cl + 1, mr, cr - 1);
						CannibalGraphGenerator.this.checkThatNumberOfPeopleHasNotChanged(node, candidate);
						if (!candidate.isLost()) {
							successors.add(new NodeExpansionDescription<>(candidate, "1c<-"));
						}
					}
					if (mr >= 1 && cr >= 1) {
						CannibalProblem candidate = new CannibalProblem(true, ml + 1, cl + 1, mr - 1, cr - 1);
						CannibalGraphGenerator.this.checkThatNumberOfPeopleHasNotChanged(node, candidate);
						if (!candidate.isLost()) {
							successors.add(new NodeExpansionDescription<>(candidate, "1m1c<-"));
						}
					}
					if (cr >= 2) {
						CannibalProblem candidate = new CannibalProblem(true, ml, cl + 2, mr, cr - 2);
						CannibalGraphGenerator.this.checkThatNumberOfPeopleHasNotChanged(node, candidate);
						if (!candidate.isLost()) {
							successors.add(new NodeExpansionDescription<>(candidate, "2c<-"));
						}
					}
				}
				return successors;
			}
		};
	}

	private void checkThatNumberOfPeopleHasNotChanged(final CannibalProblem a, final CannibalProblem b) {
		if (a.getTotalNumberOfPeople() != b.getTotalNumberOfPeople()) {
			throw new IllegalStateException("Number of people has changed from " + a.getTotalNumberOfPeople() + " to " + b.getTotalNumberOfPeople());
		}
	}
}
