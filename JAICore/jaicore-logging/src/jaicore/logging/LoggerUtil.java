package jaicore.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jaicore.order.SetUtil.Pair;

public class LoggerUtil {

	public static String getExceptionInfo(Throwable e) {
		return getExceptionInfo(e, new ArrayList<>());
	}
	
	public static String getExceptionInfo(Throwable e, List<Pair<String, Object>> additionalInformationObjects) {

		String eMessage = e.getMessage();
		boolean containsLineBreaks = eMessage != null ? eMessage.contains("\n") : false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("Some detailed information about the execution:");
		if (containsLineBreaks) {
			sb.append("\n\tHere is the full message:\n\t\t");
			sb.append(eMessage.replace("\n", "\n\t\t"));
		}
		sb.append("\n\tHere is the stack trace:");
		Arrays.asList(e.getStackTrace()).forEach(ste -> sb.append("\n\t\t" + ste.toString()));
		while (e.getCause() != null) {
			e = e.getCause();
			sb.append("\n\tCaused by " + e.getClass().getName() + " with message " + e.getMessage()
					+ ". Stack trace of the cause:");
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
}
