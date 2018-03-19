package jaicore.logging;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import jaicore.basic.SetUtil.Pair;

public class LoggerUtil {
	
	public static void logException(String message, Throwable e, Logger logger) {
		logException(message, e, logger, null);
	}
	
	public static void logException(String message, Throwable e, Logger logger, List<Pair<String,Object>> additionalInformationObjects) {
		try {
			String eMessage = e.getMessage();
			boolean containsLineBreaks = eMessage != null ? eMessage.contains("\n") : false;
			String shortenedEMessage = (eMessage != null && containsLineBreaks) ? eMessage.substring(0, eMessage.indexOf("\n") - 1) : eMessage;
			String shortenedInfo = containsLineBreaks ? " The message has a line break. Enable debug for the full message." : "";
			logger.error("{} Exception is {} with message {}.{} Enable debug mode for stack trace", message, e.getClass().getName(), shortenedEMessage, shortenedInfo);
			if (logger.isDebugEnabled()) {
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
					sb.append("\n\tCaused by " + e.getClass().getName() + " with message " + e.getMessage() + ". Stack trace of the cause:");
					Arrays.asList(e.getStackTrace()).forEach(ste -> sb.append("\n\t\t" + ste.toString()));
				}
				
				/* if additional objects are given, add their content */
				if (additionalInformationObjects != null) {
					for (Pair<String,Object> additionalObject : additionalInformationObjects) {
						sb.append("\n\t" + additionalObject.getX() + "\n\t\t");
						sb.append(additionalObject.getY().toString().replaceAll("\n", "\n\t\t"));
					}
				}
				logger.debug(sb.toString());
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}
