package iarfmoose;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.raw.StartRaw;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.spatial.Size2dI;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;

public class CreepManager extends ActiveManager {

	private static final float SPREAD_RADIUS = 9;
	private static final float MINIMUM_SPACE = 5;
	private static final float LOCAL_RADIUS = 15;
	private static final float MAX_DISTANCE_FROM_GOAL = 8;
	private static final float BASE_RADIUS = 6;
	
	Set<CreepNode> creepNodes;
	Point2d creepGoal;
	
	public CreepManager(S2Agent bot, BaseLocator baseLocator) {
		super(bot, baseLocator);
		creepNodes = new HashSet<CreepNode>();
		creepGoal = getEnemyStartLocation();
	}
	
	@Override
	public void onStep() {
		creepGoal = getGoal();
		if (bot.observation().getGameLoop() % 22 == 0) {
			spread();
		}
	}

	@Override
	public void onUnitCreated(UnitInPool unitInPool) {
		UnitType unitType = unitInPool.unit().getType();
		if(UnitData.isCreepTumor(unitType) ||
				unitType == Units.ZERG_HATCHERY) {
			creepNodes.add(new CreepNode(unitInPool));
		}
	}

	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		for (Iterator<CreepNode> nodeIterator = creepNodes.iterator(); nodeIterator.hasNext();) {
			CreepNode node = nodeIterator.next();
			if (node.getNode() == unitInPool) {
				nodeIterator.remove();
			}
		}
	}
	
	public List<Threat> filterThreatsOnCreep(List<Threat> threats) {
		List<Threat> filteredThreats = new ArrayList<Threat>();
		for (Threat threat : threats) {
			Point threatPosition = threat.getCentreOfGroup();
			for (CreepNode creepNode : creepNodes) {
				Point nodePosition = creepNode.getNode().unit().getPosition();
				if (threatPosition.distance(nodePosition) < LOCAL_RADIUS &&
						nodePosition.distance(bot.observation().getStartLocation()) < 50) {
					filteredThreats.add(threat);
				}
			}
		}
		return filteredThreats;
	}
	
	public Point2d calculateCreepTumorPoint(UnitInPool queen) {
		Point2d point = findUnconnectedBase();
		if (point == null) {
			point = findClosestCreepEdge(queen);
		}
		return point;
	}
	
	private Point2d findClosestCreepEdge(UnitInPool unit) {
		UnitInPool node = getClosestUnitTo(creepGoal, creepNodesToUnitInPool());
		if(node == null) {
			return bot.observation().getStartLocation().toPoint2d();
		}
		return calculateNewNodePoint(node, unit.unit().getType());
	}
	
	private Point2d findUnconnectedBase() {
		for (CreepNode node : creepNodes) {
			UnitInPool base = node.getNode();
			if (base.unit().getType() == Units.ZERG_HATCHERY && base.unit().getBuildProgress() == 1.0) {
				int nearbyNodes = 0;
				for (CreepNode nearbyNode : creepNodes) {
					Unit nearby = nearbyNode.getNode().unit();
					if (UnitData.isCreepTumor(nearby.getType()) &&
							nearby.getPosition().distance(base.unit().getPosition()) < LOCAL_RADIUS) {
						nearbyNodes++;
					}
				}
				if (nearbyNodes < 3) {
					return calculateNewNodePoint(base, Units.ZERG_QUEEN);
				}
			}
		}
		return null;
	}
	
	private void spread() {
		for (CreepNode node : creepNodes) {
			if (node.isActive() && node.getNode().unit().getBuildProgress() == 1.0) {
				Point2d target = calculateNewNodePoint(node.getNode(), Units.ZERG_CREEP_TUMOR);
				bot.actions().unitCommand(
						node.getNode().unit(), 
						Abilities.BUILD_CREEP_TUMOR_TUMOR, 
						target, 
						false);
			}
		}
	}
	
	private Point2d calculateNewNodePoint(UnitInPool currentNode, UnitType caster) {
		List<Point> spawnGrid = generateSpawnGrid(currentNode);
		//creep tumors and queens have different goals when spawning creep tumors
		if (UnitData.isCreepTumor(caster)) { //tumors try to spread away from the mass centre
			spawnGrid = prioritiseSpawnGridAwayFromCentre(spawnGrid, currentNode);
		} else {
			spawnGrid = prioritiseSpawnGridTowardsGoal(spawnGrid);
		}	
		for (Point point : spawnGrid) {
			UnitInPool closestNode = getClosestUnitTo(point.toPoint2d(), creepNodesToUnitInPool());
			if (closestNode != null) {
				if (point.distance(closestNode.unit().getPosition()) > MINIMUM_SPACE &&
						bot.observation().hasCreep(point.toPoint2d()) &&
						bot.observation().isPathable(point.toPoint2d()) &&
						!pointBlocksExpansionLocation(point) &&
						!pointContainsMapFeature(point)) {
					return point.toPoint2d();
				}
			}
		}
		return currentNode.unit().getPosition().toPoint2d();
	}
		
	private List<Point> generateSpawnGrid(UnitInPool currentNode) {
		Size2dI mapSize = getMapSize();
		List<Point> spawnGrid = new ArrayList<Point>();
		float minX = currentNode.unit().getPosition().getX() - SPREAD_RADIUS;
		float minY = currentNode.unit().getPosition().getY() - SPREAD_RADIUS;
		float maxX = currentNode.unit().getPosition().getX() + SPREAD_RADIUS;
		float maxY = currentNode.unit().getPosition().getY() + SPREAD_RADIUS;
		if (minX < 0) {
			minX = 0;
		}
		if (minY < 0) {
			minY = 0;
		}
		if (mapSize != null) {
			int mapMaxX = mapSize.getX();
			int mapMaxY = mapSize.getY();
			if (maxX > mapMaxX) {
				maxX = mapMaxX;
			}
			if (maxY > mapMaxY) {
				maxY = mapMaxY;
			}
		}
		for (float x = minX; x < maxX; x++) {
			for (float y = minY; y < maxY; y++) {
				spawnGrid.add(Point.of(x, y));
			}
		}
		return spawnGrid;
	}
	
	private Size2dI getMapSize() {
		Optional<StartRaw> startRaw = bot.observation().getGameInfo().getStartRaw();
		if (startRaw.isPresent()) {
			return startRaw.get().getMapSize();
		} else {
			return null;
		}
	}
	
	private List<Point> prioritiseSpawnGridAwayFromCentre(List<Point> grid, UnitInPool node) {
		List<Point> prioritisedGrid = new ArrayList<Point>();
		//Point2d creepCentre = calculateCreepCentre();
		Point2d creepCentre = calculateLocalCentre(node);
		for (int i = 0; i < grid.size(); i++) {
			Point furthestPoint = findFurthestPointFrom(creepCentre, grid);
			prioritisedGrid.add(furthestPoint);
			grid.remove(furthestPoint);
		}
		return prioritisedGrid;
	}
		
	private List<Point> prioritiseSpawnGridTowardsGoal(List<Point> grid) {
		List<Point> prioritisedGrid = new ArrayList<Point>();
		for (int i = 0; i < grid.size(); i++) {
			Point closestPoint = findClosestPointTo(creepGoal, grid);
			prioritisedGrid.add(closestPoint);
			grid.remove(closestPoint);
		}
		return prioritisedGrid;
	}
	
	private Point2d getEnemyStartLocation() {
		Set<Point2d> startLocations = getPossibleStartLocations();
		Point2d ourStartLocation = bot.observation().getStartLocation().toPoint2d();
		Point2d enemyStartLocation = bot.observation().getGameInfo().findCenterOfMap().toPoint2d();
		for (Point2d location : startLocations) {
			if (location != ourStartLocation) {
				enemyStartLocation = location;
			}
		}
		return enemyStartLocation;
	}
	
	private Set<Point2d> getPossibleStartLocations() {
		try {
			return bot.observation().getGameInfo().getStartRaw().orElseThrow().getStartLocations();
		} catch (NoSuchElementException e) {
			return new HashSet<Point2d>();
		}
	}
	
	private Point findClosestPointTo(Point2d startPoint, List<Point> grid) {
		Point closestPoint = null;
		for (Point point : grid) {
			if (closestPoint == null ||
					point.toPoint2d().distance(startPoint) < 
					closestPoint.toPoint2d().distance(startPoint)) {
				closestPoint = point;
			}
		}
		if (closestPoint == null) {
			return Point.of(startPoint.getX(), startPoint.getY());
		} else {
			return closestPoint;
		}
	}
	
	private Point findFurthestPointFrom(Point2d startPoint, List<Point> grid) {
		Point furthestPoint = null;
		for (Point point : grid) {
			if (furthestPoint == null ||
					point.toPoint2d().distance(startPoint) > 
					furthestPoint.toPoint2d().distance(startPoint)) {
				furthestPoint = point;
			}
		}
		if (furthestPoint == null) {
			return Point.of(startPoint.getX(), startPoint.getY());
		} else {
			return furthestPoint;
		}
	}
	
	private Set<UnitInPool> creepNodesToUnitInPool() {
		Set<UnitInPool> output = new HashSet<UnitInPool>();
		for (CreepNode node : creepNodes) {
			output.add(node.getNode());
		}
		return output;
	}
	
	private Point2d calculateLocalCentre(UnitInPool startNode) {
		Point2d centre = startNode.unit().getPosition().toPoint2d();
		float averageX = 0;
		float averageY = 0;
		int localNodes = 0;
		for (CreepNode node : creepNodes) {
			if (node.getNode().unit().getPosition().toPoint2d().distance(centre) < LOCAL_RADIUS) {
				Point nodePoint = node.getNode().unit().getPosition();
				averageX += nodePoint.getX();
				averageY += nodePoint.getY();
				localNodes++;
			}
		}
		averageX /= localNodes;
		averageY /= localNodes;
		centre = Point2d.of(averageX, averageY);
		return centre;	
	}
		
	private boolean pointContainsMapFeature(Point point) {
		for (UnitInPool unitInPool : bot.observation().getUnits(Alliance.NEUTRAL)) {
			if (unitInPool.unit().getPosition().distance(point) <= 3) {
				return true;
			}
		}
		return false;
	}
	
	private boolean pointBlocksExpansionLocation(Point point) {
		for (Base base : baseLocator.getBasesSortedClosestFirst()) {
			if (point.distance(base.getExpansionLocation()) <= BASE_RADIUS) {
				return true;
			}
		}
		return false;
	}
	
	private Point2d getGoal() {
		List<Point> goals = generateGoals();
		Random randomValue = new Random();
		return goals.get(randomValue.nextInt(goals.size())).toPoint2d();
	}
	
	private List<Point> generateGoals() {
		List<Point> goals = new ArrayList<Point>();
		Point2d enemyStartLocation = getEnemyStartLocation();
		Point2d middle = bot.observation().getGameInfo().findCenterOfMap().toPoint2d();
		
		if (!nearbyNodeExists(Point.of(middle.getX(), middle.getY()))) {
			goals.add(Point.of(middle.getX(), middle.getY()));
		}
		if (!nearbyNodeExists(Point.of(enemyStartLocation.getX(), enemyStartLocation.getY()))) {
			goals.add(Point.of(middle.getX(), middle.getY()));
		}
		for (Base base : baseLocator.getBasesSortedFurthestFirst()) {
			if (!nearbyNodeExists(base.getExpansionLocation())) {
				goals.add(base.getExpansionLocation());
			}
		}
		return goals;
	}
	
	private boolean nearbyNodeExists(Point location) {
		for (CreepNode node : creepNodes) {
			if (node.getNode().unit().getType() != Units.ZERG_HATCHERY) {
				Point nodeLocation = node.getNode().unit().getPosition();
				if (location.distance(nodeLocation) < MAX_DISTANCE_FROM_GOAL) {
					return true;
				}
			}
		}
		return false;
	}
	
	public UnitInPool getClosestUnitTo(Point2d point, Collection<UnitInPool> units) {
		Point2d startPoint = point;
		UnitInPool closestUnit = null;
		for (UnitInPool unitInPool : units) {
			Point otherUnitPoint = unitInPool.unit().getPosition();
			if (closestUnit == null || 
					startPoint.distance(otherUnitPoint.toPoint2d()) < 
					startPoint.distance(closestUnit.unit().getPosition().toPoint2d())) {
				closestUnit = unitInPool;
			}
		}
		return closestUnit;
	}
}
