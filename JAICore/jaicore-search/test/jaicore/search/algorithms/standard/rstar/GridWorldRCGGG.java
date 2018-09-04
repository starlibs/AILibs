package jaicore.search.algorithms.standard.rstar;

import org.junit.BeforeClass;
import org.junit.Test;

import jaicore.search.core.interfaces.ISolutionEvaluator;

import java.util.List;
import java.util.stream.Collectors;

public class GridWorldRCGGG {

    static GridWorldBasicGraphGenerator graphGenerator;
    static RandomCompletionGammaGraphGenerator<GridWorld> ggg;


    @BeforeClass
    public static void setUp() {
        graphGenerator = new GridWorldBasicGraphGenerator(0, 0, 15, 15);

        ISolutionEvaluator<GridWorld, Double> solutionEvaluator = new ISolutionEvaluator<GridWorld, Double>() {
            @Override
            public Double evaluateSolution(List<GridWorld> solutionPath) throws Exception {
                return null;
            }

            @Override
            public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<GridWorld> partialSolutionPath) {
                return false;
            }

			@Override
			public void cancel() {
				/* nothing to do here */
			}
        };

        ggg = new RandomCompletionGammaGraphGenerator(graphGenerator, solutionEvaluator, 5, 42 );

    }

    @Test
    public void testRandom() {
        RStar<GridWorld, String, Integer> rStar = new RStar<>(ggg, 1, 10, 5);
        rStar.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Exception while sleeping.");
        }
        rStar.interrupt();
        List<GridWorld> solution = rStar.getSolutionPath();
        double costOfSolution = 0;
        for (GridWorld pos : solution)
            costOfSolution += GridWorld.myGrid[pos.getX()][pos.getY()];

        List<GammaNode<GridWorld, RStarK>> gammaSolution = rStar.getGammaSolutionPath();
        //List<GridWorld> intermediateHopsChosenByGammaSolution = gammaSolution.stream().map(n -> n.getPoint()).collect(Collectors.toList());
        System.out.println(gammaSolution);
        //System.out.println(intermediateHopsChosenByGammaSolution);

        System.out.println(solution);
        System.out.println(costOfSolution);

    }

}
