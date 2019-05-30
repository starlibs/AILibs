package jaicore.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jaicore.basic.sets.SetUtil.Pair;

public class LoggerUtil {

	public static String getExceptionInfo(final Throwable e) {
		return getExceptionInfo(e, new ArrayList<>());
	}

	public static String getExceptionInfo(Throwable e, final List<Pair<String, Object>> additionalInformationObjects) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\tError class: ");
		sb.append(e.getClass().getName());
		sb.append("\n\tError message: ");
		if (e.getMessage() != null) {
			sb.append(e.getMessage().replace("\n", "\n\t\t"));
		} else {
			sb.append("NaN");
		}
		sb.append("\n\tError trace:");
		Arrays.asList(e.getStackTrace()).forEach(ste -> sb.append("\n\t\t" + ste.toString()));
		while (e.getCause() != null) {
			e = e.getCause();
			sb.append("\n\tCaused by " + e.getClass().getName() + " with message " + e.getMessage() + ". Stack trace of the cause:");
			Arrays.asList(e.getStackTrace()).forEach(ste -> sb.append("\n\t\t" + ste.toString()));
		}

		/* if additional objects are given, add their content */
		if (additionalInformationObjects != null) {
			for (Pair<String, Object> additionalObject : additionalInformationObjects) {
				sb.append("\n\t" + additionalObject.getX() + "\n\t\t");
				sb.append(additionalObject.getY().toString().replaceAll("\n", "\n\t\t"));
			}
		}
		return sb.toString();
	}

	public static String logException(final Throwable e) {
		return getExceptionInfo(e, new ArrayList<>());
	}
}
