package jaicore.search.model.travesaltree;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import jaicore.graphvisualizer.TooltipGenerator;

public class NodeTooltipGenerator<N, V extends Comparable<V>> implements TooltipGenerator<Node<N, V>> {

	private final TooltipGenerator<N> tooltipGeneratorForPoints;

	public NodeTooltipGenerator() {
		this(null);
	}

	public NodeTooltipGenerator(TooltipGenerator<N> tooltipGeneratorForPoints) {
		super();
		this.tooltipGeneratorForPoints = tooltipGeneratorForPoints;
	}

	@Override
	public String getTooltip(Node<N, V> node) {
		StringBuilder sb = new StringBuilder();

		Map<String, Object> annotations = node.getAnnotations();
		sb.append("<h2>Annotation</h2><table><tr><th>Key</th><th>Value</th></tr>");
		for (String key : annotations.keySet()) {
			sb.append("<tr><td>" + key + "</td><td>" + annotations.get(key) + "</td></tr>");
		}
		sb.append("</table>");
		sb.append("<h2>F-Value</h2>");
		sb.append(node.getInternalLabel());
		if (annotations.containsKey("fRPSamples")) {
			sb.append(" (based on " + annotations.get("fRPSamples") + " samples)");
		}
		if (annotations.containsKey("fError") && (annotations.get("fError") instanceof Throwable)) {
			sb.append("<h2>Error Details:</h2><pre style=\"color: red;\">");
			Throwable e = (Throwable) annotations.get("fError");
			sb.append("Error Type " + e.getClass().getName() + "\nMessage: " + e.getMessage() + "\nStack Trace:\n");
			for (StackTraceElement ste : e.getStackTrace()) {
				sb.append("  " + ste.toString() + "\n");
			}
			if (e instanceof RuntimeException) {
				Throwable e2 = ((RuntimeException) e).getCause();
				if (e2 != null) {
					sb.append("Sub-Error Type " + e2.getClass().getName() + "\nMessage: " + e2.getMessage() + "\nStack Trace:\n");
					for (StackTraceElement ste : e2.getStackTrace()) {
						sb.append("  " + ste.toString() + "\n");
					}
				} else
					sb.append("No cause was attached.\n");

			} else if (e instanceof InvocationTargetException) {
				Throwable e2 = ((InvocationTargetException) e).getCause();
				if (e2 != null) {
					sb.append("Sub-Error Type " + e2.getClass().getName() + "\nMessage: " + e2.getMessage() + "\nStack Trace:\n");
					for (StackTraceElement ste : e2.getStackTrace()) {
						sb.append("  " + ste.toString() + "\n");
					}
				} else
					sb.append("No cause was attached.\n");

			}
			sb.append("</pre>");
		}
		if (tooltipGeneratorForPoints != null)
			sb.append(tooltipGeneratorForPoints.getTooltip(node.getPoint()));
		return sb.toString();
	}

}
