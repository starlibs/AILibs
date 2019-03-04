package jaicore.search.testproblems.enhancedttsp;

import java.util.List;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import jaicore.graph.LabeledGraph;
import jaicore.search.core.interfaces.ISolutionEvaluator;

/**
 * This class provides a search graph generator for an augmented version of the timed TSP (TTSP) problem. In TTSP, the original route graph has not only one cost per edge (u,v) but a list of costs where each position in the list represents an hour of departure from u. That is,
 * depending on the time when leaving u, the cost to reach v may vary.
 *
 * The intended time measure is hours. That is, the length of the lists of all edges should be identical and reflect the number of hours considered. Typically, this is 24 or a multiple of that, e.g. 7 * 24.
 *
 * Time is considered cyclic. That is, if you consider 24 hours and the current time is 25.5, then it is automatically set to 1.5.
 *
 * The TTSP is enhanced by the consideration of blocking times for the streets, e.g. on Sundays (as in Germany) or during nights (as in Austria or Switzerland). Blocking times are stored in a separated list of Booleans.
 *
 * Also, the TTSP is enhanced in that driving pauses need to be considered. That is, the driver must only drive x hours in a row and then make a break of duration y. In addition, there must be an uninterrupted break of z hours every day.
 *
 * IT IS ASSUMED THAT BREAKS CAN ONLY BE MADE AT LOCATIONS AND NOT "ON THE ROAD"!
 *
 * @author fmohr
 *
 */
public class EnhancedTTSP {

	private final short startLocation;
	private final int numberOfConsideredHours;
	private final LabeledGraph<Short, Double> minTravelTimesGraph;
	private final List<Boolean> blockedHours;
	private final double hourOfDeparture;
	private final double durationOfShortBreak;
	private final double durationOfLongBreak;
	private final double maxConsecutiveDrivingTime;
	private final double maxDrivingTimeBetweenLongBreaks;
	private final ShortList possibleDestinations;

	public EnhancedTTSP(final LabeledGraph<Short, Double> minTravelTimesGraph, final short startLocation, final List<Boolean> blockedHours, final double hourOfDeparture, final double maxConsecutiveDrivingTime,
			final double durationOfShortBreak, final double durationOfLongBreak) {
		super();
		this.startLocation = startLocation;
		this.numberOfConsideredHours = blockedHours.size();
		this.minTravelTimesGraph = minTravelTimesGraph;
		this.blockedHours = blockedHours;
		this.hourOfDeparture = hourOfDeparture;
		this.durationOfShortBreak = durationOfShortBreak;
		this.durationOfLongBreak = durationOfLongBreak;
		this.maxConsecutiveDrivingTime = maxConsecutiveDrivingTime;
		this.maxDrivingTimeBetweenLongBreaks = 24 - durationOfLongBreak;
		this.possibleDestinations = new ShortArrayList(minTravelTimesGraph.getItems().stream().sorted().collect(Collectors.toList()));
	}

	public short getStartLocation() {
		return this.startLocation;
	}

	public int getNumberOfConsideredHours() {
		return this.numberOfConsideredHours;
	}

	public LabeledGraph<Short, Double> getMinTravelTimesGraph() {
		return this.minTravelTimesGraph;
	}

	public List<Boolean> getBlockedHours() {
		return this.blockedHours;
	}

	public double getHourOfDeparture() {
		return this.hourOfDeparture;
	}

	public double getDurationOfShortBreak() {
		return this.durationOfShortBreak;
	}

	public double getDurationOfLongBreak() {
		return this.durationOfLongBreak;
	}

	public double getMaxConsecutiveDrivingTime() {
		return this.maxConsecutiveDrivingTime;
	}

	public double getMaxDrivingTimeBetweenLongBreaks() {
		return this.maxDrivingTimeBetweenLongBreaks;
	}

	public ShortList getPossibleDestinations() {
		return this.possibleDestinations;
	}

	public ISolutionEvaluator<EnhancedTTSPNode, Double> getSolutionEvaluator() {
		return new ISolutionEvaluator<EnhancedTTSPNode, Double>() {

			@Override
			public Double evaluateSolution(final List<EnhancedTTSPNode> solutionPath) {
				if (solutionPath == null || solutionPath.isEmpty()) {
					return Double.MAX_VALUE;
				}
				return solutionPath.get(solutionPath.size() - 1).getTime() - EnhancedTTSP.this.hourOfDeparture;
			}

			@Override
			public boolean doesLastActionAffectScoreOfAnySubsequentSolution(final List<EnhancedTTSPNode> partialSolutionPath) {
				return true;
			}

			@Override
			public void cancel() {
				/* nothing to do here */
			}
		};
	}
}
