package jaicore.basic;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * This class provides handy utility functions when dealing with Strings.
 *
 * @author fmohr, mwever
 *
 */
public class StringUtil {

	private StringUtil() {
		// prevent instantiation of this util class.
	}

	/**
	 * Getter for all available common characters of the system. Digits can be included if desired.
	 *
	 * @param includeDigits Flag whether to include digits in the array of the system's common characters.
	 * @return An array of the system's common characters.
	 */
	public static char[] getCommonChars(final boolean includeDigits) {
		/* create char array */
		List<Character> chars = new LinkedList<>();
		for (int i = 65; i <= 90; i++) {
			chars.add((char) i);
		}
		for (int i = 97; i <= 122; i++) {
			chars.add((char) i);
		}
		if (includeDigits) {
			for (int i = 48; i <= 57; i++) {
				chars.add((char) i);
			}
		}
		char[] charsAsArray = new char[chars.size()];
		for (int i = 0; i < charsAsArray.length; i++) {
			charsAsArray[i] = chars.get(i);
		}
		return charsAsArray;
	}

	/**
	 * Returns a random string of a desired length and from a given set of characters.
	 *
	 * @param length The length of the resulting random string.
	 * @param chars The set of characters to be used to generate a random string.
	 * @return The generated random string.
	 */
	public static String getRandomString(final int length, final char[] chars) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < length; i++) {
			s.append(chars[new Random().nextInt(chars.length)]);
		}
		return s.toString();
	}

	/**
	 * Concatenates the string representations of an array of objects using ", " as a separator.
	 * @param array The array of objects of which the string representation is to be concatenated.
	 * @return The concatenated string of the given objects' string representation.
	 */
	public static String implode(final Object[] array) {
		return implode(array, ", ");
	}

	/**
	 * Concatenates the string representations of a set of objects using ", " as a separator.
	 * @param set The set of objects of which the string representation is to be concatenated.
	 * @return The concatenated string of the given objects' string representation.
	 */
	public static String implode(final Set<Object> set) {
		return implode(set, ", ");
	}

	/**
	 * Concatenates the string representations of a set of objects using the specified delimiter as a separator.
	 * @param set The set of objects of which the string representation is to be concatenated.
	 * @param delimter A string separating the respective string representations.
	 * @return The concatenated string of the given objects' string representation.
	 */
	public static String implode(final Collection<?> set, final String delimiter) {
		if (set.isEmpty()) {
			return "";
		}
		StringBuilder s = new StringBuilder();
		for (Object elem : set) {
			s.append(delimiter + elem.toString());
		}
		String result = s.toString();
		return result.substring(delimiter.length(), result.length());
	}

	/**
	 * Concatenates the string representations of a collection of objects using ", " as a separator.
	 * @param collection The set of objects of which the string representation is to be concatenated.
	 * @return The concatenated string of the given objects' string representation.
	 */
	public static String implode(final Collection<Object> collection) {
		Object[] array = new Object[collection.size()];
		collection.toArray(array);
		return implode(array);
	}

	/**
	 * Concatenates the string representations of an array of objects using the specified delimiter as a separator.
	 * @param array The array of objects of which the string representation is to be concatenated.
	 * @param delimter A string separating the respective string representations.
	 * @return The concatenated string of the given objects' string representation.
	 */
	public static String implode(final Object[] array, final String delimiter) {
		StringBuilder s = new StringBuilder();
		if (array == null || array.length == 0) {
			return s.toString();
		}
		for (int i = 0; i < array.length - 1; i++) {
			if (array[i] == null) {
				s.append("NULL" + delimiter);
			} else {
				s.append(array[i].toString() + delimiter);
			}
		}
		if (array[array.length - 1] == null) {
			s.append("NULL");
		} else {
			s.append(array[array.length - 1].toString());
		}
		return s.toString();
	}

	/**
	 * Splits a string using delimiter as a separator of elements into an array.
	 * @param string The string to be split.
	 * @param delimiter A string separating the sub-strings.
	 * @return An array of sub-string which were formerly separeted by the specified delimiter.
	 */
	public static String[] explode(final String string, final String delimiter) {
		return string.split(delimiter);
	}

	/**
	 * Merges two string arrays into one single array.
	 * Note: This is a duplicate for apache-commons-lang: ArrayUtils.addAll(array1, array2);
	 *
	 * @param array1 The first array.
	 * @param array2 The second array.
	 * @return Concatenated array of Strings.
	 */
	public static String[] merge(final String[] array1, final String[] array2) {
		String[] output = new String[array1.length + array2.length];
		for (int i = 0; i < output.length; i++) {
			output[i] = (i < array1.length ? array1[i] : array2[i - array1.length]);
		}
		return output;
	}

	public static String[] getArrayWithValues(final int size, final String value) {
		String[] array = new String[size];
		for (int i = 0; i < size; i++) {
			array[i] = value.replace("%", "" + (i + 1)).replace("$", String.valueOf((char) (i + 97)));
		}
		return array;
	}

	/**
	 * Strips a specific character from a string.
	 *
	 * @param str The string from which the character shall be stripped.
	 * @param c The character to strip.
	 * @return The stripped string where the specified character does not occur any longer.
	 */
	public static String stripChar(final String str, final char c) {
		int length = str.length();
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (str.charAt(i) != c) {
				s.append(str.charAt(i));
			}
		}
		return s.toString();
	}

	/**
	 * Removes the first entry of the value and returns an array containing all values but the first one.
	 *
	 * @param input The array of values to be shifted.
	 * @return The resulting shifted array.
	 */
	public static String[] shiftFirst(final String[] input) {
		String[] output = new String[input.length - 1];
		for (int i = 1; i < input.length; i++) {
			output[i - 1] = input[i];
		}
		return output;
	}

	/**
	 * Translates a binary representation of a string to the respective string.
	 * @param binarySequence The binary encoding of the string.
	 * @return The translated string.
	 */
	public static String fromBinary(String binarySequence) {
		StringBuilder sb = new StringBuilder(); // Some place to store the chars
		binarySequence = binarySequence.replace(" ", "");
		Arrays.stream( // Create a Stream
				binarySequence.split("(?<=\\G.{8})") // Splits the input string into 8-char-sections (Since a char has 8 bits = 1 byte)
		).forEach(s -> // Go through each 8-char-section...
		sb.append((char) Integer.parseInt(s, 2)) // ...and turn it into an int and then to a char
		);

		return sb.toString(); // Output text (t)
	}

	/**
	 * Limits the toString output of an object to a specified length.
	 *
	 * @param o The object for which a string limited string representation is to be obtained.
	 * @param limit The maximum length of the toString output.
	 * @return The resulting (potentially cut) string.
	 */
	public static String toStringLimited(final Object o, final int limit) {
		String str = o.toString();
		if (str.length() <= limit) {
			return str;
		}
		return str.substring(0, limit - 4) + " ...";
	}
}