package jaicore.graphvisualizer.gui;

import jaicore.graphvisualizer.INodeDataSupplier;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestGraphGenerator;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

import java.util.ArrayList;
import java.util.List;

public class FXGuiTester2 extends GuiApplication {



    @Override
    void startGui() {
        GraphGenerator generator = new TestGraphGenerator();
        BestFirst bf = new BestFirst<>(generator, n->(double)Math.round(Math.random()*100));

        TooltipGraphDataSupplier dataSupplier = new TooltipGraphDataSupplier();
        dataSupplier.setTooltipGenerator((n -> {
            Node node = (Node) n;
            Comparable c = node.getInternalLabel();
            String s = String.valueOf(c);
            return String.valueOf(s);
        }));

        List<INodeDataSupplier> supplierList = new ArrayList<>();
        supplierList.add(dataSupplier);
        this.gui.open(bf, "Test", supplierList);

        bf.nextSolution();

    }


    public static void main(String[] args){
        launch(args);
    }
}
