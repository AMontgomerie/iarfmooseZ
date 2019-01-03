package iarfmoose;

import java.nio.file.Path;
import java.nio.file.Paths;
import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.Race;

public class IarfMoose {
    private static class Bot extends S2Agent {
    	
    	private ProductionManager productionManager;
    	private OverlordManager overlordManager;
    	private ArmyManager armyManager;
    	private QueenManager queenManager;
    	private BaseLocator baseLocator;
    	private InformationManager informationManager;
    	
    	@Override
    	public void onGameStart() {
    		baseLocator = new BaseLocator(this);
    		productionManager = new ProductionManager(this, baseLocator);
    		overlordManager = new OverlordManager(this, baseLocator);
    		armyManager = new ArmyManager(this, baseLocator);
    		queenManager = new QueenManager(this, baseLocator);
    		informationManager = new InformationManager(this, baseLocator);
    	}
    	
        @Override
        public void onStep() {
        	GameState currentGameState = informationManager.getCurrentGameState();
        	productionManager.onStep(currentGameState);
        	armyManager.onStep(currentGameState);
        	queenManager.onStep(currentGameState);
        	overlordManager.onStep();
        	informationManager.onStep();
        } 
                
        @Override
        public void onUnitCreated(UnitInPool unitInPool) {
        	productionManager.onUnitCreated(unitInPool);
        	overlordManager.onUnitCreated(unitInPool);
        	armyManager.onUnitCreated(unitInPool);
        	queenManager.onUnitCreated(unitInPool);
        	informationManager.onUnitCreated(unitInPool);
        }
        
        @Override
        public void onUnitDestroyed(UnitInPool unitInPool) {
        	productionManager.onUnitDestroyed(unitInPool);
        	overlordManager.onUnitDestroyed(unitInPool);
        	armyManager.onUnitDestroyed(unitInPool);
        	queenManager.onUnitDestroyed(unitInPool);
        	informationManager.onUnitDestroyed(unitInPool);
        }
        
        @Override
        public void onUnitIdle(UnitInPool unitInPool) {
        	productionManager.onUnitIdle(unitInPool);
        }
        
        @Override
        public void onUnitEnterVision(UnitInPool unitInPool) {
        	informationManager.onUnitEnterVision(unitInPool);
        }
                
        @Override
        public void onUpgradeCompleted(Upgrade upgrade) {
        	informationManager.onUpgradeCompleted(upgrade);
        }
    }

    //ladder config

    public static void main(String[] args) {
        Bot bot = new Bot();
        S2Coordinator s2Coordinator = S2Coordinator.setup()
                .loadLadderSettings(args)
                .setParticipants(S2Coordinator.createParticipant(Race.ZERG, bot))
                .connectToLadder()
                .joinGame();
        while (s2Coordinator.update()) {
        }
        s2Coordinator.quit();
    }


    //local debug
    /*
    public static void main(String[] args) {
        Bot bot = new Bot();
        Path localMapPath = Paths.get("C:/Program Files (x86)/StarCraft II/Maps/KairosJunctionLE.SC2Map");
		S2Coordinator s2Coordinator = S2Coordinator.setup()
                .loadSettings(args)
                .setParticipants(
                        S2Coordinator.createParticipant(Race.ZERG, bot),
                        S2Coordinator.createComputer(Race.RANDOM, Difficulty.VERY_HARD))
                .launchStarcraft()
                .startGame(LocalMap.of(localMapPath));

        while (s2Coordinator.update()) {
        }
        s2Coordinator.quit();
    }
    */
}