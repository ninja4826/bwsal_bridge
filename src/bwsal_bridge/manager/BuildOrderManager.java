package bwsal_bridge.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UpgradeType;
import bwsal_bridge.GameHandler;
import bwsal_bridge.priority.PriorityMap;
import bwapi.UnitType;

public class BuildOrderManager {
	protected final BuildManager buildManager;
    protected final TechManager techManager;
    protected final UpgradeManager upgradeManager;
    protected final Map<Integer, List<BuildItem>> items =
        new PriorityMap();
    protected int usedMinerals;
    protected int usedGas;
    
    public BuildOrderManager(BuildManager buildManager, TechManager techManager, UpgradeManager upgradeManager) {
        this.buildManager = buildManager;
        this.techManager = techManager;
        this.upgradeManager = upgradeManager;
        usedMinerals = 0;
        usedGas = 0;
    }
    
    public void update() {
        if (items.isEmpty()) {
            return;
        }
        
        
        
        //TODO: last? really? aren't std::map orders random?
        //  who cares, I'm just gonna toss it in a list and do 
        //  what was done...but this is weird
        List<Entry<Integer, List<BuildItem>>> list = new ArrayList<Entry<Integer, List<BuildItem>>>(items.entrySet());
        int index = list.size() - 1;
        while (list.get(index).getValue().isEmpty()) {
            //erase it from me and the set
            list.remove(index);
            items.remove(list.get(index));
            if (items.isEmpty()) {
                return;
            }
            index = list.size() - 1;
        }
        @SuppressWarnings("unused")
		int o = 5;
        Iterator<BuildItem> itemIterator = list.get(0).getValue().iterator();
        while (itemIterator.hasNext()) {
            BuildItem item = itemIterator.next();
//            if (!item.unitType.equals(UnitType.None)) {
            if (item.unitType != UnitType.None) {
                if (item.isAdditional) {
                    if (item.count > 0) {
                        if (hasResources(item.unitType)) {
                            buildManager.build(item.unitType, item.seedPosition);
                            spendResources(item.unitType);
                            item.count--;
                        }
                    } else {
                        itemIterator.remove();
                    }
                } else {
                    if (buildManager.getPlannedCount(item.unitType) >= item.count) {
                        itemIterator.remove();
                    } else {
                        if (hasResources(item.unitType)) {
                            buildManager.build(item.unitType, item.seedPosition);
                            spendResources(item.unitType);
                        }
                    }
                }
//            } else if (!item.techType.equals(TechType.None)) {
            } else if (item.techType != TechType.None) {
                if (techManager.planned(item.techType)) {
                    itemIterator.remove();
                } else if (hasResources(item.techType)) {
                    techManager.research(item.techType);
                    spendResources(item.techType);
                }
            } else {
                if (upgradeManager.getPlannedLevel(item.upgradeType) >= item.count) {
                    itemIterator.remove();
                } else {
                    if (!GameHandler.getGame().self().isUpgrading(item.upgradeType) &&
                            hasResources(item.upgradeType)) {
                        upgradeManager.upgrade(item.upgradeType);
                        spendResources(item.upgradeType);
                    }
                }
            }
            o += 20;
        }
    }
    
    public String getName() {
        return "Build Order Manager";
    }
    
    public void build(int count, UnitType type, int priority) {
    	build(count, type, priority, TilePosition.None);
    }

    public void build(int count, UnitType type, int priority, TilePosition seedPosition) {
//        if (type.equals(UnitType.None) || type.equals(UnitType.Unknown)) {
    	if (type == UnitType.None || type == UnitType.Unknown) {
            return;
        }
//        if (seedPosition.equals(TilePosition.None) || seedPosition.equals(TilePosition.Unknown)) {
    	if (seedPosition == TilePosition.None || seedPosition == TilePosition.Unknown) {
            seedPosition = GameHandler.getGame().self().getStartLocation();
        }
        BuildItem newItem = new BuildItem();
        newItem.unitType = type;
        newItem.techType = TechType.None;
        newItem.upgradeType = UpgradeType.None;
        newItem.count = count;
        newItem.seedPosition = seedPosition;
        newItem.isAdditional = false;
        List<BuildItem> buildItems = items.get(priority);
        if (buildItems == null) {
            buildItems = new ArrayList<BuildItem>();
            items.put(priority, buildItems);
        }
        buildItems.add(newItem);
    }
    
