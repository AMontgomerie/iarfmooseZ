package iarfmoose;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.UnitTypeData;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.game.PlayerInfo;
import com.github.ocraft.s2client.protocol.observation.raw.Visibility;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class InformationManager extends ActiveManager {
	
	private static final int EARLY_GAME = 8000;
	
	ThreatHandler threatHandler;
	
	Set<UnitInPool> enemyArmy;
	Set<UnitInPool> enemyWorkers;
	Set<UnitInPool> enemyStructures;
	Set<UnitInPool> enemyBases;
	Set<UnitInPool> enemyGases;
	Set<UnitInPool> ourArmy;
	Set<UnitInPool> ourWorkers;
	Set<UnitInPool> ourStructures;
	Set<UnitInPool> ourBases;
	Set<UnitInPool> ourGases;
	float enemyArmySupply;
	float ourArmySupply;
	Set<Upgrade> ourCompletedUpgrades;
	
	public InformationManager(S2Agent bot, BaseLocator baseLocator) {
		super(bot, baseLocator);
		threatHandler = new ThreatHandler(bot);
		enemyArmy = new HashSet<UnitInPool>();
		enemyWorkers = new HashSet<UnitInPool>();
		enemyStructures = new HashSet<UnitInPool>();
		enemyBases = new HashSet<UnitInPool>();
		enemyGases = new HashSet<UnitInPool>();
		ourArmy = new HashSet<UnitInPool>();
		ourWorkers = new HashSet<UnitInPool>();
		ourStructures = new HashSet<UnitInPool>();
		ourBases = new HashSet<UnitInPool>();
		ourGases = new HashSet<UnitInPool>();
		enemyArmySupply = 0;
		ourArmySupply = 0;
		ourCompletedUpgrades = new HashSet<Upgrade>();
	}
	
	@Override
	public void onStep() {
		if (bot.observation().getGameLoop() % 22 == 0) {
			List<UnitInPool> enemyUnits = bot.observation().getUnits(Alliance.ENEMY);
			enemyUnits.addAll(enemyStructures);
			List<UnitInPool> threateningUnits = filterThreateningUnits(enemyUnits);
			threatHandler.updateThreats(threateningUnits);
		}
	}
	
	@Override
	public void onUnitCreated(UnitInPool unitInPool) {
		addToOurGroup(unitInPool);
	}

	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		if (unitInPool.unit().getAlliance() == Alliance.ENEMY) {
			removeFromEnemyGroup(unitInPool);
		} else {
			removeFromOurGroup(unitInPool);
		}
	}
	
	public void onUnitEnterVision(UnitInPool unitInPool) {
		if (unitInPool.unit().getAlliance() == Alliance.ENEMY) {
			addToEnemyGroup(unitInPool);
		}
	}
	
	public void onUpgradeCompleted(Upgrade upgrade) {
		ourCompletedUpgrades.add(upgrade);
	}
	
	public GameState getCurrentGameState() {
		return makeGameState();
	}
	
	private void addToEnemyGroup(UnitInPool enemy) {
		UnitType enemyType = enemy.unit().getType();
		if (UnitData.isFightingUnit(enemyType)) {
			if(enemyArmy.add(enemy)) {
				addToArmySupply(enemy.unit().getType(), Alliance.ENEMY);
			}
		} else if (UnitData.isWorker(enemyType)) {
			enemyWorkers.add(enemy);
		} else if (UnitData.isStructure(enemyType)) {
			enemyStructures.add(enemy);
			if (UnitData.isTownHall(enemyType)) {
				enemyBases.add(enemy);
			} else if (UnitData.isGasGeyser(enemyType)) {
				enemyGases.add(enemy);
			}
		}
	}
	
	private void removeFromEnemyGroup(UnitInPool enemy) {
		UnitType enemyType = enemy.unit().getType();
		if (UnitData.isFightingUnit(enemyType)) {
			if(enemyArmy.remove(enemy)) {
				removeFromArmySupply(enemy.unit().getType(), Alliance.ENEMY);
			}
		} else if (UnitData.isWorker(enemyType)) {
			enemyWorkers.remove(enemy);
		} else if (UnitData.isStructure(enemyType)) {
			enemyStructures.remove(enemy);
			if (UnitData.isTownHall(enemyType)) {
				enemyBases.remove(enemy);
			} else if (UnitData.isGasStructure(enemyType)) {
				enemyGases.remove(enemy);
			}
		}
	}
	
	private void addToOurGroup(UnitInPool ourUnit) {
		UnitType unitType = ourUnit.unit().getType();
		if (UnitData.isFightingUnit(unitType) && unitType != Units.ZERG_QUEEN) {
			if(ourArmy.add(ourUnit)) {
				addToArmySupply(ourUnit.unit().getType(), Alliance.SELF);
			}
		} else if (UnitData.isWorker(unitType)) {
			ourWorkers.add(ourUnit);
		} else if (UnitData.isStructure(unitType)) {
			ourStructures.add(ourUnit);
			if (UnitData.isTownHall(unitType)) {
				ourBases.add(ourUnit);
			} else if (UnitData.isGasStructure(unitType)) {
				ourGases.add(ourUnit);
			}
		}
	}
	
	private void removeFromOurGroup(UnitInPool ourUnit) {
		UnitType unitType = ourUnit.unit().getType();
		if (UnitData.isFightingUnit(unitType) && unitType != Units.ZERG_QUEEN) {
			if(ourArmy.remove(ourUnit)) {
				removeFromArmySupply(ourUnit.unit().getType(), Alliance.SELF);
			}
		} else if (UnitData.isWorker(unitType)) {
			ourWorkers.remove(ourUnit);
		} else if (UnitData.isStructure(unitType)) {
			ourStructures.remove(ourUnit);
			if (UnitData.isTownHall(unitType)) {
				ourBases.remove(ourUnit);
			} else if (UnitData.isGasGeyser(unitType)) {
				ourGases.remove(ourUnit);
			}
		}
	}
	
	private void addToArmySupply(UnitType unitType, Alliance alliance) {
		if (alliance == Alliance.SELF) {
			ourArmySupply += getUnitTypeSupply(unitType);
		} else {
			enemyArmySupply += getUnitTypeSupply(unitType);
		}	
	}
	
	private void removeFromArmySupply(UnitType unitType, Alliance alliance) {
		if (alliance == Alliance.SELF) {
			ourArmySupply -= getUnitTypeSupply(unitType);
		} else {
			enemyArmySupply -= getUnitTypeSupply(unitType);
		}	
	}
		
	private GameState makeGameState() {
		GameState gameState = new GameState(
				getGamePhase(),
				bot.observation().getGameLoop(),
				threatHandler.getThreats(),
				getPlayerStates()
				);
		return gameState;
	}
	
	private float getUnitTypeSupply(UnitType enemyType) {
		UnitTypeData enemyTypeData = bot.observation().getUnitTypeData(false).get(enemyType);
		return enemyTypeData.getFoodRequired().orElse((float) 0);
	}
		
	private GamePhase getGamePhase() {
		if (bot.observation().getGameLoop() < EARLY_GAME && ourWorkers.size() < 40) {
			return GamePhase.EARLY;
		} else if (completedStructureExists(Units.ZERG_HIVE) && ourBases.size() > 3) {
			return GamePhase.LATE;
		} else {
			return GamePhase.MID;
		}
	}
	
	private List<PlayerState> getPlayerStates() {
		List<PlayerState> playerStates = new ArrayList<PlayerState>();
		for (PlayerInfo player : bot.observation().getGameInfo().getPlayersInfo()) {
			PlayerState state = makePlayerState(player);
			playerStates.add(state);
		}
		return playerStates;
	}
	
	private PlayerState makePlayerState(PlayerInfo playerInfo) {
		int player = playerInfo.getPlayerId();
		Alliance alliance = getAllianceFor(player);
		float armySupply = getArmySupplyFor(player);
		int workerCount = getWorkerCountFor(player);
		Set<UnitInPool> structures = getStructuresFor(player);
		Set<Upgrade> upgrades = getCompletedUpgradesFor(player);
		return new PlayerState(playerInfo, alliance, armySupply, workerCount, structures, upgrades);	
	}
	
	private Alliance getAllianceFor(int playerID) {	
		if (playerID == bot.observation().getPlayerId()) {
			return Alliance.SELF;
		} else {
			return Alliance.ENEMY;
		}
	}
	
	private float getArmySupplyFor(int playerID) {
		if (playerID == bot.observation().getPlayerId()) {
			return ourArmySupply;
		} else {
			return enemyArmySupply;
		}
	}
	
	private int getWorkerCountFor(int playerID) {
		if (playerID == bot.observation().getPlayerId()) {
			return ourWorkers.size();
		} else {
			return enemyWorkers.size();
		}
	}
	
	private Set<UnitInPool> getStructuresFor(int playerID) {
		if (playerID == bot.observation().getPlayerId()) {
			return ourStructures;
		} else {
			removeAnyStructuresThatHaveMoved();
			return enemyStructures;
		}
	}
	
	private Set<Upgrade> getCompletedUpgradesFor(int playerID) {
		if (playerID == bot.observation().getPlayerId()) {
			return ourCompletedUpgrades;
		} else {
			removeAnyStructuresThatHaveMoved();
			return new HashSet<Upgrade>();
		}
	}
	
	private void removeAnyStructuresThatHaveMoved() {
		for (Iterator<UnitInPool> structureIterator = enemyStructures.iterator(); structureIterator.hasNext();) {
			UnitInPool structure = structureIterator.next();
			Point2d structureLocation = structure.unit().getPosition().toPoint2d();
			if (bot.observation().getVisibility(structureLocation) == Visibility.VISIBLE &&
					structure.getLastSeenGameLoop() != bot.observation().getGameLoop()) {
				structureIterator.remove();
			}
		}
	}
	
	private boolean completedStructureExists(UnitType structureType) {
		for (UnitInPool structure : ourStructures) {
			if (structure.unit().getType() == structureType && 
					structure.unit().getBuildProgress() == 1.0) {
				return true;
			}
		}
		return false;
	}
	
	private List<UnitInPool> filterThreateningUnits(Collection<UnitInPool> enemyUnits) {
		List<UnitInPool> filteredUnits = new ArrayList<UnitInPool>();
		for(UnitInPool enemy : enemyUnits) {
			Point enemyPosition = enemy.unit().getPosition();
			boolean closeToOurStructure = false;
			for (UnitInPool structure : ourStructures) {
				Point structurePosition = structure.unit().getPosition();
				if (structurePosition.distance(enemyPosition) < threatHandler.getThreatRadius(getGamePhase())) {
					closeToOurStructure = true;
				}
			}
			if (closeToOurStructure && !UnitData.isChangeling(enemy.unit().getType())) {
				filteredUnits.add(enemy);
			}
		}
		return filteredUnits;
	}
}
