package iarfmoose;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.spatial.Point;

public abstract class UnitGroup {

	List<ActiveUnit> members;
	S2Agent bot;
	
	public void addMember(ActiveUnit unit) {
		members.add(unit);
	}
	
	public void addMember(UnitInPool unit) {
		members.add(new ActiveUnit(bot, unit));
	}
	
	public void removeMember(ActiveUnit unit) {
		members.remove(unit);
	}
	
	public void removeMember(UnitInPool unitInPool) {
		for (Iterator<ActiveUnit> memberIterator = members.iterator(); memberIterator.hasNext();) {
			ActiveUnit member = memberIterator.next();
			if (member.getUnitInPool().isPresent() &&
					member.getUnitInPool().get() == unitInPool) {
				memberIterator.remove();
			}
		}
	}
	
	public float getTotalSupply() {
		return calculateSupply();
	}
	
	public Point getCentreOfGroup() {
		return calculateCentre();
	}
		
	private float calculateSupply() {
		float supply = 0;
		for (ActiveUnit member : members) {
			UnitType unitType = member.getLastKnownType();
			supply += bot.observation().getUnitTypeData(false).get(unitType).getFoodRequired().orElse((float) 0);
		}
		return supply;
	}
	
	private Point calculateCentre() {
		float x = 0;
		float y = 0;
		for (ActiveUnit activeUnit : members) {
			Optional<UnitInPool> unitInPool = activeUnit.getUnitInPool();
			if (unitInPool.isPresent()) {
				x += unitInPool.get().unit().getPosition().getX();
				y += unitInPool.get().unit().getPosition().getY();
			} else {
				x += activeUnit.getLastKnownLocation().getX();
				y += activeUnit.getLastKnownLocation().getY();
			}
		}
		x /= members.size();
		y /= members.size();
		return Point.of(x, y);
	}
}
