package iarfmoose;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.DisplayType;

public class BuildingPlacer {
	
	private static final int MINIMUM_OFFSET = -10;
	private static final int MAXIMUM_OFFSET = 10;
	private static final int STRUCTURES_PER_BASE = 5;
	private static final float LOCAL_RADIUS = 15;
	
	S2Agent bot;
	private List<Base> openBases;
	private List<Base> occupiedBases;
	UnitInPool currentTargetHatchery;
	
	public BuildingPlacer(S2Agent bot, BaseLocator baseLocator) {
		this.bot = bot;
		this.openBases = new ArrayList<Base>(baseLocator.getBasesSortedClosestFirst());
		this.openBases.remove(0);
		this.occupiedBases = new ArrayList<Base>();
		occupiedBases.add(baseLocator.getMainBase());
		currentTargetHatchery = findHatchery();
	}
	
	public void update() {
		if (currentTargetHatchery == null || 
				!currentTargetHatchery.isAlive() ||
				calculateNearbyStructureCount(currentTargetHatchery) > STRUCTURES_PER_BASE) {
			currentTargetHatchery = findHatchery();
		}
	}
	
	public void buildStructure(UnitInPool builder, Ability structureType) {
		Point2d buildPoint = findBuildPoint(structureType);
		bot.actions().unitCommand(builder.unit(), structureType, buildPoint, false);
	}
	
	public void buildGas(UnitInPool builder) {
		UnitInPool geyser = getNextGeyser();
		if (geyser != null) {
			//units with DisplayType == snapshot cannot be targeted with abilities
			geyser = tryToUpdateSnapshotGeyser(geyser);
			//if this geyser is not visible, issue a move command towards it and then we can call again when in visibility range
			if (geyser.unit().getDisplayType() == DisplayType.SNAPSHOT) {
				bot.actions().unitCommand(builder.unit(), Abilities.MOVE, geyser.unit().getPosition().toPoint2d(), false);
			} else {
				bot.actions().unitCommand(builder.unit(), Abilities.BUILD_EXTRACTOR, geyser.unit(), false);
			}
		} 
	}
	
	public void buildSpineCrawler(UnitInPool builder) {
		Point2d buildPoint = findSpineCrawlerBuildPoint();
		bot.actions().unitCommand(builder.unit(), Abilities.BUILD_SPINE_CRAWLER, buildPoint, false);
	}
	
	public void expand(UnitInPool builder) {
		Point buildPoint = getNextExpansionLocation();	
		bot.actions().unitCommand(builder.unit(), Abilities.BUILD_HATCHERY, buildPoint.toPoint2d(), false);
	}
	
	public Point getNextExpansionLocation() {
		if (openBases.size() > 0) {
			return openBases.get(0).getExpansionLocation();
		} else {
			return Point.of(0, 0);
		}
	}
	
	public void expansionStarted() {
		if (openBases.size() > 0) {
			Base newBase = openBases.get(0);
			occupiedBases.add(newBase);
			openBases.remove(0);
		}
	}
	
	public void baseDestroyed(Point location) {
		Base destroyedBase = findClosestBaseTo(location, occupiedBases);
		occupiedBases.remove(destroyedBase);
		addDestroyedBaseToOpenBases(destroyedBase);
	}
		
	public Point getCurrentTargetHatcheryLocation() {
		if (currentTargetHatchery == null) {
			currentTargetHatchery = findHatchery();	
		} 
		if (currentTargetHatchery != null) {
			return currentTargetHatchery.unit().getPosition();
		} else {
			return bot.observation().getStartLocation();
		}
	}
	
	private Base findClosestBaseTo(Point location, List<Base> bases) {
		Base closestBase = null;
		for (Base base : bases) {
			if (closestBase == null ||
					base.getExpansionLocation().distance(location) < 
					closestBase.getExpansionLocation().distance(location)) {
				closestBase = base;
			}
		}
		if (closestBase == null) {
			return new Base();
		}
		return closestBase;
	}

