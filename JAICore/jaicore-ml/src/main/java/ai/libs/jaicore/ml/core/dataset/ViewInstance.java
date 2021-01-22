package ai.libs.jaicore.ml.core.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public class ViewInstance implements ILabeledInstance {

	private final ILabeledInstance baseInstance;
	private final List<Integer> mask;

	public ViewInstance(final ILabeledInstance baseInstance, final Collection<Integer> mask) {
		super();
		this.baseInstance = baseInstance;
		this.mask = mask instanceof List ? (List<Integer>)mask : new ArrayList<>(mask);
	}

	@Override
	public Object getAttributeValue(final int pos) {
		return this.baseInstance.getAttributeValue(this.mask.get(pos));
	}

	@Override
	public double getPointValue(final int pos) {
		return this.baseInstance.getPointValue(pos);
	}

	@Override
	public Object[] getAttributes() {
		//		System.err.println("atts");
		//		Object[] atts = new Object[this.mask.size()];
		//		for (int i = 0; i < this.mask.size(); i++) {
		//			atts[i] = this.baseInstance.getAttributeValue(this.mask.get(i));
		//		}
		//		return atts;
		throw new IllegalArgumentException();
	}

	@Override
	public int getNumAttributes() {
		return this.mask.size();
	}

	@Override
	public double[] getPoint() {
		double[] atts = new double[this.mask.size()];
		for (int i = 0; i < this.mask.size(); i++) {
			atts[i] = (double)this.baseInstance.getAttributeValue(this.mask.get(i));
		}
		return atts;
	}

	@Override
	public void removeColumn(final int columnPos) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getLabel() {
		return this.baseInstance.getLabel();
	}

	@Override
	public void setLabel(final Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAttributeValue(final int pos, final Object value) {
		throw new UnsupportedOperationException();
	}

}
