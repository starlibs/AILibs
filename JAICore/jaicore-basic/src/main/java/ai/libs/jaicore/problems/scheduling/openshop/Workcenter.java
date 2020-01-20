package ai.libs.jaicore.problems.scheduling.openshop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gonzalo Mejia
 * @author Felix Mohr
 * @version 4.0
 */
public class Workcenter {
	private final String workcenterID; // unique name
	private final List<Machine> machines = new ArrayList<>();  // the machines in this work center
	private final int[][] setupMatrix; // setupMatrix[i][j] defines for each tool the time to move any of its machines from state i into state j

	Workcenter(final String workcenterID,  final int[][] setupMatrix) {
		super();
		this.workcenterID = workcenterID;
		for (int i = 0; i < setupMatrix.length; i++) {
			if (setupMatrix[i][i] != 0) {
				throw new IllegalArgumentException("The diagonal entries of the setup matrix must always be 0.");
			}
		}
		this.setupMatrix = setupMatrix;
	}

	public String getWorkcenterID() {
		return this.workcenterID;
	}

	public List<Machine> getMachines() {
		return Collections.unmodifiableList(this.machines);
	}

	void addMachine(final Machine m) {
		this.machines.add(m);
	}

	public int[][] getSetupMatrix() {
		return this.setupMatrix;
	}
}