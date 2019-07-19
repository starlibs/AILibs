package ai.libs.jaicore.planning.hierarchical.problems.htn;

import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public interface IHierarchicalPlanningToGraphSearchReduction<N, A, I1 extends IHTNPlanningProblem, O1 extends IPlan, I2 extends GraphSearchInput<N, A>, O2 extends IPath<N, A>> extends AlgorithmicProblemReduction<I1, O1, I2, O2> {

}
