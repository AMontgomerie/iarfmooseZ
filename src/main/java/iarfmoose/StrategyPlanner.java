package iarfmoose;

import java.util.NoSuchElementException;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class StrategyPlanner {
	private GameState currentGameState;
	private BuildOrderGenerator buildOrderGenerator;
	private ThreatStateHandler threatStateHandler;
	private GoalHandler goalHandler;
	private ProductionEmphasis currentEmphasis;
	
	public StrategyPlanner(S2Agent bot) {
		currentGameState = new GameState();
		buildOrderGenerator = new BuildOrderGenerator(bot);
		threatStateHandler = new ThreatStateHandler();
		goalHandler = new GoalHandler();
		currentEmphasis = ProductionEmphasis.ECONOMY;
	}
	
	public void onStep(GameState gameState) {
		currentGameState = gameState;
		threatStateHandler.update(gameState);
		if (threatStateHandler.getCurrentThreatState() != null) {
		}
		goalHandler.update(gameState, threatStateHandler.getCurrentThreatState());
	}

	public BuildOrder getNewBuildOrder() {	
		ThreatState immediateThreat = threatStateHandler.getCurrentThreatState();
		if (!threatsExist() && immediateThreat == null) {
			currentEmphasis = ProductionEmphasis.ECONOMY;
		} else {
			currentEmphasis = ProductionEmphasis.DEFENCE;
		}
		ProductionGoalState goalState = goalHandler.getNextGoalState(currentGameState, immediateThreat);
		if (enemyArmyIsTooBig() && currentEmphasis != ProductionEmphasis.DEFENCE) {
			currentEmphasis = ProductionEmphasis.ARMY;
		}
		BuildOrder buildOrder = buildOrderGenerator.generate(currentEmphasis, goalState, currentGameState);
		return buildOrder;
	}
		
	public BuildOrder getOpeningBuildOrder() {
		switch(getEnemyRace()) {
		case ZERG:
			//return getZvZOpening();
		case PROTOSS:
			//return getZvPOpening();
		case TERRAN:
			//return getZvTOpening();
		case RANDOM:
		default:
			return buildOrderGenerator.getZvROpening();
		}
	}
	
	public boolean newBuildOrderRequired() {
		if ((threatsExist() || 
				threatStateHandler.isImmediateThreat(currentGameState) || 
				enemyArmyIsTooBig()) &&
				(currentEmphasis != ProductionEmphasis.ARMY && 
				currentEmphasis != ProductionEmphasis.DEFENCE)) {
			return true;
		} else if (!threatsExist() && 
				threatStateHandler.getCurrentThreatState() == null &&				
				currentEmphasis == ProductionEmphasis.DEFENCE) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean threatsExist() {
		if (currentGameState.getThreats().size() == 0) {
			return false;
		} else {
			for (Threat threat : currentGameState.getThreats()) {
				if (threat.getTotalSupply() > 2 || threat.containsStructure()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean enemyArmyIsTooBig() {
		PlayerState self = currentGameState.findPlayerState(Alliance.SELF).orElse(new PlayerState());
		PlayerState enemy = currentGameState.findPlayerState(Alliance.ENEMY).orElse(new PlayerState());
		return self.getArmySupply().orElse((float) 0) * 1.7 < enemy.getArmySupply().orElse((float) 0);
	}
	
	private Race getEnemyRace() {
		try {
			PlayerState enemy = currentGameState.findPlayerState(Alliance.ENEMY).orElseThrow();
			return enemy.getRace();
		} catch (NoSuchElementException e) {
			return Race.RANDOM;
		}
	}
			
}
