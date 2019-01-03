package iarfmoose;

import java.util.ArrayList;
import java.util.List;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class ThreatStateHandler {
	
	private List<ThreatState> threatStates;
	private ThreatState currentThreatState;
	
	public ThreatStateHandler() {
		threatStates = new ArrayList<ThreatState>();
		initialiseThreatStates();
		currentThreatState = null;
	}
	
	public ThreatState getCurrentThreatState() {
		return currentThreatState;
	}
	
	public void update(GameState currentGameState) {
		if (currentThreatState != null) {
			if(currentThreatState.getTimeWindow().getGamePhase() != currentGameState.getCurrentGamePhase()) {
				currentThreatState = null;
			}
		}
	}
	
	public boolean isImmediateThreat(GameState currentGameState) {
		PlayerState enemyState = getCurrentEnemyState(currentGameState);
		if (enemyState == null) {
			return false;
		}
		for (ThreatState threatState : threatStates) {
			if (!threatState.getResponse().alreadyResponded() && 
					enemyRaceMatchesThreatState(enemyState.getRace(), threatState) &&
					currentTimeIsWithinThreatWindow(currentGameState, threatState.getTimeWindow())) {
				if (sufficientMatches(enemyState, threatState)) {
					currentThreatState = threatState;
					System.out.println(threatState.getName() + " detected");
					return true;
				}
			} 
		}
		return false;	
	}
	
	private PlayerState getCurrentEnemyState(GameState currentGameState) {
		return currentGameState.findPlayerState(Alliance.ENEMY).orElse(new PlayerState());
	}
	
	private boolean enemyRaceMatchesThreatState(Race enemyRace, ThreatState threatState) {
		for (Race race : threatState.getRaces()) {
			if (enemyRace == race) {
				return true;
			}
		}
		return false;
	}
	
	private boolean currentTimeIsWithinThreatWindow(GameState currentGameState, TimeWindow threatWindow) {
		if (currentGameState.getCurrentGamePhase() == threatWindow.getGamePhase() &&
				currentGameState.getGameLoop() >= threatWindow.getStartGameLoop() &&
				currentGameState.getGameLoop() <= threatWindow.getEndGameLoop()) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean sufficientMatches(PlayerState enemyState, ThreatState threatState) {
		int criteria = getPresentCriteria(threatState);
		if (criteria <= 0) {
			return false;
		}
		int matches = 0;
		if (enemyState.getArmySupply().isPresent() && threatState.getArmySupply().isPresent()) {
			if (enemyState.getArmySupply().get() >= threatState.getArmySupply().get()) {
				matches++;
			}
		}
		if (enemyState.getWorkerCount().isPresent() && threatState.getWorkerCount().isPresent()) {
			if (enemyState.getWorkerCount().get() >= threatState.getWorkerCount().get()) {
				matches++;
			}
		}
		if (enemyState.getBaseCount().isPresent() && threatState.getBaseCount().isPresent()) {
			if (enemyState.getBaseCount().get() == threatState.getBaseCount().get()) {
				matches++;
			}
		}
		if (enemyState.getGasCount().isPresent() && threatState.getGasCount().isPresent()) {
			if (enemyState.getGasCount().get() == threatState.getGasCount().get()) {
				matches++;
			}
		}
		if (threatState.getProductionFacilityCount().isPresent()) {
			if (enemyState.getProductionFacilityCount() >= threatState.getProductionFacilityCount().get()) {
				matches++;
			}
		}
		return matches == criteria;
	}
	
	private int getPresentCriteria(ThreatState threatState) {
		int criteria = 0;
		if (threatState.getArmySupply().isPresent()) {
			criteria++;
		}
		if (threatState.getBaseCount().isPresent()) {
			criteria++;
		}
		if (threatState.getGasCount().isPresent()) {
			criteria++;
		}
		if (threatState.getProductionFacilityCount().isPresent()) {
			criteria++;
		}
		if (threatState.getWorkerCount().isPresent()) {
			criteria++;
		}
		return criteria;
	}
		
	private void initialiseThreatStates() {		
		ThreatState massZealot = new ThreatState(
				"mass zealots", 
				new TimeWindow(GamePhase.EARLY, 50, 180), 
				new ThreatResponse(true, false, false)
				);
		massZealot.addRace(Race.PROTOSS);
		massZealot.setProductionFacilityCount(3);
		threatStates.add(massZealot);
				
		ThreatState massMarine = new ThreatState(
				"mass marines", 
				new TimeWindow(GamePhase.EARLY, 50, 180), 
				new ThreatResponse(true, false, false)
				);
		massMarine.addRace(Race.TERRAN);
		massMarine.setProductionFacilityCount(3);
		threatStates.add(massMarine);
		
	}		
}
