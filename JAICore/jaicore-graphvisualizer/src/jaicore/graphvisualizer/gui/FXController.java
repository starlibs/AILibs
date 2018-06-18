package jaicore.graphvisualizer.gui;

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
import jaicore.graphvisualizer.events.add.AddSupplierEvent;
import jaicore.graphvisualizer.events.add.InfoEvent;
import jaicore.graphvisualizer.events.controlEvents.FileEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.events.controlEvents.ResetEvent;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import jaicore.graphvisualizer.gui.dataVisualizer.IVisualizer;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

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

    //control variables
    private int index;
    private int maxIndex;
    private long sleepTime;

    //EventBus
    private EventBus controlEventBus;

    //Thread for playing
    private Thread playThread;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.index = 0;
        this.maxIndex = 0;
        this.sleepTime = 50;

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

    private void initializeVisualization(SwingNode swingNode) {
        SearchVisualizationPanel visu = new SearchVisualizationPanel();
        visu.addNodeListener(this);

        SwingUtilities.invokeLater(()->swingNode.setContent(visu));
    }

    @FXML
    public void play(ActionEvent actionEvent) {
        Runnable run = ()->{
            try{
                while (index < maxIndex && index >= 0){
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
        if(index == maxIndex)
            return;
        this.controlEventBus.post(new StepEvent(true, 1));
        this.index ++;
        this.timeline.setValue(index);
    }

    @FXML
    public void back(ActionEvent actionEvent) {
        if(index == 0)
            return;
        if(index == 1) {
            this.reset(null);
            return;
        }
        this.controlEventBus.post(new StepEvent(false, 1));
        this.index --;
        timeline.setValue(index);
    }

    @FXML
    public void reset(ActionEvent actionEvent) {
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

    @FXML
    public void save(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Event-File");
//        File file = chooser.showSaveDialog(null);

        File file = new File("/home/jkoepe/Documents/Test.txt");


        this.controlEventBus.post(new FileEvent(false, file));

    }

    @FXML
    public void load(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Event-File");
//        File file = chooser.showOpenDialog(null);

        File file = new File("/home/jkoepe/Documents/Test.txt");

        this.controlEventBus.post(new FileEvent(true, file));

    }

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


    public void registerListener(Recorder listener){
        //TODO
        this.controlEventBus.register(listener);
        SearchVisualizationPanel visu = (SearchVisualizationPanel) visuPanel.getContent();
        listener.registerListener(visu);
        listener.registerInfoListener(this);


    }


    private void updateTimeline(){
        if(maxIndex == 0)
            return;

        timeline.setMax(maxIndex);
    }


    public void registerSupplier(ISupplier supplier){

//       IVisualizer visualizer = (IVisualizer) findClassByName(supplier.getVisualizerName());
//

//        if(supplier instanceof TooltipSupplier){
//           IVisualizer visualizer= new TooltipVisualizer();
//           supplier.registerListener(visualizer);
//
//           this.controlEventBus.register(supplier);
//           this.controlEventBus.register(visualizer);
//
//
//           Tab tab = new Tab();
//           tab.setContent(visualizer.getVisualization());
//    //       tab.setText(supplier.getVisualizerTitle());
//           this.tabPane.getTabs().add(tab);
//        }

        System.out.println(supplier.getClass().getSimpleName());
        try {
            ClassPath path = ClassPath.from(ClassLoader.getSystemClassLoader());
            Set set = path.getAllClasses();
            set.stream().forEach(cls->{
                if(cls instanceof ClassPath.ClassInfo){
                    if(((ClassPath.ClassInfo) cls).getName().contains(".dataVisualizer.")){
                       IVisualizer v = (IVisualizer) findClassByName(((ClassPath.ClassInfo) cls).getName());
                       if(v!= null){
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
    }


    @Subscribe
    public void receiveInfoEvent(InfoEvent event){
        this.maxIndex = event.getMaxIndex();
        //TODO
        updateTimeline();

    }

    @Subscribe
    public void receiveAddSupplierEvent(AddSupplierEvent event){
        ISupplier supplier = event.getSupplier();
        this.registerSupplier(supplier);
    }
}
