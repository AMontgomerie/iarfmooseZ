package iarfmoose;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.UnitTypeData;
import com.github.ocraft.s2client.protocol.data.Weapon;
import com.github.ocraft.s2client.protocol.data.Weapon.TargetType;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;

public class Overlord extends ArmyUnit {
	
	private static final float OVERLORD_DISTANCE = 5;
	private static final float RANGE_BUFFER = 3;
	private static final long WAIT_LOOPS = 672; //22.4 loops per second * 30 seconds
	
	Base assignedPosition;
	long lastEscapeLoop;
	
	public Overlord(S2Agent bot, UnitInPool unitInPool) {
		super(bot, unitInPool);
		assignedPosition = new Base();
		lastEscapeLoop = 0;
	}
	
	@Override
	public void attack(Point2d target) {}
	
	@Override
	public void attack(UnitInPool target) {}
	
	public Base getAssignedPosition() {
		return assignedPosition;
	}
	
	public void assignPosition(Base position) {
		this.assignedPosition = position;
	}
	
	public boolean hasBeenAssignedAPosition() {
		//Point.of(0, 0) represents an unassigned value
		return assignedPosition.getExpansionLocation().getX() != 0 && 
				assignedPosition.getExpansionLocation().getY() != 0;
	}
	
	public void watchPosition() throws UnitNotFoundException {
		if (!dodgeNearbyEffects() && 
				!runAway() && 
				!isCloseToAssignedPosition()) {
			move(assignedPosition.getExpansionLocation().toPoint2d());
		}
	}
		
	public void generateCreep() throws UnitNotFoundException {
		UnitInPool overlord = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (!alreadyIssued(Abilities.BEHAVIOR_GENERATE_CREEP_ON)) {
			bot.actions().unitCommand(overlord.unit(), Abilities.BEHAVIOR_GENERATE_CREEP_ON, false);
		}
	}
	
	public void morphToOverseer() throws UnitNotFoundException {
		UnitInPool overlord = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (!alreadyIssued(Abilities.MORPH_OVERSEER)) {
			bot.actions().unitCommand(overlord.unit(), Abilities.MORPH_OVERSEER, false);
		}
	}
	
	public void morphToTransport() throws UnitNotFoundException {
		UnitInPool overlord = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		if (!alreadyIssued(Abilities.MORPH_OVERLORD_TRANSPORT)) {
			bot.actions().unitCommand(overlord.unit(), Abilities.MORPH_OVERLORD_TRANSPORT, false);
		}
	}
	
	private boolean runAway() throws UnitNotFoundException {
		if (bot.observation().getGameLoop() - lastEscapeLoop < WAIT_LOOPS) {
			return true;
		} else {
			UnitInPool overlord = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
			UnitInPool enemy = getClosestAntiAirEnemy();
			if (enemy != null) {
				Weapon airWeapon = getAirWeapon(enemy.unit().getType());
				if (airWeapon != null) {
					Point overlordPosition = overlord.unit().getPosition();
					Point enemyPosition = enemy.unit().getPosition();
					float range = airWeapon.getRange();
					if (overlordPosition.distance(enemyPosition) <= range + RANGE_BUFFER) {
						move(bot.observation().getStartLocation().toPoint2d());
						lastEscapeLoop = bot.observation().getGameLoop();
						return true;
					}
				}
			}
			return false;
		}
	}
	
	private Weapon getAirWeapon(UnitType unitType) {
		UnitTypeData unitData = bot.observation().getUnitTypeData(false).get(unitType);
		for (Weapon weapon : unitData.getWeapons()) {
			if(weapon.getTargetType() != TargetType.GROUND) {
				return weapon;
			}
		}
		return null;
	}
	
	private boolean isCloseToAssignedPosition() throws UnitNotFoundException {
		if (!hasBeenAssignedAPosition()) {
			return true;
		}
		UnitInPool overlord = getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
		Point overlordPosition = overlord.unit().getPosition();
		return overlordPosition.distance(assignedPosition.getExpansionLocation()) < OVERLORD_DISTANCE;
	}
}
