package ai.libs.jaicore.search.algorithms.standard.bestfirst;

public enum ENodeAnnotation {
	F_SCORE("f"), F_ERROR("fError"), F_MESSAGE("fMessage"), F_TIME("fTime"), F_UNCERTAINTY("fUncertainty");

	private String name;

	private ENodeAnnotation(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
