package org.concord.sensor.server.data;

import org.concord.sensor.ExperimentConfig;

public interface DataSink {
	public void setLastPolledData(ExperimentConfig config, float[] data);
	public float[] getLastPolledData();
	
	public void appendCollectedData(int numSamples, float[] data);
	public void startNewCollection(ExperimentConfig config);
}
