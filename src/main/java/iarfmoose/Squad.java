package iarfmoose;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Unit;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class Squad extends UnitGroup{
	
	Point2d currentTarget;
	
	public Squad(S2Agent bot) {
		this.bot = bot;
		this.members = new ArrayList<ActiveUnit>();
		this.currentTarget = Point2d.of(0, 0);
	}
	
	public Squad(S2Agent bot, List<ActiveUnit> units) {
		this.bot = bot;
		this.members = units;
		this.currentTarget = getCentreOfGroup().toPoint2d();
	}
	
	@Override
	public void addMember(ActiveUnit unit) {
		members.add(unit);
		//bot.actions().unitCommand(unit.getUnitInPool().unit(), Abilities.ATTACK, currentTarget, false);
	}
	
	public void addUnits(List<ActiveUnit> units) {
		members.addAll(units);
	}
	
	public void addUnits(Squad squad) {
		members.addAll(squad.getMembers());
	}
	
	public List<ActiveUnit> getMembers() {
		return members;
	}
	
	public void attack(Point2d target) {
		currentTarget = target;
		if (!orderAlreadyIssued(Abilities.ATTACK, target) && members.size() > 0) {
			for (Iterator<ActiveUnit> unitIterator = members.iterator(); unitIterator.hasNext();) {
				ActiveUnit member = unitIterator.next();
				try {
					ArmyUnit armyUnit = (ArmyUnit) member;
					armyUnit.attack(target);
				//if for some reason there are units in the squad that are not our ArmyUnits, then just remove them
				} catch (ClassCastException e) {
					unitIterator.remove();
				} catch (UnitNotFoundException e) {
					unitIterator.remove();
				}
			}
		}
	}
	
	public void returnHome() {
		UnitInPool closestBase = findClosestBase();
		if (closestBase != null && members.size() > 0) {
			bot.actions().unitCommand(
					removeWrapper(members), 
					Abilities.MOVE, 
					closestBase.unit().getPosition().toPoint2d(), 
					false);
		}
	}
	
	public Point getHomeLocation() {
		UnitInPool base = findClosestBase();
		if (base != null) {
			return base.unit().getPosition();
		} else 
			return bot.observation().getStartLocation();
	}
	
	private boolean orderAlreadyIssued(Ability ability, Point2d target) {
		if (members.isEmpty()) {
			return false;
		}
		try {
			UnitInPool firstUnit = members.get(0).getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
			if (firstUnit.unit().getOrders().isEmpty()) {
				return false;
			}
			UnitOrder firstOrder = firstUnit.unit().getOrders().get(0);
			Ability currentAbility = firstOrder.getAbility();
			Point2d currentTarget = firstOrder.getTargetedWorldSpacePosition().orElse(Point.of(0, 0)).toPoint2d();
			if (currentAbility == ability && currentTarget == target) {
				return true;
			} else {
				return false;
			}
		} catch (UnitNotFoundException e) {
			return false;
		}
	}
		
	private List<Unit> removeWrapper(List<ActiveUnit> units) {
		List<Unit> outputUnits = new ArrayList<Unit>();
		for (Iterator<ActiveUnit> unitIterator = members.iterator(); unitIterator.hasNext();) {
			ActiveUnit activeUnit = unitIterator.next();
			try {
				UnitInPool unitInPool = activeUnit.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
				outputUnits.add(unitInPool.unit());
			} catch (UnitNotFoundException e) {
				unitIterator.remove();
			}
		}
		return outputUnits;
	}
		
	private UnitInPool findClosestBase() {
		UnitInPool closestBase = null;
		Point currentLocation = getCentreOfGroup();
		for (UnitInPool unitInPool : bot.observation().getUnits(
				Alliance.SELF, unitInPool -> UnitData.isTownHall(unitInPool.unit().getType()))) {
			Point baseLocation = unitInPool.unit().getPosition();
			if (closestBase == null || 
					currentLocation.distance(baseLocation) < 
					currentLocation.distance(closestBase.unit().getPosition())) {
				closestBase = unitInPool;
			}
		}
		return closestBase;
	}
} 
