package iarfmoose;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class OverlordManager extends ActiveManager {

	private static final float OVERLORD_DISTANCE = 5;
	
	private ChangelingManager changelingManager;
	private List<Overlord> overlords;
	private List<Overlord> unassignedOverlords;
	private List<Overseer> overseers;
	private List<Base> overlordPositions;
	
	public OverlordManager(S2Agent bot, BaseLocator baseLocator) {
		super(bot, baseLocator);
		changelingManager = new ChangelingManager(bot, baseLocator);
		overlordPositions = baseLocator.getBasesSortedFurthestFirst();
		overlords = new ArrayList<Overlord>();
		unassignedOverlords = new ArrayList<Overlord>();
		overseers = new ArrayList<Overseer>();
	}
	
	@Override
	public void onStep() {
		changelingManager.onStep();
		assignOverlords();
		updateOverlords();
		updateOverseers();
	}

	@Override
	public void onUnitCreated(UnitInPool unitInPool) {
		changelingManager.onUnitCreated(unitInPool);
		if (unitInPool.unit().getType() == Units.ZERG_OVERLORD) {
			Overlord newOverlord = new Overlord(bot, unitInPool);
			overlords.add(newOverlord);
			unassignedOverlords.add(newOverlord);
		}
	}

	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		changelingManager.onUnitDestroyed(unitInPool);
		removeOverlordFrom(unitInPool, overlords);
		removeOverlordFrom(unitInPool, unassignedOverlords);
	}
	
	private void removeOverlordFrom(UnitInPool unitInPool, List<Overlord> group) {
		for (Iterator<Overlord> overlordIterator = group.iterator(); overlordIterator.hasNext();) {
			Overlord overlord = overlordIterator.next();
			try {
				UnitInPool overlordInPool = overlord.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
				if (overlordInPool == unitInPool) {
					freeUpAssignedPosition(overlord);
					overlordIterator.remove();
				}
			} catch (UnitNotFoundException e) {
				overlordIterator.remove();
			}
		}
	}
		
	private void assignOverlords() {
		if (unassignedOverlords.size() > 0) {
			assignOverlordToScoutExpansionLocation();
		}
	}
		
	private void assignOverlordToScoutExpansionLocation() {
		if (overlordPositions.size() > 0) {
			Iterator<Overlord> overlordIterator = unassignedOverlords.iterator();
			Overlord overlord = overlordIterator.next();
			Base target = overlordPositions.get(0);
			try {
				overlord.assignPosition(target);
				overlord.move(target.getExpansionLocation().toPoint2d());
				overlordIterator.remove();
				overlordPositions.remove(0);
			} catch (UnitNotFoundException e) {
				overlordIterator.remove();
			}
		}
	}
	
	private void freeUpAssignedPosition(Overlord overlord) {
		if (overlord.hasBeenAssignedAPosition()) {
			Base assignedBase = overlord.getAssignedPosition();
			overlordPositions.add(0, assignedBase);
		}
	}
	
	private void updateOverlords() {
		checkIfAnyOverlordsMorphed();
		for (Iterator<Overlord> overlordIterator = overlords.iterator(); overlordIterator.hasNext();) {
			Overlord overlord = overlordIterator.next();
			try {
				UnitInPool overlordInPool = overlord.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
				if (overlord.hasBeenAssignedAPosition()) {
					overlord.watchPosition();
					if (overlordInPool.unit().getOrders().isEmpty()) {
						overlord.generateCreep();
					}
				}
			} catch (UnitNotFoundException e) {
				overlordIterator.remove();
			}
		}
	}
	
	private void checkIfAnyOverlordsMorphed() {
		for (Iterator<Overlord> overlordIterator = overlords.iterator(); overlordIterator.hasNext();) {
			Overlord overlord = overlordIterator.next();
			try {
				UnitInPool overlordInPool = overlord.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
				if (overlordInPool.unit().getType() == Units.ZERG_OVERSEER) {
					overseers.add(new Overseer(bot, overlordInPool));
					overlordIterator.remove();
				}
			} catch (UnitNotFoundException e) {
				overlordIterator.remove();
			}
		}
	}
	
	private void updateOverseers() {
		tryToAddNewOverseers();
		spawnChangelings();
		moveOverseers();
	}
	
	private void tryToAddNewOverseers() {
		if (overseers.isEmpty() && !overlords.isEmpty()) {
			try {
				Overlord overlord = findClosestOverlordToMain();
				if (overlord != null) {
					overlord.morphToOverseer();
				}
			} catch (UnitNotFoundException e) {
				overlords.remove(0);
			}
		}
	}
	
	private Overlord findClosestOverlordToMain() {
		Point main = baseLocator.getMainBase().getExpansionLocation();
		Overlord closestOverlord = null;
		for (Overlord overlord : overlords) {
			if (closestOverlord == null ||
					overlord.getUnitPosition().distance(main) <
					closestOverlord.getUnitPosition().distance(main)) {
				closestOverlord = overlord;
			}
		}
		return closestOverlord;
	}
	
	private void spawnChangelings() {
		for (Overseer overseer : overseers) {
			try {
				overseer.spawnChangeling();
			} catch (UnitNotFoundException e) {
			}
		}
	}
	
	private void moveOverseers() {
		UnitInPool unitToFollow = null;
		List<UnitInPool> ourArmy = bot.observation().getUnits(
				Alliance.SELF, 
				unitInPool -> 
				UnitData.isFightingUnit(unitInPool.unit().getType()) &&
				unitInPool.unit().getType() != Units.ZERG_QUEEN);
		if(!ourArmy.isEmpty()) {
			unitToFollow = ourArmy.get(0);
		}
		if (unitToFollow != null) {
			for (Iterator<Overseer> overseerIterator = overseers.iterator(); overseerIterator.hasNext();) {
				Overseer overseer = overseerIterator.next();
				Point target = unitToFollow.unit().getPosition();
				try {
					UnitInPool overseerInPool = overseer.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
					if (overseerInPool != null && 
							overseerInPool.unit().getPosition().distance(target) > OVERLORD_DISTANCE) {
						overseer.move(target.toPoint2d());
					}
				} catch (UnitNotFoundException e) {
					
				}
			}
		}
	}
}
