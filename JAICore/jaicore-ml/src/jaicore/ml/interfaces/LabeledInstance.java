package jaicore.ml.interfaces;

public interface LabeledInstance<L> extends Instance {
	
	public void setLabel(L label);
	
	public L getLabel();
}
