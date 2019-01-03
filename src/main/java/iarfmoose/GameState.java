package iarfmoose;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class GameState {
	private GamePhase gamePhase;
	private long gameLoop;
	private List<Threat> threats;
	private List<PlayerState> players;

	public GameState(GamePhase gamePhase, long gameLoop, List<Threat> threats, List<PlayerState> players) {
		this.gamePhase = gamePhase;
		this.gameLoop = gameLoop;
		this.threats = threats;
		this.players = players;
	}
			
	public GameState() {
		this.gamePhase = GamePhase.EARLY;
		this.gameLoop = 0;
		this.threats = new ArrayList<Threat>();
		this.players = new ArrayList<PlayerState>();
	}
	
	public GamePhase getCurrentGamePhase() {
		return gamePhase;
	}
	
	public long getGameLoop() {
		return gameLoop;
	}
	
	public Optional<PlayerState> findPlayerState(int id) {
		for (PlayerState player : players) {
			if (player.getPlayerID() == id) {
				return Optional.of(player);
			}
		}
		return Optional.empty();
	}
	
	public Optional<PlayerState> findPlayerState(Alliance alliance) {
		for (PlayerState player : players) {
			if (player.getAlliance() == alliance) {
				return Optional.of(player);
			}
		}
		return Optional.empty();
	}
	
	public List<PlayerState> getPlayerStates() {
		return players;
	}
		
	public List<Threat> getThreats() {
		return threats;
	}
	
	public boolean structureExists(UnitType structureType, Alliance alliance) {
		Set<UnitInPool> structures = getStructuresOf(alliance);
		UnitType morphType = UnitData.getMorphsIntoType(structureType); //in case the structure has morphed e.g. looking for lair when we have hive
		for (UnitInPool structure : structures) {
			if (structure.unit().getType() == structureType) {
				return true;
			} else if (structure.unit().getType() == morphType) {
				return true;
			}
		}
		return false;
	}
	
	public boolean completedStructureExists(UnitType structureType, Alliance alliance) {
		Set<UnitInPool> structures = getStructuresOf(alliance);
		UnitType morphType = UnitData.getMorphsIntoType(structureType); //in case the structure has morphed e.g. looking for lair when we have hive
		for (UnitInPool structure : structures) {
			if (structure.unit().getType() == structureType && 
					structure.unit().getBuildProgress() == 1.0) {
				return true;
			} else if (structure.unit().getType() == morphType && 
					structure.unit().getBuildProgress() == 1.0) {
				return true;
			}
		}
		return false;
	}
		
	private Set<UnitInPool> getStructuresOf(Alliance alliance) {
		Set<UnitInPool> structures = new HashSet<UnitInPool>();
		for (PlayerState player : players) {
			if (player.getAlliance() == alliance) {
				structures.addAll(player.getStructures());
			}
		}
		return structures;
	}
	
	public Optional<UnitInPool> findClosestEnemyStructureTo(UnitInPool ourUnit) {
		UnitInPool closestEnemy = null;
		Point ourUnitPosition = ourUnit.unit().getPosition();
		for (UnitInPool enemy : getStructuresOf(Alliance.ENEMY)) {
			if (closestEnemy == null || ourUnitPosition.distance(enemy.unit().getPosition()) <
					ourUnitPosition.distance(closestEnemy.unit().getPosition())) {
				closestEnemy = enemy;
			}
		}
		if (closestEnemy != null) {
			return Optional.of(closestEnemy);

		} else {
			return Optional.empty();
		}
	}
	
	public boolean upgradeCompleted(Upgrade upgrade, Alliance alliance) {
		if (alliance != Alliance.SELF) {
			return false;
		}
		PlayerState self = findPlayerState(Alliance.SELF).orElse(new PlayerState());
		for (Upgrade completedUpgrade : self.getCompletedUpgrades()) {
			if (completedUpgrade.getUpgradeId() == upgrade.getUpgradeId()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean upgradeStarted(Upgrade upgrade, Alliance alliance) {
		if (alliance != Alliance.SELF) {
			return false;
		}
		PlayerState self = findPlayerState(Alliance.SELF).orElse(new PlayerState());
		for (UnitInPool structure : self.getStructures()) {
			for (UnitOrder order : structure.unit().getOrders()) {
				if (order.getAbility() == AbilityData.getAbilityToResearchUpgrade(upgrade) ||
						AbilityData.areEquivalent(order.getAbility(), AbilityData.getAbilityToResearchUpgrade(upgrade))) {
					return true;
				}
			}
		}
		return false;
	}
}
