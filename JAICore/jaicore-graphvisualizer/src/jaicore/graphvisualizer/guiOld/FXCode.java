package jaicore.graphvisualizer.guiOld;

import java.io.IOException;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.ClassPath;

import jaicore.graphvisualizer.events.misc.AddSupplierEvent;
import jaicore.graphvisualizer.guiOld.dataSupplier.ISupplier;
import jaicore.graphvisualizer.guiOld.dataVisualizer.IVisualizer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FXCode extends Application{
	//FX-Variables
    Slider timeline;
    TabPane tabPane;
    SwingNode visuPanel;
    
    //EventBuses
    EventBus controlEventBus;
    
//    misc
    private int numberOfSupplier;
    
    
    public FXCode() {
    	this.controlEventBus = new EventBus();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        open();
    }
    
    
    /**
     * Opens a new Gui-VisualizationWindow
     */
    
    public boolean open() {
    	return open(null, "Test");
    }
    
    public boolean open(Recorder recorder) {
    	return open(recorder, "Test");
    }
    
    public boolean open(Recorder recorder, String title){
        //FX-elements
        BorderPane root = new BorderPane();
        BorderPane top = new BorderPane();
        //Top
        ToolBar toolbar = new ToolBar();
        Slider speedSlider = new Slider();
        speedSlider.setMin(0);
        speedSlider.setMax(200);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setSnapToTicks(true);
        speedSlider.setMajorTickUnit(10);
        speedSlider.setValue(150);

        Button playButton = new Button("play");
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                play();
            }
        });
        Button stepButton = new Button("step");
        Button backButton = new Button("back");
        Button resetButton = new Button("reset");
        Button stopButton = new Button("stop");
        Button saveButton = new Button("save");
        Button loadButton = new Button("load");

//      setting the top elements
        root.setTop(top);
        top.setTop(toolbar);
        toolbar.getItems().add(playButton);
        toolbar.getItems().add(stepButton);
        toolbar.getItems().add(backButton);
        toolbar.getItems().add(resetButton);
        toolbar.getItems().add(stopButton);
        toolbar.getItems().add(saveButton);
        toolbar.getItems().add(loadButton);
        top.setBottom(speedSlider);

//        Center
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0, 0.25);
        tabPane = new TabPane();
        BorderPane centerBorder = new BorderPane();
        SwingNode visuPanel = new SwingNode();

        splitPane.getItems().add(tabPane);
        splitPane.getItems().add(centerBorder);
        centerBorder.setCenter(visuPanel);
        root.setCenter(splitPane);

//        bottom
        timeline = new Slider();
        timeline.setShowTickLabels(true);
        timeline.setShowTickMarks(true);
        root.setBottom(timeline);

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root, 800,600));
        stage.show();
        
        this.registerRecorder(recorder);
        
        return true;

    }
    
    /**
     * Registers an existing recorder to the controller.
     * @param listener
     */
    public void registerRecorder(Recorder listener){
        
        this.controlEventBus.register(listener);
//        SearchVisualizationPanel visu = (SearchVisualizationPanel) visuPanel.getContent();
//        listener.registerListener(visu);
        listener.registerInfoListener(this);


    }
    
    
    /**
     * A function which is called by receiveing an infoEvent.
     * By receiving such an event, the maxindex and the timeline are updated.
     * @param event
     */
//    @Subscribe
//    public void receiveInfoEvent(InfoEvent event){
//        this.maxIndex = event.getMaxIndex();
//        //TODO
////        if (event.getNumberOfDataSupplier() != this.numberSuppliers && !requested) {
////            this.controlEventBus.post(new RequestSuppliersEvent());
////            this.cleanVisualizer();
//
////        }
//       
//        updateTimeline();
//        if(live) {
//        	this.index = maxIndex;
//        	this.timeline.setValue(index);	
//        }
//
//    }
    
    /**
     * This function is called if a AddSupplierEvent is received.
     * @param evetn
     */
    @Subscribe
    public void receiveAddSupplierEvent(AddSupplierEvent event) {
    	if(Platform.isFxApplicationThread()) {
    		ISupplier supplier = event.getSupplier();
    		this.numberOfSupplier ++;
    		this.registerSupplier(supplier);
    	}
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
    
    private void play(){
        System.out.println("Test");
    }
}
