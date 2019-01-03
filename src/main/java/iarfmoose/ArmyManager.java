package iarfmoose;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class ArmyManager extends ResponsiveManager {

	private static final float REGROUP_RADIUS = 10;
	
	ArmyUnitTypeHandler armyUnitTypeHandler;
	DefenceManager defenceManager;
	boolean attacking;
	
	List<ArmyUnit> army;
	private Point2d rallyPoint;
	private Point2d rampBottom;
	GameState gameState;
	
	public ArmyManager(S2Agent bot, BaseLocator baseLocator) {
		super(bot, baseLocator);
		armyUnitTypeHandler = new ArmyUnitTypeHandler(bot);
		defenceManager = new DefenceManager(bot, baseLocator);	
		army = new ArrayList<ArmyUnit>();
		rallyPoint = bot.observation().getStartLocation().toPoint2d();
		rampBottom = null;
		gameState = new GameState();
		attacking = false;
	}
	
	@Override
	public void onStep(GameState currentGameState) {
		defenceManager.onStep(currentGameState);
		gameState = currentGameState;
		checkForMorphedUnits();
		if (rampBottom == null) {
			findRampBottom();
		}
		PlayerState self = currentGameState.findPlayerState(Alliance.SELF).orElse(new PlayerState());
		if (self.getBaseCount().orElse(0) >= 2) {
			rallyPoint = rampBottom;
		}
		if (bot.observation().getGameLoop() % 10 == 0) {
		defenceManager.handleThreats(gameState.getThreats());
			if (canAttack()) {
				attack();
			} else if (gameState.getThreats().isEmpty()) {
				retreat();
			}
		}
	}

	@Override
	public void onUnitCreated(UnitInPool unitInPool) {
		UnitType unitType = unitInPool.unit().getType();
		if (unitInPool.unit().getAlliance() == Alliance.SELF &&
				UnitData.isFightingUnit(unitType) && 
				unitType != Units.ZERG_QUEEN &&
				unitType != Units.ZERG_BROODLING) {
			ArmyUnit newUnit = armyUnitTypeHandler.assignBehaviourType(unitInPool);
			army.add(newUnit);
			bot.actions().unitCommand(unitInPool.unit(), Abilities.ATTACK, rallyPoint, false);
			defenceManager.onUnitCreated(unitInPool);
		}
	}

	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		defenceManager.onUnitDestroyed(unitInPool);
		removeArmyUnit(unitInPool);
	}
	
	private void checkForMorphedUnits() {
		List<ArmyUnit> toAdd = new ArrayList<ArmyUnit>();
		for (Iterator<ArmyUnit> armyIterator = army.iterator(); armyIterator.hasNext();) {
			ArmyUnit armyUnit = armyIterator.next();
			if (!armyUnitTypeHandler.isCorrectType(armyUnit)) {
				try {
					UnitInPool unitInPool = armyUnit.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
					armyIterator.remove();
					toAdd.add(armyUnitTypeHandler.assignBehaviourType(unitInPool));
				} catch (UnitNotFoundException e) {
					armyIterator.remove();
				}
			}
		}
		army.addAll(toAdd);
	}
		
	private void removeArmyUnit(UnitInPool unitInPool) {
		for (Iterator<ArmyUnit> armyIterator = army.iterator(); armyIterator.hasNext();) {
			ArmyUnit armyUnit = armyIterator.next();
			if (armyUnit.getUnitInPool().isPresent() && 
					armyUnit.getUnitInPool().get() == unitInPool) {
				armyIterator.remove();
				return;
			}
		}
	}
		
	private boolean canAttack() {
		if (gameState.getThreats().size() > 0) {
			return false;
		}
		float ourArmySupply = getArmySupplyFor(Alliance.SELF);
		float enemyArmySupply = getArmySupplyFor(Alliance.ENEMY);
		if (!attacking) {
			if (ourArmySupply > enemyArmySupply * 1.5 || bot.observation().getFoodUsed() >= 195) {
				attacking = true;
				return true;
			} else {
				return false;
			}
		} else {
			if (ourArmySupply > enemyArmySupply || bot.observation().getFoodUsed() > 150) {
				return true;
			} else {
				attacking = false;
				return false;
			}
		}
	}
	
	private float getArmySupplyFor(Alliance alliance) {
		PlayerState playerState = gameState.findPlayerState(alliance).orElse(new PlayerState());
		return playerState.getArmySupply().orElse((float) 0);
	}
			
	private void attack() {
		UnitInPool ourUnit = getAUnitFromArmy();
		if (ourUnit == null) {
			return;
		}
		try {		
			UnitInPool target = gameState.findClosestEnemyStructureTo(ourUnit).orElseThrow(() -> new UnitNotFoundException());
			for (Iterator<ArmyUnit> armyIterator = army.iterator(); armyIterator.hasNext();) {
				ArmyUnit armyUnit = armyIterator.next();
				try {
					armyUnit.attack(target);
				} catch (UnitNotFoundException e) { //in the event our unit doesn't exist
					armyIterator.remove();
				}
			}
		} catch (UnitNotFoundException e) { //in the event we couldn't find an enemy to attack towards
			searchRandomly();
		}
	}
	
	private void retreat() {
		for (Iterator<ArmyUnit> armyIterator = army.iterator(); armyIterator.hasNext();) {
			ArmyUnit armyUnit = armyIterator.next();
			try {
				UnitInPool armyUnitInPool = armyUnit.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
				Point2d armyUnitPosition = armyUnitInPool.unit().getPosition().toPoint2d();
				if (armyUnitPosition.distance(rallyPoint) > REGROUP_RADIUS) {
					armyUnit.move(rallyPoint);
				}
			} catch (UnitNotFoundException e) { //in the event our unit doesn't exist
				armyIterator.remove();
			}
		}
	}
	
	private UnitInPool getAUnitFromArmy() {
		Iterator<ArmyUnit> armyIterator = army.iterator();
		while (armyIterator.hasNext()) {
			ArmyUnit armyUnit = armyIterator.next();
			try {
				UnitInPool armyUnitInPool = armyUnit.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
				return armyUnitInPool;
			} catch (UnitNotFoundException e) {
				armyIterator.remove();
			}
		}
		return null;
	}
			
	private void searchRandomly() {
		for (ArmyUnit armyUnit : army) {
			if (armyUnit.isIdle()) {
				Point2d target = bot.observation().getGameInfo().findRandomLocation();
				try {
					armyUnit.attack(target);
				} catch (UnitNotFoundException e) {
					//we don't need to do anything here because a dead unit should be removed next frame
				}
			}
		}
	}
	
	private void findRampBottom() {
		for (UnitInPool unit : bot.observation().getUnits(Alliance.NEUTRAL)) {
			if (UnitData.isUnbuildableRocks(unit.unit().getType()) &&
					unit.unit().getPosition().distance(bot.observation().getStartLocation()) < 30) {
				rampBottom = unit.unit().getPosition().toPoint2d();
			}
		}
	}

}
