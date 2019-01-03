package iarfmoose;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.data.UpgradeData;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class UpgradeHandler {

	S2Agent bot;
	Set<Upgrade> completedUpgrades;
	
	public UpgradeHandler(S2Agent bot) {
		this.bot = bot;
		completedUpgrades = new HashSet<Upgrade>();
	}
	
	public void updateCompletedUpgrades(Set<Upgrade> completedUpgrades) {
		this.completedUpgrades = completedUpgrades;
	}
	
	public Optional<Abilities> addNewUpgradeFor(UnitType structure) {
		List<Upgrade> upgrades = iarfmoose.UpgradeData.getUpgradesForStructureType(structure);
		for (Upgrade upgrade : upgrades) {
			if (haveTechFor(upgrade) && 
					!alreadyComplete(upgrade) && 
					!currentlyInProduction(upgrade) &&
					gasIncomeIsSufficientForUpgrade(upgrade)) {
				Abilities upgradeAbility = AbilityData.getAbilityToResearchUpgrade(upgrade);
				return Optional.of(upgradeAbility);
			}
		}
		return Optional.empty();
	}
	
	private boolean haveTechFor(Upgrade upgrade) {
		UnitType currentTechLevel = getCurrentTechLevel();
		UnitType requiredLevel = iarfmoose.UpgradeData.getTechLevelRequiredFor(upgrade);
		if (currentTechLevel == Units.ZERG_HATCHERY) {
			if (requiredLevel == Units.ZERG_HATCHERY) {
				return true;
			}
		} else if (currentTechLevel == Units.ZERG_LAIR) {
			if (requiredLevel == Units.ZERG_HATCHERY ||
				requiredLevel == Units.ZERG_LAIR) {
				return true;
			}
		} else if (currentTechLevel == Units.ZERG_HIVE) {
			if (requiredLevel == Units.ZERG_HATCHERY ||
				requiredLevel == Units.ZERG_LAIR ||
				requiredLevel == Units.ZERG_HIVE) {
				return true;
			}
		}
		return false;
	}
	
	private UnitType getCurrentTechLevel() {
		UnitType highest = Units.INVALID;
		for (UnitInPool townHall : bot.observation().getUnits(
				Alliance.SELF, 
				unitInPool -> 
				UnitData.isTownHall(unitInPool.unit().getType()))) {
			UnitType structureType = townHall.unit().getType();
			if (structureType == Units.ZERG_HATCHERY && 
					highest == Units.INVALID) {
				highest = structureType;
			} else if (structureType == Units.ZERG_LAIR && 
					(highest == Units.INVALID || 
					highest == Units.ZERG_HATCHERY)) {
				highest = structureType;		
			} else if (townHall.unit().getType() == Units.ZERG_HIVE) {
				return Units.ZERG_HIVE;
			}
		}
		return highest;
	}
	
	private boolean alreadyComplete(Upgrade upgrade) {
		for (Upgrade completedUpgrade : completedUpgrades) {
			if (completedUpgrade == upgrade) {
				return true;
			}
		}
		return false;
	}
	
	private boolean currentlyInProduction(Upgrade upgrade) {
		for (UnitInPool structure : bot.observation().getUnits(
				Alliance.SELF, 
				unitInPool -> 
				UnitData.isStructure(unitInPool.unit().getType()))) {
			for (UnitOrder order : structure.unit().getOrders()) {
				if (AbilityData.areEquivalent(order.getAbility(), AbilityData.getAbilityToResearchUpgrade(upgrade))) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean gasIncomeIsSufficientForUpgrade(Upgrade upgrade) {
		return getCurrentGasCount() >= requiredGasesForResearch(upgrade);
	}
	
	private int requiredGasesForResearch(Upgrade upgrade) {
		UpgradeData upgradeData = loadUpgradeDataFor((Upgrades) upgrade);
		int vespeneCost = upgradeData.getVespeneCost().orElse(0);
		if (vespeneCost <= 100) {
			return 1;
		} else if (vespeneCost <= 150) {
			return 2;
		} else if (vespeneCost <= 200) {
			return 3;
		} else {
			return 4;
		}
	}
	
	private int getCurrentGasCount() {
		return bot.observation().getUnits(
				Alliance.SELF, 
				unitInPool -> 
				UnitData.isGasStructure(unitInPool.unit().getType())).size();
	}
		
	private UpgradeData loadUpgradeDataFor(Upgrades upgrade) {
		return bot.observation().getUpgradeData(false).get(upgrade);
	}
}
