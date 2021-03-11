package ai.libs.jaicore.basic;

import java.util.Map;
import java.util.Set;

/**
 * The IAnnotatable interface allows for annotating objects with arbitrary other objects. In this way, additional data that does not directly correspond
 * to the main object can be annotated to it.
 *
 * @author mwever
 */
public interface IAnnotatable {

	/**
	 * Add an annotation to this component instance.
	 * @param key The key of how to address this annotation.
	 * @param annotation The annotation value.
	 */
	default void setAnnotation(final String key, final Object annotation) {
		this.getAnnotations().put(key, annotation);
	}

	/**
	 * Retrieve an annotation by its key.
	 * @param key The key for which to retrieve the annotation.
	 * @return The annotation value.
	 */
	default Object getAnnotation(final String key) {
		return this.getAnnotations().get(key);
	}

	default Set<String> getAnnotationsKeySet() {
		return this.getAnnotations().keySet();
	}

	default boolean hasAnnotation(final String key) {
		return this.getAnnotations().containsKey(key);
	}

	/**
	 * @return A map containing annotations for this object.
	 */
	public Map<String, Object> getAnnotations();

}
