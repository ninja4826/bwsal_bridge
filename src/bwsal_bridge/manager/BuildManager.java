package bwsal_bridge.manager;

import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwsal_bridge.Arbitrator;
import bwsal_bridge.GameHandler;

public class BuildManager {
	
	protected final Arbitrator<Unit, Double> arbitrator;
    protected final BuildingPlacer buildingPlacer;
    protected final ConstructionManager constructionManager;
    protected final ProductionManager productionManager;
    protected final MorphManager morphManager;
    
    public BuildManager(Arbitrator<Unit, Double> arbitrator) {
        this.arbitrator = arbitrator;
        buildingPlacer = new BuildingPlacer();
        constructionManager = new ConstructionManager(arbitrator, buildingPlacer);
        productionManager = new ProductionManager(arbitrator, buildingPlacer);
        morphManager = new MorphManager(arbitrator);
    }
    
    public void update() {
        constructionManager.update();
        productionManager.update();
        morphManager.update();
    }
    
    public String getName() {
        return "Build Manager";
    }
    
    public BuildingPlacer getBuildingPlacer() {
        return buildingPlacer;
    }
    
    public ProductionManager getProductionManager() {
    	return productionManager;
    }
    
    public void onRemoveUnit(Unit unit) {
        constructionManager.onRemoveUnit(unit);
        productionManager.onRemoveUnit(unit);
        morphManager.onRemoveUnit(unit);
    }

    public boolean build(UnitType type) {
        return build(type, GameHandler.getGame().self().getStartLocation());
    }
    
    public boolean build(UnitType type, TilePosition goalPosition) {
        if (type.equals(UnitType.None) || type.equals(UnitType.Unknown)) {
            return false;
        }
        if (type.getRace().equals(Race.Zerg) && type.isBuilding() == type.
                whatBuilds().first.isBuilding()) {
            return morphManager.morph(type);
        } else if (type.isBuilding()) {
            return constructionManager.build(type, goalPosition);
        } else {
            return productionManager.train(type);
        }
    }
    
    public int getPlannedCount(UnitType type) {
        if (type.getRace().equals(Race.Zerg) && type.isBuilding() == 
                type.whatBuilds().first.isBuilding()) {
        	System.out.println("morph");
            return GameHandler.getGame().self().completedUnitCount(type) +
                morphManager.getPlannedCount(type);
        } else if (type.isBuilding()) {
        	System.out.println("construction");
            return GameHandler.getGame().self().completedUnitCount(type) +
                constructionManager.getPlannedCount(type);
        } else {
        	System.out.println("production");
            return GameHandler.getGame().self().completedUnitCount(type) +
                productionManager.getPlannedCount(type);
        }
    }
    
    public int getStartedCount(UnitType type) {
        if (type.getRace().equals(Race.Zerg) && type.isBuilding() == 
                type.whatBuilds().first.isBuilding()) {
            return GameHandler.getGame().self().completedUnitCount(type) +
                morphManager.getStartedCount(type);
        } else if (type.isBuilding()) {
            return GameHandler.getGame().self().completedUnitCount(type) +
                constructionManager.getStartedCount(type);
        } else {
            return GameHandler.getGame().self().completedUnitCount(type) +
                productionManager.getStartedCount(type);
        }
    }

    public int getCompletedCount(UnitType type) {
        return GameHandler.getGame().self().completedUnitCount(type);
    }
    
    public void setBuildDistance(int distance) {
        buildingPlacer.setBuildDistance(distance);
    }
}
