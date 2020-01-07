package ai.libs.jaicore.search.algorithms.standard.bestfirst;

public enum ENodeAnnotation {
	F_ERROR("fError"), F_MESSAGE("fMessage"), F_TIME("fTime");

	private String name;

	private ENodeAnnotation(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
