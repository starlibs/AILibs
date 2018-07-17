package jaicore.search.algorithms.standard.rstar;

import jaicore.search.structure.core.OpenCollection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of the R* algorithm.
 *
 * @param <T> a nodes external label i.e. a state of a problem
 * @param <A> action (action space of problem)
 * @param <D> type of Delta distance
 */
public class RStar<T, A, D> {

    /* Open list. */
    protected OpenCollection<GammaNode<T, RStarK>> open;

    /* Closed list of already expanded states. */
    protected ArrayList<GammaNode<T, RStarK>> closed;

    /* For actual search problem */
    GammaGraphGenerator<T,D> gammaGraphGenerator;
    protected final double w;
    protected final int K;
    protected final D delta;

    private final GammaNode<T, RStarK> n_start;
    private final GammaNode<T, RStarK> n_goal;

    /**
     * 
     * @param gammaGraphGenerator
     * @param w
     * @param K
     * @param delta
     */
    public RStar(GammaGraphGenerator<T, D> gammaGraphGenerator, double w, int K, D delta) {
        // Set root and successor generator for graph of actual search problem.
        this.gammaGraphGenerator = gammaGraphGenerator;
        this.w = w;
        this.K = K;
        this.delta = delta;

        /**
         * Initialize empty goal node.
         */
        n_goal = gammaGraphGenerator.getGoal();
        RStarK k_goal = new RStarK(true, Double.MAX_VALUE);
        n_goal.setInternalLabel(k_goal);

        /**
         * Initialize root node.
         */
        n_start = gammaGraphGenerator.getRoot();
        RStarK k_start = new RStarK(false, w*h(n_start, n_goal));
        n_start.setInternalLabel(k_start);
    }

    /**
     * Updates a state i.e. node n in the open list.
     *
     * @param n
     */
    private void updateState(GammaNode<T, RStarK> n) {
        T s = n.getPoint();
        open.remove(n);  // What if n is not in open list.
        if ((n.g > w*h(n_start, n)) || ((n.backpointer == null || (n.backpointer.path.get(n) == null)) && n.avoid)) {
            n.setInternalLabel(new RStarK(true, n.g + w*h(n, n_goal)));
        } else {
            n.setInternalLabel(new RStarK(false, n.g + w*h(n, n_goal)));
        }
        open.add(n);
    }

    private void reevaluateState(GammaNode<T, RStarK> n) {
        /**
         * Try to compute the local path from bp(n) to n.
         */
        // Line 7
        PathAndCost pac = gammaGraphGenerator.computePath(n.backpointer, n);
        n.backpointer.path.put(n, pac.path);
        if (pac.path != null) {
            n.backpointer.c_low.put(n, pac.cost);
        }

        // Line 8
        if ((n.backpointer.path.get(n) == null) || (n.backpointer.g + n.backpointer.c_low.get(n) > w*h(n_start, n))) {
            n.backpointer = argminCostToStateOverPredecessors(n);
            n.avoid = true;
        }
        n.g = n.backpointer.g + n.backpointer.c_low.get(n);
        updateState(n);
    }

    public void run() {
        // Line 14 to 16: see constructor.
        // Line 17
        open.add(n_start);

        /**
         * Run while the open list is not empty and there exists a node in the open list
         * with higher priority i.e. less k than k_n_goal = [1, inf].
         */
        // Line 18
        while (!open.isEmpty() && open.peek().compareTo(n_goal) <= 0) {
            /**
             * Remove node n with highest priority i.e. smallest k-value from open.
             */
            GammaNode<T, RStarK> n = open.peek();
            open.remove(n);

            // Line 20
            if ((!n.equals(n_start)) && (n.backpointer == null || (n.backpointer.path.get(n) == null))) {
                /**
                 * The path that corresponds to the edge bp(s)->s has not been computed yet.
                 * Try to compute it using reevaluateState.
                 */
                reevaluateState(n);
            } else {
                /**
                 * The path from bp(s)->s has already been computed.
                 * Expand the state s (i.e. node n).
                 */
                // Line 23.
                closed.add(n);

                // Line 24 to 27
                Collection<GammaNode<T, RStarK>> succ_s = generateGammaSuccessors(n);
                n.setSuccessors(succ_s);

                // Line 28
                for (GammaNode<T, RStarK> n_ : succ_s) {

                    /**
                     * Initialize successors by setting the path from s to s_ to null,
                     * and by estimating the lowest cost from s to s_ with the heuristic h(s, s_).
                     */
                    // Line 29
                    n.path.put(n_, null);
                    n.c_low.put(n_, h(n, n_));

                    // Line 30 and 31 of the algorithm can be omitted here. They contain further initialization of
                    // the successors, but This is done implicitly in the generation process of the Gamma successors.

                    /*
                     * If the generated successor n_ i.e. s_ has never been visited yet (n_.getParent() == null)
                     * or the actual cost to s (n.g) plus the (estimated) cost from s to s_ (c_low(s, s_)) is better
                     * than the actual known cost (n_.g) to s_, then we have to update these values for s_ (because
                     * with s we found a better predecessor for s_).
                     */
                    // Line 32
                    if ((n_.backpointer == null) || (n.g + n.c_low.get(n_) < n_.g)) {
                        n_.g = n.g + n.c_low.get(n_);
                        n_.backpointer = n;
                        updateState(n_); // updates priority of n_ in open list.
                    }
                }
            }
        }

        // After the wile loop of R* terminates, the solution can be re-constructed
        // by following backpointers bp backwards starting at state s_gaol until s_start is reached.
    }

    private double h(GammaNode<T, RStarK> from, GammaNode<T, RStarK> to) {
        return gammaGraphGenerator.h(from, to);
    }

    private GammaNode<T, RStarK> argminCostToStateOverPredecessors(GammaNode<T, RStarK> n) {
        GammaNode<T, RStarK> argmin = null;
        for (GammaNode<T, RStarK> p : n.getPredecessors()) {
            if ((argmin == null) || (p.g + p.c_low.get(n) < argmin.g + argmin.c_low.get(n))) {
                argmin = p;
            }
        }
        return argmin;
    }

    /**
     * Generates this.RStarK Gamma graph successors for a state s within distance this.delta.
     * Queries the this.gammaSuccessorGenerator and checks if a generate state has been
     * visited i.e. generated in Gamma before. If yes, it takes the old reference from
     * the this.alreadyGeneratedStates list.
     * TODO: Also maintains predecessor sets of nodes.
     *
     * @param n Gamma node to generate successors for.
     * @return List of Gamma nodes.
     */
    private Collection<GammaNode<T, RStarK>> generateGammaSuccessors(GammaNode<T, RStarK> n) {
        Collection<GammaNode<T, RStarK>> succ = gammaGraphGenerator.generateRandomSuccessors(n, K, delta);
        for (GammaNode<T, RStarK> s : succ) {
            s.addPredecessor(n);
        }
        return succ;
    }

}
