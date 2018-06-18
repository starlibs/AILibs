package jaicore.graphvisualizer.events.add;

import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;

public class AddSupplierEvent {

    ISupplier supplier;

    public AddSupplierEvent(ISupplier supplier){
        this.supplier = supplier;
    }

    public ISupplier getSupplier() {
        return supplier;
    }
}
