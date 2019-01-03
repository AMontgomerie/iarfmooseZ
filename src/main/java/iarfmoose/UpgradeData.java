package iarfmoose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.data.Upgrades;

public abstract class UpgradeData {
	
	public static UnitType getTechLevelRequiredFor(Upgrade upgrade) {
		if (upgrade == Upgrades.ZERG_GROUND_ARMORS_LEVEL1 ||
			upgrade == Upgrades.ZERG_MELEE_WEAPONS_LEVEL1 ||
			upgrade == Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1 ||
			upgrade == Upgrades.ZERGLING_MOVEMENT_SPEED ||
			upgrade == Upgrades.OVERLORD_SPEED ||
			upgrade == Upgrades.BURROW) {
			return Units.ZERG_HATCHERY;
		} else if (upgrade == Upgrades.ZERG_FLYER_ARMORS_LEVEL1 ||
				upgrade == Upgrades.ZERG_FLYER_ARMORS_LEVEL2 ||
				upgrade == Upgrades.ZERG_FLYER_WEAPONS_LEVEL1 ||
				upgrade == Upgrades.ZERG_FLYER_WEAPONS_LEVEL2 ||
				upgrade == Upgrades.ZERG_GROUND_ARMORS_LEVEL2 ||
				upgrade == Upgrades.ZERG_MELEE_WEAPONS_LEVEL2 ||
				upgrade == Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2 ||
				upgrade == Upgrades.OVERLORD_TRANSPORT ||
				upgrade == Upgrades.ZERG_BURROW_MOVE ||
				upgrade == Upgrades.CENTRIFICAL_HOOKS ||
				upgrade == Upgrades.DRILL_CLAWS ||
				upgrade == Upgrades.GLIALRE_CONSTITUTION ||
				upgrade == Upgrades.EVOLVE_GROOVED_SPINES ||
				upgrade == Upgrades.EVOLVE_MUSCULAR_AUGMENTS ||
				upgrade == Upgrades.NEURAL_PARASITE ||
				upgrade == Upgrades.INFESTOR_ENERGY_UPGRADE) {
			return Units.ZERG_LAIR;
		} else if (upgrade == Upgrades.ZERG_FLYER_ARMORS_LEVEL3 ||
				upgrade == Upgrades.ZERG_FLYER_WEAPONS_LEVEL3 ||
				upgrade == Upgrades.ZERG_GROUND_ARMORS_LEVEL3 ||
				upgrade == Upgrades.ZERG_MELEE_WEAPONS_LEVEL3 ||
				upgrade == Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3 ||
				upgrade == Upgrades.ZERGLING_ATTACK_SPEED ||
				upgrade == Upgrades.CHITINOUS_PLATING) {
			return Units.ZERG_HIVE;
		} else {
			return Units.INVALID;
		}
	}
	
	public static List<Upgrade> getUpgradesForStructureType(UnitType unitType) {
		if (unitType == Units.ZERG_BANELING_NEST) {
			return new ArrayList<Upgrade>(
					Arrays.asList(Upgrades.CENTRIFICAL_HOOKS)
					);
			//removed hatchery from this list as researching these upgrades too early delays important things like ling speed
		} else if (unitType == Units.ZERG_LAIR || 
				unitType == Units.ZERG_HIVE) {
			return new ArrayList<Upgrade>(
					Arrays.asList(Upgrades.OVERLORD_SPEED, Upgrades.BURROW)
					);
		} else if (unitType == Units.ZERG_ROACH_WARREN) {
			return new ArrayList<Upgrade>(
					Arrays.asList(Upgrades.GLIALRE_CONSTITUTION, Upgrades.TUNNELING_CLAWS)
					);
		} else if (unitType == Units.ZERG_SPAWNING_POOL) {
			return new ArrayList<Upgrade>(
					Arrays.asList(Upgrades.ZERGLING_MOVEMENT_SPEED, Upgrades.ZERGLING_ATTACK_SPEED)
					);
		} else if (unitType == Units.ZERG_HYDRALISK_DEN) {
			return new ArrayList<Upgrade>(
					Arrays.asList(Upgrades.EVOLVE_MUSCULAR_AUGMENTS, Upgrades.EVOLVE_GROOVED_SPINES)
					);
		} else if (unitType == Units.ZERG_EVOLUTION_CHAMBER) {
			return new ArrayList<Upgrade>(
					Arrays.asList(Upgrades.ZERG_GROUND_ARMORS_LEVEL1,
							Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1,
							Upgrades.ZERG_MELEE_WEAPONS_LEVEL1,
							Upgrades.ZERG_MELEE_WEAPONS_LEVEL2,
							Upgrades.ZERG_GROUND_ARMORS_LEVEL2,
							Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2,
							Upgrades.ZERG_GROUND_ARMORS_LEVEL3,
							Upgrades.ZERG_MELEE_WEAPONS_LEVEL3,					
							Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3)
					);
		} else if (unitType == Units.ZERG_GREATER_SPIRE) {
			return new ArrayList<Upgrade>(
					Arrays.asList(Upgrades.ZERG_FLYER_WEAPONS_LEVEL1, 
							Upgrades.ZERG_FLYER_ARMORS_LEVEL1,
							Upgrades.ZERG_FLYER_WEAPONS_LEVEL2, 
							Upgrades.ZERG_FLYER_ARMORS_LEVEL2,
							Upgrades.ZERG_FLYER_WEAPONS_LEVEL3, 
							Upgrades.ZERG_FLYER_ARMORS_LEVEL3)
					);
		} else {
			return new ArrayList<Upgrade>();
		}
	}
}