	private void addDestroyedBaseToOpenBases(Base destroyedBase) {
		int index = 0;
		Point startLocation = bot.observation().getStartLocation();
		while(index < openBases.size() - 1 && 
				openBases.get(index).getExpansionLocation().distance(startLocation) <
				destroyedBase.getExpansionLocation().distance(startLocation)) {
			index++;
		}
		openBases.add(index, destroyedBase);
	}
					
	private Point2d findBuildPoint(Ability ability) {
		Point2d buildPoint = bot.observation().getStartLocation().toPoint2d();
		if (currentTargetHatchery != null) {
			Point2d centre = currentTargetHatchery.unit().getPosition().toPoint2d();
			List<Point2d> placementGrid = findPlaceablePoints(centre, ability);
			buildPoint = getRandomPoint(placementGrid);
		}
		return buildPoint;
	}
	
	private Point2d findSpineCrawlerBuildPoint() {
		Point2d buildPoint = bot.observation().getStartLocation().toPoint2d();
		if (occupiedBases.size() != 0) {
			Base targetBase = occupiedBases.get(occupiedBases.size() - 1);
			List<Point2d> placeablePoints = findPlaceablePoints(
					targetBase.getExpansionLocation().toPoint2d(), 
					Abilities.BUILD_SPINE_CRAWLER);
			List<Point2d> prioritisedPoints = prioritiseTowardsCentre(placeablePoints);
			for(Point2d point : prioritisedPoints) {
				if (!isBlockedByStructure(point) &&
						isSurroundedByCreep(point)) {
					buildPoint = point;
					return buildPoint;
				}
			}
		}
		return buildPoint;
	}
		
