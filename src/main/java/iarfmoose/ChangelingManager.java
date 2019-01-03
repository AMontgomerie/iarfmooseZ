package iarfmoose;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class ChangelingManager extends ActiveManager {

	private List<ArmyUnit> changelings;
	
	public ChangelingManager(S2Agent bot, BaseLocator baseLocator) {
		super(bot, baseLocator);
		changelings = new ArrayList<ArmyUnit>();
	}
	
	@Override
	public void onStep() {
		moveChangelings();
	}

	@Override
	public void onUnitCreated(UnitInPool unitInPool) {
		UnitType unitType = unitInPool.unit().getType();
		if (UnitData.isChangeling(unitType)) {
			changelings.add(new ArmyUnit(bot, unitInPool));
		}
	}

	@Override
	public void onUnitDestroyed(UnitInPool unitInPool) {
		UnitType unitType = unitInPool.unit().getType();
		if (UnitData.isChangeling(unitType)) {
			removeChangeling(unitInPool);
		}
	}
	
	private void moveChangelings() {
		for (Iterator<ArmyUnit> changelingIterator = changelings.iterator(); changelingIterator.hasNext();) {
			ArmyUnit changeling = changelingIterator.next();
			if (changeling.isIdle()) {
				for (UnitInPool enemyBase : bot.observation().getUnits(
						Alliance.ENEMY, 
						unitInPool -> 
						UnitData.isTownHall(unitInPool.unit().getType()))) {
					Point2d enemyBasePosition = enemyBase.unit().getPosition().toPoint2d();
					try {
						changeling.shiftMove(enemyBasePosition);
					} catch (UnitNotFoundException e) {
						changelingIterator.remove();
					}
				}
			}
		}
	}
	
	private void removeChangeling(UnitInPool unitInPool) {
		for (Iterator<ArmyUnit> changelingIterator = changelings.iterator(); changelingIterator.hasNext();) {
			ArmyUnit changeling = changelingIterator.next();
			try {
				UnitInPool changelingInPool = changeling.getUnitInPool().orElseThrow(() -> new UnitNotFoundException());
				if (changelingInPool == unitInPool) {
					changelingIterator.remove();
				}
			} catch (UnitNotFoundException e) {
				changelingIterator.remove();
			}
		}
	}

}
