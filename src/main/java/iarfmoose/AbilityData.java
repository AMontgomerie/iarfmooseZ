package iarfmoose;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.data.Upgrades;

public abstract class AbilityData {
	
	public static UnitType getResearchStructure(Abilities ability) {
		switch(ability) {
		case RESEARCH_ZERG_FLYER_ARMOR:
		case RESEARCH_ZERG_FLYER_ARMOR_LEVEL1:
		case RESEARCH_ZERG_FLYER_ARMOR_LEVEL2:
		case RESEARCH_ZERG_FLYER_ARMOR_LEVEL3:
		case RESEARCH_ZERG_FLYER_ATTACK:
		case RESEARCH_ZERG_FLYER_ATTACK_LEVEL1:
		case RESEARCH_ZERG_FLYER_ATTACK_LEVEL2:
		case RESEARCH_ZERG_FLYER_ATTACK_LEVEL3:
			return Units.ZERG_SPIRE;
		case RESEARCH_ZERG_GROUND_ARMOR:
		case RESEARCH_ZERG_GROUND_ARMOR_LEVEL1:
		case RESEARCH_ZERG_GROUND_ARMOR_LEVEL2:
		case RESEARCH_ZERG_GROUND_ARMOR_LEVEL3:
		case RESEARCH_ZERG_MELEE_WEAPONS:
		case RESEARCH_ZERG_MELEE_WEAPONS_LEVEL1:
		case RESEARCH_ZERG_MELEE_WEAPONS_LEVEL2:
		case RESEARCH_ZERG_MELEE_WEAPONS_LEVEL3:
		case RESEARCH_ZERG_MISSILE_WEAPONS:
		case RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL1:
		case RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL2:
		case RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL3:
			return Units.ZERG_EVOLUTION_CHAMBER;
		case RESEARCH_ZERGLING_ADRENAL_GLANDS:
		case RESEARCH_ZERGLING_METABOLIC_BOOST:
			return Units.ZERG_SPAWNING_POOL;
		case RESEARCH_ZERG_LAIR_EVOLVE_VENTRAL_SACKS:
			return Units.ZERG_LAIR;
		case RESEARCH_BURROW:
		case RESEARCH_PNEUMATIZED_CARAPACE:
			return Units.ZERG_HATCHERY;
		case RESEARCH_CENTRIFUGAL_HOOKS:
			return Units.ZERG_BANELING_NEST;
		case RESEARCH_DRILLING_CLAWS:
		case RESEARCH_GLIAL_REGENERATION:
			return Units.ZERG_ROACH_WARREN;
		case RESEARCH_CHITINOUS_PLATING:
			return Units.ZERG_ULTRALISK_CAVERN;
		case RESEARCH_GROOVED_SPINES:
		case RESEARCH_MUSCULAR_AUGMENTS:
			return Units.ZERG_HYDRALISK_DEN;
		case RESEARCH_NEURAL_PARASITE:
		case RESEARCH_PATHOGEN_GLANDS:
			return Units.ZERG_INFESTATION_PIT;
		default:
			return Units.INVALID;				
		}
	}
	
	public static ProductionType getProductionType(ProductionItem item) {
		if (abilityMakesUnit(item.getAbility())) {
			return ProductionType.TRAIN;
		} else if (abilityMakesUnitFromLarva(item.getAbility())) {
			return ProductionType.TRAIN_FROM_LARVA;
		} else if (abilityMakesStructure(item.getAbility())) {
			return ProductionType.BUILD;
		} else if (abilityMakesTech(item.getAbility())) {
			return ProductionType.RESEARCH;
		} else if (abilityMorphsUnit(item.getAbility())) {
			return ProductionType.MORPH;
		} else {
			return ProductionType.UNKNOWN;
		}
	}
	
