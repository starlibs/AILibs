package jaicore.search.algorithms.standard.rstar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import jaicore.concurrent.TimeoutTimer;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.MultipleRootGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SingleSuccessorGenerator;

public class RandomCompletionGammaGraphGenerator<T> implements GammaGraphGenerator<T, Integer> {

    // TODO: Warum werden nur 1 bis 2 successors generiert
    // TODO: Kine random completion für goal nodes starten. Sondern da den evaluator nutzen (wieso macht RandomCompletionEvaluator das nicht??).

    private final GraphGenerator<T, String> graphGenerator;
    private final int samples;
    private final int seed;

    private final HashMap<GammaNode, GammaNode> alreadyGeneratedStates = new HashMap<>();
    private final HashMap<T, HashMap<T, List<Node<T, String>>>> computedPaths = new HashMap<>();

    private final RootGenerator<GammaNode<T,RStarK>> gammaRootGenerator;
    private final NodeGoalTester<T> gammaGoalTester;
    private final RandomCompletionBasedNodeEvaluator<T, Double> randomCompletionEvaluator;
    private final ISolutionEvaluator<T, Double> solutionEvaluator;

    /* Timeout stuff for Random Completions. */
    private int timeoutForComputationOfH = 1000;
    private TimeoutTimer.TimeoutSubmitter timeoutSubmitter;

    /**
     *
     * @param graphGenerator
     * @param solutionEvaluator for RandomCompletionGenerator
     * @param samples
     * @param seed
     */
    public RandomCompletionGammaGraphGenerator(SerializableGraphGenerator<T, String> graphGenerator, ISolutionEvaluator<T, Double> solutionEvaluator, int samples, int seed) {
        this.graphGenerator = graphGenerator;
        this.samples = samples;
        this.seed = seed;

        /**
         * Generate Gamma roots from root states and create GammaRootGenerators with these.
         */
        RootGenerator<T> rootGenerator = graphGenerator.getRootGenerator();

        if (rootGenerator instanceof MultipleRootGenerator) {
            // Create list from multiple roots.
            ArrayList<GammaNode<T,RStarK>> gammaRoots = new ArrayList<>();
            for (T root : ((MultipleRootGenerator<T>) rootGenerator).getRoots()) {
                GammaNode<T,RStarK> gammaRoot = new GammaNode<T, RStarK>(root);
                gammaRoots.add(gammaRoot);
                alreadyGeneratedStates.put(gammaRoot, gammaRoot);
            }
            // And return this list in the Gamma Root generator.
            gammaRootGenerator = new MultipleRootGenerator<GammaNode<T, RStarK>>() {
                @Override
                public Collection<GammaNode<T, RStarK>> getRoots() {
                    return gammaRoots;
                }
            };
        } else {
            assert rootGenerator instanceof SingleRootGenerator : "Only MultipleRootGenerator or SingleRootGenerators allowed.";

            // Create Gamma root from single root.
            T root = ((SingleRootGenerator<T>) rootGenerator).getRoot();
            GammaNode<T,RStarK> gammaRoot = new GammaNode<>(root);
            alreadyGeneratedStates.put(gammaRoot, gammaRoot);
            // And return this root in the Gamma Root generator.
            gammaRootGenerator = new SingleRootGenerator<GammaNode<T, RStarK>>() {
                @Override
                public GammaNode<T, RStarK> getRoot() {
                    return gammaRoot;
                }
            };
        }

        /**
         * Just take the goalTester (but only if its a NodeGoalTester).
         */
        GoalTester<T> goalTester = graphGenerator.getGoalTester();
        assert goalTester instanceof NodeGoalTester : "RStar only supports NodeGoalTesters.";
        gammaGoalTester = (NodeGoalTester)goalTester;

        this.solutionEvaluator = solutionEvaluator;
        randomCompletionEvaluator = new RandomCompletionBasedNodeEvaluator<>(new Random(seed), samples, solutionEvaluator);
        randomCompletionEvaluator.setGenerator(graphGenerator);
    }

    @Override
    public RootGenerator<GammaNode<T, RStarK>> getRootGenerator() {
        return gammaRootGenerator;
    }

    private void savePathComputation(T from, T to, List<Node<T, String>> path) {
        // Create new HashMap at first call for the 2from" state.
        if (!computedPaths.containsKey(from)) {
            computedPaths.put(from, new HashMap<T, List<Node<T, String>>>());
        }
        computedPaths.get(from).put(to, path);
    }