    public void buildAdditional(int count, UnitType type, int priority) {
    	buildAdditional(count, type, priority, TilePosition.None);
    }

    public void buildAdditional(int count, UnitType type, int priority, TilePosition seedPosition) {
//        if (type.equals(UnitType.None) || type.equals(UnitType.Unknown)) {
    	if (type == UnitType.None || type == UnitType.Unknown) {
            return;
        }
        if (seedPosition.equals(TilePosition.None) || seedPosition.equals(TilePosition.Unknown)) {
            seedPosition = GameHandler.getGame().self().getStartLocation();
        }
        BuildItem newItem = new BuildItem();
        newItem.unitType = type;
        newItem.techType = TechType.None;
        newItem.upgradeType = UpgradeType.None;
        newItem.count = count;
        newItem.seedPosition = seedPosition;
        newItem.isAdditional = true;
        List<BuildItem> buildItems = items.get(priority);
        if (buildItems == null) {
            buildItems = new ArrayList<BuildItem>();
            items.put(priority, buildItems);
        }
        buildItems.add(newItem);
    }

    public void research(TechType type, int priority) {
        if (type.equals(UnitType.None) || type.equals(UnitType.Unknown)) {
            return;
        }
        BuildItem newItem = new BuildItem();
        newItem.unitType = UnitType.None;
        newItem.techType = type;
        newItem.upgradeType = UpgradeType.None;
        newItem.count = 1;
        newItem.isAdditional = false;
        List<BuildItem> buildItems = items.get(priority);
        if (buildItems == null) {
            buildItems = new ArrayList<BuildItem>();
            items.put(priority, buildItems);
        }
        buildItems.add(newItem);
    }

    public void upgrade(int level, UpgradeType type, int priority) {
        if (type.equals(UnitType.None) || type.equals(UnitType.Unknown)) {
            return;
        }
        BuildItem newItem = new BuildItem();
        newItem.unitType = UnitType.None;
        newItem.techType = TechType.None;
        newItem.upgradeType = type;
        newItem.count = level;
        newItem.isAdditional = false;
        List<BuildItem> buildItems = items.get(priority);
        if (buildItems == null) {
            buildItems = new ArrayList<BuildItem>();
            items.put(priority, buildItems);
        }
        buildItems.add(newItem);
    }

    public boolean hasResources(UnitType type) {
        return GameHandler.getGame().self().minerals() - usedMinerals >= type.mineralPrice() ||
                GameHandler.getGame().self().gas() - usedGas >= type.gasPrice();
    }
    
    public boolean hasResources(TechType type) {
        return GameHandler.getGame().self().minerals() - usedMinerals >= type.mineralPrice() ||
                GameHandler.getGame().self().gas() - usedGas >= type.gasPrice();
    }

    public boolean hasResources(UpgradeType type) {
        return GameHandler.getGame().self().minerals() - usedMinerals >= type.mineralPrice() +
                type.mineralPriceFactor() * (GameHandler.getGame().self().getUpgradeLevel(type) - 1) ||
                GameHandler.getGame().self().gas() - usedGas >= type.mineralPrice() +
                type.mineralPriceFactor() * (GameHandler.getGame().self().getUpgradeLevel(type) - 1);
    }
    
    public void spendResources(UnitType type) {
        usedMinerals += type.mineralPrice();
        usedGas += type.gasPrice();
    }
    
    public void spendResources(TechType type) {
        usedMinerals += type.mineralPrice();
        usedGas += type.gasPrice();
    }
    
    public void spendResources(UpgradeType type) {
        usedMinerals += type.mineralPrice() + type.mineralPriceFactor() * 
                (GameHandler.getGame().self().getUpgradeLevel(type) - 1);
        usedGas += type.gasPrice() + type.gasPriceFactor() *
                (GameHandler.getGame().self().getUpgradeLevel(type) - 1);
    }
    
    /**
     * SAL build item
     * 
     * @author Chad Retz
     *
     * @since 0.3
     */
    public static final class BuildItem {
        public UnitType unitType;
        public TechType techType;
        public UpgradeType upgradeType;
        public TilePosition seedPosition;
        public boolean isAdditional;
        public int count;
        
        protected BuildItem() {
        }
    }
}
