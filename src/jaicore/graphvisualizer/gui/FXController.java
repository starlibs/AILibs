package jaicore.graphvisualizer.gui;

import jaicore.graphvisualizer.*;
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

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class FXController implements Initializable, NodeListener {


    @FXML
    public Slider speedSlider;
    @FXML
    public SwingNode visuPanel;
    @FXML
    public Slider timeline;
    @FXML
    public TabPane tabPane;

    //recorder on which the controller works
    private Recorder recorder;
    private Thread playThread;
    private Thread jumpThread;


    private int index;
    private long sleepTime;
    private List<Long> eventTimes;

//
//    JLabel tip;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.index =0;
        this.sleepTime = 50;

        recorder = new Recorder();
        this.eventTimes = recorder.getReceiveTimes();

        initializeVisuPanel(visuPanel);

//        tip = new JLabel();
//        tip.setText("<html></html>");
//        initializeToolTip(toolTip);

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

        timeline.setOnMouseReleased((MouseEvent event)-> {
            double v = timeline.getValue();
            int i = 0;
            while (eventTimes.get(i) < v)
                i++;
            jumpTo(i);
        });

        updateTimeLine();

    }

    /**
     * Creates the SearchVisualizationPanel and binds it to visuPanel in the GUI
     * @param node
     */
    private void initializeVisuPanel(SwingNode node){
        SearchVisualizationPanel visu = new SearchVisualizationPanel();
        visu.addNodeListener(this);
        this.recorder.registerListener(visu);
//        visu.setTooltipGenerator(this.recorder.getTooltipGenerator());
        SwingUtilities.invokeLater(()->node.setContent(visu));


    }

    /**
     * Configures the timeline according to the current recorder
     */
    public void updateTimeLine(){
        if(eventTimes.isEmpty())
            return;

        timeline.setMax(eventTimes.get(eventTimes.size()-1));

        if(!eventTimes.isEmpty()) {
            long tickUnit = eventTimes.get(eventTimes.size()-1)/10 ;
            if(tickUnit > 0)
                timeline.setMajorTickUnit(tickUnit);
        }

        timeline.setMinorTickCount(5);
        timeline.setValue(index);

        /*create a timeline label*/
        timeline.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double aDouble) {
                long value= aDouble.longValue();

                long micro = value % 1000000;
                value = value / 1000000;
                long seconds = value %60;
                long minutes = value / 60;
                value = value /60;
                long hours = value /60;

                StringBuilder sb = new StringBuilder();

                if(hours != 0)
                    sb.append(hours + "h:");
                if(minutes != 0)
                    sb.append(minutes + "m:");
                if(seconds <10)
                    sb.append(0);
                sb.append(seconds +"s:");
                sb.append(micro);
                return sb.toString();
           }

            @Override
            public Double fromString(String s) {
                return null;
            }
        });
    }
//
//    private void initializeToolTip(SwingNode toolTip){
//        JScrollPane panel = new JScrollPane();
//        panel.setViewportView(tip);
//        SwingUtilities.invokeLater(()->toolTip.setContent(panel));
//    }

    public void setRecorder(Recorder recorder){
        if(visuPanel.getContent() != null)
            this.recorder.unregisterListener(visuPanel.getContent());

        this.recorder = recorder;
        this.eventTimes = recorder.getReceiveTimes();
        this.initializeVisuPanel(visuPanel);
        updateTimeLine();
    }

    /**
     * Starts the playback of the events. For this an own thread is created, in order to not freeze the whole GUI.
     * The playback can be stopped by pressing the stop button.
     */
    @FXML
    public void play(ActionEvent actionEvent) {
        System.out.println("Play");
//        Runnable runPlay =()->{
//            while(this.index < this.eventTimes.size()-1){
//                try {
//                    step(null);
//                    TimeUnit.MILLISECONDS.sleep(sleepTime);
//                } catch (InterruptedException e) {
//
//                }
//            }
//        };
//
//        playThread = new Thread(runPlay);
//        playThread.start();

        Runnable runPlay = () ->{
            try{
                while(index < this.eventTimes.size() && index >= 0){
                    recorder.step();
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                    timeline.setValue(eventTimes.get(index));
                    index ++;
                }
                /* index correction */
                if(index >= 0)
                    index --;
            }
            catch (InterruptedException e){}
        };
        playThread = new Thread(runPlay);
        playThread.start();
    }

    /**
     * Takes a single step, if the step-Button is pressed.
     * @param actionEvent
     *      Button press event on the Button step
     */
    @FXML
    public void step(ActionEvent actionEvent) {
        if(index >= eventTimes.size()-1)
            return;
        recorder.step();
        index ++;
        timeline.setValue(eventTimes.get(index));
    }

    /**
     * Takes one step backewards, if the button is pressed
     * @param actionEvent
     *      The event fired, by pressing the back button
     */
    @FXML
    public void back(ActionEvent actionEvent) {
        if(index == 0)
            return;
        if(index ==1)
            reset(null);
        else{
            index --;
            recorder.back();
            timeline.setValue(eventTimes.get(index));
        }
    }

    /**
     * Reset the gui
     * @param actionEvent
     *      The event fired, by pressing the reset button
     */
    @FXML
    public void reset(ActionEvent actionEvent) {
        if(playThread != null)
            playThread.interrupt();

//        updateTimeLine();
//        initializeVisuPanel(this.visuPanel);
        recorder.reset();
        index = 0;
        timeline.setValue(index);
        SearchVisualizationPanel vPanel = (SearchVisualizationPanel) visuPanel.getContent();
        vPanel.reset();
    }

    /**
     * stops all running threads
     * @param actionEvent
     */
    public void stop(ActionEvent actionEvent) {
        if(playThread!= null)
            playThread.interrupt();

        if(jumpThread != null)
            jumpThread.interrupt();
    }

    /**
     * jumps to a specifc point on the timeline
     * @param value
     */
    private void jumpTo(int value){
        Runnable run = ()-> {
            try {
                while (value < index)
                    this.back(null);
                while (value > index)
                    this.step(null);
                TimeUnit.MILLISECONDS.sleep(0);
            }
            catch(InterruptedException e){
            }
        };
        jumpThread = new Thread(run);
        jumpThread.start();
    }

    /**
     * stores a search in a file
     * @param actionEvent
     */
    public void save(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Event-File");
//        recorder.saveToFile(chooser.showSaveDialog(null));

       File file = new File("/home/jkoepe/Documents/Test.txt");
       recorder.saveToFile(file);
    }

    /**
     * loads a recorded search
     * @param actionEvent
     */
    public void load(ActionEvent actionEvent) {
        this.tabPane.getTabs().clear();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Event-File");
//        recorder.loadFromFile(chooser.showOpenDialog(null));
        File file = new File("/home/jkoepe/Documents/Test.txt");
        recorder.loadFromFile(file);

        initializeVisuPanel(visuPanel);

        updateTimeLine();
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
        this.recorder.update(node);

    }

    public void updateEventTimes(List<Long> newEventTimes){
        this.eventTimes = newEventTimes;
        updateTimeLine();
    }

    public void addTab(IDataVisualizer visualizer, String name){
        Tab tab = new Tab();
        tab.setText(name);
        tab.setContent(visualizer.getVisualization());
        this.tabPane.getTabs().add(tab);
    }

    public void test(ActionEvent actionEvent) {
       System.out.println(tabPane.getSelectionModel().getSelectedItem().getText());
    }

}
