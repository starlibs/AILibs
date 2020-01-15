package ai.libs.jaicore.search.model.travesaltree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.graph.ReadOnlyPathAccessor;
import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.ENodeAnnotation;

public class BackPointerPath<N, A, V extends Comparable<V>> implements IEvaluatedPath<N, A, V> {
	private final N nodeLabel;
	private final A edgeLabelToParent;
	private boolean goal;
	protected BackPointerPath<N, A, V> parent;
	private final Map<String, Object> annotations = new HashMap<>(); // for nodes effectively examined

	public BackPointerPath(final N point) {
		this(null, point, null);
	}

	public BackPointerPath(final BackPointerPath<N, A, V> parent, final N point, final A edgeLabelToParent) {
		super();
		this.parent = parent;
		this.nodeLabel = point;
		this.edgeLabelToParent = edgeLabelToParent;
	}

	public BackPointerPath<N, A, V> getParent() {
		return this.parent;
	}

	@Override
	public N getHead() {
		return this.nodeLabel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V getScore() {
		return (V) this.annotations.get(ENodeAnnotation.F_SCORE.toString());
	}

	public void setParent(final BackPointerPath<N, A, V> newParent) {
		this.parent = newParent;
	}

	public void setScore(final V internalLabel) {
		this.setAnnotation(ENodeAnnotation.F_SCORE.toString(), internalLabel);
	}

	public void setAnnotation(final String annotationName, final Object annotationValue) {
		this.annotations.put(annotationName, annotationValue);
	}

	public Object getAnnotation(final String annotationName) {
		return this.annotations.get(annotationName);
	}

	public Map<String, Object> getAnnotations() {
		return this.annotations;
	}

	public boolean isGoal() {
		return this.goal;
	}

	public void setGoal(final boolean goal) {
		this.goal = goal;
	}

	public List<BackPointerPath<N, A, V>> path() {
		List<BackPointerPath<N, A, V>> path = new ArrayList<>();
		BackPointerPath<N, A, V> current = this;
		while (current != null) {
			path.add(0, current);
			current = current.parent;
		}
		return path;
	}

	@Override
	public List<N> getNodes() {
		List<N> path = new ArrayList<>();
		BackPointerPath<N, A, V> current = this;
		while (current != null) {
			path.add(0, current.nodeLabel);
			current = current.parent;
		}
		return path;
	}

	public String getString() {
		String s = "Node [ref=";
		s += this.toString();
		s += ", externalLabel=";
		s += this.nodeLabel;
		s += ", goal";
		s += this.goal;
		s += ", parentRef=";
		if (this.parent != null) {
			s += this.parent.toString();
		} else {
			s += "null";
		}
		s += ", annotations=";
		s += this.annotations;
		s += "]";
		return s;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("externalLabel", this.nodeLabel);
		fields.put("goal", this.goal);
		fields.put(ENodeAnnotation.F_SCORE.name(), this.getScore());
		fields.put(ENodeAnnotation.F_ERROR.name(), this.annotations.get(ENodeAnnotation.F_ERROR.name()));
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}

	@Override
	public List<A> getArcs() {
		if (this.parent == null) {
			return new LinkedList<>();
		}
		List<A> pathToHere = this.parent.getArcs();
		pathToHere.add(this.edgeLabelToParent);
		return pathToHere;
	}

	public A getEdgeLabelToParent() {
		return this.edgeLabelToParent;
	}

	@Override
	public N getRoot() {
		return this.parent == null ? this.nodeLabel : this.parent.getRoot();
	}

	@Override
	public BackPointerPath<N, A, V> getPathToParentOfHead() {
		return this.parent;
	}

	@Override
	public int getNumberOfNodes() {
		return this.parent == null ? 1 : this.parent.getNumberOfNodes() + 1;
	}

	@Override
	public boolean isPoint() {
		return this.parent == null;
	}

	@Override
	public BackPointerPath<N, A, V> getPathFromChildOfRoot() {
		if (this.parent.getParent() == null) {
			return new BackPointerPath<>(this.nodeLabel);
		}
		return new BackPointerPath<>(this.parent.getPathFromChildOfRoot(), this.nodeLabel, this.edgeLabelToParent);
	}

	@Override
	public A getInArc(final N node) {
		if (this.nodeLabel.equals(node)) {
			return this.edgeLabelToParent;
		}
		return this.parent.getInArc(node);
	}

	@Override
	public A getOutArc(final N node) {
		if (this.parent == null) {
			throw new NoSuchElementException("No such node found.");
		}
		if (this.parent.nodeLabel.equals(node)) {
			return this.edgeLabelToParent;
		}
		return this.parent.getOutArc(node);
	}

	@Override
	public boolean containsNode(final N node) {
		return this.nodeLabel.equals(node) || (this.parent != null && this.parent.containsNode(node));
	}

	@Override
	public ILabeledPath<N, A> getUnmodifiableAccessor() {
		return new ReadOnlyPathAccessor<>(this);
	}

	@Override
	public N getParentOfHead() {
		return this.parent.getHead();
	}

	@Override
	public void extend(final N newHead, final A arcToNewHead) {
		throw new UnsupportedOperationException("To assure consistency, back-pointer paths do not support modifications.");
	}

	@Override
	public void cutHead() {
		throw new UnsupportedOperationException("To assure consistency, back-pointer paths do not support modifications.");
	}
}