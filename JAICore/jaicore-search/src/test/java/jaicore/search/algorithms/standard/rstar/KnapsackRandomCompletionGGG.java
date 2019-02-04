package jaicore.search.algorithms.standard.rstar;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.testproblems.knapsack.KnapsackProblem;
import jaicore.search.testproblems.npuzzle.standard.NPuzzleGenerator;
import jaicore.search.testproblems.npuzzle.standard.NPuzzleNode;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class KnapsackRandomCompletionGGG {

    static SerializableGraphGenerator<KnapsackProblem.KnapsackNode, String> graphGenerator;
    static RandomCompletionGammaGraphGenerator<KnapsackProblem.KnapsackNode> ggg;
    static ISolutionEvaluator<KnapsackProblem.KnapsackNode, Double> solutionEvaluator;


    @BeforeClass
    public static void setUp() {

        /**
         * Setup Knapsack.
         */
        Set<String> objects;
        Map<String, Double> weights;
        Map<String, Double> values;
        Map<Set<String>, Double> bonusPoints;

        objects = new HashSet<String>();
        for (int i = 0; i < 10; i++) {
            objects.add(String.valueOf(i));
        }
        weights = new HashMap<>();
        weights.put("0", 2.30d);
        weights.put("1", 3.10d);
        weights.put("2", 2.90d);
        weights.put("3", 4.40d);
        weights.put("4", 5.30d);
        weights.put("5", 3.80d);
        weights.put("6", 6.30d);
        weights.put("7", 8.50d);
        weights.put("8", 8.90d);
        weights.put("9", 8.20d);
        values = new HashMap<>();
        values.put("0", 92.0d);
        values.put("1", 57.0d);
        values.put("2", 49.0d);
        values.put("3", 68.0d);
        values.put("4", 60.0d);
        values.put("5", 43.0d);
        values.put("6", 67.0d);
        values.put("7", 84.0d);
        values.put("8", 87.0d);
        values.put("9", 72.0d);
        bonusPoints = new HashMap<>();
        Set<String> bonusCombination = new HashSet<>();
        bonusCombination.add("0");
        bonusCombination.add("2");
        bonusPoints.put(bonusCombination, 25.0d);
        KnapsackProblem knapsackProblem = new KnapsackProblem(objects, values, weights, bonusPoints, 50);


        /**
         * Setup RandomCompletionGammaGraphGenerator.
         */
        graphGenerator = knapsackProblem.getGraphGenerator();
        solutionEvaluator = knapsackProblem.getSolutionEvaluator();
        ggg = new RandomCompletionGammaGraphGenerator(graphGenerator, solutionEvaluator, 5, 42 );

    }

    //@Test
    public void testRandom() {
        RStar<KnapsackProblem.KnapsackNode, String, Integer> rStar = new RStar<>(ggg, 0, 5, 2, solutionEvaluator);
        rStar.start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.out.println("Exception while sleeping.");
        }
        rStar.interrupt();
        System.out.println("Test");
//        List<Node<KnapsackProblem.KnapsackNode, RStarK>> solution = rStar.getSolutionPath();
//        double costOfSolution = 0;
//        for (Node<KnapsackProblem.KnapsackNode, RStarK> pos : solution)
//            costOfSolution += 5;

        List<GammaNode<KnapsackProblem.KnapsackNode, RStarK>> gammaSolution = rStar.getGammaSolutionPath();
        //List<GridWorld> intermediateHopsChosenByGammaSolution = gammaSolution.stream().map(n -> n.getPoint()).collect(Collectors.toList());
        for (GammaNode<KnapsackProblem.KnapsackNode,?> g : gammaSolution) {
            System.out.println(g);
        }
        System.out.println(rStar.getGoalState());
        //System.out.println(intermediateHopsChosenByGammaSolution);

//        System.out.println(solution);
//        System.out.println(costOfSolution);

    }

    @Test
    public void testAsInTest() {

        // RandomCompletionGammaGraphGenerator<KnapsackProblem.KnapsackNode> ggg = new RandomCompletionGammaGraphGenerator<>(knapsackProblem.getGraphGenerator(), knapsackProblem.getSolutionEvaluator(), 3, seed);
        int k, delta;
        double score = Double.MAX_VALUE;
        k = 5;
        delta = 5;
        int timeout = 10;  // seconds

        RStar<KnapsackProblem.KnapsackNode, String, Integer> rstarSearch = new RStar<>(ggg, 0, k, delta, solutionEvaluator);

        try {
            rstarSearch.start();
            rstarSearch.join(timeout * 1000);
        } catch (InterruptedException e ) {
            System.out.println("Interrupted while joining RStar.");
            e.printStackTrace();
        }
        List<KnapsackProblem.KnapsackNode> solution = null;
        if (rstarSearch.getGoalState() != null) {
            try {
                solution = rstarSearch.getSolutionPath();
                if (solution != null) {
                    score = solutionEvaluator.evaluateSolution(solution);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println(solution);
        System.out.println(score);
        /*
        for (GammaNode g : rstarSearch.getGammaSolutionPath()) {
            System.out.println(g);
        }

        System.out.println("##############");

        for (KnapsackProblem.KnapsackNode g : rstarSearch.getSolutionPath()) {
            System.out.println(g);
        }
        */

    }

    @Test
    public void testHashing() {
        SingleRootGenerator<GammaNode<KnapsackProblem.KnapsackNode, RStarK>> rg = (SingleRootGenerator)  ggg.getRootGenerator();
        System.out.println(rg.getRoot().getPoint().hashCode());
    }

}
