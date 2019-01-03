package iarfmoose;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;

public class QueenManager extends ResponsiveManager {
	
	List<AssignedQueen> assignedQueens;
	List<Queen> idleQueens;
	List<Hatchery> hatcheries;
	CreepManager creepManager;
	
	public QueenManager(S2Agent bot, BaseLocator baseLocator) {
		super(bot, baseLocator);
		assignedQueens = new ArrayList<AssignedQueen>();
		idleQueens = new ArrayList<Queen>();
		hatcheries = new ArrayList<Hatchery>();
		creepManager = new CreepManager(bot, baseLocator);
	}
	
	@Override
	public void onStep(GameState currentState) {
		creepManager.onStep();
		if(bot.observation().getGameLoop() % 8 == 0) {
			updateAssignedQueens();
			updateQueens(currentState);
		}
	}

	@Override
	public void onUnitCreated(UnitInPool unitInPool) {
		creepManager.onUnitCreated(unitInPool);
		UnitType unitType = unitInPool.unit().getType();
		if (unitType == Units.ZERG_QUEEN) {
			idleQueens.add(new Queen(bot, unitInPool));
		} else if (unitType == Units.ZERG_HATCHERY) {
			hatcheries.add(new Hatchery(bot, unitInPool));
		}
	}

	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		creepManager.onUnitDestroyed(unitInPool);
		UnitType unitType = unitInPool.unit().getType();
		if (unitType == Units.ZERG_QUEEN) {
			removeQueen(unitInPool);
		} else if (unitType == Units.ZERG_HATCHERY) {
			removeHatchery(unitInPool);
		}
	}
	

	private void removeQueen(UnitInPool unitInPool) {
		for (Iterator<Queen> queenIterator = idleQueens.iterator(); queenIterator.hasNext();) {
			Queen queen = queenIterator.next();
			try {
				if (queen.getUnitInPool().orElseThrow(() -> new UnitNotFoundException()) == unitInPool) {
					queenIterator.remove();
				}
			} catch (UnitNotFoundException e) {
				queenIterator.remove();
			}
		}
	}
	
	private void removeHatchery(UnitInPool unitInPool) {
		for(Iterator<Hatchery> hatcheryIterator = hatcheries.iterator(); hatcheryIterator.hasNext();) {
			Hatchery hatchery = hatcheryIterator.next();
			if (hatchery.getHatchery() == unitInPool) {
				hatcheryIterator.remove();
			}
		}
	}
		
	private void updateQueens(GameState currentState) {
		tryToInjectLarva();
		tryToDefend(currentState.getThreats());
		tryToSpreadCreep();
	}
		
	private void tryToInjectLarva() {
		for (Iterator<Queen> queenIterator = idleQueens.iterator(); queenIterator.hasNext();) {
			Queen queen = queenIterator.next();
			try {
				Hatchery targetHatchery = findClosestIdleHatchery(queen);
				if (targetHatchery != null) {
					if (queen.injectLarva(targetHatchery.getHatchery())) {
						assignedQueens.add(new AssignedQueen(queen, targetHatchery));
						queenIterator.remove();
					}
				}
			} catch (UnitNotFoundException e) {
				queenIterator.remove();
			}
		}
	}
	
	private void tryToDefend(List<Threat> threats) {
		for (Iterator<Queen> queenIterator = idleQueens.iterator(); queenIterator.hasNext();) {
			Queen queen = queenIterator.next();
			try {
				Threat closestThreat = findClosestThreatOnCreep(queen, threats);
				if (closestThreat != null) {
					Point2d target = closestThreat.getCentreOfGroup().toPoint2d();
					queen.attack(target);
				}
			} catch (UnitNotFoundException e) {
				queenIterator.remove();
			}
		}
	}
	
	private void tryToSpreadCreep() {
		for (Iterator<Queen> queenIterator = idleQueens.iterator(); queenIterator.hasNext();) {
			Queen queen = queenIterator.next();
			try {
				UnitInPool queenInPool = queen.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
				Point2d target = creepManager.calculateCreepTumorPoint(queenInPool);
				queen.spawnCreepTumor(target);
			} catch (UnitNotFoundException e) {
				queenIterator.remove();
			}
			
		}
	}
	
	private void updateAssignedQueens() {
		for (Iterator<AssignedQueen> assignedQueenIterator = assignedQueens.iterator();
				assignedQueenIterator.hasNext();) {
			AssignedQueen assignedQueen = assignedQueenIterator.next();
			if (assignedQueen.spawnLarvaStarted()) {
				assignedQueen.getHatchery().setLastInjectLoop(bot.observation().getGameLoop());
				idleQueens.add(assignedQueen.getQueen());
				assignedQueenIterator.remove();
			} else {
				try {
					assignedQueen.injectHatchery();
				} catch (UnitNotFoundException e) {
					assignedQueenIterator.remove();
				}
			}
		}
 	}
	
	private Threat findClosestThreatOnCreep(Queen queen, List<Threat> threats) throws UnitNotFoundException {
		UnitInPool queenInPool = queen.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		Point queenPosition = queenInPool.unit().getPosition();
		List<Threat> filteredThreats = creepManager.filterThreatsOnCreep(threats);
		Threat closestThreat = null;
		for (Threat threat : filteredThreats) {
			if (closestThreat == null || 
					threat.getCentreOfGroup().distance(queenPosition) < 
					closestThreat.getCentreOfGroup().distance(queenPosition)) {
				closestThreat = threat;
			}
		}
		return closestThreat;
	}
	
	private Hatchery findClosestIdleHatchery(Queen queen) throws UnitNotFoundException {
		UnitInPool queenInPool = queen.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		Point queenPosition = queenInPool.unit().getPosition();
		Hatchery closestHatchery = null;
		for (Hatchery hatchery : hatcheries) {
			if (!hatcheryIsAlreadyAssigned(hatchery) && !hatchery.currentlySpawningLarva()) {
				if (closestHatchery == null ||
						hatchery.getHatchery().unit().getPosition().distance(queenPosition) <
						closestHatchery.getHatchery().unit().getPosition().distance(queenPosition)) {
					closestHatchery = hatchery;
				}
			}
		}
		return closestHatchery;
	}
	
	private boolean hatcheryIsAlreadyAssigned(Hatchery hatchery) {
		for (AssignedQueen assignedQueen : assignedQueens) {
			if (assignedQueen.getHatchery().getHatchery() == hatchery.getHatchery()) {
				return true;
			}
		}
		return false;
	}
}
