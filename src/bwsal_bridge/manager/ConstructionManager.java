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
import java.util.Map.Entry;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwsal_bridge.Arbitrator;
import bwsal_bridge.GameHandler;

public class ConstructionManager extends ArbitratedManager {
	protected final BuildingPlacer placer;
    protected final Map<Unit, Building> builders = new HashMap<Unit, Building>();
    protected final List<Building> incompleteBuildings = new ArrayList<Building>();
    protected final Map<UnitType, Set<Building>> buildingsNeedingBuilders = 
        new HashMap<UnitType, Set<Building>>();
    protected final Map<UnitType, Integer> plannedCount;
    protected final Map<UnitType, Integer> startedCount;
    
    public ConstructionManager(Arbitrator<Unit, Double> arbitrator, BuildingPlacer placer) {
        super(arbitrator);
        this.placer = placer;
        Set<UnitType> allUnitTypes = new HashSet<>();
        Field[] unitTypeFields = UnitType.class.getDeclaredFields();
        for (Field f : unitTypeFields) {
        	try {
        		if (Modifier.isPublic(f.getModifiers()) && f.getType().getSimpleName() == "UnitType") {
        			System.out.printf("%d%n %s %s %s%n",f.getModifiers(), Modifier.toString(f.getModifiers()), f.getType().getSimpleName(), f.getName());
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
    public void onOffer(Set<Unit> objects) {
        Set<Unit> units = new HashSet<Unit>(objects);
        for (Set<Building> buildings : buildingsNeedingBuilders.values()) {
            for (Building building : buildings) {
                double minDist = 1000000;
                Unit builder = null;
                for (Unit unit : units) {
                    double dist = unit.getPosition().getDistance(building.position);
                    if (dist < minDist) {
                        minDist = dist;
                        builder = unit;
                    }
                }
                if (builder != null) {
                    arbitrator.accept(this, builder);
                    arbitrator.setBid(this, builder, 100D);
                    builders.put(builder, building);
                    building.builderUnit = builder;
                    if (building.type.isAddon()) {
                        building.tilePosition = builder.getTilePosition();
                        if (builder.isLifted()) {
                            if (!placer.canBuildHereWithSpace(building.tilePosition, building.type)) {
                                building.tilePosition = placer.getBuildLocationNear(building.tilePosition, 
                                        building.type.whatBuilds().first);
                            }
                        } else {
                            boolean buildable = true;
                            for (int x = building.tilePosition.getX() + 4; x < building.tilePosition.getX() + 6; x++) {
                                for (int y = building.tilePosition.getY() + 1; y < building.tilePosition.getY() + 3; y++) {
                                    if (!placer.buildable(x, y)) {
                                        buildable = false;
                                    }
                                }
                            }
                            if (!buildable) {
                                placer.freeTiles(building.tilePosition, 4, 3);
                                building.tilePosition = placer.getBuildLocationNear(building.tilePosition, 
                                        building.type.whatBuilds().first);
                            }
                        }
                        placer.reserveTiles(building.tilePosition, 4, 3);
                        placer.reserveTiles(new TilePosition(building.tilePosition.getX() + 4, building.tilePosition.getY() + 1), 2, 2);
                    }
                    units.remove(builder);
                    buildings.remove(building);
                }
            }
        }
        for (Unit unit : units) {
            arbitrator.decline(this, unit, 0D);
            arbitrator.removeBid(this, unit);
        }
    }
    
    @Override
    public void onRevoke(Unit unit, Double bid) {
        onRemoveUnit(unit);
    }
    
    @Override
    public void update() {
        List<Unit> myPlayerUnits = GameHandler.getGame().self().getUnits();
        for (Entry<UnitType, Set<Building>> buildings : buildingsNeedingBuilders.entrySet()) {
            if (!buildings.getValue().isEmpty()) {
                for (Unit unit : myPlayerUnits) {
                    if (unit.isCompleted() && unit.getType().equals(buildings.getKey()) && 
                            unit.getAddon() == null && !builders.containsKey(unit)) {
                        if (!unit.getType().isWorker()) {
                            arbitrator.setBid(this, unit, 80D);
                        } else {
                            double minDist = 1000000;
                            for (Building building : buildings.getValue()) {
                                double dist = unit.getPosition().getDistance(building.position);
                                if (dist < minDist) {
                                    minDist = dist;
                                }
                            }
                            minDist = Math.max(minDist, 10);
                            minDist = Math.min(minDist, 256 * 32 + 10);
                            arbitrator.setBid(this, unit, 80 - (minDist - 10) / (256 * 32) * 60);
                        }
                    }
                }
            }
        }
        int index = 0;
        Iterator<Building> iterator = incompleteBuildings.iterator();
        while (iterator.hasNext()) {
            Building building = iterator.next();
            index++;
            if (!building.started && building.buildingUnit != null) {
                incrementMapValue(startedCount, building.type);
                building.started = true;
            }
            if (building.type.isAddon()) {
                if (building.builderUnit != null) {
                    building.buildingUnit = building.builderUnit.getAddon();
                }
                if (building.buildingUnit != null && building.buildingUnit.isCompleted()) {
                    decrementMapValue(startedCount, building.type);
                    decrementMapValue(plannedCount, building.type);
                    if (building.builderUnit != null) {
                        builders.remove(building.builderUnit);
                        arbitrator.removeBid(this, building.builderUnit);
                    }
                    placer.freeTiles(building.tilePosition, 4, 3);
                    placer.reserveTiles(new TilePosition(building.tilePosition.getX() + 4, building.tilePosition.getY() + 1), 2, 2);
                    //if the building is complete, we can forget about it
                    iterator.remove();
                } else if (GameHandler.getGame().canMake(building.type)) {
                    if (building.builderUnit == null) {
                        Set<Building> buildings = buildingsNeedingBuilders.get(
                                building.type.whatBuilds().first);
                        if (buildings == null) {
                            buildings = new HashSet<Building>();
                            buildingsNeedingBuilders.put(building.type.
                                    whatBuilds().first, buildings);
                        }
                    } else if (building.builderUnit.getAddon() == null) {
                        if (building.builderUnit.isLifted() && GameHandler.getGame().getFrameCount() > building.lastOrderFrame +
                            GameHandler.getGame().getLatency() * 2) {
                            if (!placer.canBuildHereWithSpace(building.tilePosition, building.type)) {
                                placer.freeTiles(building.tilePosition, 4, 3);
                                placer.reserveTiles(new TilePosition(building.tilePosition.getX() + 4, building.tilePosition.getY() + 1), 2, 2);
                                building.tilePosition = placer.getBuildLocationNear(
                                        building.tilePosition, building.type.whatBuilds().first);
                                placer.reserveTiles(building.tilePosition, 4, 3);
                                placer.reserveTiles(new TilePosition(building.tilePosition.getX() + 4, building.tilePosition.getY() + 1), 2, 2);
                            }
                            building.builderUnit.land(building.tilePosition);
                            building.lastOrderFrame = GameHandler.getGame().getFrameCount();
                        } else if (building.builderUnit.isTraining()) {
                            building.builderUnit.cancelTrain();
                        } else if (!building.builderUnit.getTilePosition().equals(building.tilePosition)) {
                            building.builderUnit.lift();
                            building.lastOrderFrame = GameHandler.getGame().getFrameCount();
                        } else {
                            boolean buildable = true;
                            for (int x = building.tilePosition.getX() + 4; x < building.tilePosition.getX() + 6; x++) {
                                for (int y = building.tilePosition.getY() + 1; y < building.tilePosition.getY() + 3; y++) {
                                    if (!placer.buildable(x, y) || GameHandler.getGame().hasCreep(x, y)) {
                                        buildable = false;
                                    }
                                }
                            }
                            if (buildable) {
                                building.builderUnit.buildAddon(building.type);
                            } else {
                                building.builderUnit.lift();
                                building.lastOrderFrame = GameHandler.getGame().getFrameCount();
                            }
                        }
                    }
                }
            } else {
                if (building.tilePosition == null || building.tilePosition.equals(TilePosition.None)) {
                    if ((GameHandler.getGame().getFrameCount() + index) % 25 == 0 && GameHandler.getGame().canMake(building.type)) {
                        building.tilePosition = placer.getBuildLocationNear(building.goalPosition, building.type);
                        if (building.tilePosition != null && !building.tilePosition.equals(TilePosition.None)) {
                            building.position = new Position(building.tilePosition.getX() * 32 + building.type.tileWidth() * 16,
                                    building.tilePosition.getY() * 32 + building.type.tileHeight() * 16);
                            placer.reserveTiles(building.tilePosition, building.type.tileWidth(), building.type.tileHeight());
                        }
                    }
                    if (building.tilePosition == null || building.tilePosition.equals(TilePosition.None)) {
                        continue;
                    }
                }
                if (building.builderUnit != null && !building.builderUnit.exists()) {
                    building.builderUnit = null;
                }
                if (building.buildingUnit != null && (!building.buildingUnit.exists() || 
                        !building.buildingUnit.getType().equals(building.type))) {
                    building.buildingUnit = null;
                }
                if (building.buildingUnit == null) {
                    for (Unit unitOnTile : GameHandler.getGame().getUnitsOnTile(building.tilePosition.getX(), building.tilePosition.getY())) {
                        if (unitOnTile.getType().equals(building.type) && !unitOnTile.isLifted()) {
                            building.buildingUnit = unitOnTile;
                            break;
                        }
                    }
                    if (building.buildingUnit == null && building.builderUnit != null && building.builderUnit.getType().isBuilding()) {
                        building.buildingUnit = building.builderUnit;
                    }
                }
                if (building.buildingUnit != null && building.buildingUnit.isCompleted()) {
                    decrementMapValue(startedCount, building.type);
                    decrementMapValue(plannedCount, building.type);
                    if (building.builderUnit != null) {
                        builders.remove(building.builderUnit);
                        arbitrator.removeBid(this, building.builderUnit);
                    }
                    placer.freeTiles(building.tilePosition, building.type.tileWidth(), building.type.tileHeight());
                    iterator.remove();
                } else {
                    if (building.buildingUnit == null && GameHandler.getGame().canMake(building.type)) {
                        if (building.builderUnit == null) {
                            Set<Building> buildings = buildingsNeedingBuilders.get(
                                    building.type.whatBuilds().first);
                            if (buildings == null) {
                                buildings = new HashSet<Building>();
                                buildingsNeedingBuilders.put(building.type.whatBuilds().
                                        first, buildings);
                            }
                            buildings.add(building);
                        } else if (!building.builderUnit.isConstructing()) {
                            double dist = building.builderUnit.getPosition().getDistance(building.position);
                            if (dist > 100) {
                                building.builderUnit.rightClick(building.position);
                            } else if (GameHandler.getGame().canBuildHere(building.tilePosition, building.type, building.builderUnit)) {
                                if (GameHandler.getGame().canMake(building.type, building.builderUnit)) {
                                    building.builderUnit.build(building.type, building.tilePosition);
                                }
                            } else {
                                placer.freeTiles(building.tilePosition, building.type.tileWidth(), building.type.tileHeight());
                                building.tilePosition = TilePosition.None;
                                building.position = Position.None;
                            }
                        }
                    } else if (!building.type.getRace().equals(Race.Terran)) {
                        if (building.builderUnit != null) {
                            builders.remove(building.builderUnit);
                            arbitrator.removeBid(this, building.builderUnit);
                            building.builderUnit = null;
                        }
                    } else if (building.builderUnit == null) {
                        Set<Building> buildings = buildingsNeedingBuilders.get(building.type.
                                whatBuilds().first);
                        if (buildings == null) {
                            buildings = new HashSet<Building>();
                            buildingsNeedingBuilders.put(building.type.whatBuilds().
                                    first, buildings);
                        }
                        buildings.add(building);
                    } else if (GameHandler.getGame().getFrameCount() % (4 * GameHandler.getGame().getLatency()) == 0 && 
                            !building.builderUnit.isConstructing() || !building.buildingUnit.isBeingConstructed()) {
                        building.builderUnit.rightClick(building.buildingUnit);
                        building.buildingUnit.rightClick(building.builderUnit);
                    }
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return "Construction Manager";
    }
    
    public void onRemoveUnit(Unit unit) {
        Building building = builders.get(unit);
        if (building != null) {
            building.builderUnit = null;
            builders.remove(unit);
        }
    }
    
    public boolean build(UnitType type, TilePosition position) {
        if (!type.isBuilding()) {
            return false;
        }
        Building building = new Building();
        building.type = type;
        building.goalPosition = position;
        building.tilePosition = TilePosition.None;
        building.position = Position.None;
        building.lastOrderFrame = 0;
        building.started = false;
        incrementMapValue(plannedCount, type);
        incompleteBuildings.add(building);
        return true;
    }
    
    public int getPlannedCount(UnitType type) {
        Integer count = plannedCount.get(type);
        return count == null ? 0 : count;
    }
    
    public int getStartedCount(UnitType type) {
        Integer count = startedCount.get(type);
        return count == null ? 0 : count;
    }
    
    /**
     * Building
     * 
     * @author Chad Retz
     *
     * @since 0.3
     */
    protected static final class Building {
        protected TilePosition goalPosition;
        protected TilePosition tilePosition;
        protected Position position;
        protected UnitType type;
        protected Unit buildingUnit;
        protected Unit builderUnit;
        protected int lastOrderFrame;
        protected boolean started;
        
        protected Building() {
        }
    }
}
