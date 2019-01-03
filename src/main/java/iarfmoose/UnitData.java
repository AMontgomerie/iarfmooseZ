package iarfmoose;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;

public abstract class UnitData {
	
	public static Abilities getAbilityToMakeUnitType(UnitType unitType) {	
	 	if (unitType == Units.ZERG_BANELING) {
	 		return Abilities.TRAIN_BANELING;	 	 
		} else if (unitType == Units.ZERG_BANELING_NEST) {
			return Abilities.BUILD_BANELING_NEST;	 	 
		} else if (unitType == Units.ZERG_BROODLORD) {
			return Abilities.MORPH_BROODLORD;	 	  
		} else if (unitType == Units.ZERG_CORRUPTOR) {
			return Abilities.TRAIN_CORRUPTOR;	 
		} else if (unitType == Units.ZERG_DRONE) {
			return Abilities.TRAIN_DRONE;	 	 	 
		} else if (unitType == Units.ZERG_EVOLUTION_CHAMBER) {
			return Abilities.BUILD_EVOLUTION_CHAMBER; 
		} else if (unitType == Units.ZERG_EXTRACTOR) {
			return Abilities.BUILD_EXTRACTOR; 
		} else if (unitType == Units.ZERG_GREATER_SPIRE) {
			return Abilities.MORPH_GREATER_SPIRE; 
		} else if (unitType == Units.ZERG_HATCHERY) {
			return Abilities.BUILD_HATCHERY; 
		} else if (unitType == Units.ZERG_HIVE) {
			return Abilities.MORPH_HIVE;	 
		} else if (unitType == Units.ZERG_HYDRALISK) {
			return Abilities.TRAIN_HYDRALISK;	 	 
		} else if (unitType == Units.ZERG_HYDRALISK_DEN) {
			return Abilities.BUILD_HYDRALISK_DEN; 
		} else if (unitType == Units.ZERG_INFESTATION_PIT) {
			return Abilities.BUILD_INFESTATION_PIT;	 	 
		} else if (unitType == Units.ZERG_INFESTOR) {
			return Abilities.TRAIN_INFESTOR;	 
		} else if (unitType == Units.ZERG_LAIR) {
			return Abilities.MORPH_LAIR;	   
		} else if (unitType == Units.ZERG_LURKER_DEN_MP) {
			return Abilities.MORPH_LURKER_DEN;	 
		} else if (unitType == Units.ZERG_LURKER_MP) {
			return Abilities.MORPH_LURKER;
		} else if (unitType == Units.ZERG_MUTALISK) {
			return Abilities.TRAIN_MUTALISK;	 
		} else if (unitType == Units.ZERG_NYDUS_CANAL) {
			return Abilities.BUILD_NYDUS_WORM;	 
		} else if (unitType == Units.ZERG_NYDUS_NETWORK) {
			return Abilities.BUILD_NYDUS_NETWORK;
		} else if (unitType == Units.ZERG_OVERLORD) {
			return Abilities.TRAIN_OVERLORD; 	 
		} else if (unitType == Units.ZERG_OVERLORD_TRANSPORT) {	
			return Abilities.MORPH_OVERLORD_TRANSPORT; 
		} else if (unitType == Units.ZERG_OVERSEER) {
			return Abilities.MORPH_OVERSEER; 
		} else if (unitType == Units.ZERG_QUEEN) {
			return Abilities.TRAIN_QUEEN;
		} else if (unitType == Units.ZERG_RAVAGER) {
			return Abilities.MORPH_RAVAGER;  
		} else if (unitType == Units.ZERG_ROACH) {
			return Abilities.TRAIN_ROACH;  
		} else if (unitType == Units.ZERG_ROACH_WARREN) {
			return Abilities.BUILD_ROACH_WARREN;	 
		} else if (unitType == Units.ZERG_SPAWNING_POOL) {
			return Abilities.BUILD_SPAWNING_POOL;	 
		} else if (unitType == Units.ZERG_SPINE_CRAWLER) {
			return Abilities.BUILD_SPINE_CRAWLER;	 
		} else if (unitType == Units.ZERG_SPIRE) {
			return Abilities.BUILD_SPIRE;
		} else if (unitType == Units.ZERG_SPORE_CRAWLER) {
			return Abilities.BUILD_SPORE_CRAWLER;	 
		} else if (unitType == Units.ZERG_SWARM_HOST_MP) {
			return Abilities.TRAIN_SWARMHOST;	  
		} else if (unitType == Units.ZERG_ULTRALISK) {
			return Abilities.TRAIN_ULTRALISK;
		} else if (unitType == Units.ZERG_ULTRALISK_CAVERN) {
			return Abilities.BUILD_ULTRALISK_CAVERN;	 
		} else if (unitType == Units.ZERG_VIPER) {
			return Abilities.TRAIN_VIPER;
		} else if (unitType == Units.ZERG_ZERGLING) {
			return Abilities.TRAIN_ZERGLING;
		} else {
			return Abilities.INVALID;
		}
	}
		