    @Override
    public Collection<GammaNode<T, RStarK>> generateRandomSuccessors(GammaNode<T, RStarK> n, int K, Integer delta) throws IllegalArgumentException, InterruptedException {
        T s = n.getPoint();

        // The successors for this node have already been computed.
        if (computedPaths.containsKey(s)) {
            throw new IllegalArgumentException("Generate sucessors twice for the same node: " + n);
        }
        // Never generate successors for a node goal.
        if (isGoal(n.getPoint())) {
            throw new IllegalArgumentException("The given node is a goal node. Can not generate successors for a goal node.");
        }

        Collection<GammaNode<T,RStarK>> gammaSuccessors = new HashSet<>();



        Node<T, String> parent;
        T currentState;
        Node<T, String> currentNode;

        if (!isGoal(s)) {
            for (int k = 0; k < K; k++) {
                // Generate successor in depth delta.
                List<Node<T, String>> path = new ArrayList<>(K);
                parent = null;
                currentState = s;
                currentNode = new Node(parent, currentState);
                path.add(currentNode);
                for (int i = 0; i < delta; i++) {
                    if (!gammaGoalTester.isGoal(currentState)) {

                        if (graphGenerator.getSuccessorGenerator() instanceof SingleSuccessorGenerator) {
                            int random = new Random().nextInt(Integer.MAX_VALUE);
                            NodeExpansionDescription<T, String> succ = ((SingleSuccessorGenerator) graphGenerator.getSuccessorGenerator()).generateSuccessor(currentState, random);
                            if (succ == null) {
                                throw new IllegalStateException("SingleSucessorGenerator generated no successor for " + currentState.toString());
                            }
                            currentState = succ.getTo();
                            parent = currentNode;
                            currentNode = new Node<>(parent, currentState);
                        } else {
                            List<NodeExpansionDescription<T, String>> succ = graphGenerator.getSuccessorGenerator().generateSuccessors(currentState);
                            if (succ.size() == 0) {
                                boolean goal = gammaGoalTester.isGoal(currentState);
                                throw new IllegalStateException("SuccessorGenerator generated no successor for " + currentState.toString());
                            }
                            int random = new Random().nextInt(succ.size());
                            currentState = succ.get(random).getTo();
                            parent = currentNode;
                            currentNode = new Node<>(parent, currentState);
                        }
                        path.add(currentNode);
                    }
                }
                // System.out.println(String.format("k=%d, Current node = %s, path=%s", k, currentNode, path));
                // Save path in computed paths.
                savePathComputation(s, currentState, path);
                // Create gamma successor and add it, or the previously generate equal gamma node
                // to the list of successors.
                GammaNode<T, RStarK> gammaSucc = new GammaNode<>(currentState);
                if (alreadyGeneratedStates.containsKey(gammaSucc)) {
                    // Already generated before. Take this gamma node instead of the newly generated.
                    gammaSucc = alreadyGeneratedStates.get(gammaSucc);
                } else {
                    alreadyGeneratedStates.put(gammaSucc, gammaSucc);
                }
                // TODO: Since this is a set, there will possible not K successors generated.

                gammaSuccessors.add(gammaSucc);
            }
        }else {
            gammaSuccessors = null;
        }
        // System.out.println("Generated successors " + gammaSuccessors.size() + "is goal = " + isGoal(n.getPoint()));
        return gammaSuccessors;
    }

    @Override
    public PathAndCost computePath(GammaNode<T, RStarK> from, GammaNode<T, RStarK> to) {
        List<Node<T, String>> path = computedPaths.get(from.getPoint()).get(to.getPoint());
        return new PathAndCost(path, h(from, to));
    }


    private boolean isGoal(T n) {
        return ((NodeGoalTester)getGoalTester()).isGoal(n);
    }

    private double h(GammaNode<T, RStarK> n) {
        if (n.getAnnotation("h") != null) {
            return (Double) n.getAnnotation("h");
        } else {
            double h = Double.MAX_VALUE;
            if (isGoal(n.getPoint())) {
                try {
                    h = solutionEvaluator.evaluateSolution(n.externalPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                h = Double.MAX_VALUE;
                try {
                    h = randomCompletionEvaluator.f(n);
                } catch (Throwable w) {
                    System.err.println("Error while trying to compute random completion evaluation." + n + "is goal = " + isGoal(n.getPoint()));
                    w.printStackTrace();
                }
            }
            // System.out.println("Evaluated node " + n + ", h= " + h);
            return h;
        }
    }

    @Override
    public double h(GammaNode<T, RStarK> n1, GammaNode<T, RStarK> n2){
        return h(n1)-h(n2);
    }

    @Override
    public double hToGoal(GammaNode<T, RStarK> from) {
        return h(from);
    }

    @Override
    public double hFromStart(GammaNode<T, RStarK> to) {

        // Calculate minimum h for all start states
        double h_start = Double.MAX_VALUE;

        if (gammaRootGenerator instanceof SingleRootGenerator) {
            h_start = h(((SingleRootGenerator<GammaNode<T,RStarK>>) gammaRootGenerator).getRoot());
        } else {
            assert gammaRootGenerator instanceof MultipleRootGenerator : "Only MultipleRootGenerator or SingleRootGenerators allowed.";
            for (GammaNode<T,RStarK> n : ((MultipleRootGenerator<GammaNode<T,RStarK>>) gammaRootGenerator).getRoots()) {
                if (h(n) < h_start) {
                    h_start = h(n);
                }
            }
        }

        return h_start;
    }

    @Override
    public GoalTester<T> getGoalTester() {
        return graphGenerator.getGoalTester();
    }
}
