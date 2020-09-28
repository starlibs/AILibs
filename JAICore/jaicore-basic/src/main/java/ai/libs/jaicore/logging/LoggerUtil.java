package ai.libs.jaicore.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ai.libs.jaicore.basic.sets.Pair;

public class LoggerUtil {

	public static final String LOGGER_NAME_EXAMPLE = "example"; // name for loggers of an object describing an example usage
	public static final String LOGGER_NAME_TESTER = "tester"; // name for loggers that conduct tests
	public static final String LOGGER_NAME_TESTEDALGORITHM = "testedalgorithm"; // name for loggers of algorithms that ARE tested within a test
	public static final String LOGGER_NAME_EVALUATOR = "evaluator"; // name for loggers that conduct scientific experiments
	public static final String LOGGER_NAME_EVALUATEDALGORITHM = "evaluatedalgorithm"; // name for loggers of algorithms that ARE evaluated in an experiment

	private static final String INDENTED_LINEBREAK = "\n\t\t";

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
			sb.append(e.getMessage().replace("\n", INDENTED_LINEBREAK));
		} else {
			sb.append("NaN");
		}
		sb.append("\n\tError trace:");
		Arrays.asList(e.getStackTrace()).forEach(ste -> sb.append(INDENTED_LINEBREAK + ste.toString()));
		Throwable current = e;
		while (current.getCause() != null) {
			current = current.getCause();
			sb.append("\n\tCaused by " + current.getClass().getName() + " with message " + current.getMessage() + ". Stack trace of the cause:");
			Arrays.asList(current.getStackTrace()).forEach(ste -> sb.append(INDENTED_LINEBREAK + ste.toString()));
		}

		/* if additional objects are given, add their content */
		if (additionalInformationObjects != null) {
			for (Pair<String, Object> additionalObject : additionalInformationObjects) {
				sb.append("\n\t" + additionalObject.getX() + INDENTED_LINEBREAK);
				sb.append(additionalObject.getY().toString().replace("\n", INDENTED_LINEBREAK));
			}
		}
		return sb.toString();
	}

	public static String logException(final Throwable e) {
		return getExceptionInfo(e, new ArrayList<>());
	}
}
