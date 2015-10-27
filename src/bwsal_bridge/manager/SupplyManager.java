package bwsal_bridge.manager;

import bwapi.Unit;
import bwapi.UnitType;
import bwsal_bridge.GameHandler;

public class SupplyManager {
	protected BuildManager buildManager;
    protected BuildOrderManager buildOrderManager;
    protected int lastFrameCheck;
    
    public SupplyManager(BuildManager buildManager, BuildOrderManager buildOrderManager) {
        this.buildManager = buildManager;
        this.buildOrderManager = buildOrderManager;
    }
    
    public void update() {
        if (GameHandler.getGame().getFrameCount() > lastFrameCheck + 25) {
            int productionCapacity = 0;
            lastFrameCheck = GameHandler.getGame().getFrameCount();
            for (Unit unit : GameHandler.getGame().self().getUnits()) {
                if (unit.getType().canProduce()) {
                    productionCapacity += 4;
                }
                if (getPlannedSupply() <= GameHandler.getGame().self().
                        supplyUsed() + productionCapacity) {
                    buildManager.build(GameHandler.getGame().self().
                            getRace().getSupplyProvider());
                    buildOrderManager.spendResources(GameHandler.getGame().self().getRace().getSupplyProvider());
                }
            }
        }
    }
    
    public String getName() {
        return "Supply Manager";
    }
    
    public int getPlannedSupply() {
        int plannedSupply = 0;
        plannedSupply += buildManager.getPlannedCount(UnitType.Terran_Supply_Depot) * UnitType.Terran_Supply_Depot.supplyProvided();
        plannedSupply += buildManager.getPlannedCount(UnitType.Terran_Command_Center) * UnitType.Terran_Command_Center.supplyProvided();
        plannedSupply += buildManager.getPlannedCount(UnitType.Protoss_Pylon) * UnitType.Protoss_Pylon.supplyProvided();
        plannedSupply += buildManager.getPlannedCount(UnitType.Protoss_Nexus) * UnitType.Protoss_Nexus.supplyProvided();
        plannedSupply += buildManager.getPlannedCount(UnitType.Zerg_Overlord) * UnitType.Zerg_Overlord.supplyProvided();
        plannedSupply += buildManager.getPlannedCount(UnitType.Zerg_Hatchery) * UnitType.Zerg_Hatchery.supplyProvided();
        plannedSupply += buildManager.getPlannedCount(UnitType.Zerg_Lair) * UnitType.Zerg_Lair.supplyProvided();
        plannedSupply += buildManager.getPlannedCount(UnitType.Zerg_Hive) * UnitType.Zerg_Hive.supplyProvided();
        return plannedSupply;
    }
}
