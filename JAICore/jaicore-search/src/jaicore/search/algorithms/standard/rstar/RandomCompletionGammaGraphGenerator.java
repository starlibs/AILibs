package jaicore.search.algorithms.standard.rstar;

import jaicore.basic.sets.SetUtil;
import jaicore.concurrent.TimeoutTimer;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.logging.LoggerUtil;
import jaicore.search.algorithms.interfaces.IPathUnification;
import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.algorithms.standard.bestfirst.RandomCompletionEvaluator;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.*;

import java.util.*;
import java.util.stream.Collectors;

public class RandomCompletionGammaGraphGenerator<T> implements GammaGraphGenerator<T, Integer> {

    // TODO: Warum werden nur 1 bis 2 successors generiert
    // TODO: Kine random completion für goal nodes starten. Sondern da den evaluator nutzen (wieso macht RandomCompletionEvaluator das nicht??).

    private final GraphGenerator<T, String> graphGenerator;
    private final int samples;
    private final int seed;

    private final HashMap<GammaNode, GammaNode> alreadyGeneratedStates = new HashMap<>();
    private final HashMap<T, HashMap<T, Node<T, String>>> computedPaths = new HashMap<>();

    private final RootGenerator<GammaNode<T,RStarK>> gammaRootGenerator;
    private final NodeGoalTester<T> gammaGoalTester;
    private final RandomCompletionEvaluator<T, Double> randomCompletionEvaluator;

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

        /**
         * Create RandomCompletion evaluator.
         */
        IPathUnification<T> pathUnification = new IPathUnification<T>() {
            @Override
            public List<T> getSubsumingKnownPathCompletion(Map<List<T>, List<T>> knownPathCompletions, List<T> path) throws InterruptedException {
                return null;
            }
        };
        randomCompletionEvaluator = new RandomCompletionEvaluator<>(new Random(seed), samples, pathUnification, solutionEvaluator);
        randomCompletionEvaluator.setGenerator(graphGenerator);
    }

    @Override
    public RootGenerator<GammaNode<T, RStarK>> getRootGenerator() {
        return gammaRootGenerator;
    }

    @Override
    public Collection<GammaNode<T, RStarK>> generateRandomSuccessors(GammaNode<T, RStarK> n, int K, Integer delta) throws IllegalArgumentException {
        T s = n.getPoint();
        if (computedPaths.containsKey(s)) {
            // The successors for this node have already been computed.
            throw new IllegalArgumentException("Generate sucessors twice for the same node: " + n);
        }

        Collection<GammaNode<T,RStarK>> gammaSuccessors = new HashSet<>();

        computedPaths.put(s, new HashMap<T, Node<T, String>>());

        Node<T, String> parent = null;
        T currentState = s;
        Node<T, String> currentNode = new Node(parent, currentState);
        for(int k=0; k < K; k++) {
            // Generate successor in depth delta.
            List<Node<T, String>> path = new ArrayList<>(K);
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

            // Save path in computed paths.
            computedPaths.get(s).put(currentState, currentNode);

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

        return gammaSuccessors;
    }

    @Override
    public PathAndCost computePath(GammaNode<T, RStarK> from, GammaNode<T, RStarK> to) {
        return new PathAndCost(computedPaths.get(from.getPoint()).get(to.getPoint()).path(), 0d);
    }




    private double h(GammaNode<T, RStarK> n) {
        if (n.getAnnotation("h") != null) {
            return (Double) n.getAnnotation("h");
        } else {
            ArrayList<Double> results = new ArrayList<>();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    /* compute node label */
                    Double h = null;
                    boolean computationTimedout = false;
                    long startComputation = System.currentTimeMillis();

                    /* set timeout on thread that interrupts it after the timeout */
                    int taskId = -1;
                    if (timeoutForComputationOfH > 0) {
                        if (timeoutSubmitter == null) {
                            timeoutSubmitter = TimeoutTimer.getInstance().getSubmitter();
                        }
                        taskId = timeoutSubmitter.interruptMeAfterMS(timeoutForComputationOfH);
                    }

                    try {
                        h = randomCompletionEvaluator.f(n);

                        /* check whether the required time exceeded the timeout */
                        long fTime = System.currentTimeMillis() - startComputation;

                    } catch (InterruptedException e) {
                        n.setAnnotation("fError", "Timeout");
                        computationTimedout = true;
                        try {
                            h = Double.MAX_VALUE;
                        } catch (Throwable e2) {
                            e2.printStackTrace();
                        }
                    } catch (Throwable e) {
                        System.err.println("Observed an execution during computation of h:");
                        System.err.println(e);
                    } finally {
                        results.add(h);
                    }
                    if (taskId >= 0)
                        timeoutSubmitter.cancelTimeout(taskId);
                    /* register time required to compute this node label */
                    long fTime = System.currentTimeMillis() - startComputation;
                    n.setAnnotation("fTime", fTime);

                }
            });

            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Random compeltion of " + n + " was interrupted.");
            }
            Double h = null;
            if (!results.isEmpty()) {
                System.err.println("No random completion result for node " + n + "found. Using maximal Double value instead.");
                h =  Double.MAX_VALUE;
            } else {
                System.out.println("Random completion h value = " + results.get(0));
                h = results.get(0);
            }

            n.setAnnotation("h", h);
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
