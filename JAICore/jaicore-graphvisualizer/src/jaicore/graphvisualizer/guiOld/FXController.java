package jaicore.graphvisualizer.guiOld;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.ClassPath;

import jaicore.graphvisualizer.NodeListener;
import jaicore.graphvisualizer.SearchVisualizationPanel;
import jaicore.graphvisualizer.events.controlEvents.*;
import jaicore.graphvisualizer.events.misc.AddSupplierEvent;
import jaicore.graphvisualizer.events.misc.InfoEvent;
import jaicore.graphvisualizer.events.misc.RequestSuppliersEvent;
import jaicore.graphvisualizer.guiOld.dataSupplier.ISupplier;
import jaicore.graphvisualizer.guiOld.dataVisualizer.IVisualizer;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
/**
 * A class which is used to controll the gui.
 * The gui itself is created with a fxml-file and a file-loader.
 * @author jkoepe
 *
 */
public class FXController implements Initializable, NodeListener {

    //FXMl objects
    @FXML
    public Slider speedSlider;
    @FXML
    public SwingNode visuPanel;
    @FXML
    public Slider timeline;
    @FXML
    public TabPane tabPane;
    @FXML
    public ToolBar toolbar;	
    @FXML
    public RadioButton livebutton;

    //control variables
    private int index;
    private int maxIndex;
    private long sleepTime;
    private int numberSuppliers;
    private boolean live;

    //EventBus
    private EventBus controlEventBus;

    //Thread for playing
    private Thread playThread;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.index = 0;
        this.maxIndex = 0;
        this.sleepTime = 50;
        this.numberSuppliers = 0;
        this.live = false;

        this.controlEventBus = new EventBus();

        initializeVisualization(visuPanel);

