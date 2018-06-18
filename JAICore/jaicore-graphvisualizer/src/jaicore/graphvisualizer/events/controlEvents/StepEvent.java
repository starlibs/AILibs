package jaicore.graphvisualizer.events.controlEvents;

public class StepEvent implements ControlEvent {
    private boolean forward;
    private int steps;

    public StepEvent(boolean forward, int steps){
        this.forward = forward;
        this.steps = steps;
    }

    public boolean forward() {
        return forward;
    }

    public int getSteps() {
        return steps;
    }
}
