package jaicore.CustomDataTypes;

import java.util.HashMap;
import java.util.ArrayList;

/**
 *Table.java - This class is used to store probleminstance and their according solutions and
 *performances for that solution. 
 *
 * @author Helen Beierling
 *
 * @param <I> ProblemInstances saved in the table
 * @param <S> Solutions for a probleminstance
 * @param <P> Performance of a solution for a probleminstance
 */

public class Table<I,S,P> {
	HashMap<ProblemInstance<I>,ArrayList<Tuple<Solution<S>,Performance<P>>>> InformationForRanking;
	
	public Table(){
		this.InformationForRanking = new HashMap<ProblemInstance<I>,ArrayList<Tuple<Solution<S>,Performance<P>>>>();
	}

	/**
	 * Gets the Solutions for a given probleminstance.
	 * @param consideredProblemInstance The consideredProblemInstance
	 * @return Gives an ArrayList with all solutions for a probleminstance
	 */
	ArrayList<Solution<S>> getSolutionforProblemInstanceTable(ProblemInstance<I> consideredProblemInstance){
		ArrayList<Tuple<Solution<S>,Performance<P>>> listOfInformationForProblemInst = this.InformationForRanking.get(consideredProblemInstance);
		ArrayList<Solution<S>> SolutionsForInstance = new ArrayList<Solution<S>>();
		for(Tuple<Solution<S>, Performance<P>> i:listOfInformationForProblemInst) {
			SolutionsForInstance.add(i.getSolution());
		}
		return SolutionsForInstance;
	}
	
	/**
	 * Gets the Performance for a given probleminstance.
	 * @param consideredProblemInstance the considered problemInstance
	 * @return Gives an ArrayList with all performances for a probleminstance
	 */
	ArrayList<Performance<P>> getPerformanceforProblemInstanceTable(ProblemInstance<I> consideredProblemInstance){
		ArrayList<Tuple<Solution<S>, Performance<P>>> listOfInformationForProblemInst = this.InformationForRanking.get(consideredProblemInstance);
		ArrayList<Performance<P>> PerformanceForInstance = new ArrayList<Performance<P>>();
		for(Tuple<Solution<S>, Performance<P>> i:listOfInformationForProblemInst) {
			PerformanceForInstance.add(i.getPerformance());
		}
		return PerformanceForInstance;
	}
	
	/**
	 * Gets the list of all Solutions and the performance values with that 
	 * for a given probleminstance
	 * @param consideredProblemInstance the considered problemInstance
	 * @return Gives an ArrayList of tuple consisting of the solution and its performance
	 */
	ArrayList<Tuple<Solution<S>, Performance<P>>> getInfromationforInstance(ProblemInstance<I> consideredProblemInstance){
		return this.InformationForRanking.get(consideredProblemInstance);
	}
	
	/**
	 * Gets all information for all saved probleminstances
	 * @return The hashmap with the probleminstances as keys and their solutions and the according
	 * performances as values.
	 */
	HashMap<ProblemInstance<I>, ArrayList<Tuple<Solution<S>, Performance<P>>>> getInformationForRanking(){
		return this.InformationForRanking;
	}
	
	/** 
	 * Adds a new probleminstace to the table as well as the according solutions and performances
	 * @param newProblemInstanceForTab the new Instance
	 * @param informationForInstance adds a new key,value pair to the Hashmap Table
	 */
	void addProblemInstanceToTable(ProblemInstance<I> newProblemInstanceForTab, ArrayList<Tuple<Solution<S>, Performance<P>>> informationForInstance){
		this.InformationForRanking.put(newProblemInstanceForTab,informationForInstance);	
	}
}
