package ai.libs.jaicore.search.model.travesaltree;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.checkerframework.checker.units.qual.A;

import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGenerator;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.ENodeAnnotation;

public class JaicoreNodeInfoGenerator<N, V extends Comparable<V>> implements NodeInfoGenerator<BackPointerPath<N, A, V>> {

	private final NodeInfoGenerator<List<N>> nodeInfoGeneratorForPoints;

	public JaicoreNodeInfoGenerator() {
		this(null);
	}

	public JaicoreNodeInfoGenerator(final NodeInfoGenerator<List<N>> nodeInfoGeneratorForPoints) {
		super();
		this.nodeInfoGeneratorForPoints = nodeInfoGeneratorForPoints;
	}

	@Override
	public String generateInfoForNode(final BackPointerPath<N, A, V> node) {
		StringBuilder sb = new StringBuilder();

		Map<String, Object> annotations = node.getAnnotations();

		sb.append("<h2>Annotation</h2><table><tr><th>Key</th><th>Value</th></tr>");
		for (Entry<String, Object> annotationEntry : annotations.entrySet()) {
			if (!annotationEntry.getKey().equals(ENodeAnnotation.F_ERROR.toString())) {
				sb.append("<tr><td>" + annotationEntry.getKey() + "</td><td>" + annotationEntry.getValue() + "</td></tr>");
			}
		}
		sb.append("</table>");
		sb.append("<h2>Node Score</h2>");
		sb.append(annotations.get(ENodeAnnotation.F_SCORE.toString()) + "");
		if (annotations.containsKey("fRPSamples")) {
			sb.append(" (based on " + annotations.get("fRPSamples") + " samples)");
		}
		if (annotations.containsKey(ENodeAnnotation.F_ERROR.toString()) && (annotations.get(ENodeAnnotation.F_ERROR.toString()) instanceof Throwable)) {
			sb.append("<h2>Error Details:</h2><pre style=\"color: red;\">");
			Throwable e = (Throwable) annotations.get(ENodeAnnotation.F_ERROR.toString());
			do {
				sb.append("Error Type " + e.getClass().getName() + "\nMessage: " + e.getMessage() + "\nStack Trace:\n");
				for (StackTraceElement ste : e.getStackTrace()) {
					sb.append("  " + ste.toString() + "\n");
				}
				e = e.getCause();
				if (e != null) {
					sb.append("Caused By: ");
				}
			}
			while (e != null);
			sb.append("</pre>");
		}
		if (this.nodeInfoGeneratorForPoints != null) {
			sb.append(this.nodeInfoGeneratorForPoints.generateInfoForNode(node.getNodes()));
		}
		return sb.toString();
	}

}
