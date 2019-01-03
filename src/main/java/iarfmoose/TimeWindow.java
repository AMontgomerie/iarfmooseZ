package iarfmoose;

public class TimeWindow {
	
	private GamePhase phase;
	private int startTimeInSeconds;
	private int endTimeInSeconds;
	
	public TimeWindow(GamePhase phase, int startTimeInSeconds, int endTimeInSeconds) {
		this.phase = phase;
		this.startTimeInSeconds = startTimeInSeconds;
		this.endTimeInSeconds = endTimeInSeconds;
	}
	
	public GamePhase getGamePhase() {
		return phase;
	}
	
	public int getStartTimeInSeconds() {
		return startTimeInSeconds;
	}
	
	public int getEndTimeInSeconds() {
		return endTimeInSeconds;
	}
	
	public long getStartGameLoop() {
		return (long) (startTimeInSeconds * 22.4);
	}
	
	public long getEndGameLoop() {
		return (long) (endTimeInSeconds * 22.4);
	}
}