	public static UnitType getCreatedUnitType(Abilities ability) {
		switch(ability) {
		case TRAIN_BANELING:
			return Units.ZERG_BANELING;
		case BUILD_BANELING_NEST:
			return Units.ZERG_BANELING_NEST;
		case MORPH_BROODLORD:
			return Units.ZERG_BROODLORD;
		case TRAIN_CORRUPTOR:
			return Units.ZERG_CORRUPTOR;
		case TRAIN_DRONE:
			return Units.ZERG_DRONE;
		case BUILD_EVOLUTION_CHAMBER:
			return Units.ZERG_EVOLUTION_CHAMBER;
		case BUILD_EXTRACTOR:
			return Units.ZERG_EXTRACTOR;
		case MORPH_GREATER_SPIRE:
			return Units.ZERG_GREATER_SPIRE;
		case BUILD_HATCHERY: 
			return Units.ZERG_HATCHERY;
		case MORPH_HIVE:
			return Units.ZERG_HIVE;
		case TRAIN_HYDRALISK:
			return Units.ZERG_HYDRALISK;
		case BUILD_HYDRALISK_DEN:
			return Units.ZERG_HYDRALISK_DEN;
		case BUILD_INFESTATION_PIT:
			return Units.ZERG_INFESTATION_PIT;
		case TRAIN_INFESTOR:
			return Units.ZERG_INFESTOR;
		case MORPH_LAIR:
			return Units.ZERG_LAIR;
		case MORPH_LURKER_DEN:
			return Units.ZERG_LURKER_DEN_MP;
		case MORPH_LURKER:
			return Units.ZERG_LURKER_MP;
		case TRAIN_MUTALISK: 
			return Units.ZERG_MUTALISK;
		case BUILD_NYDUS_WORM:
			return Units.ZERG_NYDUS_CANAL;
		case BUILD_NYDUS_NETWORK:
			return Units.ZERG_NYDUS_NETWORK;
		case TRAIN_OVERLORD:
			return Units.ZERG_OVERLORD;
		case MORPH_OVERLORD_TRANSPORT:
			return Units.ZERG_OVERLORD_TRANSPORT;
		case MORPH_OVERSEER:
			return Units.ZERG_OVERSEER;
		case TRAIN_QUEEN:
			return Units.ZERG_QUEEN;
		case MORPH_RAVAGER:
			return Units.ZERG_RAVAGER;
		case TRAIN_ROACH:
			return Units.ZERG_ROACH;
		case BUILD_ROACH_WARREN:
			return Units.ZERG_ROACH_WARREN;
		case BUILD_SPAWNING_POOL:	 
			return Units.ZERG_SPAWNING_POOL;
		case BUILD_SPINE_CRAWLER:
			return Units.ZERG_SPINE_CRAWLER;
		case BUILD_SPIRE:
			return Units.ZERG_SPIRE;
		case BUILD_SPORE_CRAWLER:
			return Units.ZERG_SPORE_CRAWLER;
		case TRAIN_SWARMHOST:
			return Units.ZERG_SWARM_HOST_MP;
		case TRAIN_ULTRALISK:
			return Units.ZERG_ULTRALISK;
		case BUILD_ULTRALISK_CAVERN: 
			return Units.ZERG_ULTRALISK_CAVERN;
		case TRAIN_VIPER:
			return Units.ZERG_VIPER;
		case TRAIN_ZERGLING:
			return Units.ZERG_ZERGLING;
		default:
			return Units.INVALID;
		}
	}
	