	public static boolean isStructure(UnitType unitType) {
		if (unitType == Units.PROTOSS_ASSIMILATOR ||
				unitType == Units.PROTOSS_CYBERNETICS_CORE ||	 
				unitType == Units.PROTOSS_DARK_SHRINE ||
				unitType == Units.PROTOSS_FLEET_BEACON ||	 
				unitType == Units.PROTOSS_FORGE ||	 
				unitType == Units.PROTOSS_GATEWAY ||	 
				unitType == Units.PROTOSS_NEXUS ||	 
				unitType == Units.PROTOSS_PHOTON_CANNON ||	
				unitType == Units.PROTOSS_PYLON ||	 
				unitType == Units.PROTOSS_PYLON_OVERCHARGED ||	 
				unitType == Units.PROTOSS_ROBOTICS_BAY ||	 
				unitType == Units.PROTOSS_ROBOTICS_FACILITY ||	
				unitType == Units.PROTOSS_STARGATE ||
				unitType == Units.PROTOSS_TEMPLAR_ARCHIVE ||	 
				unitType == Units.PROTOSS_TWILIGHT_COUNCIL ||
				unitType == Units.PROTOSS_WARP_GATE ||
				unitType == Units.TERRAN_ARMORY ||	 
				unitType == Units.TERRAN_AUTO_TURRET ||
				unitType == Units.TERRAN_BARRACKS ||	 
				unitType == Units.TERRAN_BARRACKS_FLYING ||	 
				unitType == Units.TERRAN_BARRACKS_REACTOR ||	 
				unitType == Units.TERRAN_BARRACKS_TECHLAB ||
				unitType == Units.TERRAN_BUNKER ||	 
				unitType == Units.TERRAN_COMMAND_CENTER	|| 
				unitType == Units.TERRAN_COMMAND_CENTER_FLYING ||
				unitType == Units.TERRAN_ENGINEERING_BAY ||	 
				unitType == Units.TERRAN_FACTORY ||	 
				unitType == Units.TERRAN_FACTORY_FLYING ||	 
				unitType == Units.TERRAN_FACTORY_REACTOR ||	 
				unitType == Units.TERRAN_FACTORY_TECHLAB ||	 
				unitType == Units.TERRAN_FUSION_CORE ||
				unitType == Units.TERRAN_GHOST_ACADEMY ||
				unitType == Units.TERRAN_MISSILE_TURRET ||
				unitType == Units.TERRAN_ORBITAL_COMMAND ||	 
				unitType == Units.TERRAN_ORBITAL_COMMAND_FLYING ||	 
				unitType == Units.TERRAN_PLANETARY_FORTRESS ||
				unitType == Units.TERRAN_REACTOR ||
				unitType == Units.TERRAN_REFINERY ||
				unitType == Units.TERRAN_SENSOR_TOWER ||
				unitType == Units.TERRAN_STARPORT ||	 
				unitType == Units.TERRAN_STARPORT_FLYING ||	 
				unitType == Units.TERRAN_STARPORT_REACTOR ||	 
				unitType == Units.TERRAN_STARPORT_TECHLAB ||	 
				unitType == Units.TERRAN_SUPPLY_DEPOT ||	 
				unitType == Units.TERRAN_SUPPLY_DEPOT_LOWERED ||	 
				unitType == Units.TERRAN_TECHLAB ||
				unitType == Units.ZERG_BANELING_NEST ||	
				unitType == Units.ZERG_EVOLUTION_CHAMBER ||	 
				unitType == Units.ZERG_EXTRACTOR ||	 
				unitType == Units.ZERG_GREATER_SPIRE ||	 
				unitType == Units.ZERG_HATCHERY ||	 
				unitType == Units.ZERG_HIVE ||
				unitType == Units.ZERG_HYDRALISK_DEN ||
				unitType == Units.ZERG_INFESTATION_PIT ||
				unitType == Units.ZERG_LAIR ||	
				unitType == Units.ZERG_LURKER_DEN_MP ||
				unitType == Units.ZERG_NYDUS_CANAL ||	 
				unitType == Units.ZERG_NYDUS_NETWORK ||
				unitType == Units.ZERG_ROACH_WARREN ||	 
				unitType == Units.ZERG_SPAWNING_POOL ||	 
				unitType == Units.ZERG_SPINE_CRAWLER ||	 
				unitType == Units.ZERG_SPINE_CRAWLER_UPROOTED ||	 
				unitType == Units.ZERG_SPIRE ||	 
				unitType == Units.ZERG_SPORE_CRAWLER ||	 
				unitType == Units.ZERG_SPORE_CRAWLER_UPROOTED ||
				unitType == Units.ZERG_ULTRALISK_CAVERN) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isWorker(UnitType unitType) {
		if (unitType == Units.TERRAN_SCV ||
			unitType == Units.TERRAN_MULE ||
			unitType == Units.ZERG_DRONE ||
			unitType == Units.ZERG_DRONE_BURROWED ||
			unitType == Units.PROTOSS_PROBE) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isGasGeyser(UnitType unitType) {
		if (unitType == Units.NEUTRAL_VESPENE_GEYSER ||
			unitType == Units.NEUTRAL_PROTOSS_VESPENE_GEYSER ||
			unitType == Units.NEUTRAL_PURIFIER_VESPENE_GEYSER ||
			unitType == Units.NEUTRAL_RICH_VESPENE_GEYSER ||
			unitType == Units.NEUTRAL_SHAKURAS_VESPENE_GEYSER ||
			unitType == Units.NEUTRAL_SPACE_PLATFORM_GEYSER)
		{
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isMineral(UnitType unitType) {
		if (unitType == Units.NEUTRAL_MINERAL_FIELD ||
				unitType == Units.NEUTRAL_MINERAL_FIELD750 ||
				unitType == Units.NEUTRAL_BATTLE_STATION_MINERAL_FIELD ||
				unitType == Units.NEUTRAL_BATTLE_STATION_MINERAL_FIELD750 ||
				unitType == Units.NEUTRAL_LAB_MINERAL_FIELD ||
				unitType == Units.NEUTRAL_LAB_MINERAL_FIELD750 ||
				unitType == Units.NEUTRAL_PURIFIER_MINERAL_FIELD ||
				unitType == Units.NEUTRAL_PURIFIER_MINERAL_FIELD750 ||
				unitType == Units.NEUTRAL_PURIFIER_RICH_MINERAL_FIELD ||
				unitType == Units.NEUTRAL_PURIFIER_RICH_MINERAL_FIELD750 ||
				unitType == Units.NEUTRAL_RICH_MINERAL_FIELD ||
				unitType == Units.NEUTRAL_RICH_MINERAL_FIELD750)
			{
				return true;
			} else {
				return false;
			}
	}
	
	public static boolean isUnbuildableRocks(UnitType unitType) {
		if (unitType == Units.NEUTRAL_UNBUILDABLE_PLATES_DESTRUCTIBLE ||
				unitType == Units.NEUTRAL_UNBUILDABLE_BRICKS_DESTRUCTIBLE || 
				unitType.getUnitTypeId() == 472) { //472 is NEUTRAL_UNBUILDABLE_ROCKS_DESTRUCTIBLE)
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isCreepTumor(UnitType unitType) {
		if (unitType == Units.ZERG_CREEP_TUMOR ||
			unitType == Units.ZERG_CREEP_TUMOR_BURROWED ||
			unitType == Units.ZERG_CREEP_TUMOR_QUEEN) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isTownHall(UnitType unitType) {
		if (unitType == Units.ZERG_HATCHERY ||
				unitType == Units.ZERG_LAIR ||
				unitType == Units.ZERG_HIVE ||
				unitType == Units.PROTOSS_NEXUS ||
				unitType == Units.TERRAN_COMMAND_CENTER ||
				unitType == Units.TERRAN_COMMAND_CENTER_FLYING ||
				unitType == Units.TERRAN_ORBITAL_COMMAND ||
				unitType == Units.TERRAN_ORBITAL_COMMAND_FLYING ||
				unitType == Units.TERRAN_PLANETARY_FORTRESS) {
				return true;
			} else {
				return false;
			}
	}
	
	public static boolean isChangeling(UnitType unitType) {
		if(unitType == Units.ZERG_CHANGELING ||	 
				unitType == Units.ZERG_CHANGELING_MARINE ||	 
				unitType == Units.ZERG_CHANGELING_MARINE_SHIELD ||	 
				unitType == Units.ZERG_CHANGELING_ZEALOT ||	 
				unitType == Units.ZERG_CHANGELING_ZERGLING ||	 
				unitType == Units.ZERG_CHANGELING_ZERGLING_WINGS) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isFightingUnit(UnitType unitType) {
		if (unitType == Units.PROTOSS_ADEPT ||
				unitType == Units.PROTOSS_ADEPT_PHASE_SHIFT ||	 
				unitType == Units.PROTOSS_ARCHON ||	 
				unitType == Units.PROTOSS_CARRIER ||	 
				unitType == Units.PROTOSS_COLOSSUS ||	 
				unitType == Units.PROTOSS_DARK_TEMPLAR ||	 
				unitType == Units.PROTOSS_DISRUPTOR ||
				unitType == Units.PROTOSS_DISRUPTOR_PHASED ||	 
				unitType == Units.PROTOSS_HIGH_TEMPLAR ||	 
				unitType == Units.PROTOSS_IMMORTAL ||	 
				unitType == Units.PROTOSS_INTERCEPTOR ||	 
				unitType == Units.PROTOSS_MOTHER_SHIP_CORE ||	 
				unitType == Units.PROTOSS_MOTHERSHIP ||	 
				unitType == Units.PROTOSS_OBSERVER ||	 
				unitType == Units.PROTOSS_ORACLE ||	 
				unitType == Units.PROTOSS_ORACLE_STASIS_TRAP ||	 
				unitType == Units.PROTOSS_PHOENIX ||	 	 
				unitType == Units.PROTOSS_SENTRY ||	 
				unitType == Units.PROTOSS_STALKER ||	  
				unitType == Units.PROTOSS_TEMPEST ||	 	 
				unitType == Units.PROTOSS_VOIDRAY ||	 	 
				unitType == Units.PROTOSS_WARP_PRISM ||	 
				unitType == Units.PROTOSS_WARP_PRISM_PHASING ||	 
				unitType == Units.PROTOSS_ZEALOT ||	 	 
				unitType == Units.TERRAN_BANSHEE ||	 	 
				unitType == Units.TERRAN_BATTLECRUISER ||	 	 
				unitType == Units.TERRAN_CYCLONE ||	 	 
				unitType == Units.TERRAN_GHOST ||	 	 
				unitType == Units.TERRAN_HELLION ||	 
				unitType == Units.TERRAN_HELLION_TANK ||	 	 
				unitType == Units.TERRAN_LIBERATOR ||	 
				unitType == Units.TERRAN_LIBERATOR_AG ||	 
				unitType == Units.TERRAN_MARAUDER ||	 
				unitType == Units.TERRAN_MARINE ||	 
				unitType == Units.TERRAN_MEDIVAC ||	 	  	 	 
				unitType == Units.TERRAN_POINT_DEFENSE_DRONE ||	 
				unitType == Units.TERRAN_RAVEN ||	 
				unitType == Units.TERRAN_REAPER ||	 	 	 
				unitType == Units.TERRAN_SIEGE_TANK ||	 
				unitType == Units.TERRAN_SIEGE_TANK_SIEGED ||	  
				unitType == Units.TERRAN_THOR ||	 
				unitType == Units.TERRAN_THOR_AP ||	 
				unitType == Units.TERRAN_VIKING_ASSAULT ||	 
				unitType == Units.TERRAN_VIKING_FIGHTER ||	 
				unitType == Units.TERRAN_WIDOWMINE ||	 
				unitType == Units.TERRAN_WIDOWMINE_BURROWED ||	 
				unitType == Units.ZERG_BANELING ||	 
				unitType == Units.ZERG_BANELING_BURROWED ||	 
				unitType == Units.ZERG_BANELING_COCOON ||	  
				unitType == Units.ZERG_BROODLING ||	 
				unitType == Units.ZERG_BROODLORD ||	 
				unitType == Units.ZERG_BROODLORD_COCOON ||	 	 
				unitType == Units.ZERG_CORRUPTOR ||	 	  	 	 
				unitType == Units.ZERG_HYDRALISK ||	 
				unitType == Units.ZERG_HYDRALISK_BURROWED ||	 
				unitType == Units.ZERG_INFESTED_TERRANS_EGG ||	 
				unitType == Units.ZERG_INFESTOR ||	 
				unitType == Units.ZERG_INFESTOR_BURROWED ||	 
				unitType == Units.ZERG_INFESTOR_TERRAN ||	 
				unitType == Units.ZERG_LOCUS_TMP ||	 
				unitType == Units.ZERG_LOCUS_TMP_FLYING ||	 	 
				unitType == Units.ZERG_LURKER_MP ||	 
				unitType == Units.ZERG_LURKER_MP_BURROWED ||	 
				unitType == Units.ZERG_LURKER_MP_EGG ||	 
				unitType == Units.ZERG_MUTALISK ||	 	 	 
				unitType == Units.ZERG_QUEEN ||	 
				unitType == Units.ZERG_QUEEN_BURROWED ||	 
				unitType == Units.ZERG_RAVAGER ||	 
				unitType == Units.ZERG_RAVAGER_COCOON ||	 
				unitType == Units.ZERG_ROACH ||	 
				unitType == Units.ZERG_ROACH_BURROWED ||	 
				unitType == Units.ZERG_SWARM_HOST_BURROWED_MP ||	 
				unitType == Units.ZERG_SWARM_HOST_MP ||	 
				unitType == Units.ZERG_TRANSPORT_OVERLORD_COCOON ||	 
				unitType == Units.ZERG_ULTRALISK ||	 	 
				unitType == Units.ZERG_VIPER ||	 
				unitType == Units.ZERG_ZERGLING ||	 
				unitType == Units.ZERG_ZERGLING_BURROWED) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean canAttackAir(UnitType unitType) {
		if (unitType == Units.PROTOSS_ARCHON ||	 
				unitType == Units.PROTOSS_CARRIER ||	 	 
				unitType == Units.PROTOSS_HIGH_TEMPLAR ||	  
				unitType == Units.PROTOSS_INTERCEPTOR ||	  
				unitType == Units.PROTOSS_MOTHERSHIP ||	 
				unitType == Units.PROTOSS_PHOENIX ||	 	 
				unitType == Units.PROTOSS_SENTRY ||	 
				unitType == Units.PROTOSS_STALKER ||	  
				unitType == Units.PROTOSS_TEMPEST ||	 	 
				unitType == Units.PROTOSS_VOIDRAY ||
				unitType == Units.PROTOSS_PHOTON_CANNON ||
				unitType == Units.TERRAN_BATTLECRUISER ||	 	 
				unitType == Units.TERRAN_CYCLONE ||	 	 
				unitType == Units.TERRAN_GHOST ||	 	 	 	 
				unitType == Units.TERRAN_LIBERATOR ||	 
				unitType == Units.TERRAN_MARINE ||  
				unitType == Units.TERRAN_THOR ||	 
				unitType == Units.TERRAN_THOR_AP || 
				unitType == Units.TERRAN_VIKING_FIGHTER ||	 
				unitType == Units.TERRAN_WIDOWMINE ||	 
				unitType == Units.TERRAN_WIDOWMINE_BURROWED ||
				unitType == Units.TERRAN_AUTO_TURRET ||
				unitType == Units.TERRAN_MISSILE_TURRET ||
				unitType == Units.ZERG_CORRUPTOR ||	 	  	 	 
				unitType == Units.ZERG_HYDRALISK ||	 
				unitType == Units.ZERG_HYDRALISK_BURROWED ||	 
				unitType == Units.ZERG_INFESTED_TERRANS_EGG ||
				unitType == Units.ZERG_INFESTOR_TERRAN ||	 
				unitType == Units.ZERG_MUTALISK ||	 	 	 
				unitType == Units.ZERG_QUEEN ||	 
				unitType == Units.ZERG_QUEEN_BURROWED ||	 
				unitType == Units.ZERG_RAVAGER ||	 
				unitType == Units.ZERG_RAVAGER_COCOON ||	 	 
				unitType == Units.ZERG_VIPER ||	 
				unitType == Units.ZERG_SPORE_CRAWLER ||
				unitType == Units.ZERG_SPORE_CRAWLER_UPROOTED) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isRanged(UnitType unitType) {
		if (	unitType == Units.ZERG_BROODLORD ||	 
				unitType == Units.ZERG_BROODLORD_COCOON ||	 	 
				unitType == Units.ZERG_CORRUPTOR ||	 	  	 	 
				unitType == Units.ZERG_HYDRALISK ||	 
				unitType == Units.ZERG_HYDRALISK_BURROWED ||	 
				unitType == Units.ZERG_INFESTED_TERRANS_EGG ||	 
				unitType == Units.ZERG_INFESTOR_TERRAN ||	 
				unitType == Units.ZERG_LOCUS_TMP ||	 
				unitType == Units.ZERG_LOCUS_TMP_FLYING ||	 	 
				unitType == Units.ZERG_LURKER_MP ||	 
				unitType == Units.ZERG_LURKER_MP_BURROWED ||	 
				unitType == Units.ZERG_LURKER_MP_EGG ||	 
				unitType == Units.ZERG_MUTALISK ||	 	 
				unitType == Units.ZERG_QUEEN ||	 
				unitType == Units.ZERG_QUEEN_BURROWED ||	 
				unitType == Units.ZERG_RAVAGER ||	 
				unitType == Units.ZERG_RAVAGER_COCOON ||	 
				unitType == Units.ZERG_ROACH ||	 
				unitType == Units.ZERG_ROACH_BURROWED) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean canHoldCargo(UnitType unitType) {
		if (unitType == Units.PROTOSS_WARP_PRISM ||	 
				unitType == Units.PROTOSS_WARP_PRISM_PHASING ||
				unitType == Units.TERRAN_MEDIVAC ||
				unitType == Units.ZERG_OVERLORD_TRANSPORT ||
				unitType == Units.ZERG_TRANSPORT_OVERLORD_COCOON) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isGasStructure(UnitType unitType) {
		if (unitType == Units.TERRAN_REFINERY ||
			unitType == Units.ZERG_EXTRACTOR ||
			unitType == Units.PROTOSS_ASSIMILATOR) {
			return true;
		} else {
			return false;
		}
	}
	
	public static Set<UnitType> getProductionFacilitiesForRace(Race race) {
		Set<UnitType> productionFacilities = new HashSet<UnitType>();
		switch(race) {
		case ZERG:
			productionFacilities.add(Units.ZERG_HATCHERY);
			productionFacilities.add(Units.ZERG_LAIR);
			productionFacilities.add(Units.ZERG_HIVE);
			break;
		case TERRAN:
			productionFacilities.add(Units.TERRAN_BARRACKS);
			productionFacilities.add(Units.TERRAN_FACTORY);
			productionFacilities.add(Units.TERRAN_STARPORT);
			break;
		case PROTOSS:
			productionFacilities.add(Units.PROTOSS_GATEWAY);
			productionFacilities.add(Units.PROTOSS_WARP_GATE);
			productionFacilities.add(Units.PROTOSS_STARGATE);
			productionFacilities.add(Units.PROTOSS_ROBOTICS_FACILITY);
			break;
		default:
			break;
		}
		return productionFacilities;
	}
	
	public static UnitType getGasTypeFor(Race race) {
		switch(race) {
		case TERRAN:
			return Units.TERRAN_REFINERY;
		case PROTOSS:
			return Units.PROTOSS_ASSIMILATOR;
		case ZERG:
			return Units.ZERG_EXTRACTOR;
		default:
			return Units.INVALID;
		}
	}
	
	public static List<UnitType> getTownHallTypeFor(Race race) {
		List<UnitType> townhallTypes = new ArrayList<UnitType>();
		switch(race) {
		case TERRAN:
			townhallTypes = getTerranTownHallTypes();
			break;
		case PROTOSS:
			townhallTypes = getProtossTownHallTypes();
			break;
		case ZERG:
			townhallTypes = getZergTownHallTypes();
			break;
		default:
			townhallTypes.addAll(getTerranTownHallTypes());
			townhallTypes.addAll(getProtossTownHallTypes());
			townhallTypes.addAll(getZergTownHallTypes());
			break;
		}
		return townhallTypes;
	}
	
	private static List<UnitType> getTerranTownHallTypes() {
		List<UnitType> townhallTypes = new ArrayList<UnitType>();
		townhallTypes.add(Units.TERRAN_COMMAND_CENTER);
		townhallTypes.add(Units.TERRAN_COMMAND_CENTER_FLYING);
		townhallTypes.add(Units.TERRAN_ORBITAL_COMMAND);
		townhallTypes.add(Units.TERRAN_ORBITAL_COMMAND_FLYING);
		townhallTypes.add(Units.TERRAN_PLANETARY_FORTRESS);
		return townhallTypes;
	}
	
	private static List<UnitType> getProtossTownHallTypes() {
		List<UnitType> townhallTypes = new ArrayList<UnitType>();
		townhallTypes.add(Units.PROTOSS_NEXUS);
		return townhallTypes;
	}
	
	private static List<UnitType> getZergTownHallTypes() {
		List<UnitType> townhallTypes = new ArrayList<UnitType>();
		townhallTypes.add(Units.ZERG_HATCHERY);
		townhallTypes.add(Units.ZERG_LAIR);
		townhallTypes.add(Units.ZERG_HIVE);
		return townhallTypes;
	}
	
	
	
	public static UnitType getMorphingUnitCocoonType(UnitType unitType) {
		if (unitType == Units.ZERG_BANELING) {
			return Units.ZERG_BANELING_COCOON;
		} else if (unitType == Units.ZERG_BROODLORD) {
			return Units.ZERG_BROODLORD_COCOON;
		} else if (unitType == Units.ZERG_RAVAGER) {
			return Units.ZERG_RAVAGER_COCOON;
		} else if (unitType == Units.ZERG_OVERSEER) {
			return Units.ZERG_OVERLORD_COCOON;
		} else if (unitType == Units.ZERG_OVERLORD_TRANSPORT) {
			return Units.ZERG_TRANSPORT_OVERLORD_COCOON;
		} else {
			return Units.INVALID;
		}
	}
	
	public static UnitType getMorphsIntoType(UnitType unitType) {
		if (unitType == Units.ZERG_HATCHERY) {
			return Units.ZERG_LAIR;
		} else if (unitType == Units.ZERG_LAIR) {
			return Units.ZERG_HIVE;
		} else if (unitType == Units.ZERG_SPIRE) {
			return Units.ZERG_GREATER_SPIRE;
		} else {
			return Units.INVALID;
		}
	}
	
	public static boolean canCloak(UnitType unitType) {
		if (unitType == Units.PROTOSS_DARK_TEMPLAR ||	 
				unitType == Units.PROTOSS_OBSERVER ||	 	 
				unitType == Units.TERRAN_BANSHEE ||	 	 
				unitType == Units.TERRAN_GHOST ||	 	 
				unitType == Units.TERRAN_WIDOWMINE ||	 
				unitType == Units.TERRAN_WIDOWMINE_BURROWED ||
				unitType == Units.ZERG_INFESTOR ||	 
				unitType == Units.ZERG_INFESTOR_BURROWED ||	  
				unitType == Units.ZERG_LURKER_MP ||	 
				unitType == Units.ZERG_LURKER_MP_BURROWED ||	 
				unitType == Units.ZERG_LURKER_MP_EGG) {
			return true;
		} else {
			return false;
		}
	}
}
