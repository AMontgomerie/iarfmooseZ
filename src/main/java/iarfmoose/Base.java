package iarfmoose;

import java.util.ArrayList;
import java.util.List;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point;

public class Base {
	private Point expansionLocation;
	private List<UnitInPool> minerals;
	private List<UnitInPool> geysers;
	
	public Base() {
		this.expansionLocation = Point.of(0, 0);
		this.minerals = new ArrayList<UnitInPool>();
		this.geysers = new ArrayList<UnitInPool>();	
	}
	
	public Base (Point expansionLocation, List<UnitInPool> minerals, List<UnitInPool> geysers) {
		this.expansionLocation = expansionLocation;
		this.minerals = minerals;
		this.geysers = geysers;
	}
	
	public Point getExpansionLocation() {
		return expansionLocation;
	}
	
	public List<UnitInPool> getMinerals() {
		return minerals;
	}
	
	public List<UnitInPool> getGeysers() {
		return geysers;
	}
}