	public static Upgrades getResearchedUpgradeType(Abilities ability) {
		switch(ability) {
		case RESEARCH_ZERG_FLYER_ARMOR:
		case RESEARCH_ZERG_FLYER_ARMOR_LEVEL1:
			return Upgrades.ZERG_FLYER_ARMORS_LEVEL1;
		case RESEARCH_ZERG_FLYER_ARMOR_LEVEL2:
			return Upgrades.ZERG_FLYER_ARMORS_LEVEL2;
		case RESEARCH_ZERG_FLYER_ARMOR_LEVEL3:
			return Upgrades.ZERG_FLYER_ARMORS_LEVEL3;
		case RESEARCH_ZERG_FLYER_ATTACK:
		case RESEARCH_ZERG_FLYER_ATTACK_LEVEL1:
			return Upgrades.ZERG_FLYER_WEAPONS_LEVEL1;
		case RESEARCH_ZERG_FLYER_ATTACK_LEVEL2:
			return Upgrades.ZERG_FLYER_WEAPONS_LEVEL2;
		case RESEARCH_ZERG_FLYER_ATTACK_LEVEL3:
			return Upgrades.ZERG_FLYER_WEAPONS_LEVEL3;
		case RESEARCH_ZERG_GROUND_ARMOR:
		case RESEARCH_ZERG_GROUND_ARMOR_LEVEL1:
			return Upgrades.ZERG_GROUND_ARMORS_LEVEL1;
		case RESEARCH_ZERG_GROUND_ARMOR_LEVEL2:
			return Upgrades.ZERG_GROUND_ARMORS_LEVEL2;
		case RESEARCH_ZERG_GROUND_ARMOR_LEVEL3:
			return Upgrades.ZERG_GROUND_ARMORS_LEVEL3;
		case RESEARCH_ZERG_MELEE_WEAPONS:
		case RESEARCH_ZERG_MELEE_WEAPONS_LEVEL1:
			return Upgrades.ZERG_MELEE_WEAPONS_LEVEL1;
		case RESEARCH_ZERG_MELEE_WEAPONS_LEVEL2:
			return Upgrades.ZERG_MELEE_WEAPONS_LEVEL2;
		case RESEARCH_ZERG_MELEE_WEAPONS_LEVEL3:
			return Upgrades.ZERG_MELEE_WEAPONS_LEVEL3;
		case RESEARCH_ZERG_MISSILE_WEAPONS:
		case RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL1:
			return Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1;
		case RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL2:
			return Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2;
		case RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL3:
			return Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3;
		case RESEARCH_ZERGLING_ADRENAL_GLANDS:
			return Upgrades.ZERGLING_ATTACK_SPEED;
		case RESEARCH_ZERGLING_METABOLIC_BOOST:
			return Upgrades.ZERGLING_MOVEMENT_SPEED;
		case RESEARCH_ZERG_LAIR_EVOLVE_VENTRAL_SACKS:
			return Upgrades.OVERLORD_TRANSPORT;
		case RESEARCH_BURROW:
			return Upgrades.ZERG_BURROW_MOVE;
		case RESEARCH_PNEUMATIZED_CARAPACE:
			return Upgrades.OVERLORD_SPEED;
		case RESEARCH_CENTRIFUGAL_HOOKS:
			return Upgrades.CENTRIFICAL_HOOKS;
		case RESEARCH_DRILLING_CLAWS:
			return Upgrades.DRILL_CLAWS;
		case RESEARCH_GLIAL_REGENERATION:
			return Upgrades.GLIALRE_CONSTITUTION;
		case RESEARCH_CHITINOUS_PLATING:
			return Upgrades.CHITINOUS_PLATING;
		case RESEARCH_GROOVED_SPINES:
			return Upgrades.EVOLVE_GROOVED_SPINES;
		case RESEARCH_MUSCULAR_AUGMENTS:
			return Upgrades.EVOLVE_MUSCULAR_AUGMENTS;
		case RESEARCH_NEURAL_PARASITE:
			return Upgrades.NEURAL_PARASITE;
		case RESEARCH_PATHOGEN_GLANDS:
			return Upgrades.INFESTOR_ENERGY_UPGRADE;
		default:
			return Upgrades.INVALID;
		}
	}
		
	public static UnitType getRequiredTechForMorph(Abilities ability) {
		switch(ability) {
		case TRAIN_BANELING:
			return Units.ZERG_BANELING_NEST;
		case MORPH_BROODLORD:
			return Units.ZERG_GREATER_SPIRE;
		case MORPH_GREATER_SPIRE:
			return Units.ZERG_SPIRE;
		case MORPH_HIVE:
			return Units.ZERG_INFESTATION_PIT;
		case MORPH_LAIR:
			return Units.ZERG_SPAWNING_POOL;
		case MORPH_LURKER:
			return Units.ZERG_LURKER_DEN_MP;
		case MORPH_LURKER_DEN:
			return Units.ZERG_HYDRALISK_DEN;
		case MORPH_OVERLORD_TRANSPORT:
		case MORPH_OVERSEER:
			return Units.ZERG_LAIR;
		case MORPH_RAVAGER:
			return Units.ZERG_ROACH_WARREN;
		case MORPH_SPINE_CRAWLER_UPROOT:
		case MORPH_UPROOT:
			return Units.ZERG_SPINE_CRAWLER;
		case MORPH_ROOT:
		case MORPH_SPINE_CRAWLER_ROOT:
			return Units.ZERG_SPINE_CRAWLER_UPROOTED;
		case MORPH_SPORE_CRAWLER_ROOT:
			return Units.ZERG_SPORE_CRAWLER_UPROOTED;
		case MORPH_SPORE_CRAWLER_UPROOT:
			return Units.ZERG_SPORE_CRAWLER;
		default:
			return Units.INVALID;
		}
	}
	
