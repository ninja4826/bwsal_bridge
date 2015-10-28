package bwsal_bridge.test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bwapi.*;
import bwsal_bridge.Arbitrator;
import bwsal_bridge.Controller;
import bwsal_bridge.MirrorBWSAL;
import bwsal_bridge.manager.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class TestBot extends DefaultBWListener {
	private MirrorBWSAL mirror = new MirrorBWSAL();
	
	private Game game;
	
	private Player self;
	
//	Managers
	private Arbitrator<Unit, Double> arbitrator;
	private BaseManager baseManager;
	private BuildingPlacer buildingPlacer;
	private BuildManager buildManager;
	private BuildOrderManager buildOrderManager;
	private ScoutManager scoutManager;
	private SupplyManager supplyManager;
	private TechManager techManager;
	private UpgradeManager upgradeManager;
	private WorkerManager workerManager;
	
	private boolean started = false;
	
	private boolean workerStarted = false;
	
	public void run() {
		mirror.getModule().setEventListener(this);
		mirror.startGame();
	}
	
	@Override
	public void onUnitCreate(Unit unit) {
		System.out.println("New unit " + unit.getType());
	}
	
	@Override
	public void onStart() {
		game = mirror.getGame();
		
		game.enableFlag(1);
		
		game.setLocalSpeed(20);
		
		self = game.self();
		
		System.out.println("Analyzing map...");
		BWTA.readMap();
		BWTA.analyze();
		System.out.println("Map data ready");
		
		int i = 0;
		for (BaseLocation baseLocation : BWTA.getBaseLocations()) {
			System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
			for (Position position: baseLocation.getRegion().getPolygon().getPoints()) {
				System.out.println(position + ", ");
			}
			System.out.println();
		}
		
		System.out.println("Equality test");
		System.out.println("equals method: " + UnitType.Protoss_Arbiter.equals(UnitType.Protoss_Arbiter));
		System.out.println("equals method: " + UnitType.Protoss_Arbiter.equals(UnitType.Protoss_Archon));
		System.out.println("== : " + (UnitType.Protoss_Arbiter == UnitType.Protoss_Arbiter));
		System.out.println("== : " + (UnitType.Protoss_Arbiter == UnitType.Protoss_Archon));
		
		
//		Initialize Managers
		arbitrator = new Arbitrator<>();
		
		buildManager = new BuildManager(arbitrator);
		buildingPlacer = buildManager.getBuildingPlacer();
		techManager = new TechManager(arbitrator, buildingPlacer);
		upgradeManager = new UpgradeManager(arbitrator, buildingPlacer);
		scoutManager = new ScoutManager(arbitrator);
		buildOrderManager = new BuildOrderManager(buildManager, techManager, upgradeManager);
		baseManager = new BaseManager(buildOrderManager);
		supplyManager = new SupplyManager(buildManager, buildOrderManager);
		workerManager = new WorkerManager(arbitrator, baseManager);
		
		int buildID = 1;
		
		if (self.getRace() == Race.Zerg) {
			if (buildID == 1) {
				buildOrderManager.build(20, UnitType.Zerg_Drone, 90);
//				buildOrderManager.buildAdditional(1, UnitType.Zerg_Overlord, 85);
//				buildOrderManager.build(12, UnitType.Zerg_Drone, 84);
//				buildOrderManager.buildAdditional(1, UnitType.Zerg_Lair, 82);
//				buildOrderManager.buildAdditional(5, UnitType.Zerg_Lurker, 80);
//				buildOrderManager.build(12, UnitType.Zerg_Drone, 30);
			}
		}
		workerManager.updateWorkerAssignments();
		
		buildManager.getProductionManager().announceValues();
		
		
		
		
		
	}
	
	@Override
	public void onFrame() {
		if (game.isPaused()) return;
		
		game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
		
		StringBuilder units = new StringBuilder("My units:\n");
		
		for (Unit myUnit : self.getUnits()) {
			units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");
			
			if (!workerStarted && myUnit.getType().isWorker() && myUnit.isIdle()) {
				Unit closestMineral = null;
				workerStarted = false;
				for (Unit neutralUnit : game.neutral().getUnits()) {
					if (neutralUnit.getType().isMineralField()) {
						if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) closestMineral = neutralUnit;
					}
				}
				if (closestMineral != null) myUnit.gather(closestMineral, true);
			}
			
			if (arbitrator.hasBid(myUnit)) {
				int x = myUnit.getPosition().getX();
				int y = myUnit.getPosition().getY();
				List<Entry<Controller<Unit, Double>, Double>> bids = arbitrator.getAllBidders(myUnit);
				int y_off = 0;
				for (Entry<Controller<Unit, Double>, Double> j : bids) {
					game.drawTextMap(new Position(x, y+y_off), j.getKey().getName() + ": " + j.getValue());
					y_off += 15;
				}
			}
		}
		
		baseManager.update();
		buildManager.update();
		scoutManager.update();
		supplyManager.update();
		techManager.update();
		upgradeManager.update();
		workerManager.update();
		
		buildOrderManager.update();
		
		arbitrator.update();
	}
	
	public static void main(String[] args) {
		new TestBot().run();
	}
}
