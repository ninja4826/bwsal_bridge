package bwsal_bridge.priority;

import java.util.List;

import bwsal_bridge.manager.BuildOrderManager.BuildItem;

public class Priority {
	private Integer priority;
	private List<BuildItem> items;
	
	public Priority(Integer priority, List<BuildItem> items) {
		this.priority = priority;
		this.items = items;
	}
	
	public Integer getPriority() { return this.priority; }
	public List<BuildItem> getItems() { return this.items; }
	
	public void setPriority(Integer e) { this.priority = e; }
	public void setItems(List<BuildItem> e) { this.items = e; }
}
