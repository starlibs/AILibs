package jaicore.basic.algorithm;

/**
 * This enum encapsulates the states an algorithm may take.
 *
 * @author fmohr, mwever
 */
public enum EAlgorithmState {
	// the algorithm just got created and needs to be initialized first.
	CREATED,
	// the algorithm has already been initialized and is running
	ACTIVE,
	// the algorithm already terminated for whatever reason
	INACTIVE;
}
