package linda.shm;

import linda.Callback;
import linda.Linda.eventMode;

public class Abonnement {

	private eventMode mode;
	private Callback callback;
	
	public Abonnement(eventMode evMode, Callback call) {
		setMode(evMode);
		setCallback(call);
	}

	public eventMode getMode() {
		return mode;
	}

	public void setMode(eventMode mode) {
		this.mode = mode;
	}

	public Callback getCallback() {
		return callback;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	
}
