package iarfmoose;

import java.util.ArrayList;
import java.util.List;
import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.UnitTypeData;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class BuildOrderGenerator {
	
	S2Agent bot;
	BuildOrder nextBuildOrder;
	float supplyGap;
	
	public BuildOrderGenerator(S2Agent bot) {
		this.bot = bot;
		nextBuildOrder = new BuildOrder();
		supplyGap = 2;
	}
		
	public BuildOrder generate(ProductionEmphasis currentEmphasis, ProductionGoalState goalState, GameState currentState) {
		nextBuildOrder = new BuildOrder();
		PlayerState self = currentState.findPlayerState(Alliance.SELF).orElse(new PlayerState());
		supplyGap = bot.observation().getFoodCap() - bot.observation().getFoodUsed();
		if (self.playerInfo != null) {
			int requiredWorkers = goalState.getWorkerCount().orElse(0) - self.getWorkerCount().orElse(0);
			int requiredBases = goalState.getBaseCount().orElse(0) - getMiningBaseCount(self);
			int requiredGases = goalState.getGasCount().orElse(0) - self.getGasCount().orElse(0);
			addBases(requiredBases, currentEmphasis);
			addGases(requiredGases);
			addRequiredStructures(goalState.getStructureTypes(), currentState);
			addRequiredUpgrades(goalState.getUpgrades(), currentState);
			if (currentEmphasis == ProductionEmphasis.ECONOMY) {
				addWorkers(requiredWorkers);
				addUnits(goalState.getUnitComposition(), self.getBaseCount().orElse(1));
			} else {
				addUnits(goalState.getUnitComposition(), self.getBaseCount().orElse(1));
				addWorkers(requiredWorkers);
			}
			addQueens(currentEmphasis);
		}
		nextBuildOrder = prioritiseBuildOrder(nextBuildOrder);
		return nextBuildOrder;
	}
	
	private int getMiningBaseCount(PlayerState self) {
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
				
	private void addWorkers(int workersToTrain) {
		if (workersToTrain > 10) {
			workersToTrain = 10;
		}
		for (int i = 0; i < workersToTrain; i++) {
			if (bot.observation().getFoodCap() < 200 && i != 0 && i % 7 == 0) {
				if (supplyGap > 0) {
					supplyGap--;
				} else {
					nextBuildOrder.addItem(new ProductionItem(Abilities.TRAIN_OVERLORD));
				}
			}
			nextBuildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		}
	}
	
	private void addUnits(UnitComposition composition, int baseCount) {
		float supplyAdded = 0;
		for (UnitCompositionComponent component : composition.getComposition()) {
			float supplyRequired = loadUnitTypeDataFor(component.getUnitType()).getFoodRequired().orElse((float) 0);
			for (int i = 0; i < (component.getPercentage() * baseCount / 10); i++) {
				if (bot.observation().getFoodCap() < 200 && supplyAdded % 8 == 0) {
					if (supplyGap > supplyRequired) {
						supplyGap -= supplyRequired;
					} else {
						nextBuildOrder.addItem(new ProductionItem(Abilities.TRAIN_OVERLORD));
					}
				}
				nextBuildOrder.addItem(new ProductionItem(UnitData.getAbilityToMakeUnitType(component.getUnitType())));
				supplyAdded += supplyRequired;
			}
		}		
	}
	
	private void addBases(int basesToAdd, ProductionEmphasis currentEmphasis) {
		if (basesToAdd > 0) {
			if (currentEmphasis == ProductionEmphasis.ECONOMY) {
				nextBuildOrder.addItemToFront(new ProductionItem(Abilities.BUILD_HATCHERY));	
			} else {
				nextBuildOrder.addItem(new ProductionItem(Abilities.BUILD_HATCHERY));
			}
		}
	}
	
	private void addGases(int gasesToAdd) {
		while (gasesToAdd > 0) {
			nextBuildOrder.addItem(new ProductionItem(Abilities.BUILD_EXTRACTOR));
			gasesToAdd--;
		}
	}
	
	private void addRequiredStructures(List<UnitType> requiredStructures, GameState currentState) {
		for (UnitType requiredStructure : requiredStructures) {
			if (requiredStructure == Units.ZERG_SPINE_CRAWLER || requiredStructure == Units.ZERG_SPORE_CRAWLER) {
				nextBuildOrder.addItemToFront(new ProductionItem(UnitData.getAbilityToMakeUnitType(requiredStructure)));
			} else if (!currentState.structureExists(requiredStructure, Alliance.SELF) && requiredStructure != Units.INVALID) {
				nextBuildOrder.addItem(new ProductionItem(UnitData.getAbilityToMakeUnitType(requiredStructure)));
			}
		}
	}
	
	private void addRequiredUpgrades(List<Upgrade> requiredUpgrades, GameState currentState) {
		for (Upgrade requiredUpgrade : requiredUpgrades) {
			if (!currentState.upgradeCompleted(requiredUpgrade, Alliance.SELF) && 
					!currentState.upgradeStarted(requiredUpgrade, Alliance.SELF) &&
					requiredUpgrade != Upgrades.INVALID) {
				nextBuildOrder.addItem(new ProductionItem(AbilityData.getAbilityToResearchUpgrade(requiredUpgrade)));
				return;
			}
		}
	}
	
	private void addQueens(ProductionEmphasis emphasis) {
		int queenCount = bot.observation().getUnits(
				Alliance.SELF, 
				unitInPool -> 
				unitInPool.unit().getType() == Units.ZERG_QUEEN).size();
		int baseCount = bot.observation().getUnits(
				Alliance.SELF,
				unitInPool ->
				UnitData.isTownHall(unitInPool.unit().getType())).size();
		float supplyAdded = 0;
		while (queenCount < baseCount) {
			//add overlords for queen supply
			if (bot.observation().getFoodCap() < 200 && supplyAdded % 8 == 0) {
				if (supplyGap > 2) {
					supplyGap -= 2;
				} else {
					nextBuildOrder.addItem(new ProductionItem(Abilities.TRAIN_OVERLORD));
				}
			}
			//add extra queens
			if (queenCount < 2 || emphasis == ProductionEmphasis.ECONOMY) {
				nextBuildOrder.addItemToFront(new ProductionItem(Abilities.TRAIN_QUEEN));
			} else {
				nextBuildOrder.addItem(new ProductionItem(Abilities.TRAIN_QUEEN));
			}
			queenCount++;
		}
	}
	
	private BuildOrder prioritiseBuildOrder(BuildOrder buildOrder) {
		BuildOrder prioritisedBuildOrder = new BuildOrder();
		List<ProductionItem> otherItems = new ArrayList<ProductionItem>();
		int availableLarvae = bot.observation().getUnits(
				Alliance.SELF, 
				unitInPool -> 
				UnitData.isTownHall(unitInPool.unit().getType())).size() * 3;
		int availableHatcheries = bot.observation().getUnits(
				Alliance.SELF, 
				unitInPool -> 
				UnitData.isTownHall(unitInPool.unit().getType())
				).size();
		for (ProductionItem item : buildOrder.getBuildOrder()) {
			UnitType unitType = AbilityData.getCreatedUnitType(item.getAbility());
			if ((UnitData.isFightingUnit(unitType) || UnitData.isWorker(unitType)) && availableLarvae > 0) {
				prioritisedBuildOrder.addItem(item);
				availableLarvae--;
			} else if (unitType == Units.ZERG_QUEEN && availableHatcheries > 0) {
				prioritisedBuildOrder.addItem(item);
				availableHatcheries--;
			} else {
				otherItems.add(item);
			}
		}
		for (ProductionItem item : otherItems) {
			UnitType unitType = AbilityData.getCreatedUnitType(item.getAbility());
			if (UnitData.isGasStructure(unitType)) {
				prioritisedBuildOrder.addItemToFront(item);
			} else {
				prioritisedBuildOrder.addItem(item);
			}
		}
		return prioritisedBuildOrder;
	}
		
	public BuildOrder getZvROpening() {
		BuildOrder buildOrder = new BuildOrder();
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.BUILD_SPAWNING_POOL));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.BUILD_HATCHERY));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.BUILD_EXTRACTOR));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_OVERLORD));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_QUEEN));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_ZERGLING));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_ZERGLING));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_ZERGLING));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_OVERLORD));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.RESEARCH_ZERGLING_METABOLIC_BOOST));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_QUEEN));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_OVERLORD));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_QUEEN));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_OVERLORD));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_OVERLORD));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.RESEARCH_PNEUMATIZED_CARAPACE));
		buildOrder.addItem(new ProductionItem(Abilities.BUILD_ROACH_WARREN));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.BUILD_EVOLUTION_CHAMBER));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.BUILD_EVOLUTION_CHAMBER));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_DRONE));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_QUEEN));
		buildOrder.addItem(new ProductionItem(Abilities.TRAIN_QUEEN));


		return buildOrder;
	}
	
	private UnitTypeData loadUnitTypeDataFor(UnitType unitType) {
		return bot.observation().getUnitTypeData(false).get(unitType);
	}
}
