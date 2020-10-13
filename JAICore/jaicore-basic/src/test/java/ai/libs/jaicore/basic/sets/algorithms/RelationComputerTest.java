package ai.libs.jaicore.basic.sets.algorithms;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.basic.sets.problems.CellPhoneRelationSet;

public abstract class RelationComputerTest extends GeneralAlgorithmTester {

	static Stream<Arguments> getProblemSets() {
		return Stream.of(Arguments.of(new CellPhoneRelationSet()));
	}
}
