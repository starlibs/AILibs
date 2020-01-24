package ai.libs.jaicore.search.core.interfaces;

public interface ISuccessorGenerationRelevantRemovalNode {

	public boolean allowsGeneErasure();

	public void eraseGenes();

	public void recoverGenes();
}
