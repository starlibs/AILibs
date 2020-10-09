package ai.libs.jaicore.search.syntheticgraphs.treasuremodels;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

public interface ITreasureModel extends IPathEvaluator<ITransparentTreeNode, Integer, Double>, ILoggingCustomizable {

	public boolean isPathToTreasureIsland(ILabeledPath<ITransparentTreeNode, Integer> path);

	public double getMinimumAchievable();
}
