package jaicore.search.algorithms.standard.rstar;

import java.util.Collection;

import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;

/**
 * Graph generator for the gamma graph of R*.
 *
 *
 * @param <T>
 * @param <V>
 * @param <D>
 */
public interface GammaGraphGenerator<T, D> {

    // public GammaNode<T, RStarK> getRoot();

    // public GammaNode<T, RStarK> getGoal();

    public RootGenerator<GammaNode<T, RStarK>> getRootGenerator();

    /**
     * Generates random successors for a gamma node.
     *
     * Note: If a node with the same state has been generate before, the reference
     * from the first generation has to be returned.
     *
     * @param n Node to generate successors for
     * @param K Number of successors to generate
     * @param delta Maximal distance of the successors to n
     * @return Generated successors.
     */
    public Collection<GammaNode<T,RStarK>> generateRandomSuccessors(GammaNode<T,RStarK> n, int K, D delta) throws IllegalArgumentException, InterruptedException;

    /**
     * Try to compute local path.
     *
     * If not possible, `PathCost.path = null` and `PathCost.cost = Double.MAX_VALUE`.
     * @param from
     * @param to
     * @return
     */
    public PathAndCost computePath(GammaNode<T,RStarK> from, GammaNode<T,RStarK> to) throws InterruptedException;

    /**
     * Heurisitc estimation of the cost of path from n1 to n2.
     * Probably just use h(n2)-h(n1).
     * @param n1
     * @param n2
     * @return
     */
    public double h(GammaNode<T,RStarK> n1, GammaNode<T,RStarK> n2);

    /**
     * Heurisitc estimation of the cost of path from a start state to a state `to`.
     * @param to
     * @return
     */
    public double hFromStart(GammaNode<T,RStarK> to);

    /**
     * Heurisic estimation of the cost of path from a state `from` to a goal state.
     * @param from
     * @return
     */
    public double hToGoal(GammaNode<T,RStarK> from);


    public GoalTester<T> getGoalTester();

}
