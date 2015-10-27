package bwsal_bridge;

import bwapi.Game;
import bwapi.Mirror;

public class GameHandler {
	private static Mirror mirror;
	
	public static Mirror getMirror() { return mirror; }
	
	public static void setMirror(Mirror mirror) { GameHandler.mirror = mirror; }
	
	public static Game getGame() { return mirror.getGame(); }
}
