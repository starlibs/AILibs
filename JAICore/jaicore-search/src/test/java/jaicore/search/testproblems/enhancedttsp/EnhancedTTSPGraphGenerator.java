package jaicore.search.testproblems.enhancedttsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graph.LabeledGraph;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SingleSuccessorGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class EnhancedTTSPGraphGenerator implements GraphGenerator<EnhancedTTSPNode, String>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(EnhancedTTSPGraphGenerator.class);

	private final EnhancedTTSP problem;

	public EnhancedTTSPGraphGenerator(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
	}

	@Override
	public SingleRootGenerator<EnhancedTTSPNode> getRootGenerator() {
		return () -> {
			EnhancedTTSPNode root = new EnhancedTTSPNode(this.problem.getStartLocation(), new ShortArrayList(), this.problem.getHourOfDeparture(), 0, 0);
			return root;
		};
	}

	@Override
	public SuccessorGenerator<EnhancedTTSPNode, String> getSuccessorGenerator() {
		return new SingleSuccessorGenerator<EnhancedTTSPNode, String>() {

			private ShortList getPossibleDestinationsThatHaveNotBeenGeneratedYet(final EnhancedTTSPNode n) {
				short curLoc = n.getCurLocation();
				ShortList possibleDestinationsToGoFromhere = new ShortArrayList();
				ShortList seenPlaces = n.getCurTour();
				int k = 0;
				boolean openPlaces = seenPlaces.size() < EnhancedTTSPGraphGenerator.this.problem.getPossibleDestinations().size() - 1;
				assert n.getCurTour().size() < EnhancedTTSPGraphGenerator.this.problem.getPossibleDestinations().size() : "We have already visited everything!";
				assert openPlaces || curLoc != 0 : "There are no open places (out of the " + EnhancedTTSPGraphGenerator.this.problem.getPossibleDestinations().size() + ", " + seenPlaces.size()
				+ " of which have already been seen) but we are still in the initial position. This smells like a strange TSP.";
				if (openPlaces) {
					for (short l : EnhancedTTSPGraphGenerator.this.problem.getPossibleDestinations()) {
						if (k++ == 0) {
							continue;
						}
						if (l != curLoc && !seenPlaces.contains(l)) {
							possibleDestinationsToGoFromhere.add(l);
						}
					}
				} else {
					possibleDestinationsToGoFromhere.add((short) 0);
				}
				return possibleDestinationsToGoFromhere;
			}

			@Override
			public List<NodeExpansionDescription<EnhancedTTSPNode, String>> generateSuccessors(final EnhancedTTSPNode node) {
				List<NodeExpansionDescription<EnhancedTTSPNode, String>> l = new ArrayList<>();
				if (node.getCurTour().size() >= EnhancedTTSPGraphGenerator.this.problem.getPossibleDestinations().size()) {
					EnhancedTTSPGraphGenerator.this.logger.warn("Cannot generate successors of a node in which we are in pos " + node.getCurLocation() + " and in which have already visited everything! " + (EnhancedTTSPGraphGenerator.this.getGoalTester().isGoal(node)
							? "The goal tester detects this as a goal, but the method is invoked nevertheless. Maybe the algorithm that uses this graph generator does not properly check the goal node property. Another possibility is that a goal check DIFFERENT from this one is used"
									: "The goal tester does not detect this as a goal node!"));
					return l;
				}
				ShortList possibleUntriedDestinations = this.getPossibleDestinationsThatHaveNotBeenGeneratedYet(node);
				if (possibleUntriedDestinations.contains(node.getCurLocation())) {
					throw new IllegalStateException("The list of possible destinations must not contain the current position " + node.getCurLocation() + ".");
				}
				int N = possibleUntriedDestinations.size();
				for (int i = 0; i < N; i++) {
					l.add(this.generateSuccessor(node, possibleUntriedDestinations.getShort(i)));
				}
				return l;
			}

			private NodeExpansionDescription<EnhancedTTSPNode, String> generateSuccessor(final EnhancedTTSPNode n, final short destination) {

				EnhancedTTSPGraphGenerator.this.logger.info("Generating successor for node {} to go to destination {}", n, destination);

				if (n.getCurLocation() == destination) {
					throw new IllegalArgumentException("It is forbidden to ask for the successor to the current position as a destination!");
				}

				/*
				 * there is one successor for going to any of the not visited places that can be
				 * reached without making a break and that can reached without violating the
				 * blocking hour constraints
				 */
				short curLocation = n.getCurLocation();
				double curTime = n.getTime();
				double timeSinceLastShortBreak = n.getTimeTraveledSinceLastShortBreak();
				double timeSinceLastLongBreak = n.getTimeTraveledSinceLastLongBreak();

				double minTravelTime = EnhancedTTSPGraphGenerator.this.problem.getMinTravelTimesGraph().getEdgeLabel(curLocation, destination);
				EnhancedTTSPGraphGenerator.this.logger.info("Simulating the ride from {} to " + destination + ", which minimally takes " + minTravelTime + ". We are departing at {}", curLocation, curTime);

				double timeToNextShortBreak = EnhancedTTSPGraphGenerator.this.getTimeToNextShortBreak(curTime, Math.min(timeSinceLastShortBreak, timeSinceLastLongBreak));
				double timeToNextLongBreak = EnhancedTTSPGraphGenerator.this.getTimeToNextLongBreak(curTime, timeSinceLastLongBreak);
				EnhancedTTSPGraphGenerator.this.logger.info("Next short break will be in " + timeToNextShortBreak + "h");
				EnhancedTTSPGraphGenerator.this.logger.info("Next long break will be in " + timeToNextLongBreak + "h");

				/* if we can do the tour without a break, do it */
				boolean arrived = false;
				double shareOfTheTripDone = 0.0;
				double timeOfArrival = -1;
				while (!arrived) {
					double permittedTimeToTravel = Math.min(timeToNextShortBreak, timeToNextLongBreak);
					double travelTimeWithoutBreak = EnhancedTTSPGraphGenerator.this.getActualDrivingTimeWithoutBreak(minTravelTime, curTime, shareOfTheTripDone);
					assert timeToNextShortBreak >= 0 : "Time to next short break cannot be negative!";
					assert timeToNextLongBreak >= 0 : "Time to next long break cannot be negative!";
					assert travelTimeWithoutBreak >= 0 : "Travel time cannot be negative!";
					if (permittedTimeToTravel >= travelTimeWithoutBreak) {
						curTime += travelTimeWithoutBreak;
						arrived = true;
						timeOfArrival = curTime;
						timeSinceLastLongBreak += travelTimeWithoutBreak;
						timeSinceLastShortBreak += travelTimeWithoutBreak;
						EnhancedTTSPGraphGenerator.this.logger.info("\tDriving the remaining distance to goal without a break. This takes " + travelTimeWithoutBreak + " (min time for this distance is " + minTravelTime * (1 - shareOfTheTripDone) + ")");
					} else {

						/* simulate driving to next break */
						EnhancedTTSPGraphGenerator.this.logger.info("\tCurrently achieved " + shareOfTheTripDone + "% of the trip.");
						EnhancedTTSPGraphGenerator.this.logger.info("\tCannot reach the goal within the permitted " + permittedTimeToTravel + ", because the travel without a break would take " + travelTimeWithoutBreak);
						shareOfTheTripDone = EnhancedTTSPGraphGenerator.this.getShareOfTripWhenDrivingOverEdgeAtAGivenTimeForAGivenTimeWithoutDoingABreak(minTravelTime, curTime, shareOfTheTripDone, permittedTimeToTravel);
						EnhancedTTSPGraphGenerator.this.logger.info("\tDriving the permitted " + permittedTimeToTravel + "h. This allows us to finish " + (shareOfTheTripDone * 100) + "% of the trip.");
						if (permittedTimeToTravel == timeToNextLongBreak) {
							EnhancedTTSPGraphGenerator.this.logger.info("\tDo long break, because it is necessary");
						}
						if (permittedTimeToTravel + EnhancedTTSPGraphGenerator.this.problem.getDurationOfShortBreak() + 2 > timeToNextLongBreak) {
							EnhancedTTSPGraphGenerator.this.logger.info("\tDo long break, because short break + 2 hours driving would require a long break anyway");
						}
						boolean doLongBreak = permittedTimeToTravel == timeToNextLongBreak || permittedTimeToTravel + EnhancedTTSPGraphGenerator.this.problem.getDurationOfShortBreak() + 2 >= timeToNextLongBreak;
						if (doLongBreak) {
							curTime += permittedTimeToTravel + EnhancedTTSPGraphGenerator.this.problem.getDurationOfLongBreak();
							EnhancedTTSPGraphGenerator.this.logger.info("\tDoing a long break ({}h)", EnhancedTTSPGraphGenerator.this.problem.getDurationOfLongBreak());
							timeSinceLastShortBreak = 0;
							timeSinceLastLongBreak = 0;
							timeToNextShortBreak = EnhancedTTSPGraphGenerator.this.getTimeToNextShortBreak(curTime, 0);
							timeToNextLongBreak = EnhancedTTSPGraphGenerator.this.getTimeToNextLongBreak(curTime, 0);

						} else {
							double timeElapsed = permittedTimeToTravel + EnhancedTTSPGraphGenerator.this.problem.getDurationOfShortBreak();
							curTime += timeElapsed;
							EnhancedTTSPGraphGenerator.this.logger.info("\tDoing a short break ({}h)", EnhancedTTSPGraphGenerator.this.problem.getDurationOfShortBreak());
							timeSinceLastShortBreak = 0;
							timeSinceLastLongBreak += timeElapsed;
							timeToNextShortBreak = EnhancedTTSPGraphGenerator.this.getTimeToNextShortBreak(curTime, 0);
							timeToNextLongBreak = EnhancedTTSPGraphGenerator.this.getTimeToNextLongBreak(curTime, timeSinceLastLongBreak);
						}
					}
				}
				double travelDuration = curTime - n.getTime();
				EnhancedTTSPGraphGenerator.this.logger.info("Finished travel simulation. Travel duration: {}", travelDuration);

				ShortList tourToHere = new ShortArrayList(n.getCurTour());
				tourToHere.add(destination);
				EnhancedTTSPNode newNode = new EnhancedTTSPNode(destination, tourToHere, timeOfArrival, timeSinceLastShortBreak, timeSinceLastLongBreak);
				// if (!expandedChildrenPerNode.containsKey(n)) {
				// expandedChildrenPerNode.put(n, new ShortArrayList());
				// }
				// expandedChildrenPerNode.get(n).add(destination);
				return new NodeExpansionDescription<EnhancedTTSPNode, String>(n, newNode, n.getCurLocation() + " -> " + destination, NodeType.OR);
			}

			@Override
			public NodeExpansionDescription<EnhancedTTSPNode, String> generateSuccessor(final EnhancedTTSPNode node, final int i) {
				ShortList availableDestinations = this.getPossibleDestinationsThatHaveNotBeenGeneratedYet(node);
				return this.generateSuccessor(node, availableDestinations.getShort(i % availableDestinations.size()));
			}

			@Override
			public boolean allSuccessorsComputed(final EnhancedTTSPNode node) {
				return this.getPossibleDestinationsThatHaveNotBeenGeneratedYet(node).size() == 0;
			}
		};
	}

	private double getTimeToNextShortBreak(final double time, final double timeSinceLastBreak) {
		return this.problem.getMaxConsecutiveDrivingTime() - timeSinceLastBreak;
	}

	private double getTimeToNextLongBreak(final double time, final double timeSinceLastLongBreak) {
		return Math.min(this.getTimeToNextBlock(time), this.problem.getMaxDrivingTimeBetweenLongBreaks() - timeSinceLastLongBreak);
	}

	private double getTimeToNextBlock(final double time) {
		double timeToNextBlock = Math.ceil(time) - time;
		while (!this.problem.getBlockedHours().get((int) Math.round(time + timeToNextBlock) % this.problem.getNumberOfConsideredHours())) {
			timeToNextBlock++;
		}
		return timeToNextBlock;
	}

	public double getActualDrivingTimeWithoutBreak(final double minTravelTime, final double departure, final double shareOfTheTripDone) {
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

	public double getShareOfTripWhenDrivingOverEdgeAtAGivenTimeForAGivenTimeWithoutDoingABreak(final double minTravelTime, final double departureOfCurrentPoint, final double shareOfTripDone, final double drivingTime) {

		/* do a somewhat crude numeric approximation */
		double minTravelTimeForRest = minTravelTime * (1 - shareOfTripDone);
		this.logger.info("\t\tMin travel time for rest: " + minTravelTimeForRest);
		double doableMinTravelTimeForRest = minTravelTimeForRest;
		double estimatedTravelingTimeForThatPortion;
		while ((estimatedTravelingTimeForThatPortion = this.getActualDrivingTimeWithoutBreak(doableMinTravelTimeForRest, departureOfCurrentPoint, shareOfTripDone)) > drivingTime) {
			doableMinTravelTimeForRest -= 0.01;
		}
		this.logger.info("\t\tDoable min travel time for rest: " + doableMinTravelTimeForRest + ". The estimated true travel time for that portion is " + estimatedTravelingTimeForThatPortion);
		double additionalShare = (doableMinTravelTimeForRest / minTravelTimeForRest) * (1 - shareOfTripDone);
		this.logger.info("\t\tAdditional share:" + additionalShare);
		return shareOfTripDone + additionalShare;
	}

	@Override
	public NodeGoalTester<EnhancedTTSPNode> getGoalTester() {
		return n -> {
			return n.getCurTour().size() >= this.problem.getPossibleDestinations().size() && n.getCurLocation() == this.problem.getStartLocation();
		};
	}

	@Override
	public boolean isSelfContained() {
		return true;
	}

	@Override
	public void setNodeNumbering(final boolean nodenumbering) {
	}

	public LabeledGraph<Short, Double> getMinTravelTimesGraph() {
		return this.problem.getMinTravelTimesGraph();
	}

	public static EnhancedTTSP createRandomProblem(final int problemSize, final long seed) {
		Random random = new Random(seed);
		LabeledGraph<Short, Double> minTravelTimesGraph = new LabeledGraph<>();
		List<Pair<Double, Double>> coordinates = new ArrayList<>();
		for (short i = 0; i < problemSize; i++) {
			coordinates.add(new Pair<>(random.nextDouble() * 12, random.nextDouble() * 12));
			minTravelTimesGraph.addItem(i);
		}
		for (short i = 0; i < problemSize; i++) {
			double x1 = coordinates.get(i).getX();
			double y1 = coordinates.get(i).getY();
			for (short j = 0; j < i; j++) {
				double x2 = coordinates.get(j).getX();
				double y2 = coordinates.get(j).getY();
				double minTravelTime = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
				minTravelTimesGraph.addEdge(i, j, minTravelTime);
				minTravelTimesGraph.addEdge(j, i, minTravelTime);
			}
		}
		;
		List<Boolean> blockedHours = Arrays.asList(new Boolean[] { true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true });
		double maxConsecutiveDrivingTime = random.nextInt(5) + 5;
		double durationOfShortBreak = random.nextInt(3) + 3;
		double durationOfLongBreak = random.nextInt(6) + 6;
		return new EnhancedTTSP(minTravelTimesGraph, (short) 0, blockedHours, 8, maxConsecutiveDrivingTime, durationOfShortBreak, durationOfLongBreak);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
