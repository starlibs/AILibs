package de.upb.crc901.mlplan.examples.multilabel.meka;

/**
 * Class that holds evaluation data for intermediate solutions.
 * 
 * @author Helena Graf
 *
 */
public class IntermediateSolutionEvent {

	// these variables hold the metric values or null if they could not be computed
	// for the solutione valuation this event represents.
	private String solution;
	private double value;
	private long foundAt;

	/**
	 * Constructs a new intermediate solution event representing an evaluation of
	 * the given solution.
	 * 
	 * @param solution
	 *            the solution for which this solution event contains the measures.
	 * @param value
	 *            the error/ score of the found solution
	 */
	public IntermediateSolutionEvent(String solution, double vaue) {
		this.solution = solution;
		this.foundAt = System.currentTimeMillis();
	}

	/**
	 * Get the solution for which the measures this solution event contains were
	 * made.
	 * 
	 * @return the solution as a command-line String that can be executed
	 */
	public String getSolution() {
		return solution;
	}

	/**
	 * Time in Millis when the solution for this event was found.
	 * 
	 * @return the time at which the solution was found
	 */
	public long getFoundAt() {
		return foundAt;
	}

	/**
	 * Get the value found for the intermediate solution.
	 * 
	 * @return the found value
	 */
	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "solution: " + solution + "\nvalue: " + value + "\nfound at: " + foundAt;
	}

}
