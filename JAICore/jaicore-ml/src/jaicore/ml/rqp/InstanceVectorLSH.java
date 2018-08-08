package jaicore.ml.rqp;

import be.tarsos.lsh.Vector;
import weka.core.Instance;

/**
 * Wrapper to translate a WEKA instance into a Vector for TarsosLSH.
 * Assumes last attribute is target.
 * @author michael
 *
 */

public class InstanceVectorLSH extends Vector {
	
	private static final long serialVersionUID = 2726941798101637667L;
	
	private Instance inst;
	
	public InstanceVectorLSH(Instance inst) {
		super(inst.numAttributes() - 1);
		for (int i = 0; i < inst.numAttributes() - 1; i++) {
			this.set(i, inst.value(i));
		}
	}

	public Instance getInst() {
		return inst;
	}

}
