package ai.libs.jaicore.search.syntheticgraphs;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;

public class SyntheticSearchProblemBuilder {

	private ISyntheticGraphGeneratorBuilder ggBuilder;
	private IGraphGenerator<ITransparentTreeNode, Integer> graphGenerator;
	private ITreasureModel treasureModel;

	public SyntheticSearchProblemBuilder withGGBuilder(final ISyntheticGraphGeneratorBuilder graphGeneratorBuilder) {
		this.ggBuilder = graphGeneratorBuilder;
		return this;
	}

	public SyntheticSearchProblemBuilder withGraphGenerator(final IGraphGenerator<ITransparentTreeNode, Integer> graphGenerator) {
		this.ggBuilder = null;
		this.graphGenerator = graphGenerator;
		return this;
	}

	public SyntheticSearchProblemBuilder withTreasureModel(final ITreasureModel treasureModel) {
		this.treasureModel = treasureModel;
		return this;
	}

	public IPathSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double> build() {
		if (this.ggBuilder != null) {
			this.graphGenerator = this.ggBuilder.build();
		}
		if (this.graphGenerator == null) {
			throw new IllegalStateException("Graph has not been set yet.");
		}
		if (this.treasureModel == null) {
			throw new IllegalStateException("TreasureModel has not been set yet.");
		}
		return new GraphSearchWithPathEvaluationsInput<>(this.graphGenerator, new INodeGoalTester<ITransparentTreeNode, Integer>() {

			@Override
			public boolean isGoal(final ITransparentTreeNode node) {
				return !node.hasChildren();
			}
		}, this.treasureModel);
	}
}
