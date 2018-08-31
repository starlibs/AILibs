package jaicore.graphvisualizer.gui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.ClassPath;
import jaicore.graphvisualizer.events.controlEvents.FileEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.events.controlEvents.ResetEvent;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.graphvisualizer.events.misc.AddSupplierEventNew;
import jaicore.graphvisualizer.events.misc.InfoEvent;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import jaicore.graphvisualizer.gui.dataVisualizer.IVisualizer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//TODO timeline does not alwayse update fast enough
public class FXCode implements NodeListener {
    
//    Tabpane for additional tabs
    private TabPane tabPane;
    
//    timeline
    private Slider timeline;

//    EventBus
    private EventBus eventBus;

    private Thread playThread;

    private int index;
    private int maxIndex;

    private long sleepTime;

    //Visualization window
    private GraphVisualization visualization;


    /**
     * Create a new GraphvisualizerStage
     * @param rec
     */
    public FXCode(Recorder rec){
        this.index = 0;
        this.maxIndex = 0;
        this.sleepTime = 50;

        this.eventBus = new EventBus();
        this.eventBus.register(rec);
        rec.registerInfoListener(this);

        //create BorderPane
        BorderPane root = new BorderPane();


//        top
        ToolBar toolBar = new ToolBar();
        fillToolbar(toolBar.getItems());
        BorderPane top = new BorderPane();
        top.setTop(toolBar);

        Slider sleepTimeSlider = new Slider(0,200, 150);
        sleepTimeSlider.setShowTickLabels(true);
        sleepTimeSlider.setShowTickMarks(true);
        sleepTimeSlider.setBlockIncrement(1);
        sleepTimeSlider.setOnMouseReleased((MouseEvent event) ->{
            double sliderValue = sleepTimeSlider.getValue();
            this.sleepTime = (long) (200- sliderValue);
        });
        sleepTimeSlider.setOnKeyPressed((KeyEvent event) ->{
            double sliderValue = sleepTimeSlider.getValue();
            this.sleepTime = (long) (200-sliderValue);
        });
        sleepTimeSlider.setOnKeyReleased((KeyEvent event) ->{
            double sliderValue = sleepTimeSlider.getValue();
            this.sleepTime = (long) (200-sliderValue);
        });

        sleepTimeSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                Double speed = 200- object;
                return String.valueOf(speed.longValue());
            }

            @Override
            public Double fromString(String string) {
                return null;
            }
        });

        top.setBottom(sleepTimeSlider);

        root.setTop(top);

//        center
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0,0.25);
//        left
        tabPane = new TabPane();

        splitPane.getItems().add(tabPane);
//        visualization = new GraphVisualization();
//        visualization = new HeatVisualization();
        visualization = new ScoreVisualization();
        rec.registerReplayListener(visualization);
        splitPane.getItems().add(visualization.getFXNode());


        visualization.addNodeListener(this);

        root.setCenter(splitPane);




//        Bottom
        this.timeline = new Slider();
        this.timeline.setShowTickLabels(true);
        this.timeline.setShowTickMarks(true);
        this.timeline.setOnMouseReleased((MouseEvent event)->{
            int newIndex = (int) timeline.getValue();
            jumpToIndex(newIndex);
        });
        this.timeline.setOnKeyReleased((KeyEvent event)->{
            int newIndex = (int) timeline.getValue();
            System.out.println(newIndex);
            jumpToIndex(newIndex);
        });
        this.timeline.setOnKeyPressed((KeyEvent event)->{
            int newIndex = (int)timeline.getValue();
            System.out.println(newIndex);
            jumpToIndex(newIndex);
        });
        this.timeline.setBlockIncrement(1);
        root.setBottom(this.timeline);
        


        Scene scene = new Scene(root, 800,300);
        Stage stage = new Stage();
        stage.setScene(scene);
//        stage.setMaximized(true);
        stage.show();

        rec.getSupplier();
    }

    /**
     * Creates the controll-buttons and adds them to the given List
     * @param nodeList
     *      A list which shall contain the nodes of the buttons
     */
    private void fillToolbar(List<Node> nodeList){
        //playbutton
        Button playButton = new Button("Play");
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                //play runs in an own thread to make it stoppable
                Runnable run = ()->{
                    try{
                        while(index >= 0){

                            eventBus.post(new StepEvent(true, 1));
                            TimeUnit.MILLISECONDS.sleep(sleepTime);
                            updateIndex(1, false);
                        }
                    }
                    catch(InterruptedException e){
//                e.printStackTrace();
                    }
                };

                playThread = new Thread(run);
                playThread.start();
            }
        });
        nodeList.add(playButton);
        //stepButton
        Button stepButton = new Button("Step");
        stepButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//               System.out.println("Step");
               eventBus.post(new StepEvent(true, 1));
               if(index != maxIndex)
                   updateIndex(1, false);
            }
        });
        nodeList.add(stepButton);

        //stopButton
        Button stopButton = new Button("Stop");
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                System.out.println("Stop");
                if(playThread!= null)
                    playThread.interrupt();
            }
        });
        nodeList.add(stopButton);

        //BackButton
        Button backButton = new Button("Back");
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                System.out.println("back");
                if(index == 0)
                    return;
                if(index == 1) {
                    reset();
                    return;
                }
                eventBus.post(new StepEvent(false, 1));
                updateIndex(-1, false);
            }
        });
        nodeList.add(backButton);