          /*
        if the slider for replay-speed is released, wait (200 ms - the value of the slider)
        the slider has a range from 0 to 200
        */
        speedSlider.setOnMouseReleased((MouseEvent event)-> {
            sleepTime = (long) (200 - speedSlider.getValue());
        });
        speedSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double aDouble) {
                Double speed = 200 - aDouble;
                return String.valueOf(speed.longValue());
            }

            @Override
            public Double fromString(String s) {
                return null;
            }
        });

        timeline.setOnMouseReleased((MouseEvent event)->{
            int nIndex = (int) timeline.getValue();
            jumpTo(nIndex);
        });
        
       

    }

    /**
     * Used to initiaize the swingNode with the SearchVisualizationPanel
     * @param swingNode
     */
    private void initializeVisualization(SwingNode swingNode) {
        SearchVisualizationPanel visu = new SearchVisualizationPanel();
        visu.addNodeListener(this);

        SwingUtilities.invokeLater(()->swingNode.setContent(visu));
    }

    /**
     * A function which is called by pressing the play-button.
     * @param actionEvent
     */
    @FXML
    public void play(ActionEvent actionEvent) {
    	//play runs in an own thread to make it stoppable 
        Runnable run = ()->{
            try{
                while ((index < maxIndex && index >= 0) || live){
                    this.step(null);
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                }

            }
            catch(InterruptedException e){
//                e.printStackTrace();
            }
        };

        playThread = new Thread(run);
        playThread.start();

    }

    /**
     * Posts a stepEvent which goes one step forward
     * @param actionEvent
     */
    @FXML
    public void step(ActionEvent actionEvent) {
//        if(index == maxIndex && ! live)
//            return;
    	if(index == maxIndex) {
    		this.controlEventBus.post(new AlgorithmEvent(null));
    		return;
    	}

        this.controlEventBus.post(new StepEvent(true, 1));

        if(! live)
            this.index ++;
        this.timeline.setValue(index);
    }


    /**
     * Posts a stepEvent which goes one step backward
     * @param actionEvent
     */
    @FXML
    public void back(ActionEvent actionEvent) {
        if(index == 0)
            return;
        if(index == 1) {
            this.reset(null);
            return;
        }
//        this.live = false;
//        this.controlEventBus.post(new IsLiveEvent(false));
        if(live)
            this.livebutton.fire();
        this.controlEventBus.post(new StepEvent(false, 1));
        this.index --;
        timeline.setValue(index);
    }

    /**
     * Sends a reset-Event and resets every part which is implemented in this class
     * @param actionEvent
     */
    @FXML
    public void reset(ActionEvent actionEvent) {
        if(this.playThread != null)
            playThread.interrupt();
        if(live)
            this.livebutton.fire();
        this.controlEventBus.post(new ResetEvent());
        this.index = 0;
        SearchVisualizationPanel panel = (SearchVisualizationPanel) visuPanel.getContent();
        panel.reset();
        timeline.setValue(index);
    }

    /**
     * Stops a replay
     * @param actionEvent
     */
    @FXML
    public void stop(ActionEvent actionEvent) {
        if(playThread != null)
            playThread.interrupt();
    }

    /**Sends a File-Event to save the record.
     * 
     * @param actionEvent
     */
    @FXML
    public void save(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Event-File");
        File file = chooser.showSaveDialog(null);

//        File file = new File("/home/jkoepe/Documents/Test.txt");


        this.controlEventBus.post(new FileEvent(false, file));

    }

    /**
     * Sends a File-Event which is used to load information.
     * @param actionEvent
     */
    @FXML
    public void load(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Event-File");
        File file = chooser.showOpenDialog(null);

//        File file = new File("/home/jkoepe/Documents/Test.txt");

        this.controlEventBus.post(new FileEvent(true, file));

    }

    /**
     * Request the suppliers of the recorder to get show the visualizers
     * @param event
     */
    @FXML
    public void requestSupplier(ActionEvent event) {
    	this.cleanVisualizer();
    	this.controlEventBus.post(new RequestSuppliersEvent());
    }

    /**
     * Sends an IsLiveEvent and switches the current state.
     * If the Gui is in an replay state, it is not possible to switch to Live.
     * @param event
     */
    @FXML
    public void liveButton(ActionEvent event){
        if(index == maxIndex) {
            if (live)
                live = false;
            else
                live = true;

            this.controlEventBus.post(new IsLiveEvent(live));
            return;
        }
        if(livebutton.isSelected())
            livebutton.fire();
    }

    /**
     * A function which gets a new index and posts a step-event which goes to the new index.
     * @param newIndex
     * 		The newIndex which should be achieved after the jumpto-call.
     */
    private void jumpTo(int newIndex){
        if(newIndex == 0) {
            this.reset(null);
            return;
        }
        if(newIndex > index)
            this.controlEventBus.post(new StepEvent(true, newIndex-index));
        else
            this.controlEventBus.post(new StepEvent(false, index-newIndex));
        index = newIndex;
        timeline.setValue(index);
    }


    /**
     * Registers an existing recorder to the controller.
     * @param listener
     */
    public void registerRecorder(Recorder listener){
        
        this.controlEventBus.register(listener);
        SearchVisualizationPanel visu = (SearchVisualizationPanel) visuPanel.getContent();
        listener.registerListener(visu);
        listener.registerInfoListener(this);


    }

    /**
     * Updates the timeline.
     */
    private void updateTimeline(){
        if(maxIndex == 0)
            return;

        timeline.setMax(maxIndex);
    }

    /**
     * Registers a new supplier to the Controller.
     * In addition the cooresponging Visualizers are searched and also added.
     * @param supplier
     * 		The new supplier.
     */
    public void registerSupplier(ISupplier supplier){

        System.out.println(supplier.getClass().getSimpleName());
        try {
            ClassPath path = ClassPath.from(ClassLoader.getSystemClassLoader());
            Set<?> set = path.getAllClasses();
            set.stream().forEach(cls->{
                if(cls instanceof ClassPath.ClassInfo){
                	//search for a Visualizer. 
//                	To identify a visualizer the package name has to contain .dataVisualizer.
                    if(((ClassPath.ClassInfo) cls).getName().contains(".dataVisualizer.")){
                       IVisualizer v = (IVisualizer) findClassByName(((ClassPath.ClassInfo) cls).getName());
                       if(v!= null){
                    	   //if the supplier of the visualizer matches the current one, add the visualizer to the tabpane
                            if(v.getSupplier() .equals(supplier.getClass().getSimpleName())){
                            	supplier.registerListener(v);
                            	this.controlEventBus.register(supplier);
                            	this.controlEventBus.register(v);


                            	Tab tab = new Tab();
                            	tab.setContent(v.getVisualization());
                            	tab.setText(v.getTitle());
                            	this.tabPane.getTabs().add(tab);
                            }
                       }
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Searches the loaded classes for class with a specific name.
     * @param name
     * 		The name of the searched class.
     * @return
     */
    private Object findClassByName(String name){
        try{
            Class<?> cls = Class.forName(name);
            if(cls.isInterface())
                return null;
            return cls.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void mouseOver(Object node) {

    }

    @Override
    public void mouseLeft(Object node) {

    }

    @Override
    public void buttonReleased(Object node) {

    }

    @Override
    public void buttonPushed(Object node) {
       this.controlEventBus.post(new NodePushed(node));
       if(index == maxIndex) {
    	   this.controlEventBus.post(new AlgorithmEvent(node));
       }
    }


    /**
     * A function which is called by receiveing an infoEvent.
     * By receiving such an event, the maxindex and the timeline are updated.
     * @param event
     */
    @Subscribe
    public void receiveInfoEvent(InfoEvent event){
        this.maxIndex = event.getMaxIndex();
        //TODO
//        if (event.getNumberOfDataSupplier() != this.numberSuppliers && !requested) {
//            this.controlEventBus.post(new RequestSuppliersEvent());
//            this.cleanVisualizer();

//        }
       
        updateTimeline();
        if(live) {
        	this.index = maxIndex;
        	this.timeline.setValue(index);	
        }

    }

    /**
     * This function is called if a AddSupplierEvent is received.
     * @param event
     */
    @Subscribe
    public void receiveAddSupplierEvent(AddSupplierEvent event){
    	if(Platform.isFxApplicationThread()) {
	        ISupplier supplier = event.getSupplier();
	        this.numberSuppliers ++;
	        this.registerSupplier(supplier);
    	}
    }

    /**
     * removes every tab which are currently in the tabpane.
     */
    private void cleanVisualizer(){
        this.tabPane.getTabs().removeAll(tabPane.getTabs());
    }
    
    /**
     * Registers an arbitray object as a listener to this.
     * @param listener
     */
    public void registerObject(Object listener) {
    	
    	this.controlEventBus.register(listener);
    }
}
