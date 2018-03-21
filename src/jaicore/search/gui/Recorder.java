package jaicore.search.gui;

import com.google.common.eventbus.Subscribe;
import jaicore.search.structure.core.GraphEventBus;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import java.util.ArrayList;

public class Recorder<T> {

    private GraphEventBus<T>  eventBus;
    private List<T> events;


    public Recorder(GraphEventBus eventBus){
        this.eventBus = eventBus;
        eventBus.register(this);
        this.events = new ArrayList<>();
    }

    @Subscribe
    public void receiveEvent(T e){
        events.add(e);
    }

    public void writeEventsToFile(String path)throws IOException{
        FileWriter writer = new FileWriter(path);
        for(Object event: events){
            System.out.println(event.toString());
            writer.write(event.toString());
            writer.flush();
        }
        writer.close();
    }

}
