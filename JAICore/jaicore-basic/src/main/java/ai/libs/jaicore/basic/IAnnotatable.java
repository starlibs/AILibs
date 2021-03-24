package ai.libs.jaicore.basic;

public interface IAnnotatable {
	/**
	 * Add an annotation to this component instance.
	 * @param key The key of how to address this annotation.
	 * @param annotation The annotation value.
	 */
	public void putAnnotation(final String key, final String annotation);

	/**
	 * Appends an annotation to a potentially previously annotated string.
	 * @param key The key for which to append the annotation.
	 * @param annotation The annotation value.
	 */
	public void appendAnnotation(final String key, final String annotation);

	/**
	 * Retrieve an annotation by its key.
	 * @param key The key for which to retrieve the annotation.
	 * @return The annotation value.
	 */
	public String getAnnotation(final String key);
}
