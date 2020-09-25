package ai.libs.jaicore.components.api;

public interface IComponentInstanceConstraint {

	/**
	 * @return true if this rule MUST be satisfied and false if it MUST NOT be satisfied
	 */
	public boolean isPositive();

	/**
	 * @return Component instance used to match an instance
	 */
	public IComponentInstance getPremisePattern();

	/**
	 * @return Component instance the matched instance must or must not satisfy
	 */
	public IComponentInstance getConclusionPattern();
}
