package org.concord.sensor.server.data;

import java.util.ArrayList;
import java.util.Arrays;

import net.minidev.json.JSONObject;

import org.concord.sensor.ExperimentConfig;
import org.concord.sensor.SensorConfig;
import org.concord.sensor.device.impl.SensorConfigImpl;

public class DataCollection {
	private static final Object flag = new Object();
	private static int COUNT = 0;
	private final int id;
	private ArrayList<SensorConfig> sensorConfigs;
	private int samplesCollected = 0;
	private long lastCollectedTime = 0;
	private long lastPolledTimestamp = 0;
	private float[] lastPolledData;
	private ArrayList<ArrayList<Float>> sensorsData;
	private float lastSampleTime = 0;
	private float dt;

	public DataCollection(ExperimentConfig config) {
		synchronized (flag) {
			id = 110 + COUNT;
			COUNT += 10; // for now, don't support more than 9 sensors (plus 1 time) per device
		}
		updateSensors(config);
	}
	
	public synchronized void appendCollectedData(int numSamples, float[] data) {
		int idx = 0;
		for (int sensor = 0; sensor < sensorConfigs.size(); sensor++) {
			ArrayList<Float> sensorData = sensorsData.get(sensor);
			for (int sample = 0; sample < numSamples; sample++) {
				if (sensor == 0) {
					// dt-based time value
					sensorData.add(lastSampleTime);
					lastSampleTime += dt;
				} else {
					idx = sample*(sensorConfigs.size()-1) + (sensor-1);
					sensorData.add(data[idx]);
				}
			}
		}
		samplesCollected += numSamples;
		lastCollectedTime = System.currentTimeMillis();
	}
	
	public float[][] getCollectedData() {
		synchronized (sensorsData) {
			float[][] data = new float[sensorConfigs.size()][samplesCollected];
			for (int sensor = 0; sensor < sensorConfigs.size(); sensor++) {
				ArrayList<Float> sensorData = sensorsData.get(sensor);
				for (int idx = 0; idx < samplesCollected; idx++) {
					data[sensor][idx] = sensorData.get(idx);
				}
			}
			return data;
		}
	}
	
	public void setLastPolledData(float[] data) {
		for (int i = 0; i < data.length; i++) {
			lastPolledData[i+1] = data[i];
		}
		lastPolledTimestamp = System.currentTimeMillis();
	}

	public float[] getLastPolledData() {
		return lastPolledData;
	}

	public int getId() {
		return id;
	}
	
	public JSONObject getColumnInfo() {
		JSONObject allInfo = new JSONObject();
		for (int i = 0; i < sensorConfigs.size(); i++) {
			SensorConfig config = sensorConfigs.get(i);
			int sensorId = id + i;

			String unit = config.getUnit();
			if (unit == null) {
				unit = "";
			}
			
			JSONObject sensorInfo = new JSONObject();
			sensorInfo.put("id", ""+sensorId);
			sensorInfo.put("setID", ""+id);
			sensorInfo.put("position", i);
			sensorInfo.put("name", config.getName());
			sensorInfo.put("units", unit);
			sensorInfo.put("valueCount", samplesCollected);
			sensorInfo.put("valuesTimeStamp", lastCollectedTime);
			sensorInfo.put("liveValue", ""+lastPolledData[i]);
			sensorInfo.put("liveValueTimeStamp", lastPolledTimestamp);
			
			allInfo.put(""+sensorId, sensorInfo);
		}
		return allInfo;
	}

	public int getNumberOfSensors() {
		return sensorConfigs.size();
	}

	// return true if the set of sensors is the same, and no data has been collected
	public boolean isPristine(ExperimentConfig config) {
		if (samplesCollected == 0) {
			if (hasSameSensors(config)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasSameSensors(ExperimentConfig config) {
		SensorConfig[] sensors = config.getSensorConfigs();
		if (sensors.length != sensorConfigs.size()-1) {
			return false;
		}
		
		for (int i = 1; i < sensorConfigs.size(); i++) {
			SensorConfig newSensor = sensors[i-1];
			SensorConfig oldSensor = sensorConfigs.get(i);
			
			if (newSensor.getType() != oldSensor.getType()) { return false; }
			if (newSensor.getName() != oldSensor.getName()) { return false; }
		}
		return true;
	}

	public int getNumberOfSamples() {
		return samplesCollected;
	}

	public void updateSensors(ExperimentConfig config) {
		SensorConfig[] configs = config.getSensorConfigs();
		dt = config.getPeriod();
		sensorConfigs = new ArrayList<SensorConfig>();

		// fake time sensor so we can properly export time column values
		SensorConfigImpl timeConfig = new SensorConfigImpl();
		timeConfig.setUnit("s");
		timeConfig.setName("Time");
		sensorConfigs.add(timeConfig); // Time is always the 0-index sensor
		sensorConfigs.addAll(Arrays.asList(configs));
		sensorsData = new ArrayList<ArrayList<Float>>();
		
		lastPolledData = new float[sensorConfigs.size()];
		for (int i = 0; i < sensorConfigs.size(); i++) {
			sensorsData.add(new ArrayList<Float>());
			lastPolledData[i] = 0;
		}
	}
}