//        resetButton
        Button resetButton = new Button("reset");
        resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                reset();
            }
        });
        nodeList.add(resetButton);

        //loadButton
        Button loadButton = new Button("load");
        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                System.out.println("load");
                FileChooser chooser = new FileChooser();
                File file = chooser.showOpenDialog(null);
                eventBus.post(new FileEvent(true,file ));
            }
        });
        nodeList.add(loadButton);

        //saveButton
        Button saveButton = new Button("save");
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser chooser = new FileChooser();
                File file =chooser.showSaveDialog(null);
                eventBus.post(new FileEvent(false,file));
            }
        });
        nodeList.add(saveButton);
    }

    /**
     * Receive Info-Events from the recorder to update the timeline and the maximum index
     * @param event
     *      The info-event.
     */
    @Subscribe
    public void receiveInfoEvent(InfoEvent event){
        try {
            this.maxIndex = event.getMaxIndex();
            this.timeline.setMax(this.maxIndex);
            if (event.updateIndex())
                this.updateIndex(maxIndex, true);
        } catch(NullPointerException e){
//            e.printStackTrace();
        }
    }

    /**
     * Updates the index if a new step is done. Depending on the type of step it is possible
     * to either get the actual new index (<code>isRealIndex = true</code> or an additive one.
     * @param newIndex
     *      A variable which is used to compute the new index. Either it is the actual new index or this number has
     *      to be added to the current index.
     * @param isRealIndex
     *      <code>true</code> if the newIndex is the actual new index, <code>false</code> if newIndex is additive.
     */
    private void updateIndex(int newIndex, boolean isRealIndex){
        if(! isRealIndex)
            newIndex += this.index;


        if (newIndex > this.maxIndex || newIndex < 0)
            return;

        this.index = newIndex;
        this.timeline.setValue(this.index);


    }

    /**
     * Resets the GUI
     */
    public void reset(){
        if(this.playThread != null)
            this.playThread.interrupt();
        this.updateIndex(0, true);
        this.visualization.reset();
        eventBus.post(new ResetEvent());
    }

    /**
     * Jumps to a specific index at the timeline
     * @param newIndex
     */
    public void jumpToIndex(int newIndex){
        if(this.playThread != null)
            playThread.interrupt();
        if(newIndex == 0){
            this.reset();
            return;
        }
        if(newIndex > this.index)
            this.eventBus.post(new StepEvent(true, newIndex -this.index));
        else
            this.eventBus.post(new StepEvent(false, index-newIndex));
        this.updateIndex(newIndex, true);
    }

    public TabPane getTabPane() {
        return tabPane;
    }


    /**
     * Registers a new supplier to the Controller.
     * In addition the cooresponging Visualizers are searched and also added.
     * @param supplier
     * 		The new supplier.
     */
    public void addDataSupplier(ISupplier supplier){

        System.out.println(supplier.getClass().getSimpleName());
        try {
            ClassPath path = ClassPath.from(ClassLoader.getSystemClassLoader());
            Set<?> set = path.getAllClasses();
            try {
                set.stream().forEach(cls -> {
                    if (cls instanceof ClassPath.ClassInfo) {
                        //search for a Visualizer.
//                	To identify a visualizer the package name has to contain .dataVisualizer.
                        if (((ClassPath.ClassInfo) cls).getName().contains(".dataVisualizer.")) {
                            IVisualizer v = (IVisualizer) findClassByName(((ClassPath.ClassInfo) cls).getName());
                            try {
                                if (v != null) {
                                    //if the supplier of the visualizer matches the current one, add the visualizer to the tabpane
                                    if (v.getSupplier().equals(supplier.getClass().getSimpleName())) {
                                        supplier.registerListener(v);
                                        this.eventBus.register(supplier);
                                        this.eventBus.register(v);


                                        Tab tab = new Tab();
                                        tab.setContent(v.getVisualization());
                                        tab.setText(v.getTitle());
                                        this.tabPane.getTabs().add(tab);
                                    }
                                }
                            } catch (Exception e) {
//                            e.printStackTrace();
                            }
                        }
                    }
                });
            } catch (Exception e){
//                e.printStackTrace();
            }

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

    @Subscribe
    public void receiveAddSupplierEvent(AddSupplierEventNew event){
        this.addDataSupplier(event.getSupplier());
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
        this.eventBus.post(new NodePushed(node));
    }
}
