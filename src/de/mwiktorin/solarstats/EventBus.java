package de.mwiktorin.solarstats;

import java.util.ArrayList;
import java.util.List;

import de.mwiktorin.solarstats.model.DataRow;
import de.mwiktorin.solarstats.model.System;

public class EventBus {
	
	private static EventBus instance;
	private static List<EventListener> listeners;
	private static List<DataRow> data;
	private static List<System> systems;
	
	public interface EventListener{
		public void onDataLoaded();
		public void onDataChanged();
		public void onSystemUpdate();
	}
	
	private EventBus(){
		listeners = new ArrayList<EventListener>();
	}
	
	public static EventBus getInstance(){
		if(instance == null)
			instance = new EventBus();
		
		return instance;
	}
	
	public void register(EventListener listener){
		listeners.add(listener);
	}
	
	public void unregister(EventListener listener){
		listeners.remove(listener);
	}
	
	public void setData(List<DataRow> list){
		data = new ArrayList<DataRow>(list);
	}
	
	public static List<DataRow> getData() {
	    return data;
    }
	
	public void setSystems(List<System> list){
		systems = new ArrayList<System>(list);
	}
	
	public static List<System> getSystems() {
	    return systems;
    }
	
	public void fireDataChanged(){
		for(EventListener listener : listeners){
			listener.onDataChanged();
		}
	}
	
	public void fireDataLoaded(){
		for(EventListener listener : listeners){
			listener.onDataLoaded();
		}
	}
	
	public void fireSystemUpdate(){
		for(EventListener listener : listeners){
			listener.onSystemUpdate();
		}
	}
}
