package jaicore.graphvisualizer.gui;

import jaicore.graphvisualizer.NodeListener;
import jaicore.graphvisualizer.SearchVisualizationPanel;
import jaicore.graphvisualizer.TooltipGenerator;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
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
    public SwingNode toolTip;
    @FXML
    public SwingNode visuPanel;
    @FXML
    public Slider timeline;

    //recorder on which the controller works
    private Recorder recorder;
    private Thread playThread;

    private int index;
    private long sleepTime;
    private List<Long> eventTimes;
    private Thread jumpThread;

    JLabel tip;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.index =0;
        this.sleepTime = 50;

        recorder = new Recorder();
        this.eventTimes = recorder.getReceiveTimes();

        initializeVisuPanel(visuPanel);
        initializeTimeLine();

        tip = new JLabel();
        tip.setText("<html></html>");
        initializeToolTip(toolTip);
    }

    /**
     * Creates the SearchVisualizationPanel and binds it to visuPanel in the GUI
     * @param node
     */
    private void initializeVisuPanel(SwingNode node){
        SearchVisualizationPanel visu = new SearchVisualizationPanel();
        visu.addNodeListener(this);
        this.recorder.registerListener(visu);
        visu.setTooltipGenerator(this.recorder.getToolTipGenerator());
        SwingUtilities.invokeLater(()->node.setContent(visu));


    }

    /**
     * Configures the timeline according to the current recorder
     */
    private void initializeTimeLine(){
        if(eventTimes.isEmpty())
            return;

        timeline.setMax(eventTimes.get(eventTimes.size()-1));

        if(!eventTimes.isEmpty())
            timeline.setMajorTickUnit(eventTimes.get(eventTimes.size()-1)/10);

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

    private void initializeToolTip(SwingNode toolTip){
        JScrollPane panel = new JScrollPane();
        panel.setViewportView(tip);
        SwingUtilities.invokeLater(()->toolTip.setContent(panel));
    }

    public void setRecorder(Recorder recorder){
        if(visuPanel.getContent() != null)
            this.recorder.unregisterListener(visuPanel.getContent());

        this.recorder = recorder;
        this.eventTimes = recorder.getReceiveTimes();
        this.initializeVisuPanel(visuPanel);
        initializeTimeLine();
    }

    /**
     * Starts the playback of the events. For this an own thread is created, in order to not freeze the whole GUI.
     * The playback can be stopped by pressing the stop button.
     */
    @FXML
    public void play(ActionEvent actionEvent) {
        System.out.println("Play");
        Runnable runPlay =()->{
            while(this.index < this.eventTimes.size()-1){
                try {
                    step(null);
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
        if(index == eventTimes.size()-1)
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

        initializeTimeLine();
        initializeVisuPanel(this.visuPanel);
        recorder.reset();
        index = 0;
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
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Event-File");
//        recorder.loadFromFile(chooser.showOpenDialog(null));
        File file = new File("/home/jkoepe/Documents/Test.txt");
        recorder.loadFromFile(file);

        initializeVisuPanel(visuPanel);

        initializeTimeLine();
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
        SearchVisualizationPanel panel = (SearchVisualizationPanel) visuPanel.getContent();
        TooltipGenerator gen = panel.getTooltipGenerator();
        StringBuilder sb = new StringBuilder();
//						sb.append("<html><div style='padding: 5px; background: #ffffcc; border: 1px solid black;'>");
        sb.append("<html><div style='padding: 5px;'>");
        sb.append(gen.getTooltip(node));
        sb.append("</div></html>");
        tip.setText(sb.toString());

    }
}
