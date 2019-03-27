package jaicore.basic;

import java.util.LinkedList;
import java.util.List;

public class ListHelper {

	/**
	 * Forbid to create an object of ListHelper as there are only static methods allowed here.
	 */
	private ListHelper() {
		// intentionally do nothing
	}

	public static List<String> commaSeparatedStringToList(final String stringList) {
		List<String> values = new LinkedList<>();
		String[] split = stringList.split(",");
		for (String splitElement : split) {
			values.add(splitElement);
		}
		return values;
	}

	public static String implode(final Iterable<? extends Object> collection, final String separator) {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (Object o : collection) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}
			sb.append(o + "");
		}

		return sb.toString();
	}

}
