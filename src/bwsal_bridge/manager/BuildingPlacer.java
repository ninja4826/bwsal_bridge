package bwsal_bridge.manager;

import java.util.Arrays;
import java.util.List;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwsal_bridge.GameHandler;

public class BuildingPlacer {
	protected static final List<UnitType> EXTRA_SPACED =
	        Arrays.asList(UnitType.Terran_Command_Center,
	                UnitType.Terran_Factory,
	                UnitType.Terran_Starport,
	                UnitType.Terran_Science_Facility);

	    protected int buildDistance;
	    
	    protected final boolean[][] reserves = new boolean[GameHandler.getGame().mapWidth()][GameHandler.getGame().mapHeight()];
	    
	    public BuildingPlacer() {
	        buildDistance = 1;
	        //java defaults booleans to false, so we're good here
	    }
	    
	    public int getBuildDistance() {
	        return buildDistance;
	    }
	    
	    public void setBuildDistance(int buildDistance) {
	        this.buildDistance = buildDistance;
	    }
	    
	    public boolean canBuildHere(TilePosition position, UnitType type) {
	        if (!GameHandler.getGame().canBuildHere(null, type)) {
	            return false;
	        }
	        
	        for (int x = position.getX(); x < position.getX() + type.tileWidth(); x++) {
	            for (int y = position.getY(); y < position.getY() + type.tileWidth(); y++) {
	                if (reserves[x][y]) {
	                    return false;
	                }
	            }
	        }
	        return true;
	    }
	    
	    public boolean canBuildHereWithSpace(TilePosition position, UnitType type) {
	        if (!canBuildHere(position, type)) {
	            return false;
	        }
	        int width = type.tileWidth();
	        int height = type.tileHeight();
	        if (EXTRA_SPACED.contains(type)) {
	            width += 2;
	        }
	        int startx = Math.max(position.getX() - buildDistance, 0);
	        int starty = Math.max(position.getY() - buildDistance, 0);
	        int endx = Math.min(position.getX() + width + buildDistance, 
	                GameHandler.getGame().mapWidth());
	        int endy = Math.min(position.getY() + height + buildDistance, 
	                GameHandler.getGame().mapHeight());
	        //TODO: be smarter than these nested loops
	        for (int x = startx; x < endx; x++) {
	            for (int y = starty; y < endy; y++) {
	                if (!type.isRefinery() && !buildable(x, y)) {
	                    return false;
	                }
	            }
	        }
	        if (position.getX() > 3) {
	            int startx2 = Math.max(startx - 2, 0);
	            for (int x = startx2; x < startx; x++) {
	                for (int y = starty; y < endy; y++) {
	                    for (Unit unit : GameHandler.getGame().getUnitsOnTile(x, y)) {
	                        if (!unit.isLifted() && EXTRA_SPACED.contains(unit.getType())) {
	                            return false;
	                        }
	                    }
	                }
	            }
	        }
	        return true;
	    }
	    
	    public TilePosition getBuildLocation(UnitType type) {
	        //I don't really like this performance wise
	        //TODO: update TilePosition to include setters for x and y
	        //  so I don't have all this object creation here
	        for (int x = 0; x < GameHandler.getGame().mapWidth(); x++) {
	            for (int y = 0; y < GameHandler.getGame().mapHeight(); y++) {
	                TilePosition position = new TilePosition(x, y);
	                if (canBuildHere(position, type)) {
	                    return position;
	                }
	            }
	        }
	        return TilePosition.None;
	    }
	    
	    public TilePosition getBuildLocationNear(TilePosition position, UnitType type) {
	        int x = position.getX();
	        int y = position.getY();
	        int length = 1;
	        int j = 0;
	        boolean first = true;
	        int dx = 0;
	        int dy = 0;
	        while (length < GameHandler.getGame().mapWidth()) {
	            if (x >= 0 && x < GameHandler.getGame().mapWidth() &&
	                    y >= 0 && y < GameHandler.getGame().mapHeight() &&
	                    canBuildHereWithSpace(new TilePosition(x, y), type)) {
	                return new TilePosition(x, y);
	            }
	            x += dx;
	            y += dy;
	            j++;
	            if (j == length) {
	                j = 0;
	                if (!first) {
	                    length++;
	                }
	                first = !first;
	                if (dx == 0) {
	                    dx = dy;
	                    dy = 0;
	                } else {
	                    dy = -dx;
	                    dx = 0;
	                }
	            }
	        }
	        return TilePosition.None;
	    }
	    
	    public boolean buildable(int x, int y) {
	        if (!GameHandler.getGame().isBuildable(x, y)) {
	            return false;
	        }
	        for (Unit unit : GameHandler.getGame().getUnitsOnTile(x, y)) {
	            if (unit.getType().isBuilding() && !unit.isLifted()) {
	                return false;
	            }
	        }
	        return true;
	    }
	    
	    public void reserveTiles(TilePosition position, int width, int height) {
	        for (int x = position.getX(); x < position.getX() + width && x < GameHandler.getGame().mapWidth(); x++) {
	            for (int y = position.getY(); y < position.getY() + height && y < GameHandler.getGame().mapHeight(); y++) {
	                reserves[x][y] = true;
	            }
	        }
	    }
	    
	    public void freeTiles(TilePosition position, int width, int height) {
	        for (int x = position.getX(); x < position.getX() + width && x < GameHandler.getGame().mapWidth(); x++) {
	            for (int y = position.getY(); y < position.getY() + height && y < GameHandler.getGame().mapHeight(); y++) {
	                reserves[x][y] = false;
	            }
	        }
	    }
}