	public static UnitType morphsFrom(Abilities ability) {
		switch(ability) {
		case TRAIN_BANELING:
			return Units.ZERG_ZERGLING;
		case MORPH_BROODLORD:
			return Units.ZERG_CORRUPTOR;
		case MORPH_GREATER_SPIRE:
			return Units.ZERG_SPIRE;
		case MORPH_HIVE:
			return Units.ZERG_LAIR;
		case MORPH_LAIR:
			return Units.ZERG_HATCHERY;
		case MORPH_LURKER:
			return Units.ZERG_HYDRALISK;
		case MORPH_OVERLORD_TRANSPORT:
			return Units.ZERG_OVERLORD;
		case MORPH_OVERSEER:
			return Units.ZERG_OVERLORD;
		case MORPH_RAVAGER:
			return Units.ZERG_ROACH;
		case MORPH_SPINE_CRAWLER_UPROOT:
		case MORPH_UPROOT:
			return Units.ZERG_SPINE_CRAWLER;
		case MORPH_ROOT:
		case MORPH_SPINE_CRAWLER_ROOT:
			return Units.ZERG_SPINE_CRAWLER_UPROOTED;
		case MORPH_SPORE_CRAWLER_ROOT:
			return Units.ZERG_SPORE_CRAWLER_UPROOTED;
		case MORPH_SPORE_CRAWLER_UPROOT:
			return Units.ZERG_SPORE_CRAWLER;
		default:
			return Units.INVALID;
		}
	}
	
