package ai.libs.jaicore.ml.classification.singlelabel.dataset;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public class SingleLabelClassificationInstance implements ISingleLabelClassificationInstance {

	private ILabeledInstance inst;

	public SingleLabelClassificationInstance(final ILabeledInstance inst) {
		this.inst = inst;
	}

	@Override
	public void setLabel(final Object obj) {
		if (!(obj instanceof Integer)) {
			throw new IllegalArgumentException("Unexpected type for label value. Expecting Integer, got: " + obj.getClass().getName());
		}
		this.inst.setLabel(obj);
	}

	@Override
	public void setAttributeValue(final int pos, final Object value) {
		this.inst.setAttributeValue(pos, value);
	}

	@Override
	public Object getAttributeValue(final int pos) {
		return this.inst.getAttributeValue(pos);
	}

	@Override
	public Object[] getAttributes() {
		return this.inst.getAttributes();
	}

	@Override
	public double[] getPoint() {
		return this.inst.getPoint();
	}

	@Override
	public double getPointValue(final int pos) {
		return this.inst.getPointValue(pos);
	}

	@Override
	public void removeColumn(final int columnPos) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public int getIntLabel() {
		return (Integer) this.inst.getLabel();
	}

}
