package ai.libs.jaicore.search.syntheticgraphs;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

public interface ISyntheticGraphGeneratorBuilder {
	public IGraphGenerator<ITransparentTreeNode, Integer> build();
}
