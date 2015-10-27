package bwsal_bridge;

import java.util.List;
import bwapi.Unit;
import bwta.BaseLocation;

public class Base {
	
	protected final BaseLocation baseLocation;
    protected Unit resourceDepot;
    protected boolean active;
    protected boolean beingConstructed;
    
    public Base(BaseLocation baseLocation) {
        this.baseLocation = baseLocation;
    }

    public BaseLocation getBaseLocation() {
        return baseLocation;
    }

    public Unit getResourceDepot() {
        return resourceDepot;
    }

    public void setResourceDepot(Unit resourceDepot) {
        this.resourceDepot = resourceDepot;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBeingConstructed() {
        return beingConstructed;
    }

    public void setBeingConstructed(boolean beingConstructed) {
        this.beingConstructed = beingConstructed;
    }
    
    public List<Unit> getMinerals() {
        return baseLocation.getMinerals();
    }
    
    public List<Unit> getGeysers() {
        return baseLocation.getGeysers();
    }
}
