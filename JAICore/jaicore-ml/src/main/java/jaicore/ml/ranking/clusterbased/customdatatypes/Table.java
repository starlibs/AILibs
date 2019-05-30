package jaicore.ml.ranking.clusterbased.customdatatypes;

import java.util.ArrayList;
import java.util.HashMap;

import jaicore.basic.sets.SetUtil;

/**
 * Table.java - This class is used to store probleminstance and their according solutions and
 * performances for that solution.
 *
 * @author Helen Beierling
 *
 * @param <I> ProblemInstances saved in the table
 * @param <S> Solutions for a probleminstance
 * @param <P> Performance of a solution for a probleminstance
 */

public class Table<I, S, P> {
	private HashMap<ProblemInstance<I>, ArrayList<SetUtil.Pair<S, P>>> informationForRanking;

	public Table() {
		this.informationForRanking = new HashMap<>();
	}

	/**
	 * Gets the Solutions for a given probleminstance.
	 *
	 * @param consideredProblemInstance The consideredProblemInstance
	 * @return Gives an ArrayList with all solutions for a probleminstance
	 */
	ArrayList<S> getSolutionforProblemInstanceTable(final ProblemInstance<I> consideredProblemInstance) {
		ArrayList<SetUtil.Pair<S, P>> listOfInformationForProblemInst = this.informationForRanking.get(consideredProblemInstance);
		ArrayList<S> solutionsForInstance = new ArrayList<>();
		for (SetUtil.Pair<S, P> i : listOfInformationForProblemInst) {
			solutionsForInstance.add(i.getX());
		}
		return solutionsForInstance;
	}

	/**
	 * Gets the Performance for a given probleminstance.
	 *
	 * @param consideredProblemInstance the considered problemInstance
	 * @return Gives an ArrayList with all performances for a probleminstance
	 */
	ArrayList<P> getPerformanceforProblemInstanceTable(final ProblemInstance<I> consideredProblemInstance) {
		ArrayList<SetUtil.Pair<S, P>> listOfInformationForProblemInst = this.informationForRanking.get(consideredProblemInstance);
		ArrayList<P> performanceForInstance = new ArrayList<>();
		for (SetUtil.Pair<S, P> i : listOfInformationForProblemInst) {
			performanceForInstance.add(i.getY());
		}
		return performanceForInstance;
	}

	/**
	 * Gets the list of all Solutions and the performance values with that
	 * for a given probleminstance
	 *
	 * @param consideredProblemInstance the considered problemInstance
	 * @return Gives an ArrayList of tuple consisting of the solution and its performance
	 */
	ArrayList<SetUtil.Pair<S, P>> getInfromationforInstance(final ProblemInstance<I> consideredProblemInstance) {
		return this.informationForRanking.get(consideredProblemInstance);
	}

	/**
	 * Gets all information for all saved probleminstances
	 *
	 * @return The hashmap with the probleminstances as keys and their solutions and the according
	 *         performances as values.
	 */
	HashMap<ProblemInstance<I>, ArrayList<SetUtil.Pair<S, P>>> getInformationForRanking() {
		return this.informationForRanking;
	}

	/**
	 * Adds a new probleminstace to the table as well as the according solutions and performances
	 *
	 * @param newProblemInstanceForTab the new Instance
	 * @param informationForInstance adds a new key,value pair to the Hashmap Table
	 */
	void addProblemInstanceToTable(final ProblemInstance<I> newProblemInstanceForTab, final ArrayList<SetUtil.Pair<S, P>> informationForInstance) {
		this.informationForRanking.put(newProblemInstanceForTab, informationForInstance);
	}
}
