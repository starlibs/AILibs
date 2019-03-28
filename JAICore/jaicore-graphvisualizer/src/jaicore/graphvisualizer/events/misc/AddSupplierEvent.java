package jaicore.graphvisualizer.events.misc;

import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;

/**
 * An event which is used to inform the receiving object about a new supplier.
 *
 */
public class AddSupplierEvent {

	ISupplier supplier;

	public AddSupplierEvent(ISupplier supplier) {
		this.supplier = supplier;
	}

	public ISupplier getSupplier() {
		return supplier;
	}
}
