package jaicore.search.testproblems.enhancedttsp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil;
import jaicore.graph.LabeledGraph;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SingleSuccessorGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 * This class provides a search graph generator for an augmented version of the timed TSP (TTSP) problem. In TTSP, the original route graph has not only one cost per edge (u,v) but a list of costs where each position in the list represents
 * an hour of departure from u. That is, depending on the time when leaving u, the cost to reach v may vary.
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

	private static final Logger logger = LoggerFactory.getLogger(EnhancedTTSP.class);

	private final short startLocation;
	private final int numberOfConsideredHours;
	private final LabeledGraph<Short, Double> minTravelTimesGraph;
	private final List<Boolean> blockedHours;
	private final double hourOfDeparture;
	private final double durationOfShortBreak;
	private final double durationOfLongBreak;
	private final double maxConsecutiveDrivingTime;
	private final double maxDrivingTimeBetweenLongBreaks;

	public EnhancedTTSP(LabeledGraph<Short, Double> minTravelTimesGraph, short startLocation, List<Boolean> blockedHours, double hourOfDeparture, double maxConsecutiveDrivingTime,
			double durationOfShortBreak, double durationOfLongBreak) {
		super();
		this.startLocation = startLocation;
		numberOfConsideredHours = blockedHours.size();
		this.minTravelTimesGraph = minTravelTimesGraph;
		this.blockedHours = blockedHours;
		this.hourOfDeparture = hourOfDeparture;
		this.durationOfShortBreak = durationOfShortBreak;
		this.durationOfLongBreak = durationOfLongBreak;
		this.maxConsecutiveDrivingTime = maxConsecutiveDrivingTime;
		this.maxDrivingTimeBetweenLongBreaks = 24 - durationOfLongBreak;
	}

	public SerializableGraphGenerator<EnhancedTTSPNode, String> getGraphGenerator() {
		return new SerializableGraphGenerator<EnhancedTTSPNode, String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public SingleRootGenerator<EnhancedTTSPNode> getRootGenerator() {
				return () -> new EnhancedTTSPNode(startLocation, minTravelTimesGraph.getItems(), hourOfDeparture, 0, 0);
			}

			@Override
			public SuccessorGenerator<EnhancedTTSPNode, String> getSuccessorGenerator() {
				return new SingleSuccessorGenerator<EnhancedTTSPNode, String>() {

					private List<Short> getPossibleDestinations(EnhancedTTSPNode n) {
						List<Short> possibleDestinations = SetUtil.intersection(n.getUnvisitedLocations(), minTravelTimesGraph.getSuccessors(n.getCurLocation())).stream().sorted()
								.collect(Collectors.toList());
						
						/* remove starting position until it is the last unvisited place */
						if (possibleDestinations.size() > 1 && n.getCurLocation() != 0)
							possibleDestinations.remove(0);
						return possibleDestinations;
					}

					@Override
					public List<NodeExpansionDescription<EnhancedTTSPNode, String>> generateSuccessors(EnhancedTTSPNode node) {
						List<NodeExpansionDescription<EnhancedTTSPNode, String>> l = new ArrayList<>();
						List<Short> possibleDestinations = getPossibleDestinations(node);
						int N = possibleDestinations.size();
						for (int i = 0; i < N; i++)
							l.add(generateSuccessor(node, possibleDestinations, i));
						return l;
					}

					public NodeExpansionDescription<EnhancedTTSPNode, String> generateSuccessor(EnhancedTTSPNode n, List<Short> destinations, int i) {

						/*
						 * there is one successor for going to any of the not visited places that can be
						 * reached without making a break and that can reached without violating the
						 * blocking hour constraints
						 */
						short curLocation = n.getCurLocation();
						int N = destinations.size();
						short destination = destinations.get(i % N);
						double curTime = n.getTime();
						double timeSinceLastShortBreak = n.getTimeTraveledSinceLastShortBreak();
						double timeSinceLastLongBreak = n.getTimeTraveledSinceLastLongBreak();

						double minTravelTime = minTravelTimesGraph.getEdgeLabel(curLocation, destination);
						logger.info("Simulating the ride from {} to " + destination + ", which minimally takes " + minTravelTime + ". We are departing at {}", curLocation, curTime);

						double timeToNextShortBreak = getTimeToNextShortBreak(curTime, Math.min(timeSinceLastShortBreak, timeSinceLastLongBreak));
						double timeToNextLongBreak = getTimeToNextLongBreak(curTime, timeSinceLastLongBreak);
						logger.info("Next short break will be in " + timeToNextShortBreak + "h");
						logger.info("Next long break will be in " + timeToNextLongBreak + "h");

						/* if we can do the tour without a break, do it */
						boolean arrived = false;
						double shareOfTheTripDone = 0.0;
						double timeOfArrival = -1;
						while (!arrived) {
							double permittedTimeToTravel = Math.min(timeToNextShortBreak, timeToNextLongBreak);
							double travelTimeWithoutBreak = getActualDrivingTimeWithoutBreak(minTravelTime, curTime, shareOfTheTripDone);
							assert timeToNextShortBreak >= 0 : "Time to next short break cannot be negative!";
							assert timeToNextLongBreak >= 0 : "Time to next long break cannot be negative!";
							assert travelTimeWithoutBreak >= 0 : "Travel time cannot be negative!";
							if (permittedTimeToTravel >= travelTimeWithoutBreak) {
								curTime += travelTimeWithoutBreak;
								arrived = true;
								timeOfArrival = curTime;
								timeSinceLastLongBreak += travelTimeWithoutBreak;
								timeSinceLastShortBreak += travelTimeWithoutBreak;
								logger.info("\tDriving the remaining distance to goal without a break. This takes " + travelTimeWithoutBreak + " (min time for this distance is "
										+ minTravelTime * (1 - shareOfTheTripDone) + ")");
							} else {

								/* simulate driving to next break */
								logger.info("\tCurrently achieved " + shareOfTheTripDone + "% of the trip.");
								logger.info("\tCannot reach the goal within the permitted " + permittedTimeToTravel + ", because the travel without a break would take " + travelTimeWithoutBreak);
								shareOfTheTripDone = getShareOfTripWhenDrivingOverEdgeAtAGivenTimeForAGivenTimeWithoutDoingABreak(minTravelTime, curTime, shareOfTheTripDone, permittedTimeToTravel);
								logger.info("\tDriving the permitted " + permittedTimeToTravel + "h. This allows us to finish " + (shareOfTheTripDone * 100) + "% of the trip.");
								if (permittedTimeToTravel == timeToNextLongBreak)
									logger.info("\tDo long break, because it is necessary");
								if (permittedTimeToTravel + durationOfShortBreak + 2 > timeToNextLongBreak)
									logger.info("\tDo long break, because short break + 2 hours driving would require a long break anyway");
								boolean doLongBreak = permittedTimeToTravel == timeToNextLongBreak || permittedTimeToTravel + durationOfShortBreak + 2 >= timeToNextLongBreak;
								if (doLongBreak) {
									curTime += permittedTimeToTravel + durationOfLongBreak;
									logger.info("\tDoing a long break (" + durationOfLongBreak + "h)");
									timeSinceLastShortBreak = 0;
									timeSinceLastLongBreak = 0;
									timeToNextShortBreak = getTimeToNextShortBreak(curTime, 0);
									timeToNextLongBreak = getTimeToNextLongBreak(curTime, 0);

								} else {
									double timeElapsed = permittedTimeToTravel + durationOfShortBreak;
									curTime += timeElapsed;
									logger.info("\tDoing a short break (" + durationOfShortBreak + "h)");
									timeSinceLastShortBreak = 0;
									timeSinceLastLongBreak += timeElapsed;
									timeToNextShortBreak = getTimeToNextShortBreak(curTime, 0);
									timeToNextLongBreak = getTimeToNextLongBreak(curTime, timeSinceLastLongBreak);
								}
							}
						}
						double travelDuration = curTime - n.getTime();
						logger.info("Finished travel simulation. Travel duration: " + travelDuration);

						Set<Short> unvisitedLocations = new HashSet<>(n.getUnvisitedLocations());
						unvisitedLocations.remove(destination);
						assert unvisitedLocations.size() < n.getUnvisitedLocations().size() : "The unvisited cities haven't been reduced!";
						EnhancedTTSPNode newNode = new EnhancedTTSPNode(destination, unvisitedLocations, timeOfArrival, timeSinceLastShortBreak, timeSinceLastLongBreak);
						return new NodeExpansionDescription<EnhancedTTSPNode, String>(n, newNode, n.getCurLocation() + " -> " + destination, NodeType.OR);
					}

					@Override
					public NodeExpansionDescription<EnhancedTTSPNode, String> generateSuccessor(EnhancedTTSPNode node, int i) {
						return generateSuccessor(node, getPossibleDestinations(node), i);
					}
				};
			}

			private double getTimeToNextShortBreak(double time, double timeSinceLastBreak) {
				return maxConsecutiveDrivingTime - timeSinceLastBreak;
			}

			private double getTimeToNextLongBreak(double time, double timeSinceLastLongBreak) {
				return Math.min(getTimeToNextBlock(time), maxDrivingTimeBetweenLongBreaks - timeSinceLastLongBreak);
			}

			private double getTimeToNextBlock(double time) {
				double timeToNextBlock = Math.ceil(time) - time;
				while (!blockedHours.get((int) Math.round(time + timeToNextBlock) % numberOfConsideredHours))
					timeToNextBlock++;
				return timeToNextBlock;
			}

			public double getActualDrivingTimeWithoutBreak(double minTravelTime, double departure, double shareOfTheTripDone) {
				int t = (int) Math.round(departure);
				double travelTime = minTravelTime * (1 - shareOfTheTripDone);
				int departureTimeRelativeToSevenNineInterval = t > 9 ? t - 24 : t;
				double startSevenNineSubInterval = Math.max(7, departureTimeRelativeToSevenNineInterval);
				double endSevenNineSubInterval = Math.min(9, departureTimeRelativeToSevenNineInterval + travelTime);
				double travelTimeInSevenToNineSlot = Math.max(0, endSevenNineSubInterval - startSevenNineSubInterval);
				// logger.info("Travel time in 7-9 slot: " + travelTimeInSevenToNineSlot
				// + ". Increasing travel time by " + travelTimeInSevenToNineSlot * 0.2);
				travelTime += travelTimeInSevenToNineSlot * 0.5;

				int departureTimeRelativeToFourSixInterval = t > 18 ? t - 24 : t;
				double startFourSixSubInterval = Math.max(16, departureTimeRelativeToFourSixInterval);
				double endFourSixSubInterval = Math.min(18, departureTimeRelativeToFourSixInterval + travelTime);
				double travelTimeInFourToSixSlot = Math.max(0, endFourSixSubInterval - startFourSixSubInterval);
				// logger.info("Increasing travel time by " + travelTimeInFourToSixSlot);
				travelTime += travelTimeInFourToSixSlot * 0.5;
				return travelTime;
			}

			public double getShareOfTripWhenDrivingOverEdgeAtAGivenTimeForAGivenTimeWithoutDoingABreak(double minTravelTime, double departureOfCurrentPoint, double shareOfTripDone,
					double drivingTime) {

				/* do a somewhat crude numeric approximation */
				double minTravelTimeForRest = minTravelTime * (1 - shareOfTripDone);
				logger.info("\t\tMin travel time for rest: " + minTravelTimeForRest);
				double doableMinTravelTimeForRest = minTravelTimeForRest;
				double estimatedTravelingTimeForThatPortion;
				while ((estimatedTravelingTimeForThatPortion = getActualDrivingTimeWithoutBreak(doableMinTravelTimeForRest, departureOfCurrentPoint, shareOfTripDone)) > drivingTime) {
					doableMinTravelTimeForRest -= 0.01;
				}
				logger.info("\t\tDoable min travel time for rest: " + doableMinTravelTimeForRest + ". The estimated true travel time for that portion is " + estimatedTravelingTimeForThatPortion);
				double additionalShare = (doableMinTravelTimeForRest / minTravelTimeForRest) * (1 - shareOfTripDone);
				logger.info("\t\tAdditional share:" + additionalShare);
				return shareOfTripDone + additionalShare;
			}

			@Override
			public NodeGoalTester<EnhancedTTSPNode> getGoalTester() {
				return n -> {
					return n.getUnvisitedLocations().isEmpty() && n.getCurLocation() == startLocation;
				};
			}

			@Override
			public boolean isSelfContained() {
				return true;
			}

			@Override
			public void setNodeNumbering(boolean nodenumbering) {
			}
		};
	}

	public ISolutionEvaluator<EnhancedTTSPNode, Double> getSolutionEvaluator() {
		return new ISolutionEvaluator<EnhancedTTSPNode, Double>() {

			@Override
			public Double evaluateSolution(List<EnhancedTTSPNode> solutionPath) throws Exception {
				if (solutionPath == null || solutionPath.size() == 0) {
					return Double.MAX_VALUE;
				}
				return solutionPath.get(solutionPath.size() - 1).getTime() - hourOfDeparture;
			}

			@Override
			public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<EnhancedTTSPNode> partialSolutionPath) {
				return true;
			}

			@Override
			public void cancel() {
				/* nothing to do here */
			}
		};
	}

	public LabeledGraph<Short, Double> getMinTravelTimesGraph() {
		return minTravelTimesGraph;
	}
}
