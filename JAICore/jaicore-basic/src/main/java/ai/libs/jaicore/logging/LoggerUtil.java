package ai.libs.jaicore.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ai.libs.jaicore.basic.sets.Pair;

public class LoggerUtil {

	private static final String STR_INDENTED_LB = "\n\t\t";

	private LoggerUtil() {
		/* avoid instantiation */
	}

	public static String getExceptionInfo(final Throwable e) {
		return getExceptionInfo(e, new ArrayList<>());
	}

	public static String getExceptionInfo(final Throwable e, final List<Pair<String, Object>> additionalInformationObjects) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\tError class: ");
		sb.append(e.getClass().getName());
		sb.append("\n\tError message: ");
		if (e.getMessage() != null) {
			sb.append(e.getMessage().replace("\n", STR_INDENTED_LB));
		} else {
			sb.append("NaN");
		}
		sb.append("\n\tError trace:");
		Arrays.asList(e.getStackTrace()).forEach(ste -> sb.append(STR_INDENTED_LB + ste.toString()));
		Throwable current = e;
		while (current.getCause() != null) {
			current = current.getCause();
			sb.append("\n\tCaused by " + current.getClass().getName() + " with message " + current.getMessage() + ". Stack trace of the cause:");
			Arrays.asList(current.getStackTrace()).forEach(ste -> sb.append(STR_INDENTED_LB + ste.toString()));
		}

		/* if additional objects are given, add their content */
		if (additionalInformationObjects != null) {
			for (Pair<String, Object> additionalObject : additionalInformationObjects) {
				sb.append("\n\t" + additionalObject.getX() + STR_INDENTED_LB);
				sb.append(additionalObject.getY().toString().replaceAll("\n", STR_INDENTED_LB));
			}
		}
		return sb.toString();
	}

	public static String logException(final Throwable e) {
		return getExceptionInfo(e, new ArrayList<>());
	}
}
