package ai.libs.jaicore.basic.reconstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.api4.java.common.reconstruction.IReconstructionPlan;
import org.api4.java.common.reconstruction.ReconstructionException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReconstructionPlan implements IReconstructionPlan {

	/**
	 *
	 */
	private static final long serialVersionUID = 2149341607637260587L;
	private final List<ReconstructionInstruction> instructions;

	public ReconstructionPlan() {
		super();
		this.instructions = new ArrayList<>(0);
	}

	@JsonCreator
	public ReconstructionPlan(@JsonProperty("instructions") final List<ReconstructionInstruction> instructions) {
		super();
		this.instructions = instructions;
	}

	@Override
	public Object reconstructObject() throws ReconstructionException {
		int n = this.instructions.size();
		Object o = this.instructions.get(0).applyToCreate();
		for (int i = 1; i < n; i++) {
			o = this.instructions.get(i).apply(o);
		}
		return o;
	}

	@Override
	public List<IReconstructionInstruction> getInstructions() {
		return Collections.unmodifiableList(this.instructions);
	}

}
