package jaicore.graphvisualizer.gui;

import jaicore.graph.IControllableGraphAlgorithm;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import javafx.application.Platform;

/**
 * Class which creates a thread and a VisualizationWindow.
 * @author jkoepe
 */
public class VisualizationWindow<T> {
    /**
     * The Javafx-thread which contains the GUI
     */
    static Thread fxThread;

    /**
     * A recorder which is connected to the algorithm
     */
    Recorder recorder;




    /**
     * The construction of a new VisualizationWindow.
     *
     * @param observable
     * 		The algorithm which should be observed
     * @param title
     * 		The title of the window
     */
    public VisualizationWindow(IObservableGraphAlgorithm observable, String title) {
        if(fxThread == null){
            try{
                fxThread = new Thread(){
                    @Override
                    public void run(){
                        javafx.application.Application.launch(GuiApp.class);
                    }
                };
                fxThread.start();
            }
            catch(IllegalStateException e){
//                e.printStackTrace();
            }
        }


        //try to create a recorder and start the gui in the fxthread.
        //if it fails to create the recorder the system is exited.
        try {
            recorder = new Recorder(observable);
            Platform.runLater(()->{
                GuiApp app = new GuiApp();

                FXCode code = new FXCode(recorder);

            });

        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void addDataSupplier(ISupplier supplier){
        recorder.addDataSupplier(supplier);
    }

}
