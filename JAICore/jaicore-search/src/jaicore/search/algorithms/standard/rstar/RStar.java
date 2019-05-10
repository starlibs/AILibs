package jaicore.search.algorithms.standard.rstar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;

import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.structure.graphgenerator.MultipleRootGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;

/**
 * Implementation of the R* algorithm.
 *
 * @param <T> a nodes external label i.e. a state of a problem
 * @param <A> action (action space of problem)
 * @param <D> type of Delta distance
 */
public class RStar<T, A, D> extends Thread {

    /* Open list. */
    protected PriorityQueue<GammaNode<T, RStarK>> open = new PriorityQueue<GammaNode<T, RStarK>>();

    /* Closed list of already expanded states. */
    protected ArrayList<GammaNode<T, RStarK>> closed = new ArrayList<>();

    /* For actual search problem */
    GammaGraphGenerator<T,D> gammaGraphGenerator;
    protected final double w;
    protected final int K;
    protected final D delta;
    private boolean stopAtFirstSolution;
    private ISolutionEvaluator<T, Double> solutionEvaluator;

    private final ArrayList<GammaNode<T, RStarK>> startStates = new ArrayList<>();
    private GammaNode<T, RStarK> n_goal = null;


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
        stopAtFirstSolution = true;
        solutionEvaluator = null;

        /**
         * Initialize empty goal node.
         */
        // n_goal = gammaGraphGenerator.getGoal();
        // RStarK k_goal = new RStarK(true, Double.MAX_VALUE);
        // n_goal.setInternalLabel(k_goal);

