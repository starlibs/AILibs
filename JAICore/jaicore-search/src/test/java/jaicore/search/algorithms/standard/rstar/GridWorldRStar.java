package jaicore.search.algorithms.standard.rstar;

import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.structure.graphgenerator.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class GridWorldRStar {

    static GridWorldGammaGraphGenerator ggg;
    static GammaNode root;

    @BeforeClass
    public static void setUp() {
        ggg = new GridWorldGammaGraphGenerator();
        root = ((SingleRootGenerator<GammaNode<GridWorld, RStarK>>) ggg.getRootGenerator()).getRoot();
    }
    /*
    @Test
    public void testgammaGrapgGenerator() {

        GammaNode root = ggg.getRoot();

        Collection<GammaNode<GridWorld, RStarK>> suc1c = ggg.generateRandomSuccessors(root, 7, 1);
        Collection<GammaNode> succ2 = ggg.generateRandomSuccessors(root, 7, 1);

        Collection<GammaNode<GridWorld, RStarK>> succ = ggg.generateRandomSuccessors(root, 7, 1);

        System.out.println(succ.size());

        for (GammaNode s : succ) {
            for (GammaNode s2 : succ2) {
                if (s == s2) {
                    System.out.println(s);
                }
            }
        }
    }

    @Test
    public void testSuccGenerator() {


        Collection<GammaNode> succ3 = ggg.generateRandomSuccessors(root, 0, 15);
    }
    */

    @Test
    public void gridWorldRStar() throws InterruptedException {

    	/* compute reference solution */
    	SimpleAStarGraphSearch<GridWorld, String> astar = new SimpleAStarGraphSearch<GridWorld, String>(
                new GridWorldBasicGraphGenerator(0, 0, 15, 15),
                (n1, n2)->GridWorld.myGrid[n2.getPoint().getX()][n2.getPoint().getY()],
                new GridWorldHeuristic(new GridWorld(15, 15)));
    	PathAndCost<GridWorld, Double> referenceSolution = astar.solution();
    	List<GridWorld> referenceSolutionPath = referenceSolution.path.stream().map(n -> n.getPoint()).collect(Collectors.toList());
    	System.out.println(referenceSolutionPath);
    	System.out.println(referenceSolution.cost);
    	
        RStar<GridWorld, Integer, Integer> rStar = new RStar<>(ggg, 1.0, 88, 5);
        rStar.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Exception while slpeeing.");
        }
        rStar.interrupt();
        List<GridWorld> solution = rStar.getSolutionPath();
        double costOfSolution = 0;
        for (GridWorld pos : solution)
        	costOfSolution += GridWorld.myGrid[pos.getX()][pos.getY()];
        
        List<GammaNode<GridWorld, RStarK>> gammaSolution = rStar.getGammaSolutionPath();
        List<GridWorld> intermediateHopsChosenByGammaSolution = gammaSolution.stream().map(n -> n.getPoint()).collect(Collectors.toList());
        System.out.println(gammaSolution);
        System.out.println(intermediateHopsChosenByGammaSolution);
        
        System.out.println(solution);
        System.out.println(costOfSolution);
        
        

        Assert.assertTrue(costOfSolution == referenceSolution.cost);


    }
}
