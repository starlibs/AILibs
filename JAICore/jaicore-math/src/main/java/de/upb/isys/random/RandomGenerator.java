package de.upb.isys.random;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class serves as a way to obtain a globally synchronized random variable. Any part of the system requiring a random number should make use of the random variable provided by this class in order to assure a repeatability of
 * experiments.
 * 
 * @author Alexander Hetzer
 *
 */
public class RandomGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(RandomGenerator.class);

	private static final String INITIALIZING_THE_RANDOM_GENERATOR_TO_SEED = "Initializing the random generator to seed: %d .";
	private static final String INITIALIZING_RANDOM_GENERATOR = "Random number generator not initialized; initializing to 1234.";

	/** The default value for the random value seed. */
	public static final int DEFAULT_SEED = 1234;

	private static Random randomVariable = null;
	private static long seed = -1;

	/**
	 * Hides the public constructor.
	 */
	private RandomGenerator() {
		throw new IllegalAccessError("Utility class");
	}

	/**
	 * Initializes the random generator with the given seed.
	 * 
	 * @param seed
	 *            The random seed to use for initialization.
	 */
	public static void initializeRNG(long seed) {
		RandomGenerator.seed = seed;
		randomVariable = new Random(seed);
		LOGGER.debug(String.format(INITIALIZING_THE_RANDOM_GENERATOR_TO_SEED, seed));
	}

	/**
	 * Returns the random variable of this class.
	 * 
	 * @return The random variable of this class.
	 */
	public static Random getRNG() {
		if (randomVariable == null) {
			LOGGER.warn(INITIALIZING_RANDOM_GENERATOR);
			initializeRNG(DEFAULT_SEED);
		}
		return randomVariable;
	}

	/**
	 * Returns the seed of the random variable singleton.
	 * 
	 * @return The seed of the random variable singleton.
	 */
	public static long getSeed() {
		return seed;
	}

}
