package jaicore.ml.core.dataset.attribute.transformer.multivalue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.multivalue.MultiValueAttributeValue;
import jaicore.ml.core.dataset.attribute.transformer.ISingleAttributeTransformer;

/**
 * Converts the sets of multi-value features to short signatures. At first the
 * feature value is transformed into a binaryzation, i.e. a 0/1 Vector, and the
 * MinHashing applied on this vectors afterwards. If two multi-value feature
 * sets are very similar with respect to the Jaccard-Similarity, then the two
 * signatures will be similar as well with a high probability depending on the
 * desired length of the signatures.<br/>
 * For a signature of length n, the same amount of permutations will be created
 * and the n-th element of the signature is determined by the index where the
 * n-th permutation finds the finds the first 1 in the 0/1 Vector.
 * 
 * @author Lukas Brandt
 */
public class MinHashingTransformer implements ISingleAttributeTransformer {

	private int[][] permutations;

	/**
	 * Constructor where the user gives predefined permutations.
	 * 
	 * @param permutations
	 *            Predefined permutations. The amount of permutations defines the
	 *            length of the signature the MinHashing creates and each
	 *            permutation has to have the length of the domain size.
	 */
	public MinHashingTransformer(int[][] permutations) {
		this.permutations = permutations;
	}

	/**
	 * Constructor where suitable permutations are created randomly.
	 * 
	 * @param domainSize
	 * @param signatureLength
	 */
	public MinHashingTransformer(int domainSize, int signatureLength, long seed) {
		permutations = new int[signatureLength][domainSize];
		Random random = new Random(seed);
		for (int i = 0; i < signatureLength; i++) {
			List<Integer> permutation = new ArrayList<>(domainSize);
			for (int p = 0; p < domainSize; p++) {
				permutation.add(p);
			}
			Collections.shuffle(permutation, random);
			for (int p = 0; p < domainSize; p++) {
				permutations[i][p] = permutation.get(p);
			}
		}
	}

	@Override
	public double[] transformAttribute(IAttributeValue<?> attributeToTransform) {
		if (!(attributeToTransform instanceof MultiValueAttributeValue)) {
			throw new IllegalArgumentException("Can only perform Multi-Value Binaryzation for multi-value attributes.");
		}

		// Transform the feature set into a binaryzation.
		MultiValueBinaryzationTransformer binaryzationTransformer = new MultiValueBinaryzationTransformer();
		double[] binaryzation = binaryzationTransformer.transformAttribute(attributeToTransform);

		// Create signature with MinHashing.
		double[] signature = new double[permutations.length];
		for (int i = 0; i < permutations.length; i++) {
			int signatureElement = 0;

			// Search for the first one of the permutation.
			for (int p = 0; p < permutations[i].length; p++) {
				if (binaryzation[permutations[i][p]] == 1) {
					signatureElement = p + 1;
					break;
				}
			}

			signature[i] = signatureElement;
		}

		return signature;
	}

}
