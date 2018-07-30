package jaicore.search.algorithms.standard.rstar;

import jaicore.basic.sets.SetUtil;
import jaicore.search.algorithms.interfaces.IPathUnification;
import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.algorithms.standard.bestfirst.RandomCompletionEvaluator;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.*;

import java.util.*;
import java.util.stream.Collectors;

public class RandomCompletionGammaGraphGenerator<T> implements GammaGraphGenerator<T, Integer> {

    private final GraphGenerator<T, String> graphGenerator;
    private final ISolutionEvaluator<T, Double> solutionEvaluator;
    private final int samples;
    private final int seed;

    private final HashMap<GammaNode, GammaNode> alreadyGeneratedStates = new HashMap<>();
    private final HashMap<T, HashMap<T, List<T>>> computedPaths = new HashMap<>();

    private final RootGenerator<GammaNode<T,RStarK>> gammaRootGenerator;
    private final NodeGoalTester<T> gammaGoalTester;
    private final RandomCompletionEvaluator<T, Double> randomCompletionEvaluator;

    public RandomCompletionGammaGraphGenerator(SerializableGraphGenerator<T, String> graphGenerator, ISolutionEvaluator<T, Double> solutionEvaluator, int samples, int seed) {
        this.graphGenerator = graphGenerator;
        this.solutionEvaluator = solutionEvaluator;
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

        computedPaths.put(s, new HashMap<T, List<T>>());

        T current = s;
        for(int k=0; k < K; k++) {
            // Generate successor in depth delta.
            List<T> path = new ArrayList<>(K);
            path.add(current);
            for (int i = 0; i < delta; i++) {
                if (graphGenerator.getSuccessorGenerator() instanceof SingleSuccessorGenerator) {
                    int random = new Random().nextInt(Integer.MAX_VALUE);
                    NodeExpansionDescription<T, String> succ = ((SingleSuccessorGenerator) graphGenerator.getSuccessorGenerator()).generateSuccessor(current, random);
                    current = succ.getTo();
                } else {
                    List<NodeExpansionDescription<T, String>> succ = graphGenerator.getSuccessorGenerator().generateSuccessors(current);
                    int random = new Random().nextInt(succ.size());
                    current = succ.get(random).getTo();
                }
                path.add(current);
            }

            // Save path in computed paths.
            computedPaths.get(s).put(current, path);

            // Create gamma successor and add it, or the previously generate equal gamma node
            // to the list of successors.
            GammaNode<T, RStarK> gammaSucc = new GammaNode<>(current);
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
        return new PathAndCost(computedPaths.get(from.getPoint()).get(to.getPoint()), 0d);
    }

    private double h(GammaNode<T, RStarK> n) {
        if (n.getAnnotation("h") != null) {
            return (Double) n.getAnnotation("h");
        } else {
            Double h;
            try {
                h = randomCompletionEvaluator.f(n);
            } catch (Throwable e) {
                System.out.println(e.getMessage());
                System.out.println("Error: " + e + "Can not compute heuristic for node " + n + "using Double.MAX_VALUE instead.");
                h = Double.MAX_VALUE;
            }
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