	private boolean isSurroundedByCreep(Point2d point) {
		if (bot.observation().hasCreep(point) && 
				bot.observation().hasCreep(Point2d.of(point.getX() + 1, point.getY() + 1)) &&
				bot.observation().hasCreep(Point2d.of(point.getX() - 1, point.getY() + 1)) &&
				bot.observation().hasCreep(Point2d.of(point.getX() + 1, point.getY() - 1)) &&
				bot.observation().hasCreep(Point2d.of(point.getX() - 1, point.getY() - 1))) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isBlockedByStructure(Point2d point) {
		int space = 3;
		for (UnitInPool structure : bot.observation().getUnits(Alliance.SELF, 
				unitInPool -> UnitData.isStructure(unitInPool.unit().getType()) ||
				UnitData.isCreepTumor(unitInPool.unit().getType()))) {
			if (point.distance(structure.unit().getPosition().toPoint2d()) < space) {
				return true;
			}
		}
		return false;
	}
	
	private List<Point2d> prioritiseTowardsCentre(List<Point2d> grid) {
		Point2d centre = bot.observation().getGameInfo().findCenterOfMap().toPoint2d();
		List<Point2d> prioritisedGrid = new ArrayList<Point2d>();
		for (int i = 0; i < grid.size(); i++) {
			Point2d closestPoint = findClosestPointTo(centre, grid);
			prioritisedGrid.add(closestPoint);
			grid.remove(closestPoint);
		}
		return prioritisedGrid;
	}
	
	private Point2d findClosestPointTo(Point2d startPoint, List<Point2d> grid) {
		Point2d closestPoint = null;
		for (Point2d point : grid) {
			if (closestPoint == null ||
					point.distance(startPoint) < 
					closestPoint.distance(startPoint)) {
				closestPoint = point;
			}
		}
		if (closestPoint == null) {
			return Point2d.of(startPoint.getX(), startPoint.getY());
		} else {
			return closestPoint;
		}
	}
	
	private UnitInPool findHatchery() {
		List<UnitInPool> hatcheries = bot.observation().getUnits(
				Alliance.SELF, 
				unit -> 
				UnitData.isTownHall(unit.unit().getType()) &&
				unit.unit().getBuildProgress() == 1.0);
		for (UnitInPool hatchery : hatcheries) {
			if (calculateNearbyStructureCount(hatchery) < STRUCTURES_PER_BASE) {
				return hatchery;
			}
		}
		if (hatcheries.isEmpty()) {
			return null;
		} else {
			return hatcheries.get(0);
		}
		
	}
	
	private int calculateNearbyStructureCount(UnitInPool centreStructure) {
		int structureCount = 0;
		Point centreStructurePosition = centreStructure.unit().getPosition();
		List<UnitInPool> structures = bot.observation().getUnits(
				Alliance.SELF, 
				unit -> 
				UnitData.isStructure(unit.unit().getType()));
		for (UnitInPool structure : structures) {
			Point structurePosition = structure.unit().getPosition();
			if (structurePosition.distance(centreStructurePosition) < LOCAL_RADIUS &&
					structure != centreStructure) {
				structureCount++;
			}
		}
		return structureCount;
	}
	
	private List<Point2d> findPlaceablePoints(Point2d centre, Ability ability) {
		List<Point2d> placementGrid = generatePlacementGrid(centre);
		if (ability == Abilities.BUILD_SPINE_CRAWLER) {
			placementGrid = generatePlacementGrid(centre, 5);
		}
		List<Point2d> outputGrid = new ArrayList<Point2d>();
		for (Point2d point : placementGrid) {
			if (bot.observation().hasCreep(point)) {
				outputGrid.add(point);
			}
		}
		return outputGrid;
	}
	
	private ArrayList<Point2d> generatePlacementGrid(Point2d centre) {
		ArrayList<Point2d> placementGrid = new ArrayList<Point2d>();
		for (int x = MINIMUM_OFFSET; x <= MAXIMUM_OFFSET; x++) {
			for (int y = MINIMUM_OFFSET; y <= MAXIMUM_OFFSET; y++) {
				Point2d point = Point2d.of(centre.getX() + x, centre.getY() + y);
				placementGrid.add(point);
			}
		}
		return placementGrid;
	}
	
	private ArrayList<Point2d> generatePlacementGrid(Point2d centre, int size) {
		ArrayList<Point2d> placementGrid = new ArrayList<Point2d>();
		for (int x = -size; x <= size; x++) {
			for (int y = -size; y <= size; y++) {
				Point2d point = Point2d.of(centre.getX() + x, centre.getY() + y);
				placementGrid.add(point);
			}
		}
		return placementGrid;
	}
	
	private Point2d getRandomPoint(List<Point2d> points) {
		if(points.size() > 0) {
			Random randomValue = new Random();
			int index = randomValue.nextInt(points.size());
			return points.get(index);
		} else {
			return Point2d.of(0, 0);
		}
	}

	private UnitInPool getNextGeyser() {
		for (Base base : occupiedBases) {
			for (UnitInPool geyser : base.getGeysers()) {
				boolean alreadyTaken = false;
				for (UnitInPool extractor : bot.observation().getUnits(Alliance.SELF, 
						unit -> unit.unit().getType() == Units.ZERG_EXTRACTOR)) {
					if (extractor.unit().getPosition().distance(geyser.unit().getPosition()) < 2) {
						alreadyTaken = true;
					}
				}
				if (!alreadyTaken) {
					return geyser;
				}
			}
		}
		return null;
	}
					
	private UnitInPool tryToUpdateSnapshotGeyser(UnitInPool snapshotGeyser) {
		for (UnitInPool geyser : bot.observation().getUnits(Alliance.NEUTRAL, 
				geyser -> UnitData.isGasGeyser(geyser.unit().getType()))) {
			if (geyser.unit().getPosition().distance(snapshotGeyser.unit().getPosition()) < 2) {
				return geyser;
			}
		}
		return snapshotGeyser;	
	}
	
}