        /**
         * Initialize root nodes.
         */
        RootGenerator<GammaNode<T, RStarK>> rootGenerator = gammaGraphGenerator.getRootGenerator();
        if (rootGenerator instanceof MultipleRootGenerator) {
            for (GammaNode<T,RStarK> root : ((MultipleRootGenerator<GammaNode<T,RStarK>>) rootGenerator).getRoots()) {
                RStarK k = new RStarK(false, w*hToGoal(root));
                root.setInternalLabel(k);
                root.g = 0;
                startStates.add(root);
            }
        } else if (rootGenerator instanceof SingleRootGenerator) {
            GammaNode<T,RStarK> root = ((SingleRootGenerator<GammaNode<T,RStarK>>) rootGenerator).getRoot();
            RStarK k = new RStarK(false, w*hToGoal(root));
            root.setInternalLabel(k);
            root.g = 0;
            startStates.add(root);
        } else {
            assert false : "Only MultipleRootGenerator or SingleRootGenerators allowed.";
        }
    }

    public RStar(GammaGraphGenerator<T, D> gammaGraphGenerator, double w, int K, D delta, ISolutionEvaluator<T, Double> solutionEvaluator) {
        this(gammaGraphGenerator, w, K, delta);
        this.stopAtFirstSolution = false;
        this.solutionEvaluator = solutionEvaluator;
    }



    /**
     * Updates a state i.e. node n in the open list.
     *
     * @param n
     */
    private void updateState(GammaNode<T, RStarK> n) {
        T s = n.getPoint();
        open.remove(n);  // What if n is not in open list.

//        System.out.println(String.format(
//                "%b, %b, %b",
//                (n.g > w*h(n_start, n)),
//                n.backpointer == null || (n.backpointer.path.get(n) == null),
//                n.avoid
//        ));
//        System.out.println(n);
//        System.out.println("n.g = " + n.g + ", h(n_start, n) = " + h(n_start, n));

        if ((n.g > w*hFromStart(n)) || ((n.backpointer == null || (n.backpointer.path.get(n) == null)) && n.avoid)) {
            n.setInternalLabel(new RStarK(true, n.g + w*hToGoal(n)));
        } else {
            n.setInternalLabel(new RStarK(false, n.g + w*hToGoal(n)));
        }
        open.add(n);
    }

    /**
     * Tries to compute the local path
     * @param n
     * @throws InterruptedException 
     */
    private void reevaluateState(GammaNode<T, RStarK> n) throws InterruptedException {
        /**
         * Try to compute the local path from bp(n) to n.
         */
        // Line 7
        PathAndCost pac = gammaGraphGenerator.computePath(n.backpointer, n);
        n.backpointer.path.put(n, pac.path);

        if (pac.path != null) {
            n.backpointer.c_low.put(n, pac.cost);
            //n.backpointer.setAnnotation("setted path to", n);
            //n.backpointer.setAnnotation("it is", pac.path);
            //n.setAnnotation("pacset_on_bp", n.backpointer.getPoint().toString());
            //n.setAnnotation("pacset_empty", pac.path.size());
        }
        // System.out.println("Path and cost from " + n.backpointer.getPoint() + " to " + n.getPoint() + ": " + pac);

        /**
         * If no path bp(n)->n could be computed or
         * the g = "cost from n_start to bp(n)" + the cost of the found path is greater than w*h(n_start, n)
         * the state n should be avoided.
         */
        // Line 8
        if (!isGoalNode(n)) {
            if ((n.backpointer.path.get(n) == null) || (n.backpointer.g + n.backpointer.c_low.get(n) > w * hFromStart(n))) {
                n.backpointer = argminCostToStateOverPredecessors(n);
                n.avoid = true;
            }
        }
        n.g = n.backpointer.g + n.backpointer.c_low.get(n);
        if (!isGoalNode(n)) updateState(n);
    }

    @Override
    public void run() {
    	try {
        // Line 14 to 16: see constructor.
        // Line 17
//        System.out.println("Staring RStar");
        for (GammaNode<T,RStarK> n_start : startStates) {
            open.add(n_start);
        }

        /**
         * Run while the open list is not empty and there exists a node in the open list
         * with higher priority i.e. less k than k_n_goal (if the highest priority is a
         * goal node, then we return in th next lines).
         */
        // Line 18
        while (!isInterrupted() && !open.isEmpty()) {
            /**
             * Remove node n with highest priority i.e. smallest k-value from open.
             */
            GammaNode<T, RStarK> n = open.poll(); //eek();
            System.out.println("Expanding node " + n);

            /**
             * If node with highest priority is a goal node, we found our goal.
             */


            // Line 20
            if ((!isStartNode(n)) && (n.backpointer == null || (n.backpointer.path.get(n) == null))) {
                /**
                 * The path that corresponds to the edge bp(s)->s has not been computed yet.
                 * Try to compute it using reevaluateState.
                 */
                reevaluateState(n);

                if (isGoalNode(n) && !n.avoid) {

                    if (stopAtFirstSolution) {
                        n_goal = n;
                        System.out.println("RStar finished.");
                        return;
                    } else {
                        if (n_goal != null && n_goal.backpointer.path.get(n_goal).size() != 0) {
                            try {
                                List<T> currentSolutionPath = inferProblemPath(n_goal);
                                // List<T> currentSolutionPath = new ArrayList<>();
                                // currentSolutionPath.add(n_goal.getPoint());
                                double currentSolutionCost = solutionEvaluator.evaluateSolution(currentSolutionPath);
                                List<T> newSolutionPath = inferProblemPath(n);
                                // List<T> newSolutionPath = new ArrayList<>();
                                // newSolutionPath.add(n_goal.getPoint());
                                double newSolutionCost = solutionEvaluator.evaluateSolution(newSolutionPath);
                                if (currentSolutionCost > newSolutionCost) {
                                    n_goal = n;
                                }
                            } catch (Exception e) {
                                /*System.err.println("Exception on evaluation solutions.");
                                System.err.println("Goal problem path. goal = " + n_goal);
                                System.err.println("n_goal.backpointer = " + n_goal.backpointer);
                                System.err.println("n_goal path to bp = " + n_goal.backpointer.path.keySet());
                                System.err.println("n_goal path to bp = " + n_goal.backpointer.path.containsKey(n_goal));
                                System.err.println("n_goal path to bp = " + n_goal.backpointer.path.get(n_goal));
                                System.err.println("n_goal path to bp = " + n_goal.backpointer.path.get(n_goal).size());
                                System.err.println(inferProblemPath(n_goal));
                                System.err.println("new goal n problem path. n = " + n);
                                System.err.println(inferProblemPath(n)); */
                                e.printStackTrace();
                            }
                        } else {
                            n_goal = n;
                        }

                    }
                }

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
                // System.out.println("Generated successors for " + n + " : " + succ_s.size());

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
                        // System.out.println(String.format("Line 32: n.g = %g, c_low(n, n_) = %g", n.g, n.c_low.get(n_)));
                        n_.g = n.g + n.c_low.get(n_);
                        n_.backpointer = n;
                        updateState(n_); // updates priority of n_ in open list.
                    }
                }
            }
        }
        // System.out.println("RStar isInterrupted(): " + isInterrupted() + "open empty(); " + open.isEmpty());
        // After the while loop of R* terminates, the solution can be re-constructed
        // by following backpointers bp backwards starting at state n_goal until s_start is reached.
        // use getSolution() for this
    	}
    	catch (Exception e) {
    		throw new RuntimeException(e); // workaround, because run has not any checked exceptions
    	}

    }


    /**
     *
     * @return
     */
    public double getSolutionCost() {
        double cost = 0;

        if (n_goal.backpointer != null) {
            GammaNode<T, RStarK> current = n_goal;
            while (!isStartNode(current)) {
                cost += current.backpointer.c_low.get(current);
                current = current.backpointer;
            }
        }

        return cost;
    }

    /**
     *
     * @return
     */
    public List<T> getSolutionPath() {
        if (n_goal != null) {
            return inferProblemPath(n_goal);
        } else {
            return null;
        }
    }

    /**
     * Returns simply the goal state of current best solution.
     * @return
     */
    public T getGoalState() {
        if (n_goal != null) {
            return n_goal.getPoint();
        } else {
            return null;
        }
    }

    /**
     * Calculates the problem path for a Gamma node.
     * @param n
     * @return
     */
    private List<Node<T, RStarK>> inferNodePath(GammaNode<T, RStarK> n) {
        List<Node<T, RStarK>> solution = new ArrayList<>();

        if (n == null) {
            return null;
        }
        if (n.backpointer == null) {
            solution.add(new Node(null, n.getPoint()));
            return solution;
        }

        GammaNode<T, RStarK> current = n;
        // while we can follw the gamma path upwards
        while (current.backpointer != null) {
            List<Node<T,RStarK>> pathBpToCurrent = current.backpointer.path.get(current); // null because of line ??? Cant figure out why this is sometimes an empty list.
            if (pathBpToCurrent.isEmpty()) {
                break;
            }
            if (!isStartNode(current.backpointer))
                pathBpToCurrent.remove(0);
            pathBpToCurrent.addAll(solution);
            solution = pathBpToCurrent;
            current = current.backpointer;
        }
        return solution;
    }

    /**
     * Infers the problem path from a given Gamma node. GammaNode -> (->GammaNodePath) -> NodePath -> ProblemPath
     * @param n
     * @return
     */
    private List<T> inferProblemPath(GammaNode<T, RStarK> n) {
        if (n!=null) {
            // First infer node path.
            List<Node<T, RStarK>> nodePath = inferNodePath(n);
            // From node path, infer problem path.
            return inferProblemPath(nodePath);
        } else {
            return null;
        }
    }

    /**
     * Infers the problem path from a given node path.
     * @param nodePath
     * @return
     */
    public List<T> inferProblemPath(List<Node<T, RStarK>> nodePath) {
        if (nodePath != null) {
            List<T> path = new ArrayList<>(nodePath.size());
            for (Node<T, RStarK> n : nodePath) {
                path.add(n.getPoint());
            }
            return path;
        } else {
            return null;
        }

    }

    /**
     * Samples the backpointers from n_goal onwards to n_start.
     * @return
     */
    public List<GammaNode<T, RStarK>> getGammaSolutionPath()  {

        List<GammaNode<T, RStarK>> solution = new ArrayList<>();

        if (n_goal != null && n_goal.backpointer != null) {
            GammaNode<T, RStarK> current = n_goal;
            solution.add(0, current);
            // Add the backpointers in front up to the start node.
            while (!isStartNode(current)) {
                solution.add(0, current.backpointer);
                current = current.backpointer;
            }
        } else {
            solution = null;
        }
        return solution;
    }

    /**
     *
     * @param from
     * @param to
     * @return
     */
    private double h(GammaNode<T, RStarK> from, GammaNode<T, RStarK> to) {
        return gammaGraphGenerator.h(from, to);
    }

    private double hFromStart(GammaNode<T, RStarK> to) {
        return gammaGraphGenerator.hFromStart(to);
    }

    private double hToGoal(GammaNode<T, RStarK> from) {
        return gammaGraphGenerator.hToGoal(from);
    }

    private boolean isStartNode(GammaNode<T, RStarK> n) {
        return startStates.contains(n);
    }

    private boolean isGoalNode(GammaNode<T, RStarK> n ) {
        if (gammaGraphGenerator.getGoalTester() instanceof NodeGoalTester) {
            return ((NodeGoalTester)gammaGraphGenerator.getGoalTester()).isGoal(n.getPoint());
        } else {
            assert false : "GoalTester has to be a NodeGoalTester";
        }
        return false;
    }

    /**
     *
     * @param n
     * @return
     */
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
     * Also maintains the predecessor set of nodes.
     *
     * @param n Gamma node to generate successors for.
     * @return List of Gamma nodes.
     * @throws InterruptedException 
     * @throws  
     */
    private Collection<GammaNode<T, RStarK>> generateGammaSuccessors(GammaNode<T, RStarK> n) throws InterruptedException {
        Collection<GammaNode<T, RStarK>> succ = gammaGraphGenerator.generateRandomSuccessors(n, K, delta);
        ArrayList<GammaNode<T, RStarK>> succWithoutClosed = new ArrayList<>();

        for (GammaNode<T, RStarK> s : succ) {
            s.addPredecessor(n);
            // Note allow the open list
            if (!closed.contains(s)) {
                succWithoutClosed.add(s);
            }
        }
        return succWithoutClosed;
    }

}
