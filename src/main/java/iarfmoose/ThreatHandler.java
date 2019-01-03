package iarfmoose;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;

public class ThreatHandler {
	
	private static final float GROUP_RADIUS = 10;
	private static final float EARLY_GAME_THREAT_RADIUS = 50; //used for reacting to proxies
	private static final float THREAT_RADIUS = 30;
	
	S2Agent bot;
	List<Threat> threats;
	
	public ThreatHandler(S2Agent bot) {
		this.bot = bot;
		threats = new ArrayList<Threat>();
	}
	
	public List<Threat> getThreats() {
		return threats;
	}
	
	public float getThreatRadius(GamePhase gamePhase) {
		if (gamePhase == GamePhase.EARLY) {
			return EARLY_GAME_THREAT_RADIUS;
		} else {
			return THREAT_RADIUS;
		}
	}
	
	public void updateThreats(Collection<UnitInPool> enemyUnits) {
		threats.clear();
		List<UnitInPool> unallocatedUnits = new ArrayList<UnitInPool>(enemyUnits);
		while (unallocatedUnits.size() > 0) {
			unallocatedUnits = allocateUnitsToThreat(unallocatedUnits);
		}
	}
		
	private List<UnitInPool> allocateUnitsToThreat(List<UnitInPool> unallocatedUnits) {
		List<UnitInPool> threatUnits = new ArrayList<UnitInPool>();
		threatUnits.add(unallocatedUnits.get(0));
		unallocatedUnits.remove(0);
		Threat newThreat = new Threat(bot, threatUnits);
		Point2d centre = newThreat.getCentreOfGroup().toPoint2d();
		for (Iterator<UnitInPool> unitIterator = unallocatedUnits.iterator(); unitIterator.hasNext();) {
			UnitInPool enemy = unitIterator.next();
			Point unitPosition = enemy.unit().getPosition();
			if (unitPosition.toPoint2d().distance(centre) < GROUP_RADIUS) {
				newThreat.addMember(enemy);
				unitIterator.remove();
			}
		}
		threats.add(newThreat);
		return unallocatedUnits;
	}
}
