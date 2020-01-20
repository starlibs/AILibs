package ai.libs.jaicore.problems.enhancedttsp;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.shorts.Short2DoubleArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2DoubleMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

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
	private final List<Location> locations;
	private final Short2DoubleMap xCoords = new Short2DoubleArrayMap();
	private final Short2DoubleMap yCoords = new Short2DoubleArrayMap();
	private final List<Boolean> blockedHours;
	private final double hourOfDeparture;
	private final double durationOfShortBreak;
	private final double durationOfLongBreak;
	private final double maxConsecutiveDrivingTime;
	private final double maxDrivingTimeBetweenLongBreaks;
	private final ShortList possibleDestinations;

	private final EnhancedTTSPSolutionEvaluator solutionEvaluator;

	private Logger logger = LoggerFactory.getLogger(EnhancedTTSP.class);

	public EnhancedTTSP(final List<Location> locations, final short startLocation, final List<Boolean> blockedHours, final double hourOfDeparture, final double maxConsecutiveDrivingTime,
			final double durationOfShortBreak, final double durationOfLongBreak) {
		super();
		this.startLocation = startLocation;
		this.numberOfConsideredHours = blockedHours.size();
		this.locations = locations;
		for (Location l : locations) {
			this.xCoords.put(l.getId(), l.getX());
			this.yCoords.put(l.getId(), l.getY());
		}
		this.blockedHours = blockedHours;
		this.hourOfDeparture = hourOfDeparture;
		this.durationOfShortBreak = durationOfShortBreak;
		this.durationOfLongBreak = durationOfLongBreak;
		this.maxConsecutiveDrivingTime = maxConsecutiveDrivingTime;
		this.maxDrivingTimeBetweenLongBreaks = 24 - durationOfLongBreak;
		this.possibleDestinations = new ShortArrayList(locations.stream().map(Location::getId).sorted().collect(Collectors.toList()));
		this.solutionEvaluator = new EnhancedTTSPSolutionEvaluator(this);
	}

	public List<Location> getLocations() {
		return this.locations;
	}

	public short getStartLocation() {
		return this.startLocation;
	}

	public int getNumberOfConsideredHours() {
		return this.numberOfConsideredHours;
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

	public EnhancedTTSPState getInitalState() {
		return new EnhancedTTSPState(null, this.startLocation, this.hourOfDeparture, 0, 0);
	}

	public EnhancedTTSPState computeSuccessorState(final EnhancedTTSPState n, final short destination) {

		this.logger.info("Generating successor for node {} to go to destination {}", n, destination);

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

		double minTravelTime = Math.sqrt(Math.pow(this.xCoords.get(curLocation) - this.xCoords.get(destination), 2) + Math.pow(this.yCoords.get(curLocation)- this.yCoords.get(destination), 2)); // use Euclidean distance as min travel time
		this.logger.info("Simulating the ride from {} to {}, which minimally takes {}. We are departing at {}", curLocation, destination , minTravelTime, curTime);

		double timeToNextShortBreak = this.getTimeToNextShortBreak(Math.min(timeSinceLastShortBreak, timeSinceLastLongBreak));
		double timeToNextLongBreak = this.getTimeToNextLongBreak(curTime, timeSinceLastLongBreak);
		this.logger.info("Next short break will be in {}h", timeToNextShortBreak);
		this.logger.info("Next long break will be in {}h", timeToNextLongBreak);

		/* if we can do the tour without a break, do it */
		boolean arrived = false;
		double shareOfTheTripDone = 0.0;
		double timeOfArrival = -1;
		while (!arrived) {
			double permittedTimeToTravel = Math.min(timeToNextShortBreak, timeToNextLongBreak);
			double travelTimeWithoutBreak = this.getActualDrivingTimeWithoutBreak(minTravelTime, curTime, shareOfTheTripDone);
			assert timeToNextShortBreak >= 0 : "Time to next short break cannot be negative!";
			assert timeToNextLongBreak >= 0 : "Time to next long break cannot be negative!";
			assert travelTimeWithoutBreak >= 0 : "Travel time cannot be negative!";
			if (permittedTimeToTravel >= travelTimeWithoutBreak) {
				curTime += travelTimeWithoutBreak;
				arrived = true;
				timeOfArrival = curTime;
				timeSinceLastLongBreak += travelTimeWithoutBreak;
				timeSinceLastShortBreak += travelTimeWithoutBreak;
				this.logger.info("\tDriving the remaining distance to goal without a break. This takes {} (min time for this distance is {})", travelTimeWithoutBreak, minTravelTime * (1 - shareOfTheTripDone));
			} else {

				/* simulate driving to next break */
				this.logger.info("\tCurrently achieved {}% of the trip.", shareOfTheTripDone);
				this.logger.info("\tCannot reach the goal within the permitted {}, because the travel without a break would take {}", permittedTimeToTravel, travelTimeWithoutBreak);
				shareOfTheTripDone = this.getShareOfTripWhenDrivingOverEdgeAtAGivenTimeForAGivenTimeWithoutDoingABreak(minTravelTime, curTime, shareOfTheTripDone, permittedTimeToTravel);
				this.logger.info("\tDriving the permitted {}h. This allows us to finish {}% of the trip.", permittedTimeToTravel, (shareOfTheTripDone * 100));
				if (permittedTimeToTravel == timeToNextLongBreak) {
					this.logger.info("\tDo long break, because it is necessary");
				}
				if (permittedTimeToTravel + this.durationOfShortBreak + 2 > timeToNextLongBreak) {
					this.logger.info("\tDo long break, because short break + 2 hours driving would require a long break anyway");
				}
				boolean doLongBreak = permittedTimeToTravel == timeToNextLongBreak || permittedTimeToTravel + this.durationOfShortBreak + 2 >= timeToNextLongBreak;
				if (doLongBreak) {
					curTime += permittedTimeToTravel + this.durationOfLongBreak;
					this.logger.info("\tDoing a long break ({}h)", this.durationOfLongBreak);
					timeSinceLastShortBreak = 0;
					timeSinceLastLongBreak = 0;
					timeToNextShortBreak = this.getTimeToNextShortBreak(0);
					timeToNextLongBreak = this.getTimeToNextLongBreak(curTime, 0);

				} else {
					double timeElapsed = permittedTimeToTravel + this.durationOfShortBreak;
					curTime += timeElapsed;
					this.logger.info("\tDoing a short break ({}h)", this.durationOfShortBreak);
					timeSinceLastShortBreak = 0;
					timeSinceLastLongBreak += timeElapsed;
					timeToNextShortBreak = this.getTimeToNextShortBreak(0);
					timeToNextLongBreak = this.getTimeToNextLongBreak(curTime, timeSinceLastLongBreak);
				}
			}
		}
		double travelDuration = curTime - n.getTime();
		this.logger.info("Finished travel simulation. Travel duration: {}", travelDuration);

		ShortList tourToHere = new ShortArrayList(n.getCurTour());
		tourToHere.add(destination);
		return new EnhancedTTSPState(n, destination, timeOfArrival, timeSinceLastShortBreak, timeSinceLastLongBreak);
	}

	public ShortList getPossibleRemainingDestinationsInState(final EnhancedTTSPState n) {
		short curLoc = n.getCurLocation();
		ShortList possibleDestinationsToGoFromhere = new ShortArrayList();
		ShortList seenPlaces = n.getCurTour();
		int k = 0;
		boolean openPlaces = seenPlaces.size() < this.getPossibleDestinations().size() - 1;
		if (n.getCurTour().size() >= this.getPossibleDestinations().size()) {
			throw new IllegalArgumentException("We have already visited everything!");
		}
		assert openPlaces || curLoc != 0 : "There are no open places (out of the " + this.getPossibleDestinations().size() + ", " + seenPlaces.size()
		+ " of which have already been seen) but we are still in the initial position. This smells like a strange TSP.";
		if (openPlaces) {
			for (short l : this.getPossibleDestinations()) {
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

	private double getTimeToNextShortBreak(final double timeSinceLastBreak) {
		return this.maxConsecutiveDrivingTime - timeSinceLastBreak;
	}

	private double getTimeToNextLongBreak(final double time, final double timeSinceLastLongBreak) {
		return Math.min(this.getTimeToNextBlock(time), this.maxDrivingTimeBetweenLongBreaks - timeSinceLastLongBreak);
	}

	private double getTimeToNextBlock(final double time) {
		double timeToNextBlock = Math.ceil(time) - time;
		while (!(boolean)this.blockedHours.get((int) Math.round(time + timeToNextBlock) % this.numberOfConsideredHours)) {
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
		travelTime += travelTimeInSevenToNineSlot * 0.5;

		int departureTimeRelativeToFourSixInterval = t > 18 ? t - 24 : t;
		double startFourSixSubInterval = Math.max(16, departureTimeRelativeToFourSixInterval);
		double endFourSixSubInterval = Math.min(18, departureTimeRelativeToFourSixInterval + travelTime);
		double travelTimeInFourToSixSlot = Math.max(0, endFourSixSubInterval - startFourSixSubInterval);
		travelTime += travelTimeInFourToSixSlot * 0.5;
		return travelTime;
	}

	public double getShareOfTripWhenDrivingOverEdgeAtAGivenTimeForAGivenTimeWithoutDoingABreak(final double minTravelTime, final double departureOfCurrentPoint, final double shareOfTripDone, final double drivingTime) {

		/* do a somewhat crude numeric approximation */
		double minTravelTimeForRest = minTravelTime * (1 - shareOfTripDone);
		this.logger.info("\t\tMin travel time for rest: {}", minTravelTimeForRest);
		double doableMinTravelTimeForRest = minTravelTimeForRest;
		double estimatedTravelingTimeForThatPortion = 0;
		while ((estimatedTravelingTimeForThatPortion = this.getActualDrivingTimeWithoutBreak(doableMinTravelTimeForRest, departureOfCurrentPoint, shareOfTripDone)) > drivingTime) {
			doableMinTravelTimeForRest -= 0.01;
		}
		this.logger.info("\t\tDoable min travel time for rest: {}. The estimated true travel time for that portion is {}", doableMinTravelTimeForRest, estimatedTravelingTimeForThatPortion);
		double additionalShare = (doableMinTravelTimeForRest / minTravelTimeForRest) * (1 - shareOfTripDone);
		this.logger.info("\t\tAdditional share: {}", additionalShare);
		return shareOfTripDone + additionalShare;
	}

	public EnhancedTTSPSolutionEvaluator getSolutionEvaluator() {
		return this.solutionEvaluator;
	}

	@Override
	public String toString() {
		return "EnhancedTTSP [startLocation=" + this.startLocation + ", numberOfConsideredHours=" + this.numberOfConsideredHours + ", blockedHours=" + this.blockedHours + ", hourOfDeparture="
				+ this.hourOfDeparture + ", durationOfShortBreak=" + this.durationOfShortBreak + ", durationOfLongBreak=" + this.durationOfLongBreak + ", maxConsecutiveDrivingTime=" + this.maxConsecutiveDrivingTime + ", maxDrivingTimeBetweenLongBreaks="
				+ this.maxDrivingTimeBetweenLongBreaks + ", possibleDestinations=" + this.possibleDestinations + ", solutionEvaluator=" + this.solutionEvaluator + "]";
	}
}
