package jaicore.search.algorithms.standard.rstar;

import jaicore.search.algorithms.interfaces.IPathUnification;
import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.standard.awastar.AwaStarSearch;
import jaicore.search.algorithms.standard.bestfirst.RandomCompletionEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.algorithms.standard.uncertainty.BasicUncertaintySource;
import jaicore.search.algorithms.standard.uncertainty.UncertaintyRandomCompletionEvaluator;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.CosinusDistanceComparator;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoNode;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoSelection;
import jaicore.search.evaluationproblems.KnapsackProblem;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class ExperimentRunnerTest {

    static ORGraphSearch<KnapsackProblem.KnapsackNode, String, Double> search;
    static ISolutionEvaluator<KnapsackProblem.KnapsackNode, Double> solutionEvaluator;
    static KnapsackProblem knapsackProblem;
    static IPathUnification<KnapsackProblem.KnapsackNode> pathUnification;


    @BeforeClass
    public static void setUp() {

        knapsackProblem = createRandomKnapsackProblem(PROBLEM_SIZE);

        pathUnification = new IPathUnification<KnapsackProblem.KnapsackNode>() {
            @Override
            public List<KnapsackProblem.KnapsackNode> getSubsumingKnownPathCompletion(
                    Map<List<KnapsackProblem.KnapsackNode>, List<KnapsackProblem.KnapsackNode>> knownPathCompletions,
                    List<KnapsackProblem.KnapsackNode> path) throws InterruptedException {
                return null;
            }
        };

        solutionEvaluator = knapsackProblem.getSolutionEvaluator();

        // Setup search
        ORGraphSearch<KnapsackProblem.KnapsackNode, String, Double> paretoSearch = new ORGraphSearch<>(
                knapsackProblem.getGraphGenerator(),
                new UncertaintyRandomCompletionEvaluator<>(new Random(SEED), 3, pathUnification, solutionEvaluator, new BasicUncertaintySource<>())
        );
        paretoSearch.setOpen(new ParetoSelection<>(new PriorityQueue<>(new CosinusDistanceComparator(-1.0*knapsackProblem.getKnapsackCapacity(), 1.0))));
        search = paretoSearch;
    }

    // @Test
    public void testExperimentRunner() {

        ExperimentRunner<KnapsackProblem.KnapsackNode> er = new ExperimentRunner<KnapsackProblem.KnapsackNode>(search, solutionEvaluator);

        ExperimentRunner.execute(er, 10000);
        System.out.println(er.getBestSolution());
        System.out.println(er.isNoNextSolution());
    }

    @Test
    public void testRStar() {
        RandomCompletionGammaGraphGenerator<KnapsackProblem.KnapsackNode> ggg =
                new RandomCompletionGammaGraphGenerator<>(
                        knapsackProblem.getGraphGenerator(),
                        knapsackProblem.getSolutionEvaluator(),
                        3, SEED);
        int k = 100;
        int delta = 2;
        RStar<KnapsackProblem.KnapsackNode, String, Integer> rstarSearch = new RStar<>(ggg, 0, k, delta, knapsackProblem.getSolutionEvaluator());
        rstarSearch.start();
        try {
            rstarSearch.join(TIMEOUT * 1000);
        } catch (InterruptedException e ) {
            System.out.println("Interrupted while joining RStar.");
            e.printStackTrace();
        }
        ArrayList<KnapsackProblem.KnapsackNode> solution = new ArrayList<>();
        solution.add(rstarSearch.getGoalState());
        double score = Double.MAX_VALUE;
        try {
            score = knapsackProblem.getSolutionEvaluator().evaluateSolution(solution);
        } catch (Exception e) {
            System.err.println("CATCHED HERE");
            e.printStackTrace();
        }
        System.out.println(score);
    }

    @Test
    public void testAWAStar() {

        RandomCompletionEvaluator<KnapsackProblem.KnapsackNode, Double> randomCompletionEvaluator = new RandomCompletionEvaluator<>(
                new Random(SEED),
                3,
                pathUnification,
                knapsackProblem.getSolutionEvaluator()
        );
        randomCompletionEvaluator.setGenerator(knapsackProblem.getGraphGenerator());

        try {
            AwaStarSearch<KnapsackProblem.KnapsackNode, String, Double> awaStarSearch = new AwaStarSearch<>(knapsackProblem.getGraphGenerator(),
                    randomCompletionEvaluator);
            ExperimentRunnerAWAStar er = new ExperimentRunnerAWAStar(awaStarSearch, knapsackProblem.getSolutionEvaluator());
            ExperimentRunnerAWAStar.execute(er, TIMEOUT);
            System.out.println(er.getBestSolution());
            System.out.println(er.isNoNextSolution());

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }



    public static int PROBLEM_SIZE = 50;
    public static int SEED = 42;
    public static int TIMEOUT = 10000;


    public static KnapsackProblem createRandomKnapsackProblem (double problemSize) {
        Random random = new Random((long) problemSize);
        int itemAmount = random.nextInt(((int) problemSize / 2)) + 5;
        HashSet<String> objects = new HashSet<>();
        HashMap<String, Double> values = new HashMap<>();
        HashMap<String, Double> weights = new HashMap<>();
        for (int i = 0; i < itemAmount; i++) {
            String key = String.valueOf(i);
            objects.add(key);
            weights.put(key, random.nextDouble() * problemSize);
            values.put(key, random.nextDouble());
        }
        int bonusCombinationAmount = random.nextInt(((int) problemSize / 10)) + 1;
        HashMap<Set<String>, Double> bonusPoints = new HashMap<>();
        for (int i = 0; i < bonusCombinationAmount; i++) {
            int combinationSize = random.nextInt(((int) itemAmount / 4)) + 2;
            HashSet<String> combination = new HashSet<>();
            for (int o = 0; o < combinationSize; o++) {
                combination.add(String.valueOf(random.nextInt(itemAmount)));
            }
            bonusPoints.put(combination, random.nextDouble());
        }
        return new KnapsackProblem(objects, values, weights, bonusPoints, problemSize);
    }



}
