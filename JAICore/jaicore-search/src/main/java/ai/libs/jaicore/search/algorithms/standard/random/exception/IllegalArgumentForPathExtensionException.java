package ai.libs.jaicore.search.algorithms.standard.random.exception;

import org.api4.java.datastructure.graph.ILabeledPath;

public class IllegalArgumentForPathExtensionException extends IllegalArgumentException {

	private static final long serialVersionUID = -7700158497338421978L;

	private final transient ILabeledPath<?, ?> path;

	public IllegalArgumentForPathExtensionException(final String message, final ILabeledPath<?, ?> path) {
		super(message);
		this.path = path;
	}

	public ILabeledPath<?, ?> getPath() {
		return this.path;
	}
}
