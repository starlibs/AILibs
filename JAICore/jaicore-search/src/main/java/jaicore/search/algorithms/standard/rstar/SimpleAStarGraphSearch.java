package jaicore.search.algorithms.standard.rstar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import jaicore.search.algorithms.standard.astar.AStarEdgeCost;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;

public class SimpleAStarGraphSearch<T, A> {

    GraphGenerator<T, A> graphGenerator;
    HashSet<T> closed = new HashSet<>();

    PriorityQueue<Node<T, Double>> open = new PriorityQueue<>();
    HashMap<T, Node<T,Double>> openContainer = new HashMap<>();

    AStarEdgeCost<T> cost;
    INodeEvaluator<T, Double> h;


    SimpleAStarGraphSearch(GraphGenerator<T, A> graphGenerator, AStarEdgeCost<T> cost, INodeEvaluator<T, Double> h) {
        this.graphGenerator = graphGenerator;
        this.cost = cost;
        this.h = h;
    }

    public PathAndCost<T, Double> solution() throws InterruptedException {

        Node<T, Double> solution = runAstar();
        if (solution != null) {
            List<Node<T, Double>> path = solution.path();
            int depth = path.size() - 1;
            double pathCost = 0;
            if (depth > 0) {
                Iterator<Node<T, Double>> it = path.iterator();
                Node<T, Double> parent = it.next();
                Node<T, Double> current;
                while (it.hasNext()) {
                    current = it.next();
                    pathCost += cost.g(parent, current);
                    parent = current;
                }
            }

            return new PathAndCost<T, Double>(path, pathCost);
        } else {
            return new PathAndCost<T, Double>(null, Double.MAX_VALUE);
        }
    }

    public Node<T, Double> runAstar() throws InterruptedException {
        T root_state = ((SingleRootGenerator<T>)graphGenerator.getRootGenerator()).getRoot();
        Node<T, Double> root = new Node<T, Double>(null, root_state);
        root.setAnnotation("g", 0d);
        root.setInternalLabel(0d);
        open.add(root);
        openContainer.put(root_state, root);

        while (!open.isEmpty()) {

            Node<T,Double> n = open.remove();
            openContainer.remove(n.getPoint());

            if (((NodeGoalTester<T>)graphGenerator.getGoalTester()).isGoal(n.getPoint())) {
                return n;
            }

            closed.add(n.getPoint());

            // expand node
            for (NodeExpansionDescription<T, A> expansionDescription : graphGenerator.getSuccessorGenerator().generateSuccessors(n.getPoint())) {
                T s_ = expansionDescription.getTo();

                /**
                 *
                 */
                Node<T, Double> n_;
                boolean isInOpen;
                if (openContainer.containsKey(s_)) {
                    n_ = openContainer.get(s_);
                    isInOpen = true;
                } else {
                    n_ = new Node<T, Double>(n, s_);
                    isInOpen = false;
                }

                /**
                 *
                 */
                if (closed.contains(s_)) {
                    continue;
                }

                Double g_ = (Double) n.getAnnotation("g") + cost.g(n, n_);

                /**
                 * If node is already in open an the new g_ is worse than the old, do nothing.
                 */
                if (isInOpen && (g_ >= (Double) n_.getAnnotation("g"))) {
                    continue;
                }

                /**
                 * Else, update the f-value to g_+h(n_) for the node n_.
                 */
                n_.setAnnotation("g", g_);
                try {
                    n_.setInternalLabel(g_+h.f(n_));
                } catch (Throwable e) {
                    System.out.println("asd");
                }

                if (isInOpen) {
                    open.remove(n_);
                    open.add(n_);
                } else {
                    open.add(n_);
                    openContainer.put(s_, n_);
                }
            }


        }

        return null;

    }

}
