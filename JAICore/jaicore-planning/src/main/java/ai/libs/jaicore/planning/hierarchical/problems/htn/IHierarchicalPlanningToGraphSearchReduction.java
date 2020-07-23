package ai.libs.jaicore.planning.hierarchical.problems.htn;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.core.interfaces.IPlan;

public interface IHierarchicalPlanningToGraphSearchReduction<N, A, I1 extends IHTNPlanningProblem, O1 extends IPlan, I2 extends IPathSearchInput<N, A>, O2 extends ILabeledPath<N, A>> extends AlgorithmicProblemReduction<I1, O1, I2, O2> {

}
