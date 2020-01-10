package ai.libs.jaicore.ml.core.dataset.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.api4.java.common.reconstruction.IReconstructionPlan;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.basic.reconstruction.ReconstructionPlan;

public class ReproducibleSplit<D extends IDataset<?>> extends ArrayList<D> implements IReconstructible {

	/**
	 *
	 */
	private static final long serialVersionUID = 5080066497964848476L;
	private final ReconstructionPlan reproductionPlan;

	public ReproducibleSplit(final ReconstructionInstruction creationInstruction, final D dataset, final D...folds) {
		if (!(dataset instanceof IReconstructible)) {
			throw new IllegalArgumentException("The given dataset itself is not reconstructible.");
		}
		Collections.addAll(this, folds);
		List<ReconstructionInstruction> instructions = new ArrayList<>();
		((IReconstructible)dataset).getConstructionPlan().getInstructions().forEach(i -> instructions.add((ReconstructionInstruction)i));
		instructions.add(creationInstruction);
		this.reproductionPlan = new ReconstructionPlan(instructions);
	}

	@Override
	public IReconstructionPlan getConstructionPlan() {
		return this.reproductionPlan;
	}

	@Override
	public void addInstruction(final IReconstructionInstruction instruction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.reproductionPlan == null) ? 0 : this.reproductionPlan.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		ReproducibleSplit other = (ReproducibleSplit) obj;
		if (this.reproductionPlan == null) {
			if (other.reproductionPlan != null) {
				return false;
			}
		} else if (!this.reproductionPlan.equals(other.reproductionPlan)) {
			return false;
		}
		return true;
	}
}