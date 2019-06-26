package ai.libs.jaicore.ml.cache;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Instruction to track a fold-based subset computation for a {@link ReproducibleInstances} object.
 * The original dataset is split into folds using the specified fold technique, e.g., 10-fold cross validation or a simple stratified split.
 * Then, a subset of the created folds is joined as to determine the desired subset of the original set.
 *
 * For example, to obtain the training data in a 5-fold cross validation if the test data corresponds to the 3rd fold, the foldTechnique is "<method for cross validation>(5, <seed>)", and the outIndices are "0,1,3,4"
 *
 * @author fmohr, jnowack
 *
 */
public abstract class FoldBasedSubsetInstruction extends Instruction {

	/**
	 * Constructor to create a split Instruction that can be converted into json.
	 *
	 * @param foldTechnique
	 *            method used to compute the folds
	 */
	public FoldBasedSubsetInstruction(@JsonProperty("foldTechnique") final String foldTechnique) {
	}

	public static FoldBasedSubsetInstruction getInstruction(final String constructor) throws ClassNotFoundException {
		Pattern p = Pattern.compile("([^(]*)\\([^)]*)");
		Matcher m = p.matcher(constructor);
		if (!m.find()) {
			throw new IllegalArgumentException("Invalid syntax for fold-based instruction.");
		}
		Class<?> clazz = Class.forName(m.group(1));
		Object[] args = m.group(2).split(",");
		System.out.println(Arrays.toString(args));
		return null;
	}
}
