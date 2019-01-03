package iarfmoose;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Ability;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.unit.Unit;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

public class LarvaeManager extends DataManager {

	HashSet<UnitInPool> larvae;
	List<Point> expansionLocations;
	
	public LarvaeManager(S2Agent bot) {
		super(bot);
		larvae = new HashSet<UnitInPool>();
	}
		
	@Override
	public void onUnitCreated(UnitInPool unitInPool) {
		Unit unit = unitInPool.unit();
		if (unit.getType() == Units.ZERG_LARVA) {
			larvae.add(unitInPool);
		}
	}

	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		larvae.remove(unitInPool);
	}
		
	//remove any eggs we find and return an idle larva if we have one, otherwise null
	public UnitInPool findIdleLarva() {
		for (Iterator<UnitInPool> larvaeIterator = larvae.iterator(); larvaeIterator.hasNext();) {
			UnitInPool larva = larvaeIterator.next();
			if (larva.unit().getType() != Units.ZERG_LARVA) {
				larvaeIterator.remove();
			} else {
				return larva;
			}
		}
		return null;
	}
	
	//check our larvae for a morphing egg, and if we find one check if it's our next production item
	public boolean checkForEggMorph(ProductionItem item) {	
		boolean eggMatchesProductionItem = false;
		
		for (Iterator<UnitInPool> larvaeIterator = larvae.iterator(); larvaeIterator.hasNext();) {
			UnitInPool larva = larvaeIterator.next();
			if (larva.unit().getType() != Units.ZERG_LARVA) {
				if (larva.unit().getType() == Units.ZERG_EGG) {
					eggMatchesProductionItem = newEggIsNextItem(larva.unit(), item.getAbility());
				}
				larvaeIterator.remove();
			}
		}
		return eggMatchesProductionItem;
	}
	
	private boolean newEggIsNextItem(Unit egg, Ability ability) {
 		List<UnitOrder> orders = egg.getOrders();
 		for (UnitOrder order : orders) {
 			if (order.getAbility() == ability) {
 				return true;
 			}
 		}
 		return false;
	}
}
