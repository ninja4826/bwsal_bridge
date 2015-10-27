package bwsal_bridge.manager;

import java.util.Map;

import bwapi.Unit;
import bwsal_bridge.Arbitrator;
import bwsal_bridge.BridgeRuntimeException;
import bwsal_bridge.Controller;

public abstract class ArbitratedManager implements Controller<Unit, Double> {
	
	protected static <T> void decrementMapValue(Map<T, Integer> map, T key) {
		Integer val = map.get(key);
		if (val == null) throw new BridgeRuntimeException("Key not found");
		map.put(key, val - 1);
	}
	
	protected static <T> void incrementMapValue(Map<T, Integer> map, T key) {
		Integer val = map.get(key);
		if (val == null) throw new BridgeRuntimeException("Key not found");
		map.put(key, val + 1);
	}
	
	protected final Arbitrator<Unit, Double> arbitrator;
	
	protected ArbitratedManager(Arbitrator<Unit, Double> arbitrator) { this.arbitrator = arbitrator; }
}