	public static boolean abilityMakesUnit(Ability ability) {
		if (ability == Abilities.TRAIN_QUEEN) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean abilityMakesUnitFromLarva(Ability ability) {
			if (ability == Abilities.TRAIN_CORRUPTOR ||
				ability == Abilities.TRAIN_DRONE ||
				ability == Abilities.TRAIN_HYDRALISK ||
				ability == Abilities.TRAIN_INFESTOR ||
				ability == Abilities.TRAIN_MUTALISK ||
				ability == Abilities.TRAIN_OVERLORD ||
				ability == Abilities.TRAIN_ROACH ||
				ability == Abilities.TRAIN_SWARMHOST ||
				ability == Abilities.TRAIN_ULTRALISK ||
				ability == Abilities.TRAIN_VIPER ||
				ability == Abilities. TRAIN_ZERGLING) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean abilityMakesStructure(Ability ability) {
			if (ability == Abilities.BUILD_BANELING_NEST ||
				ability == Abilities.BUILD_EVOLUTION_CHAMBER ||
				ability == Abilities.BUILD_EXTRACTOR ||
				ability == Abilities.BUILD_HATCHERY ||
				ability == Abilities.BUILD_HYDRALISK_DEN ||
				ability == Abilities.BUILD_INFESTATION_PIT ||
				ability == Abilities.BUILD_NYDUS_NETWORK ||
				ability == Abilities.BUILD_NYDUS_WORM ||
				ability == Abilities.BUILD_ROACH_WARREN ||
				ability == Abilities.BUILD_SPAWNING_POOL ||
				ability == Abilities.BUILD_SPINE_CRAWLER ||
				ability == Abilities.BUILD_SPIRE ||
				ability == Abilities.BUILD_SPORE_CRAWLER ||
				ability == Abilities.BUILD_ULTRALISK_CAVERN ||
				ability == Abilities.MORPH_LURKER_DEN) {
				return true;
			} else {
				return false;
			}
	}
	
	public static boolean abilityMakesTech(Ability ability) {
		if (ability == Abilities.RESEARCH_ZERGLING_METABOLIC_BOOST ||
			ability == Abilities.RESEARCH_ZERG_FLYER_ARMOR ||
			ability == Abilities.RESEARCH_ZERG_FLYER_ARMOR_LEVEL1 ||
			ability == Abilities.RESEARCH_ZERG_FLYER_ARMOR_LEVEL2 ||
			ability == Abilities.RESEARCH_ZERG_FLYER_ARMOR_LEVEL3 ||
			ability == Abilities.RESEARCH_ZERG_FLYER_ATTACK ||
			ability == Abilities.RESEARCH_ZERG_FLYER_ATTACK_LEVEL1 ||
			ability == Abilities.RESEARCH_ZERG_FLYER_ATTACK_LEVEL2 ||
			ability == Abilities.RESEARCH_ZERG_FLYER_ATTACK_LEVEL3 ||
			ability == Abilities.RESEARCH_ZERG_GROUND_ARMOR ||
			ability == Abilities.RESEARCH_ZERG_GROUND_ARMOR_LEVEL1 ||
			ability == Abilities.RESEARCH_ZERG_GROUND_ARMOR_LEVEL2 ||
			ability == Abilities.RESEARCH_ZERG_GROUND_ARMOR_LEVEL3 ||
			ability == Abilities.RESEARCH_ZERG_MELEE_WEAPONS ||
			ability == Abilities.RESEARCH_ZERG_MELEE_WEAPONS_LEVEL1 ||
			ability == Abilities.RESEARCH_ZERG_MELEE_WEAPONS_LEVEL2 ||
			ability == Abilities.RESEARCH_ZERG_MELEE_WEAPONS_LEVEL3 ||
			ability == Abilities.RESEARCH_ZERG_MISSILE_WEAPONS ||
			ability == Abilities.RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL1 ||
			ability == Abilities.RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL2 ||
			ability == Abilities.RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL3 ||
			ability == Abilities.RESEARCH_ZERGLING_ADRENAL_GLANDS ||
			ability == Abilities.RESEARCH_ZERGLING_METABOLIC_BOOST ||
			ability == Abilities.RESEARCH_ZERG_LAIR_EVOLVE_VENTRAL_SACKS ||
			ability == Abilities.RESEARCH_BURROW ||
			ability == Abilities.RESEARCH_PNEUMATIZED_CARAPACE ||
			ability == Abilities.RESEARCH_CENTRIFUGAL_HOOKS ||
			ability == Abilities.RESEARCH_DRILLING_CLAWS ||
			ability == Abilities.RESEARCH_GLIAL_REGENERATION ||
			ability == Abilities.RESEARCH_CHITINOUS_PLATING ||
			ability == Abilities.RESEARCH_GROOVED_SPINES ||
			ability == Abilities.RESEARCH_MUSCULAR_AUGMENTS ||
			ability == Abilities.RESEARCH_NEURAL_PARASITE ||
			ability == Abilities.RESEARCH_PATHOGEN_GLANDS) {
				return true;
			} else {
				return false;
			}
	}
		
	public static boolean abilityMorphsUnit(Ability ability) {
		if (ability == Abilities.TRAIN_BANELING ||
			ability == Abilities.MORPH_LAIR ||
			ability == Abilities.MORPH_BROODLORD ||
			ability == Abilities.MORPH_GREATER_SPIRE ||
			ability == Abilities.MORPH_HIVE ||
			ability == Abilities.MORPH_LAIR ||
			ability == Abilities.MORPH_LURKER ||
			ability == Abilities.MORPH_OVERLORD_TRANSPORT ||
			ability == Abilities.MORPH_OVERSEER ||
			ability == Abilities.MORPH_RAVAGER ||
			ability == Abilities.MORPH_SPINE_CRAWLER_UPROOT ||
			ability == Abilities.MORPH_UPROOT ||
			ability == Abilities.MORPH_ROOT ||
			ability == Abilities.MORPH_SPINE_CRAWLER_ROOT ||
			ability == Abilities.MORPH_SPORE_CRAWLER_ROOT ||
			ability == Abilities.MORPH_SPORE_CRAWLER_UPROOT) {
			return true;
		} else {
			return false;
		}
	}
	
	public static Abilities getAbilityToResearchUpgrade(Upgrade upgrade) {
		if (upgrade == Upgrades.ZERG_FLYER_ARMORS_LEVEL1) {
			return Abilities.RESEARCH_ZERG_FLYER_ARMOR_LEVEL1;
		} else if (upgrade == Upgrades.ZERG_FLYER_ARMORS_LEVEL2) {
			return Abilities.RESEARCH_ZERG_FLYER_ARMOR_LEVEL2;
		} else if (upgrade == Upgrades.ZERG_FLYER_ARMORS_LEVEL3) {
			return Abilities.RESEARCH_ZERG_FLYER_ARMOR_LEVEL3;
		} else if (upgrade == Upgrades.ZERG_FLYER_WEAPONS_LEVEL1) {
			return Abilities.RESEARCH_ZERG_FLYER_ATTACK_LEVEL1;
		} else if (upgrade == Upgrades.ZERG_FLYER_WEAPONS_LEVEL2) {
			return Abilities.RESEARCH_ZERG_FLYER_ATTACK_LEVEL2;
		} else if (upgrade == Upgrades.ZERG_FLYER_WEAPONS_LEVEL3) {
			return Abilities.RESEARCH_ZERG_FLYER_ATTACK_LEVEL3;
		} else if (upgrade == Upgrades.ZERG_GROUND_ARMORS_LEVEL1) {
			return Abilities.RESEARCH_ZERG_GROUND_ARMOR_LEVEL1;
		} else if (upgrade == Upgrades.ZERG_GROUND_ARMORS_LEVEL2) {
			return Abilities.RESEARCH_ZERG_GROUND_ARMOR_LEVEL2;
		} else if (upgrade == Upgrades.ZERG_GROUND_ARMORS_LEVEL3) {
			return Abilities.RESEARCH_ZERG_GROUND_ARMOR_LEVEL3;
		} else if (upgrade == Upgrades.ZERG_MELEE_WEAPONS_LEVEL1) {
			return Abilities.RESEARCH_ZERG_MELEE_WEAPONS_LEVEL1;
		} else if (upgrade == Upgrades.ZERG_MELEE_WEAPONS_LEVEL2) {
			return Abilities.RESEARCH_ZERG_MELEE_WEAPONS_LEVEL2;
		} else if (upgrade == Upgrades.ZERG_MELEE_WEAPONS_LEVEL3) {
			return Abilities.RESEARCH_ZERG_MELEE_WEAPONS_LEVEL3;
		} else if (upgrade == Upgrades.ZERG_MISSILE_WEAPONS_LEVEL1) {
			return Abilities.RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL1;
		} else if (upgrade == Upgrades.ZERG_MISSILE_WEAPONS_LEVEL2) {
			return Abilities.RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL2;
		} else if (upgrade == Upgrades.ZERG_MISSILE_WEAPONS_LEVEL3) {
			return Abilities.RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL3;
		} else if (upgrade == Upgrades.ZERGLING_ATTACK_SPEED) {
			return Abilities.RESEARCH_ZERGLING_ADRENAL_GLANDS;
		} else if (upgrade == Upgrades.ZERGLING_MOVEMENT_SPEED) {
			return Abilities.RESEARCH_ZERGLING_METABOLIC_BOOST;
		} else if (upgrade == Upgrades.OVERLORD_TRANSPORT) {
			return Abilities.RESEARCH_ZERG_LAIR_EVOLVE_VENTRAL_SACKS;
		} else if (upgrade == Upgrades.ZERG_BURROW_MOVE) {
			return Abilities.RESEARCH_BURROW;
		} else if (upgrade == Upgrades.OVERLORD_SPEED) {
			return Abilities.RESEARCH_PNEUMATIZED_CARAPACE;
		} else if (upgrade == Upgrades.CENTRIFICAL_HOOKS) {
			return Abilities.RESEARCH_CENTRIFUGAL_HOOKS;
		} else if (upgrade == Upgrades.DRILL_CLAWS) {
			return Abilities.RESEARCH_DRILLING_CLAWS;
		} else if (upgrade == Upgrades.GLIALRE_CONSTITUTION) {
			return Abilities.RESEARCH_GLIAL_REGENERATION;
		} else if (upgrade == Upgrades.CHITINOUS_PLATING) {
			return Abilities.RESEARCH_CHITINOUS_PLATING;
		} else if (upgrade == Upgrades.EVOLVE_GROOVED_SPINES) {
			return Abilities.RESEARCH_GROOVED_SPINES;
		} else if (upgrade == Upgrades.EVOLVE_MUSCULAR_AUGMENTS) {
			return Abilities.RESEARCH_MUSCULAR_AUGMENTS;
		} else if (upgrade == Upgrades.NEURAL_PARASITE) {
			return Abilities.RESEARCH_NEURAL_PARASITE;
		} else if (upgrade == Upgrades.INFESTOR_ENERGY_UPGRADE) {
			return Abilities.RESEARCH_PATHOGEN_GLANDS;
		} else {
			return Abilities.INVALID;
		}
	}
	
	public static boolean areEquivalent(Ability abilityA, Ability abilityB) {
		if (abilityA == abilityB) {
			return true;
		} else if (compareAbilities(abilityA, abilityB) ||
			compareAbilities(abilityB, abilityA)) {
			return true;
		} else {
			return false;
		}
	}
	
	private static boolean compareAbilities(Ability abilityA, Ability abilityB) {
		if (abilityA == Abilities.RESEARCH_ZERG_FLYER_ARMOR &&
			(abilityB == Abilities.RESEARCH_ZERG_FLYER_ARMOR_LEVEL1 ||
			 abilityB == Abilities.RESEARCH_ZERG_FLYER_ARMOR_LEVEL2 ||
			 abilityB == Abilities.RESEARCH_ZERG_FLYER_ARMOR_LEVEL3)) {
			return true;
		} else if (abilityA == Abilities.RESEARCH_ZERG_FLYER_ATTACK &&
			(abilityB == Abilities.RESEARCH_ZERG_FLYER_ATTACK_LEVEL1 ||
			 abilityB == Abilities.RESEARCH_ZERG_FLYER_ATTACK_LEVEL2 ||
			 abilityB == Abilities.RESEARCH_ZERG_FLYER_ATTACK_LEVEL3)) {
			return true;
		} else if (abilityA == Abilities.RESEARCH_ZERG_GROUND_ARMOR &&
			(abilityB == Abilities.RESEARCH_ZERG_GROUND_ARMOR_LEVEL1 ||
			 abilityB == Abilities.RESEARCH_ZERG_GROUND_ARMOR_LEVEL2 ||
			 abilityB == Abilities.RESEARCH_ZERG_GROUND_ARMOR_LEVEL3)) {
			return true;
		} else if (abilityA == Abilities.RESEARCH_ZERG_MELEE_WEAPONS &&
			(abilityB == Abilities.RESEARCH_ZERG_MELEE_WEAPONS_LEVEL1 ||
			 abilityB == Abilities.RESEARCH_ZERG_MELEE_WEAPONS_LEVEL2 ||
			 abilityB == Abilities.RESEARCH_ZERG_MELEE_WEAPONS_LEVEL3)) {
			return true;
		} else if (abilityA == Abilities.RESEARCH_ZERG_MISSILE_WEAPONS &&
			(abilityB == Abilities.RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL1 ||
			 abilityB == Abilities.RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL2 ||
			 abilityB == Abilities.RESEARCH_ZERG_MISSILE_WEAPONS_LEVEL3)) {
			return true;
		} else if (abilityA == Abilities.BUILD_CREEP_TUMOR &&
				abilityB == Abilities.BUILD_CREEP_TUMOR_QUEEN ||
				abilityB == Abilities.BUILD_CREEP_TUMOR_TUMOR) {
		
		}
		return false;
	}
}
