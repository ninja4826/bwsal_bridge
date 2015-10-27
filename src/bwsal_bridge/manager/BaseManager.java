package bwsal_bridge.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import bwta.BaseLocation;
import bwapi.TilePosition;
import bwapi.Unit;
import bwsal_bridge.Base;
import bwsal_bridge.GameHandler;
import bwta.BWTA;

public class BaseManager {
	protected BuildOrderManager builder;
    protected final Map<BaseLocation, Base> bases = new HashMap<BaseLocation, Base>();
    
    public BaseManager() {
    }
    
    public BuildOrderManager getBuilder() {
        return builder;
    }
    
    public void setBuilder(BuildOrderManager builder) {
        this.builder = builder;
    }
    
    public void update() {
        for (Base base : bases.values()) {
            if (!base.isActive()) {
                if (base.getResourceDepot() == null) {
                    TilePosition tile = base.getBaseLocation().getTilePosition();
                    for (Unit unit : GameHandler.getGame().getUnitsOnTile(tile.getX(), tile.getY())) {
                        if (unit.getPlayer().equals(GameHandler.getGame().self()) && unit.getType().isResourceDepot()) {
                            base.setResourceDepot(unit);
                            break;
                        }
                    }
                }
                if (base.getResourceDepot() != null && (base.getResourceDepot().isCompleted() || 
                        base.getResourceDepot().getRemainingBuildTime() < 250)) {
                    base.setActive(true);
                }
            }
        }
        //check to see if any new base locations need to be added
        for (BaseLocation location : BWTA.getBaseLocations()) {
            if (!bases.containsKey(location)) {
                TilePosition tile = location.getTilePosition();
                for (Unit unit : GameHandler.getGame().getUnitsOnTile(tile.getX(), tile.getY())) {
                    if (unit.getPlayer().equals(GameHandler.getGame().self()) && unit.getType().isResourceDepot()) {
                        bases.put(location, new Base(location));
                        //TODO: did the orig developer purposefully not put a break here?
                    }
                }
            }
        }
    }
    
    public void expand(BaseLocation location) {
        bases.put(location, new Base(location));
        builder.buildAdditional(1, GameHandler.getGame().self().getRace().getCenter(), 100, location.getTilePosition());
    }
    
    public Collection<Base> getActiveBases() {
        return CollectionUtils.select(bases.values(), new Predicate<Base>() {
            @Override
            public boolean evaluate(Base base) {
                return base.isActive();
            }
        });
    }
    
    public Collection<Base> getAllBases() {
        return Collections.unmodifiableCollection(bases.values());
    }
}
