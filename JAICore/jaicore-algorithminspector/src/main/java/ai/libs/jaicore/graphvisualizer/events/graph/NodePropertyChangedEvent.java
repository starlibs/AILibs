package ai.libs.jaicore.graphvisualizer.events.graph;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class NodePropertyChangedEvent<T> extends AAlgorithmEvent implements GraphEvent {

	private final T node;
	private final Map<String, Object> changedProperties;

	private static Map<String, Object> getMap(final String key, final Object val) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, val);
		return map;
	}

	public NodePropertyChangedEvent(final IAlgorithm<?, ?> algorithm, final T node, final String key, final Object val) {
		this(algorithm, node, getMap(key, val));
	}

	public NodePropertyChangedEvent(final IAlgorithm<?, ?> algorithm, final T node, final Map<String, Object> changedProperties) {
		super(algorithm);
		this.node = node;
		this.changedProperties = changedProperties;
	}

	public T getNode() {
		return this.node;
	}

	public Map<String, Object> getChangedProperties() {
		return this.changedProperties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.changedProperties == null) ? 0 : this.changedProperties.hashCode());
		result = prime * result + ((this.node == null) ? 0 : this.node.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		NodePropertyChangedEvent<?> other = (NodePropertyChangedEvent<?>) obj;
		if (this.changedProperties == null) {
			if (other.changedProperties != null) {
				return false;
			}
		} else if (!this.changedProperties.equals(other.changedProperties)) {
			return false;
		}
		if (this.node == null) {
			if (other.node != null) {
				return false;
			}
		} else if (!this.node.equals(other.node)) {
			return false;
		}
		return true;
	}
}
