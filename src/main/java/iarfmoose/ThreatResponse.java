package iarfmoose;

public class ThreatResponse {
	
	private boolean responded;
	private boolean staticDefenceRequired;
	private boolean detectionRequired;
	private boolean antiAirRequired;
	
	public ThreatResponse (boolean staticDefence, boolean detection, boolean antiAir) {
		responded = false;
		staticDefenceRequired = staticDefence;
		detectionRequired = detection;
		antiAirRequired = antiAir;
	}
	
	public boolean staticDefenceIsRequired() {
		return staticDefenceRequired;
	}
	
	public boolean detectionIsRequired() {
		return detectionRequired;
	}
	
	public boolean antiAirIsRequired() {
		return antiAirRequired;
	}
	
	public boolean alreadyResponded() {
		return responded;
	}
	
	public void respond() {
		responded = true;
	}
}
