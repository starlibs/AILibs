package jaicore.search.algorithms.standard.rstar;

import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import jaicore.search.algorithms.standard.bestfirst.RandomCompletionEvaluator;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.OpenCollection;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implementation of the R* algorithm.
 * @param <T> a nodes external label i.e. a state of a problem
 * @param <A> action (action space of problem)
 */
public class RStar<T, A> {

    /* Open list. */
    protected OpenCollection<Node<T, K>> open;

    /* Closed list of already expanded states. */
    protected ArrayList<T> closed;

    /* For actual search problem */
    protected final SingleRootGenerator<T> rootGenerator;
    protected final SuccessorGenerator<T, A> successorGenerator;
    protected final HeuristicEstimator<T> heuristicEstimator;
    protected final double w;
    protected final int K;
    protected final double delta;

    // protected final HashMap<T, Double> g;
    // protected final HashMap<T, T> bp;

    private class GammaGraph {

        // set of Node<T, V>


    }

    /**
     * k-Values (Priorities used for expansion from open.)
     */
    public class K implements Comparable<K>{

        boolean avoid;
        double f;

        K(boolean avoid, double f) {
            this.avoid = avoid;
            this.f = f;
        }

        @Override
        public int compareTo(K o) {
            // Compare first AVOID flag.
            if (!this.avoid && o.avoid) {
                return +1;
            }
            if (this.avoid && !o.avoid) {
                return -1;
            }
            // Then compare f-values.
            return Double.compare(this.f, o.f);
        }
    }

    private class HeuristicEstimator<T> {
        public HeuristicEstimator(T s, SuccessorGenerator sg, GoalTester gt) {

        }

        public double h(T s) {
            return 1;
        }
    }


    /**
     * 
     * @param graphGenerator
     * @param h
     * @param w
     * @param K
     * @param delta
     */
    public RStar(GraphGenerator<T, A> graphGenerator, HeuristicEstimator<T> h, double w, int K, double delta) {
        // Set root and successor generator for graph of actual search problem.
        if (graphGenerator.getRootGenerator() instanceof SingleRootGenerator) {
            this.rootGenerator = (SingleRootGenerator<T>) graphGenerator.getRootGenerator();
        } else {
            throw new IllegalArgumentException("Only single root generators are allowed.");
        }
        this.successorGenerator = graphGenerator.getSuccessorGenerator();
        this.heuristicEstimator = h;
        this.w = w;
        this.K = K;
        this.delta = delta;
    }

    public void run() {
        // Initialize root node s_start with k_start = [0, w*h(s_start)] and add it to open list.
        T s_start = rootGenerator.getRoot();
        K k_start = new K(false, w*heuristicEstimator.h(s_start));
        Node<T, K> n_start = new Node(null, s_start);
        n_start.setInternalLabel(k_start);
        n_start.setAnnotation("g", 0.0d);
        this.open.add(n_start);

        while (!open.isEmpty()) {
            // Remove node n with smallest k-value from open.
            Node<T, K> n = this.open.peek();
            T s = n.getPoint();

            if ((!s.equals(s_start)) && (false)) {
                // Reevaluate state
            } else {
                // Expand state
                closed.add(s);
                ArrayList<T> succ_s = generateSuccessors(K, delta);  // here same states can be generated a second time possibly.
                for (T s_ : succ_s) {
                    // path(s_,s) = null
                    // c_low(path(s_,s)) = h(s, s_)

                    // if s_ visited for the first time
                        // g(s_) = inf
                        // bp(s_) = null

                    // if bp(s_) = null OR g(s)+c_low(path(s,s_)) < g(s_)
                        // g(s_) = g(s)+c_low(path(s,s_))
                        // bp(s_) = s
                        // UpdateState(s_)
                }
            }

        }

        // s = select from OPEN

        // if bp(s)->s has not been computed yet
            // try to compute this pat
            // if failed
                // labels s as AVOID
            // else
                // update g(s) based on cost of this path and g(bp(s))
        // else
            // succ_s = generated_successors(K, d) (add goal states within d to these successors)
            // for s' in succ_s
                // add s' and s->s' to G, set bp(s')=s

    }

    private ArrayList<T> generateSuccessors(int k, double d) {
        return null;
    }

    private boolean pathHasBeenComputed(T s, T t) {
        return false;
    }
}
