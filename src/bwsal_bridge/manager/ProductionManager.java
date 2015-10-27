package bwsal_bridge.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwsal_bridge.Arbitrator;
import bwsal_bridge.GameHandler;

import java.util.Map.Entry;

public class ProductionManager extends ArbitratedManager {
	protected final BuildingPlacer placer;
    protected final Map<UnitType, List<UnitType>> productionQueues =
        new HashMap<UnitType, List<UnitType>>();
    protected final Map<Unit, ProductionUnit> producingUnits =
        new HashMap<Unit, ProductionUnit>();
    protected final Map<UnitType, Integer> plannedCount;
    protected final Map<UnitType, Integer> startedCount;
    
    public ProductionManager(Arbitrator<Unit, Double> arbitrator, BuildingPlacer placer) {
        super(arbitrator);
        this.placer = placer;
        Set<UnitType> allUnitTypes = new HashSet<>();
        for (Field f : UnitType.class.getDeclaredFields()) {
        	try {
        		if (Modifier.isPublic(f.getModifiers()) && f.getType().getSimpleName() == "UnitType") {
        			allUnitTypes.add((UnitType) f.get(null));
        		}
				
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        plannedCount = new HashMap<UnitType, Integer>(allUnitTypes.size());
        startedCount = new HashMap<UnitType, Integer>(allUnitTypes.size());
        for (UnitType type : allUnitTypes) {
            plannedCount.put(type, 0);
            startedCount.put(type, 0);
        }
    }
    
    @Override
    public void onOffer(Set<Unit> units) {
        for (Unit unit : units) {
            boolean used = false;
            List<UnitType> types = productionQueues.get(unit.getType());
            if (types != null) {
                Iterator<UnitType> typeIterator = types.iterator();
                while (typeIterator.hasNext()) {
                    UnitType type = typeIterator.next();
                    if (GameHandler.getGame().canMake(type, unit)) {
                        ProductionUnit pUnit = new ProductionUnit();
                        pUnit.type = type;
                        pUnit.unit = null;
                        pUnit.lastAttemptFrame = -100;
                        pUnit.started = false;
                        producingUnits.put(unit, pUnit);
                        typeIterator.remove();
                        arbitrator.accept(this, unit);
                        arbitrator.setBid(this, unit, 100D);
                        used = true;
                        break;
                    }
                }
            }
            if (used) {
                arbitrator.decline(this, unit, 0D);
                arbitrator.removeBid(this, unit);
            }
        }
    }
    
    @Override
    public void onRevoke(Unit unit, Double bid) {
        onRemoveUnit(unit);
    }
    
    @Override
    public void update() {
        for (Unit unit : GameHandler.getGame().self().getUnits()) {
            List<UnitType> types = productionQueues.get(unit.getType());
            if (types != null && !types.isEmpty() && unit.isCompleted() && !producingUnits.containsKey(unit)) {
                arbitrator.setBid(this, unit, 50D);
            }
        }
        Iterator<Entry<Unit, ProductionUnit>> unitIterator = producingUnits.entrySet().iterator();
        while (unitIterator.hasNext()) {
            Entry<Unit, ProductionUnit> unit = unitIterator.next();
            if (unit.getKey().isLifted()) {
                if (unit.getKey().isIdle()) {
                    unit.getKey().land(placer.getBuildLocationNear(new TilePosition(unit.getKey().getTilePosition().getX(), unit.getKey().getTilePosition().getY() + 1), unit.getKey().getType()));
                }
            } else {
                if (unit.getKey().isTraining() && unit.getKey().getBuildUnit() != null) {
                    unit.getValue().unit = unit.getKey().getBuildUnit();
                }
                if (unit.getValue().unit == null) {
                    if (GameHandler.getGame().getFrameCount() > unit.getValue().lastAttemptFrame + 
                            GameHandler.getGame().getLatency() * 2 && GameHandler.getGame().canMake(
                                    unit.getValue().type, unit.getKey())) {
                        unit.getKey().train(unit.getValue().type);
                        unit.getValue().lastAttemptFrame = GameHandler.getGame().getFrameCount();
                    }
                } else {
                    if (!unit.getValue().started) {
                        incrementMapValue(startedCount, unit.getValue().type);
                        unit.getValue().started = true;
                    }
                    if (unit.getValue().unit.isCompleted()) {
                        if (unit.getValue().unit.getType().equals(unit.getValue().type)) {
                            arbitrator.removeBid(this, unit.getKey());
                            decrementMapValue(startedCount, unit.getValue().type);
                            decrementMapValue(plannedCount, unit.getValue().type);
                            unitIterator.remove();
                        } else {
                            unit.getValue().unit = null;
                        }
                    } else {
                        if (unit.getValue().unit.exists() && !unit.getValue().unit.
                                getType().equals(unit.getValue().type)) {
                            unit.getKey().cancelTrain();
                        }
                        if (!unit.getKey().isTraining()) {
                            unit.getValue().unit = null;
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return "Production Manager";
    }
    
    public void onRemoveUnit(Unit unit) {
        if (producingUnits.containsKey(unit)) {
            List<UnitType> types = productionQueues.get(unit.getType());
            if (types != null) {
                types.add(0, producingUnits.get(unit).type);
            }
            producingUnits.remove(unit);
        }
    }
    
    public boolean train(UnitType type) {
        if (!type.whatBuilds().first.canProduce() || type.isBuilding()) {
            return false;
        }
        if (type.getRace().equals(Race.Zerg) && !type.whatBuilds().first.equals(
                UnitType.Zerg_Infested_Command_Center)) {
            return false;
        }
        List<UnitType> types = productionQueues.get(type.whatBuilds().first);
        if (types == null) {
            types = new ArrayList<UnitType>();
            productionQueues.put(type.whatBuilds().first, types);
        }
        types.add(type);
        incrementMapValue(plannedCount, type);
        return true;
    }
    
    public int getPlannedCount(UnitType type) {
        return plannedCount.get(type);
    }
    
    public int getStartedCount(UnitType type) {
        return startedCount.get(type);
    }
    
    /**
     * Production unit for BWAPI SAL
     * 
     * @author Chad Retz
     */
    protected static final class ProductionUnit {
        protected UnitType type;
        protected int lastAttemptFrame;
        protected Unit unit;
        protected boolean started;
        
        protected ProductionUnit() {
        }
    }
}
