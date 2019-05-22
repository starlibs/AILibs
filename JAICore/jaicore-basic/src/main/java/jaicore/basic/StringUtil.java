package jaicore.basic;


import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class StringUtil {

	public static char[] getCommonChars(final boolean includeDigits) {
		/* create char array */
		List<Character> chars = new LinkedList<>();
		for (int i = 65; i <= 90; i++) {
			chars.add((char)i);
		}
		for (int i = 97; i <= 122; i++) {
			chars.add((char)i);
		}
		if (includeDigits) {
			for (int i = 48; i <= 57; i++) {
				chars.add((char)i);
			}
		}
		char[] charsAsArray = new char[chars.size()];
		for (int i = 0; i < charsAsArray.length; i++) {
			charsAsArray[i] = chars.get(i);
		}
		return charsAsArray;
	}

	public static String getRandomString(final int size, final char[] chars) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < size; i++) {
			s.append(chars[(int)Math.floor(Math.random() * chars.length)]);
		}
		return s.toString();
	}

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

	public static String implode(final Object[] array) {
		return implode(array, ", ");
	}

	public static String implode(final Set<Object> set) {
		return implode(set, ", ");
	}

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


	public static String[] explode(final String string, final String delimiter) {
		return string.split(delimiter);
	}

	public static String implode(final Collection<Object> list) {
		Object[] array = new Object[list.size()];
		list.toArray(array);
		return implode(array);
	}

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
			array[i] = value.replace("%", "" + (i + 1)).replace("$", String.valueOf((char)(i + 97)));
		}
		return array;
	}

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

	public static String[] shiftFirst(final String[] input){
		String[] output = new String[input.length - 1];
		for (int i = 1; i < input.length; i++) {
			output[i - 1] = input[i];
		}
		return output;
	}

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

	public static String toStringLimited(final Object o, final int limit) {
		String str = o.toString();
		if (str.length() <= limit) {
			return str;
		}
		return str.substring(0, limit - 4) + " ...";
	}
}