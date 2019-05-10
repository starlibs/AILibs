package jaicore.search.algorithms.standard.rstar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class GridWorldGammaGraphGenerator implements GammaGraphGenerator<GridWorld, Integer> {

    GammaNode<GridWorld, RStarK> nstart;
    GammaNode<GridWorld, RStarK> ngoal;
//    GraphGenerator<GridWorld, Integer> gg;
    HashMap<GammaNode, GammaNode> alreadyGeneratedStates = new HashMap<>();

    public GridWorldGammaGraphGenerator() {
        nstart = new GammaNode<>(new GridWorld(0, 0));
        ngoal = new GammaNode<>(new GridWorld(15, 15));

        alreadyGeneratedStates.put(nstart, nstart);
        alreadyGeneratedStates.put(ngoal, ngoal);
    }

    @Override
    public RootGenerator<GammaNode<GridWorld, RStarK>> getRootGenerator() {
        return new SingleRootGenerator<GammaNode<GridWorld, RStarK>>() {
            @Override
            public GammaNode<GridWorld, RStarK> getRoot() {
                return nstart;
            }
        };
    }

    @Override
    public Collection<GammaNode<GridWorld, RStarK>> generateRandomSuccessors(GammaNode<GridWorld, RStarK> n, int K, Integer delta) {
        // Use hash set to assure that no state will be added twice to the successors.
        HashSet<GammaNode<GridWorld, RStarK>> succ = new HashSet<>();

        Random r = new Random();

        int posx = n.getPoint().getX();
        int posy = n.getPoint().getY();

        /**
         * Bounds for moving in x-direction and y-direction.
         * Assures that the state is always within [0,15]x[0,15].
         */
        int dx_min = posx < delta ? -posx : -delta;
        int dx_max = 15-posx < delta ? 15-posx : delta;

        int dy_min = posy < delta ? -posy : -delta;
        int dy_max = 15-posy < delta ? 15-posy : delta;

        for(int i = 0; i < K; i++) {
            int dx = r.nextInt(dx_max + 1 - dx_min) + dx_min;
            int dy = r.nextInt(dy_max + 1 - dy_min) + dy_min;

            // Regenerate successor if dx and dx are zero (force moving).
            while (dx == 0 && dy == 0) {
                dx = r.nextInt(dx_max + 1 - dx_min) + dx_min;
                dy = r.nextInt(dy_max + 1 - dy_min) + dy_min;
            }

            assert posx + dx >= 0 || posx + dx <= 15 || posy + dy >= 0 || posy + dy <= 15 :
                    String.format("Calc wrong: dx=%d, dy=%d, posx=%d, posy=%d", dx, dy, posx, posy);



            /**
             * If this state (x,y) has already been generated, use the reference of the
             * first generation of this state.
             * If its the goal state, dont add it twice.
             */
            GammaNode<GridWorld, RStarK> g = new GammaNode<>(new GridWorld(posx + dx, posy + dy));
            if (alreadyGeneratedStates.containsKey(g)) {
                succ.add(alreadyGeneratedStates.get(g));
            } else {
                alreadyGeneratedStates.put(g, g);
                succ.add(g);
            }
        }

        /**
         * If the goal state is within distance delta, add it to list of successors.
         */
        if ((15-posx <= delta) && (15-posy<=delta)) {
            succ.add(ngoal);
        }

        return succ;
    }

    @Override
    public PathAndCost computePath(GammaNode<GridWorld, RStarK> start, GammaNode<GridWorld, RStarK> end) throws InterruptedException {

        SimpleAStarGraphSearch<GridWorld, String> astar = new SimpleAStarGraphSearch<GridWorld, String>(
                new GridWorldBasicGraphGenerator(start.getPoint(), end.getPoint()),
                (n1, n2)->GridWorld.myGrid[n2.getPoint().getX()][n2.getPoint().getY()],
                new GridWorldHeuristic(end.getPoint()));

        PathAndCost pac = astar.solution();
        return pac;

    }

    @Override
    public double h(GammaNode<GridWorld, RStarK> n1, GammaNode<GridWorld, RStarK> n2) {
        return Math.abs(n1.getPoint().getX() - n2.getPoint().getX()) + Math.abs(n1.getPoint().getY() - n2.getPoint().getY());
    }

    @Override
    public double hFromStart(GammaNode<GridWorld, RStarK> to) {
        return h(nstart, to);
    }

    @Override
    public double hToGoal(GammaNode<GridWorld, RStarK> from) {
        return h(from, ngoal);
    }

    @Override
    public GoalTester<GridWorld> getGoalTester() {
        return new NodeGoalTester<GridWorld>() {
            @Override
            public boolean isGoal(GridWorld node) {
                return node.equals(ngoal.getPoint());
            }
        };
    }

}
