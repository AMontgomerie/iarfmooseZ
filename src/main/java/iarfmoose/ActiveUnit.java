package iarfmoose;

import java.util.Optional;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Tag;

public class ActiveUnit {
	
	S2Agent bot;
	private Tag unitTag;
	private UnitType lastKnownType;
	private Point2d lastKnownLocation;
	
	public ActiveUnit (S2Agent bot, UnitInPool unitInPool) {
		this.bot = bot;
		this.unitTag = unitInPool.getTag();
		this.lastKnownType = unitInPool.unit().getType();
		this.lastKnownLocation = unitInPool.unit().getPosition().toPoint2d();
	}
	
	public Tag getTag() {
		return unitTag;
	}
	
	public Optional<UnitInPool> getUnitInPool() {
		UnitInPool unit = bot.observation().getUnit(unitTag);
		if (unit != null) {
			return Optional.of(unit);
		} else {
			return Optional.empty();
		}
	}
	
	public UnitType getLastKnownType() {
		updateType();
		return lastKnownType;
	}
	
	public Point2d getLastKnownLocation() {
		updateLocation();
		return lastKnownLocation;
	}
	
	private void updateType() {
		UnitInPool unitInPool = bot.observation().getUnit(unitTag);
		if (unitInPool != null) {
			lastKnownType = unitInPool.unit().getType();
		}
	}
	
	private void updateLocation() {
		UnitInPool unitInPool = bot.observation().getUnit(unitTag);
		if (unitInPool != null) {
			lastKnownLocation = unitInPool.unit().getPosition().toPoint2d();
		}
	}
}
