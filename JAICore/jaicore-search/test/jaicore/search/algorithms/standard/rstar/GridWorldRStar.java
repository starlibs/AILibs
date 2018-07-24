package jaicore.search.algorithms.standard.rstar;

import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.graphgenerator.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;

public class GridWorldRStar {

    static GridWorldGammaGraphGenerator ggg;
    static GammaNode root;

    @BeforeClass
    public static void setUp() {
        ggg = new GridWorldGammaGraphGenerator();
        root = ggg.getRoot();
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
    public void gridWorldRStar() {

        RStar<GridWorld, Integer, Integer> rStar = new RStar<>(ggg, 100.0, 88, 5);
        rStar.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Exception while slpeeing.");
        }
        rStar.interrupt();
        List<Node<GridWorld, RStarK>> solution = rStar.getSolutionPath();
        System.out.println(solution);

        System.out.println(rStar.getGammaSolutionPath());




    }
}
