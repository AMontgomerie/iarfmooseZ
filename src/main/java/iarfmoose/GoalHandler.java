package iarfmoose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class GoalHandler {
	
	private static int EARLY_GAME_WORKER_LIMIT = 44;
	private static int MID_GAME_WORKER_LIMIT = 70;
	private static int LATE_GAME_WORKER_LIMIT = 80;
	
	private PlayerState self;
	private PlayerState enemy;
	private GamePhase currentPhase;
	private List<Upgrade> upgrades;
	private List<UnitType> earlyGameStructures;
	private List<UnitType> midGameStructures;
	private List<UnitType> lateGameStructures;
	private boolean underThreat;
	 
	public GoalHandler() {
		self = new PlayerState();
		enemy = new PlayerState();
		currentPhase = GamePhase.EARLY;
		underThreat = false;
		initialiseUpgrades();
		initialiseStructures();
	}
	
	public void update(GameState currentGameState, ThreatState threatState) {
		self = currentGameState.findPlayerState(Alliance.SELF).orElse(new PlayerState());
		enemy = currentGameState.findPlayerState(Alliance.ENEMY).orElse(new PlayerState());
		currentPhase = currentGameState.getCurrentGamePhase();
		underThreat = threatsExist(currentGameState, threatState);
		removeCompletedUpgrades();
		
	}
	
	public ProductionGoalState getNextGoalState(GameState currentGameState, ThreatState threatState) {
		return new ProductionGoalState(
				getWorkerLimit(),
				getBaseCount(),
				getGasCount(),
				getStructures(currentGameState, threatState),
				new ArrayList<Upgrade>(Arrays.asList(getNextUpgrade())), 
				getUnitComposition());
	}
		
	private void initialiseUpgrades() {
		upgrades = new ArrayList<Upgrade>();
	}
	
	private void initialiseStructures() {
		earlyGameStructures = new ArrayList<UnitType>(
				Arrays.asList(
				Units.ZERG_SPAWNING_POOL,
				Units.ZERG_ROACH_WARREN,
				Units.ZERG_EVOLUTION_CHAMBER,
				Units.ZERG_LAIR,
				Units.ZERG_BANELING_NEST));
		midGameStructures = new ArrayList<UnitType>(
				Arrays.asList(
						Units.ZERG_HYDRALISK_DEN,
						Units.ZERG_INFESTATION_PIT,
						Units.ZERG_SPIRE,
						Units.ZERG_HIVE));
		lateGameStructures = new ArrayList<UnitType>(
				Arrays.asList(
						Units.ZERG_GREATER_SPIRE,
						Units.ZERG_ULTRALISK_CAVERN
						));
	}
	
	private void removeCompletedUpgrades() {
		for(Upgrade completedUpgrade : self.getCompletedUpgrades()) {
			for (Iterator<Upgrade> upgradeIterator = upgrades.iterator(); upgradeIterator.hasNext();) {
				Upgrade goalUpgrade = upgradeIterator.next();
				if (completedUpgrade == goalUpgrade) {
					upgradeIterator.remove();
				}
			}
		}
	}
	
	private int getWorkerLimit() {
		if (underThreat) {
			return self.getWorkerCount().orElse(0);
		}
		int workerLimit = getMiningBaseCount() * 30;
		switch(currentPhase) {
		case EARLY:
			if (workerLimit > EARLY_GAME_WORKER_LIMIT) {
				return  EARLY_GAME_WORKER_LIMIT;
			} else {
				return workerLimit;
			}
		case MID:
			if (workerLimit > MID_GAME_WORKER_LIMIT) {
				return  MID_GAME_WORKER_LIMIT;
			} else {
				return workerLimit;
			}
		case LATE:
		default:
			if (workerLimit > LATE_GAME_WORKER_LIMIT) {
				return  LATE_GAME_WORKER_LIMIT;
			} else {
				return workerLimit;
			}
		}
	}
	
	private int getBaseCount() {
		int maximumBases = 2 + (self.getWorkerCount().orElse(0) / 15);
		if (underThreat) {
			return self.getBaseCount().orElse(1);	
		} else {
			switch(currentPhase) {
			case EARLY:
				if (maximumBases > 3) {
					return 3;
				} else {
					return maximumBases;
				}
			case MID:
				if (maximumBases > 5) {
					return 5;
				} else {
					return maximumBases;
				} 
			case LATE:
			default:
				return 2 + (self.getWorkerCount().orElse(0) / 15);
			}
		}		
	}
	
	private int getGasCount() {
		switch(currentPhase) {
		case EARLY:
			return Math.round(self.getWorkerCount().orElse(0) / 16);
		case MID:
			return Math.round(self.getWorkerCount().orElse(0) / 12);
		case LATE:
		default:
			return Math.round(self.getWorkerCount().orElse(0) / 11);
		}
	}
	
	private Upgrade getNextUpgrade() {
		if (upgrades.isEmpty() || underThreat) {
			return Upgrades.INVALID;
		} else {
			return upgrades.get(0);
		}
	}
	
	private List<UnitType> getStructures(GameState currentState, ThreatState threatState) {
		List<UnitType> structures = new ArrayList<UnitType>();
		if (self.getWorkerCount().orElse(16) > 15) {
			structures.add(getNextStructure());
			List<UnitType> staticDefence = addStaticDefenceInResponseTo(currentState, threatState);
			structures.addAll(staticDefence);
		}
		return structures;
	}
	
	private UnitType getNextStructure() {
		UnitType unbuiltStructure = getFirstUnbuiltStructureIn(earlyGameStructures);
		if (unbuiltStructure == Units.INVALID && haveSufficientEconomy()) {
			unbuiltStructure = getFirstUnbuiltStructureIn(midGameStructures);
		}
		if (unbuiltStructure == Units.INVALID && haveSufficientEconomy()) {
			unbuiltStructure = getFirstUnbuiltStructureIn(lateGameStructures);
		}		
		return unbuiltStructure;
	}
	
	private List<UnitType> addStaticDefenceInResponseTo(GameState currentState, ThreatState threatState) {
		List<UnitType> necessaryStructures = new ArrayList<UnitType>();
		if (self.getWorkerCount().orElse(0) >= 12) {
			necessaryStructures.addAll(respondToThreatState(threatState));
			necessaryStructures.addAll(respondToThreats(currentState.getThreats()));
			if (extraUnitsRequired() && currentPhase != GamePhase.LATE) {
				necessaryStructures.add(Units.ZERG_SPINE_CRAWLER);
			}
		}
		return necessaryStructures;
	}
	
	private List<UnitType> respondToThreatState(ThreatState threatState) {
		List<UnitType> necessaryStructures = new ArrayList<UnitType>();
		if (threatState != null) {
			ThreatResponse response = threatState.getResponse();
			if (!response.alreadyResponded()) {
				if (response.staticDefenceIsRequired()) {
					necessaryStructures.add(Units.ZERG_SPINE_CRAWLER);
				} else if (response.detectionIsRequired() || response.antiAirIsRequired()) {
					necessaryStructures.add(Units.ZERG_SPORE_CRAWLER);
				}
				response.respond();
			}
		}
		return necessaryStructures;
	}
	private List<UnitType> respondToThreats(List<Threat> threats) {
		int spineCount = self.getCountForStructureType(Units.ZERG_SPINE_CRAWLER);
		int sporeCount = self.getCountForStructureType(Units.ZERG_SPORE_CRAWLER);
		int baseCount = self.getBaseCount().orElse(0);
		List<UnitType> necessaryStructures = new ArrayList<UnitType>();
		for (Threat threat : threats) {
			if (threat.contains(Units.TERRAN_BARRACKS) ||
					threat.contains(Units.PROTOSS_GATEWAY) &&
					spineCount < baseCount) {
				necessaryStructures.add(Units.ZERG_SPINE_CRAWLER);
			} else if (threat.containsFlying() ||
					threat.containsCloak() &&
					sporeCount < baseCount) {
				necessaryStructures.add(Units.ZERG_SPORE_CRAWLER);
			}
		}
		return necessaryStructures;
	}
	
	private UnitType getFirstUnbuiltStructureIn(List<UnitType> unitTypes) {
		for (UnitType unitType : unitTypes) {
			if (self.getCountForStructureType(unitType) == 0 && !alreadyMorphingStructure(unitType)) {
				return unitType;
			}
		}
		return Units.INVALID;
	}
	
	private boolean alreadyMorphingStructure(UnitType unitType) {
		for (UnitInPool structure : self.getStructures()) {
			for (UnitOrder order : structure.unit().getOrders()) {
				if (order.getAbility() == UnitData.getAbilityToMakeUnitType(unitType)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean haveSufficientEconomy() {
		if (self.getWorkerCount().orElse(0) >= getWorkerLimit() &&
				self.getBaseCount().orElse(0) >= expectedBasesForPhase()) {
			return true;
		} else {
			return false;
		}
	}
	
	private int expectedBasesForPhase() {
		switch(currentPhase) {
		case EARLY:
			return 3;
		case MID:
			return 4;
		case LATE:
		default:
			return 5;
		}
	}
	
	private UnitComposition getUnitComposition() {
		switch(currentPhase) {
		case EARLY:
		default:
			if (underThreat || extraUnitsRequired()) {
				if (self.completedStructureExists(Units.ZERG_ROACH_WARREN)) {
					return new UnitComposition(
							Arrays.asList(
									new UnitCompositionComponent(Units.ZERG_ROACH, 40),
									new UnitCompositionComponent(Units.ZERG_ZERGLING, 60))
							);
				} else {
					return new UnitComposition(
							Arrays.asList(
									new UnitCompositionComponent(Units.ZERG_ZERGLING, 100))
							);
				}
			}
			break;
		case MID:
			if (underThreat || extraUnitsRequired() || self.getWorkerCount().orElse(0) >= getWorkerLimit()) {
				return new UnitComposition(
						Arrays.asList(
								new UnitCompositionComponent(Units.ZERG_ZERGLING, 50),
								new UnitCompositionComponent(Units.ZERG_BANELING, 20),
								new UnitCompositionComponent(Units.ZERG_ROACH, 5),
								new UnitCompositionComponent(Units.ZERG_HYDRALISK, 20),
								new UnitCompositionComponent(Units.ZERG_RAVAGER, 5))
						);
			}
			break;
		case LATE:
			return new UnitComposition(
					Arrays.asList(
							new UnitCompositionComponent(Units.ZERG_ZERGLING, 20),
							new UnitCompositionComponent(Units.ZERG_BANELING, 20),
							new UnitCompositionComponent(Units.ZERG_HYDRALISK, 40),
							new UnitCompositionComponent(Units.ZERG_CORRUPTOR, 10),
							new UnitCompositionComponent(Units.ZERG_BROODLORD, 10))
					);
		}
		return new UnitComposition();
	}
	
	private boolean threatsExist(GameState currentGameState, ThreatState threatState) {
		if (threatState != null) {
			return true;
		} else {
			for (Threat threat : currentGameState.getThreats()) {
				if (threat.getTotalSupply() > 2 || threat.containsStructure()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean extraUnitsRequired() {
		float ourArmySize = self.getArmySupply().orElse((float) 0);
		float enemyArmySize = enemy.getArmySupply().orElse((float) 0);
		return enemyArmySize > 5 && enemyArmySize > ourArmySize * 1.5;
	}
	
	private int getMiningBaseCount() {
		int miningBaseCount = 0;
		for (UnitInPool base : self.getBases()) {
			//don't count any bases that have lost too many mineral patches
			if (base.unit().getBuildProgress() < 1.0 || 
					base.unit().getIdealHarvesters().orElse(0) >= 10) {
				miningBaseCount++;
			}
		}
		return miningBaseCount;
	}
}
