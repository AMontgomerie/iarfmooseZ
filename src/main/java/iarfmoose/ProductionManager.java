package iarfmoose;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.UnitTypeData;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.data.UpgradeData;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class ProductionManager extends ResponsiveManager {
	
	private ProductionQueue productionQueue;
	private StructureManager structureManager;
	private LarvaeManager larvaeManager;
	private WorkerManager workerManager;
	private BuildingPlacer buildingPlacer;
	private StrategyPlanner strategyPlanner;
	private UpgradeHandler upgradeHandler;
	
	private ProductionItem nextItem;
	private boolean underThreat;
	private UnitInPool lastMorphedUnit;
	
	public ProductionManager(S2Agent bot, BaseLocator baseLocator) {
		super(bot, baseLocator);
		initialiseManagers();
		initialiseProduction();
		underThreat = false;
		lastMorphedUnit = null;
	}
				
	@Override
	public void onStep(GameState currentGameState) {
		underThreat = threatExists(currentGameState.getThreats());
		if (beingProxied(currentGameState)) {
			cancelExpansion();
		}
		if (productionQueue.isEmpty()) {
			BuildOrder buildOrder = strategyPlanner.getNewBuildOrder();
			productionQueue.update(buildOrder);
		}
		nextItem = productionQueue.getNextItem();
		if (nextItem.getAbility() == Abilities.BUILD_HATCHERY) {
			if (underThreat) {
				productionQueue.removeItem();
			} else {
				Point nextExpansionLocation = buildingPlacer.getNextExpansionLocation();
				workerManager.moveBuilderTo(nextExpansionLocation);
			}
		}
		if (larvaeManager.checkForEggMorph(nextItem)) {
			productionQueue.removeItem();
		}
		if (needMoreSupply()) {
			addOverlord();
		} else if (haveRequiredTechForNextItem()) {
			startNextItem();
		}
		if (strategyPlanner.newBuildOrderRequired()) {
			productionQueue.clear();
		}
		buildingPlacer.update();
		workerManager.onStep(currentGameState);
		strategyPlanner.onStep(currentGameState);
		PlayerState self = currentGameState.findPlayerState(Alliance.SELF).orElse(new PlayerState());
		upgradeHandler.updateCompletedUpgrades(self.getCompletedUpgrades());
		if (bot.observation().getGameLoop() % 22 == 0) {
			addNewUpgrades();
		}
	}
	
	@Override
	public void onUnitCreated(UnitInPool unitInPool) {
		updateManagersNewUnit(unitInPool);
		updateProductionQueue(unitInPool);
	}
	
	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		updateManagersUnitDestroyed(unitInPool);
	}
	
	public void onUnitIdle(UnitInPool unitInPool) {
		UnitType unitType = unitInPool.unit().getType();
		if (UnitData.isWorker(unitType)) {
			workerManager.onUnitIdle(unitInPool);
		}
	}

	private void initialiseManagers() {
		buildingPlacer = new BuildingPlacer(bot, baseLocator);
		workerManager = new WorkerManager(bot, baseLocator);
		structureManager = new StructureManager(bot);
		larvaeManager = new LarvaeManager(bot);
		productionQueue = new ProductionQueue(bot);
		strategyPlanner = new StrategyPlanner(bot);
		upgradeHandler = new UpgradeHandler(bot);
	}
	
	private void initialiseProduction() {
		BuildOrder openingBuildOrder = strategyPlanner.getOpeningBuildOrder();
		productionQueue.update(openingBuildOrder);
		nextItem = productionQueue.getNextItem();
	}
	
	private void updateManagersNewUnit(UnitInPool unitInPool) {
		larvaeManager.onUnitCreated(unitInPool);
		workerManager.onUnitCreated(unitInPool);
		structureManager.onUnitCreated(unitInPool);
		if (unitInPool.unit().getType() == Units.ZERG_HATCHERY && 
				bot.observation().getGameLoop() > 1) {
			buildingPlacer.expansionStarted();
		}
	}
					
	private void updateManagersUnitDestroyed(UnitInPool unitInPool) {
		larvaeManager.onUnitDestroyed(unitInPool);
		structureManager.onUnitDestroyed(unitInPool);
		workerManager.onUnitDestroyed(unitInPool);	
		if (unitInPool.unit().getType() == Units.ZERG_HATCHERY) {
			buildingPlacer.baseDestroyed(unitInPool.unit().getPosition());
		}
	}
	
	private boolean threatExists(List<Threat> threats) {
		for (Threat threat : threats) {
			if (threat.getTotalSupply() >= 2) {
				return true;
			}
		}
		return false;
	}
	
	private boolean beingProxied(GameState currentGameState) {
		return currentGameState.getCurrentGamePhase() == GamePhase.EARLY &&
				threatContainsStructure(currentGameState.getThreats());
	}
	
	private boolean threatContainsStructure(List<Threat> threats) {
		for (Threat threat : threats) {
			if (threat.containsStructure()) {
				return true;
			}
		}
		return false;
	}
	
	private void updateProductionQueue(UnitInPool unitInPool) {
		//skip the first frame so that starting units aren't mistakenly counted
		if (bot.observation().getGameLoop() > 1) {
			if (AbilityData.getProductionType(nextItem) == ProductionType.BUILD &&
					newUnitIsNextItem(unitInPool.unit())) {
				productionQueue.removeItem();
			}
		}
	}
		
	private boolean newUnitIsNextItem(Unit unit) {
		if (UnitData.getAbilityToMakeUnitType(unit.getType()) == nextItem.getAbility()) {
			return true;
		} else {
			return false;
		}
	}
	
    private boolean needMoreSupply() {
     	if (supplyBlocked() && !overlordInProduction() && nextItem.getAbility() != Abilities.TRAIN_OVERLORD) {
     		return true;
     	} else {
     		return false;
     	}
     }
    
    private boolean supplyBlocked() {
    	if (bot.observation().getFoodCap() >= 200) {
    		return false;
    	}
    	UnitType unitType = AbilityData.getCreatedUnitType(nextItem.getAbility());
    	UnitTypeData data = loadUnitTypeDataFor(unitType);
    	float nextUnitSupply = data.getFoodRequired().orElse((float) 1);
    	if (bot.observation().getFoodCap() - bot.observation().getFoodUsed() <= nextUnitSupply) {
    		return true;
    	} else {
    		return false;
    	}
    }
	
    private void addOverlord() {
    	for (int i = 0; i < structureManager.getBases().size(); i++) {
    		productionQueue.addItemHighPriority(new ProductionItem(Abilities.TRAIN_OVERLORD));
    	}
    }
    
    private boolean haveRequiredTechForNextItem() {	
    	UnitType requiredTech = findNextItemRequiredUnitType();
		if (requiredTech == Units.INVALID) {
			return true; //nothing was required
		}
		Optional<UnitInPool> techStructure = structureManager.findStructure(requiredTech);
		if (techStructure.isPresent()) {
			return true; //we have the required structure
		} else {
			addRequiredTechToQueue(requiredTech);
			return false; //we don't have the required structure
		}
    }
    
    private UnitType findNextItemRequiredUnitType() {
    	switch (AbilityData.getProductionType(nextItem)) {
    	case TRAIN:
    	case TRAIN_FROM_LARVA:
    	case BUILD: 
			UnitType unitType = AbilityData.getCreatedUnitType(nextItem.getAbility());
			return getRequiredTechFor(unitType);
    	case RESEARCH:
    		return AbilityData.getResearchStructure(nextItem.getAbility());
    	case MORPH:
    		return AbilityData.getRequiredTechForMorph(nextItem.getAbility());
    	default:
    		return Units.INVALID;
    	}
    }

	private UnitType getRequiredTechFor(UnitType unitType) {
		UnitTypeData unitTypeData = loadUnitTypeDataFor(unitType);
		return unitTypeData.getTechRequirement().orElse(Units.INVALID);
	}
	
	private void addRequiredTechToQueue(UnitType requiredTech) {
		Abilities ability = UnitData.getAbilityToMakeUnitType(requiredTech);
		if (structureManager.findStructure(requiredTech).isEmpty()) {
			productionQueue.addItemHighPriority(new ProductionItem(ability));
		}
	}
    
	private boolean canAffordProductionItem(ProductionItem item) {
		int mineralCost = 0;
		int vespeneCost = 0;
		UnitType unitType = AbilityData.getCreatedUnitType(item.getAbility());
		mineralCost = getMineralCostForUnit(unitType);
		vespeneCost = getVespeneCostForUnit(unitType);
		//zerglings come in pairs so the cost is x2
		if (unitType == Units.ZERG_ZERGLING) {
			mineralCost *= 2;
		//UnitTypeData includes the cost of drone (which we already paid for) so -50
		//morphed buildings also include the cost of previous morph stages
		} else if (AbilityData.getProductionType(nextItem) == ProductionType.BUILD) {
			mineralCost -= 50;
		} else 	if (unitType == Units.ZERG_LAIR) {
			mineralCost -= 350;
		} else if (unitType == Units.ZERG_HIVE) {
			mineralCost -= 500;
			vespeneCost -= 100;
		} else if (unitType == Units.ZERG_GREATER_SPIRE) {
			mineralCost -= 250;
			vespeneCost -= 200;
		}
		return canAfford(mineralCost, vespeneCost);	
	}
	
	private int getMineralCostForUnit(UnitType unitType) {
		UnitTypeData unitTypeData = loadUnitTypeDataFor(unitType);
		return unitTypeData.getMineralCost().orElse(0);
	}
	
	private int getVespeneCostForUnit(UnitType unitType) {
		UnitTypeData unitTypeData = loadUnitTypeDataFor(unitType);
		return unitTypeData.getVespeneCost().orElse(0);
	}
	
	private int getMineralCostForUpgrade(Upgrades upgrade) {
		UpgradeData upgradeData = loadUpgradeDataFor(upgrade);
		return upgradeData.getMineralCost().orElse(0);
	}
	
	private int getVespeneCostForUpgrade(Upgrades upgrade) {
		UpgradeData upgradeData = loadUpgradeDataFor(upgrade);
		return upgradeData.getVespeneCost().orElse(0);
	}
	
	private UnitTypeData loadUnitTypeDataFor(UnitType unitType) {
		return bot.observation().getUnitTypeData(false).get(unitType);
	}
	
	private UpgradeData loadUpgradeDataFor(Upgrades upgrade) {
		return bot.observation().getUpgradeData(false).get(upgrade);
	}
	
	private boolean canAfford(int mineralCost, int vespeneCost) {
		if (bot.observation().getMinerals() >= mineralCost && 
				bot.observation().getVespene() >= vespeneCost) {
			return true;
		} else {
			return false;
		}
	}
	
	private void startNextItem() {
		switch(AbilityData.getProductionType(nextItem)) {
		case TRAIN:
			if(!productionStarted() && canAffordProductionItem(nextItem)) {
				trainNextUnit();
			}
			break;
		case TRAIN_FROM_LARVA:
			if (canAffordProductionItem(nextItem)) {
				trainNextUnitFromLarva();
			}
			break;
		case BUILD:
			if (canAffordProductionItem(nextItem)) {
				buildNextStructure();
			}
			break;
		case RESEARCH:
			if (underThreat) {
				productionQueue.removeItem();
				return;
			}
			if(!productionStarted() && canAffordProductionItem(nextItem)) {
				researchNextTech();
			}
			break;
		case MORPH:
			if(!morphStarted() && canAffordProductionItem(nextItem)) {
				morphNextUnit();
			}
			break;
		default:
			break;
		}
	}
	
	private boolean productionStarted() {
		UnitInPool targetProductionFacility = structureManager.getTargetProductionFacility();
		if (targetProductionFacility != null) {
			if (unitHasOrder(targetProductionFacility, nextItem.getAbility())) {
				productionQueue.removeItem();
				structureManager.clearTargetProductionFacility();
				return true;
			}
		}
		return false;
	}
	
	private boolean morphStarted() {
		UnitType requiredUnitType = AbilityData.morphsFrom(nextItem.getAbility());
		if (UnitData.isStructure(requiredUnitType)) {
			for (UnitInPool structure : structureManager.getAllStructuresOfType(requiredUnitType)) {
				if (unitHasOrder(structure, nextItem.getAbility())) {
					productionQueue.removeItem();
					return true;
				}
			}
		} else {
			UnitType targetUnitType = AbilityData.getCreatedUnitType(nextItem.getAbility());
			UnitType cocoonType = UnitData.getMorphingUnitCocoonType(targetUnitType);
			if (lastMorphedUnit != null && 
					lastMorphedUnit.unit().getType() == cocoonType) {
				productionQueue.removeItem();
				lastMorphedUnit = null;
			}
		}
		return false;
	}
	
	private boolean unitHasOrder(UnitInPool unitInPool, Ability ability) {
		for (UnitOrder order : unitInPool.unit().getOrders()) {
			if (order.getAbility() == nextItem.getAbility() ||
					AbilityData.areEquivalent(order.getAbility(), nextItem.getAbility())) {
				return true;
			}
		}
		return false;
	}
	
	private void trainNextUnit() {
		if (nextItem.getAbility() == Abilities.TRAIN_QUEEN) {
			Optional<UnitInPool> hatchery = structureManager.findIdleStructure(Units.ZERG_HATCHERY);
			if (hatchery.isPresent()) {
				bot.actions().unitCommand(hatchery.get().unit(), Abilities.TRAIN_QUEEN, false);
				structureManager.setTargetProductionFacility(hatchery.get());
			}
		}
	}
	
	private void trainNextUnitFromLarva() {
		UnitInPool larva = larvaeManager.findIdleLarva();
		if (larva != null) {
			bot.actions().unitCommand(larva.unit(), nextItem.getAbility(), false);
		}
	}
	
	private void buildNextStructure() {
		if (bot.observation().getGameLoop() % 22 == 0) {
			Ability structureType = nextItem.getAbility();
			Point target = buildingPlacer.getCurrentTargetHatcheryLocation();
			UnitInPool builder = workerManager.findBuilderCloseTo(target);
			if (builder != null) {
				if (structureType == Abilities.BUILD_HATCHERY) {
					buildingPlacer.expand(builder);
				} else if (structureType == Abilities.BUILD_EXTRACTOR) {
					buildingPlacer.buildGas(builder);
				} else if (structureType == Abilities.BUILD_SPINE_CRAWLER) {
					buildingPlacer.buildSpineCrawler(builder);
				} else {
					buildingPlacer.buildStructure(builder, structureType);
				}
			}
		}
	}
	
	private void researchNextTech() {
		Upgrades upgrade = AbilityData.getResearchedUpgradeType(nextItem.getAbility());
		int mineralCost = getMineralCostForUpgrade(upgrade);
		int vespeneCost = getVespeneCostForUpgrade(upgrade);
			if (canAfford(mineralCost, vespeneCost)) {
			UnitType requiredStructure = AbilityData.getResearchStructure(nextItem.getAbility());
			Optional<UnitInPool> techStructure = structureManager.findIdleStructure(requiredStructure);
			if (techStructure.isPresent()) {
				bot.actions().unitCommand(techStructure.get().unit(), nextItem.getAbility(), false);
				structureManager.setTargetProductionFacility(techStructure.get());
			}
		}
	}
	
	private void morphNextUnit() {
		UnitType requiredUnitType = AbilityData.morphsFrom(nextItem.getAbility());
		if (UnitData.isStructure(requiredUnitType)) {
			Optional<UnitInPool> structureToMorph = structureManager.findIdleStructure(requiredUnitType);
			if (structureToMorph.isPresent()) {
				bot.actions().unitCommand(structureToMorph.get().unit(), nextItem.getAbility(), false);
			}
		} else {
			List<UnitInPool> units = bot.observation().getUnits(
					Alliance.SELF, 
					unitInPool -> unitInPool.unit().getType() == requiredUnitType);
			if (units.size() > 0) {
				UnitInPool target = units.get(0);
				bot.actions().unitCommand(target.unit(), nextItem.getAbility(), false);
				lastMorphedUnit = target;
			} else {
				Abilities trainRequiredUnit = UnitData.getAbilityToMakeUnitType(requiredUnitType);
				productionQueue.addItemHighPriority(new ProductionItem(trainRequiredUnit));
			}
		}
	}
	
	private void addNewUpgrades() {
		Set<UnitInPool> idleStructures = new HashSet<UnitInPool>(structureManager.getIdleStructures());
		if (workerManager.getWorkerCount() >= 16) { //to prevent new techs from killing production in low eco situations
			for (UnitInPool structure : idleStructures) {
				UnitType unitType = structure.unit().getType();
				tryToAddUpgradeFor(unitType);
			}
		}
	}
	
	private void tryToAddUpgradeFor(UnitType unitType) {
		Abilities nextUpgrade = upgradeHandler.addNewUpgradeFor(unitType).orElse(Abilities.INVALID);
		if (nextUpgrade != Abilities.INVALID && !productionQueueAlreadyContainsUpgradeFor(unitType)) {
			ProductionItem researchUpgrade = new ProductionItem(nextUpgrade);
			productionQueue.addItemHighPriority(researchUpgrade);
		}
	}
	
	private boolean productionQueueAlreadyContainsUpgradeFor(UnitType unitType) {
		List<Upgrade> upgrades = iarfmoose.UpgradeData.getUpgradesForStructureType(unitType);
		for(Upgrade upgrade : upgrades) {
			Abilities upgradeAbility = AbilityData.getAbilityToResearchUpgrade(upgrade);
			if (productionQueue.contains(upgradeAbility)) {
				return true;
			}
		}
		return false;
	}
	
	private void cancelExpansion() {
		structureManager.cancelExpansion();
	}
	                            
    private boolean overlordInProduction() {
     	List<UnitInPool> eggs = getUnitsOfType(Units.ZERG_EGG);       	
     	for (UnitInPool egg : eggs) {
     		List<UnitOrder> orders = egg.unit().getOrders();
     		for (UnitOrder order : orders) {
     			if (order.getAbility() == Abilities.TRAIN_OVERLORD) {
     				return true;
     			}
     		}
     	}
     	return false;
     }

 	private List<UnitInPool> getUnitsOfType(Units unitType) {
     	return bot.observation().getUnits(
     			Alliance.SELF, 
     			unit -> unit.unit().getType() == unitType);
    }
}
