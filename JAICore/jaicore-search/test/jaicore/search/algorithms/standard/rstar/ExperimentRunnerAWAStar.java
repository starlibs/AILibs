package jaicore.search.algorithms.standard.rstar;

import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.standard.awastar.AwaStarSearch;
import jaicore.search.algorithms.standard.core.ORGraphSearch;

import java.util.List;

public class ExperimentRunnerAWAStar<T> extends Thread {

    private List<T> bestSolution = null;
    private double costOfBestSolution = 0d;

    private AwaStarSearch<T, String, Double> search;
    private ISolutionEvaluator<T, Double> solutionEvaluator;

    private boolean noNextSolution = false;

    public ExperimentRunnerAWAStar(AwaStarSearch<T, String, Double> search, ISolutionEvaluator<T, Double> solutionEvaluator) {
        this.search = search;
        this.solutionEvaluator = solutionEvaluator;
    }

    public static void execute(Thread task, long timeout) {
        task.start();
        try {
            task.join(timeout);
        } catch (InterruptedException e) {
            /* if somebody interrupts us he knows what he is doing */
        }
        if (task.isAlive()) {
            try {

                task.interrupt();
            } catch (IllegalStateException e) {
                System.err.println("Illegal state catched");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while(!isInterrupted()) {
            List<T> currentSolution = search.nextSolution();

            if (currentSolution == null) {
                noNextSolution = true;
                break;
            }

            if (bestSolution == null) {
                bestSolution = currentSolution;
            } else {
                try {
                    double costOfCurrentSolution = solutionEvaluator.evaluateSolution(currentSolution);
                    if (costOfCurrentSolution < costOfBestSolution) {
                        bestSolution = currentSolution;
                        costOfBestSolution = costOfCurrentSolution;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<T> getBestSolution() {
        return bestSolution;
    }

    public double getCostOfBestSolution() {
        return costOfBestSolution;
    }

    public boolean isNoNextSolution() {
        return noNextSolution;
    }
}


