package ai.libs.jaicore.search.syntheticgraphs.treasuremodels;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.common.control.ILoggingCustomizable;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

public interface ITreasureModel extends IPathEvaluator<ITransparentTreeNode, Integer, Double>, ILoggingCustomizable {
	public double getMinimumAchievable();
}
