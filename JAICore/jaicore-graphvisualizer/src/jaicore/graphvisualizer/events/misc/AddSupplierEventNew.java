package jaicore.graphvisualizer.events.misc;

import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;

/**
 * An event which is used to inform the receiving object about a new supplier.
 * @author jkoepe
 *
 */
public class AddSupplierEventNew {

    ISupplier supplier;

    public AddSupplierEventNew(ISupplier supplier){
        this.supplier = supplier;
    }

    public ISupplier getSupplier() {
        return supplier;
    }
}
