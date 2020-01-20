package ai.libs.jaicore.search.syntheticgraphs.experiments;

import java.util.List;

public interface ITreasureIslandExperimentSetConfig extends ISyntheticSearchExperimentConfig {
	public static final String K_ISLANDS_MAXISLANDSIZE = "maxislandsize";
	public static final String K_ISLANDS_NUMBER_OF_TREASURES = "treasures";
	public static final String K_TREASURE_MODEL = "treasuremodel";

	@Key(K_ISLANDS_MAXISLANDSIZE)
	public List<Double> maxIslandSize();

	@Key(K_ISLANDS_NUMBER_OF_TREASURES)
	public List<Integer> numbersOfTreasureIslands();

	@Key(K_TREASURE_MODEL)
	public List<String> functions();
}
