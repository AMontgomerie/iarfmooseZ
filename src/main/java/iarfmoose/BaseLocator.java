
package iarfmoose;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.DisplayType;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class BaseLocator {
	
	private static final float BASE_RADIUS = 12;
	
	private S2Agent bot;
	private List<Base> bases;
	
	public BaseLocator(S2Agent bot) {
		this.bot = bot;
		List<Point> expansionLocations = findExpansionLocations();
		//base locations are sorted by ground distance from main initially and stored in that order as Bases
		List<Point> sortedLocations = sortLocationsByClosest(expansionLocations);
		this.bases = generateBases(sortedLocations);
	}
	
	public List<Base> getBasesSortedClosestFirst() {
		return bases;
	}
	
	public List<Base> getBasesSortedFurthestFirst() {
		return sortBasesByFurthest(bases);
	}
	
	public Base getMainBase() {
		return new Base(bot.observation().getStartLocation(),
				findMinerals(bot.observation().getStartLocation()),
				findGeysers(bot.observation().getStartLocation()));
	}
	
	public List<Base> getEnemyStartLocationBases() {
		try {
			Set<Point2d> startLocations = bot.observation().getGameInfo().getStartRaw().orElseThrow().getStartLocations();
			List<Base> startLocationBases = new ArrayList<Base>();
			for (Point2d startLocation : startLocations) {
				Point location = Point.of(startLocation.getX(), startLocation.getY());
				startLocationBases.add(new Base(location, findMinerals(location), findGeysers(location)));
			}
			return startLocationBases;
		} catch (NoSuchElementException e) {
			return new ArrayList<Base>();
		}
	}
	
	public Base findClosestBaseTo(Point target) {
		Base closestBase = null;
		for (Base base : bases) {
			if (closestBase == null 
					|| base.getExpansionLocation().distance(target) < 
					closestBase.getExpansionLocation().distance(target)) {
				closestBase = base;
			}
		}
		return closestBase;
	}
	
	public void updateResourceNodeDisplayTypes() {
		List<Base> updatedBases = new ArrayList<Base>(bases);
		for (UnitInPool visibleNode : getAllVisibleMineralsAndGeysers()) {
			for (Base base : bases) {
				boolean modified = false;
				List<UnitInPool> updatedMinerals = new ArrayList<UnitInPool>(base.getMinerals());
				List<UnitInPool> updatedGeysers = new ArrayList<UnitInPool>(base.getGeysers());
				if (UnitData.isMineral(visibleNode.unit().getType())) {
					updateListWithVisibleNode(updatedMinerals, visibleNode);
				} else {
					updateListWithVisibleNode(updatedGeysers, visibleNode);
				}
				if (modified == true) {
					Point expansionLocation = base.getExpansionLocation();
					Base updatedBase = new Base(expansionLocation, updatedMinerals, updatedGeysers);
					updatedBases.remove(bases.indexOf(base));
					updatedBases.add(bases.indexOf(base), updatedBase);					
				}
			}
		}
		bases = updatedBases;
	}
	
	private List<UnitInPool> getAllVisibleMineralsAndGeysers() {
		return bot.observation().getUnits(
				Alliance.NEUTRAL, 
				unitInPool -> UnitData.isGasGeyser(unitInPool.unit().getType()) ||
							  UnitData.isMineral(unitInPool.unit().getType()) ||
							  unitInPool.unit().getDisplayType() == DisplayType.VISIBLE);
	}
	
	private List<UnitInPool> updateListWithVisibleNode(List<UnitInPool> nodes, UnitInPool visibleNode) {
		List<UnitInPool> updatedNodes = new ArrayList<UnitInPool>(nodes);
		for (UnitInPool node : nodes) {
			if (isMatchingResourceNode(node, visibleNode) && node.unit().getDisplayType() == DisplayType.SNAPSHOT) {
				updatedNodes.remove(nodes.indexOf(node));
				updatedNodes.add(visibleNode);
			}
		}
		return updatedNodes;
	}
	
	private boolean isMatchingResourceNode(UnitInPool nodeA, UnitInPool nodeB) {
		if (nodeA.unit().getPosition().distance(nodeB.unit().getPosition()) < 2) {
			return true;
		} else {
			return false;
		}
	}
		
	private List<Point> findExpansionLocations() {
		List<Point> baseLocations = bot.query().calculateExpansionLocations(bot.observation());
		return baseLocations;
	}
			
	private List<Base> generateBases(List<Point> expansionLocations) {
		List<Base> newBases = new ArrayList<Base>();
		newBases.add(getMainBase());
		for (Point location : expansionLocations) {
			List<UnitInPool> minerals = findMinerals(location);
			List<UnitInPool> geysers = findGeysers(location);
			newBases.add(new Base(location, minerals, geysers));
		}
		newBases = addEnemyStartLocations(newBases);
		return newBases;
	}
	
	private List<Base> addEnemyStartLocations(List<Base> bases) {
		List<Base> possibleEnemyBases = getEnemyStartLocationBases();
		for (Base enemyBase : possibleEnemyBases) {
			boolean alreadyExists = false;
			for (Base base : bases) {
				if (base.getExpansionLocation().distance(enemyBase.getExpansionLocation()) < 2) {
					alreadyExists = true;
				}
			}
			if (!alreadyExists) {
				bases.add(enemyBase);
			}
		}
		return bases;
	}
	
	private List<UnitInPool> findMinerals(Point baseLocation) {
		List<UnitInPool> minerals = new ArrayList<UnitInPool>();
		for (UnitInPool neutralUnit : bot.observation().getUnits(Alliance.NEUTRAL)) {
			if (UnitData.isMineral(neutralUnit.unit().getType()) && 
					neutralUnit.unit().getPosition().distance(baseLocation) < BASE_RADIUS) {
				minerals.add(neutralUnit);
			}
		}
		return minerals;
	}
	
	private List<UnitInPool> findGeysers(Point baseLocation) {
		List<UnitInPool> geysers = new ArrayList<UnitInPool>();
		for (UnitInPool neutralUnit : bot.observation().getUnits(Alliance.NEUTRAL)) {
			if (UnitData.isGasGeyser(neutralUnit.unit().getType()) && 
					neutralUnit.unit().getPosition().distance(baseLocation) < BASE_RADIUS) {
				geysers.add(neutralUnit);
			}
		}
		return geysers;
	}
	
	private List<Point> sortLocationsByClosest(List<Point> inputLocations) {
		List<Point> outputLocations = new ArrayList<Point>();
		while (inputLocations.size() > 0) {
			Point closestPoint = findClosestLocationToMain(inputLocations);
			inputLocations = removeLocationFromList(closestPoint, inputLocations);
			if (bot.observation().getStartLocation().distance(closestPoint) > 5) {
				outputLocations.add(closestPoint);
			}
		}
		outputLocations = removeUnpathableLocations(outputLocations);
		return outputLocations;
	}
	
	private Point findClosestLocationToMain(List<Point> locations) {
		Point main = bot.observation().getStartLocation();
		Point closestLocation = null;
		Unit tester = getPathingTestUnit();
		for (Point location : locations) {
			if (closestLocation == null) {
				closestLocation = location;
			} else if (tester == null) { //when we have no workers just compare air distances
				if (location.distance(main) < closestLocation.distance(main)) {
					closestLocation = location;
				}
			} else { //using a worker we can test pathing distance
				float currentDistance = bot.query().pathingDistance(tester, location.toPoint2d());
				float closestDistance = bot.query().pathingDistance(tester, closestLocation.toPoint2d());
				if (currentDistance < closestDistance) {
					closestLocation = location;
				}
			}
		}
		return closestLocation;
	}
	
	private List<Base> sortBasesByFurthest(List<Base> inputBases) {		
		List<Base> outputBases = new ArrayList<Base>();
		while (inputBases.size() > 0) {
			Base furthestBase = findFurthestBaseFromMain(inputBases);
			inputBases = removeBaseFromList(furthestBase, inputBases);
			outputBases.add(furthestBase);
		}	
		return outputBases;
	}
		
	private Base findFurthestBaseFromMain(List<Base> inputBases) {
		Point main = bot.observation().getStartLocation();
		Base furthestBase = null;
		for (Base base : inputBases) {	
			if (furthestBase == null || 
					base.getExpansionLocation().distance(main) > 
					furthestBase.getExpansionLocation().distance(main)) {
				furthestBase = base;
			}
		}
		return furthestBase;
	}
	
	private Unit getPathingTestUnit() {
		Unit tester = null;
		List<UnitInPool> workers = bot.observation().getUnits(Alliance.SELF, 
				unit -> UnitData.isWorker(unit.unit().getType()));
		if (workers.size() > 0) {
			tester = workers.get(0).unit();
		}
		return tester;
	}
	
	private List<Point> removeLocationFromList(Point toRemove, List<Point> locations) {
		List<Point> outputLocations = new ArrayList<Point>();
		for (Point location : locations) {
			if (location.distance(toRemove) > 1) {
				outputLocations.add(location);
			}
		}
		return outputLocations;
	}
	
	private List<Base> removeBaseFromList(Base toRemove, List<Base> inputBases) {
		List<Base> outputBases = new ArrayList<Base>();
		for (Base base : inputBases) {
			if (base.getExpansionLocation().distance(toRemove.getExpansionLocation()) > 1) {
				outputBases.add(base);
			}
		}
		return outputBases;
	}
	
	private List<Point> removeUnpathableLocations(List<Point> locations) {
		Unit tester = getPathingTestUnit();
		for (Iterator<Point> locationIterator = locations.iterator(); locationIterator.hasNext();) {
			Point location = locationIterator.next();
			if (bot.query().pathingDistance(tester, location.toPoint2d()) == 0) {
				locationIterator.remove();
			}
		}
		return locations;
	}
}
