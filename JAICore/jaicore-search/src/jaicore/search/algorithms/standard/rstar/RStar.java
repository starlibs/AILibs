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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Implementation of the R* algorithm.
 * @param <T> a nodes external label i.e. a state of a problem
 * @param <A> action (action space of problem)
 * @param <D> type of Delta distance
 */
public class RStar<T, A, D> {

    /* Open list. */
    protected OpenCollection<GammaNode<T, K>> open;

    /* Closed list of already expanded states. */
    protected ArrayList<T> closed;

    /* For actual search problem */
    protected final SingleRootGenerator<T> rootGenerator;
    protected final SuccessorGenerator<T, A> successorGenerator;
    protected final GammaSuccessorGenerator<T, V, D> gammaSuccessorGenerator;
    protected final HeuristicEstimator<T> heuristicEstimator;
    protected final double w;
    protected final int K;
    protected final double delta;


    // We need to assure that, if a state is generated more than once,
    // the g, bp and path values do not differ between the different
    // instances. So we need to hash the states.

    /* g values */
//    protected final HashMap<T, Double> g = new HashMap<>();
    /* Back pointer in Gamma graph. */
//    protected final HashMap<T, T> bp = new HashMap<>();
    /* Path in original graph. */
//    protected final HashMap<StatePair, T> path = new HashMap<>();

    protected final HashSet<T> alreadyGeneratedStates = new HashSet<>();

    /**
     * Captures two states and enables hashing for this pair of states.
     */
    public class StatePair {
        T from;
        T to;
        public StatePair(T from, T to) {
            this.from = from;
            this.to = to;
        }

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
        // Initialize root node s_start with k_start = [0, w*h(s_start)].
        T s_start = rootGenerator.getRoot();
        K k_start = new K(false, w*heuristicEstimator.h(s_start));
        GammaNode<T, K> n_start = new GammaNode(null, s_start);
        n_start.setInternalLabel(k_start);

        // Add start node to open list and add its state to already generated states.
        open.add(n_start);


        while (!open.isEmpty()) {
            // Remove node n with smallest k-value from open.
            GammaNode<T, K> n = this.open.peek();
            T s = n.getPoint();

            if ((!s.equals(s_start)) && (n.pathToBp == null)) {
                // Reevaluate state
            } else {
                // Expand state
                closed.add(s);
                Collection<T> succ_s = gammaSuccessorGenerator.generateSuccessors(s, K, delta);
                for (T s_ : succ_s) {

                    // If we already generated s_ before, use this object.

                    // Else add it already generated states.

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

    private void reevaluateState(GammaNode<T,V> n) {
        if ((n.g > w*h(n.getPoint())) || n.avoid) {
            // Update n respec. state s in open.
            open.remove(n);
            n.setInternalLabel(new K(true, n.g + w*h(n.getPoint())));
            open.add(n);
        }
    }

    private double h(T s) {
        return 0.0d;
    }

    private double h(T from, T to) {
        return 0.0d;
    }

    private ArrayList<T> generateSuccessors(int k, double d) {
        return null;
    }

    private boolean pathHasBeenComputed(T s, T t) {
        return false;
    }
}
