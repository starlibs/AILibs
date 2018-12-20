package jaicore.ml.dyadranking.general;

import java.util.Random;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;

/**
 * Simple helper class that can be used in (integration)-tests of the
 * Dyad-Ranker.
 * 
 * @author mirko
 *
 */
public class DyadSupplier {

	private static final int DEFAULT_INSTANCE_SIZE = 20;

	private static final int DEFAULT_ALTERNATIVE_SIZE = 10;

	/**
	 * Returns a random {@link Dyad} using the default sizes for the instance length
	 * and the alternative length.
	 * 
	 * @param seed
	 *            the seed of the random process
	 * @return a random dyad
	 */
	public static Dyad getRandomDyad(int seed) {
		return getRandomDyad(seed, DEFAULT_INSTANCE_SIZE, DEFAULT_ALTERNATIVE_SIZE);
	}

	/**
	 * Returns a random dyad w/o specifying the seed.
	 * 
	 * @return A random dyad.
	 */
	public static Dyad getRandomDyad() {
		return getRandomDyad(new Random().nextInt());
	}

	/**
	 * Returns a random {@link Dyad} that consists of <code>instanceSize</code> many
	 * random double entries and <code>alternativeSize</code> many random
	 * alternative entries.
	 * 
	 * @param seed
	 *            the seed of the random process
	 * @param instanceSize
	 *            the size of the instances
	 * @param alternativeSize
	 *            the size of the alternatives
	 * @return the random dyad.
	 */
	public static Dyad getRandomDyad(int seed, int instanceSize, int alternativeSize) {
		Random rnd = new Random(seed);
		Vector instance = new DenseDoubleVector(instanceSize);
		for (int i = 0; i < instanceSize; i++) {
			instance.setValue(i, rnd.nextGaussian());
		}
		Vector alternatives = new DenseDoubleVector(alternativeSize);
		for (int i = 0; i < alternativeSize; i++) {
			alternatives.setValue(i, rnd.nextGaussian());
		}
		return new Dyad(instance, alternatives);
	}

	/**
	 * Returns a random {@link Dyad} that consists of <code>instanceSize</code> many
	 * random double entries and <code>alternativeSize</code> many random
	 * alternative entries.
	 * 
	 * @param instanceSize
	 *            the size of the instances
	 * @param alternativeSize
	 *            the size of the alternatives
	 * @return the random dyad.
	 */
	public static Dyad getRandomDyad(int alternativeSize, int instanceSize) {
		return getRandomDyad(new Random().nextInt(), instanceSize, alternativeSize);
	}

}
