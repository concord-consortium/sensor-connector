package org.concord.sensor.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.minidev.json.JSONObject;

import org.concord.sensor.ExperimentConfig;
import org.concord.sensor.server.data.DataCollection;
import org.concord.sensor.server.data.DataSink;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.continuent.tungsten.fsm.core.FiniteStateException;

public class SensorHandler extends AbstractHandler implements DataSink {
	private SensorStateManager stateManager;
	private final String sessionId;
	public SensorHandler() {
		sessionId = UUID.randomUUID().toString();
		try {
			stateManager = new SensorStateManager(this);
		} catch (FiniteStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		JSONObject json = new JSONObject();

		if (target.equals("/") || target.equals("/status")) {
			json.put("sessionID", sessionId);
			json.put("sessionDesc", "");
			json.put("requestTimeStamp", System.currentTimeMillis());
			json.put("columnListTimeStamp", lastCollectionAdded);
			appendColumnsInfo(json);
			appendCollectionInfo(json);
		} else if (target.equals("/connect")) {
			stateManager.connect();
		} else if (target.equals("/disconnect")) {
			stateManager.disconnect();
		} else if (target.equals("/control/start")) {
			stateManager.start();
		} else if (target.equals("/control/stop")) {
			stateManager.stop();
		} else {
			Pattern p = Pattern.compile("/columns/(\\d+)");
			Matcher m = p.matcher(target);
			if (m.matches()) {
				appendColumnValues(json, Integer.parseInt(m.group(1)));
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}
		
		json.put("currentState", stateManager.currentState().getName());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "GET");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(json.toJSONString());
	}

	private void appendColumnValues(JSONObject json, int column) {
		for (DataCollection c : collections) {
			int sensorNum = column - c.getId();
			if (sensorNum >= 0 && sensorNum < c.getNumberOfSensors()) {
				// we found the right collection.
				float[][] data = c.getCollectedData();
				ArrayList<Float> values = new ArrayList<Float>();
				for (int i = 0; i < data[sensorNum].length; i++) {
					values.add(data[sensorNum][i]);
				}
				json.put("values", values);
				return;
			}
		}
	}

	private void appendColumnsInfo(JSONObject json) {
		JSONObject sInfo = new JSONObject();
		JSONObject cInfo = new JSONObject();
		for (int i = 0; i < collections.size(); i++) {
			DataCollection c = collections.get(i);
			JSONObject setInfo = new JSONObject();
			setInfo.put("name", "Run " + i);
			
			JSONObject columnInfo = c.getColumnInfo();
			cInfo.merge(columnInfo);
			
			ArrayList<Integer> colIDs = new ArrayList<Integer>();
			for (int j = 0; j < c.getNumberOfSensors(); j++) {
				colIDs.add(c.getId() + j);
			}
			setInfo.put("colIDs", colIDs);
			sInfo.put(""+c.getId(), setInfo);
		}
		json.put("columns", cInfo);
		json.put("sets", sInfo);
	}

	private void appendCollectionInfo(JSONObject json) {
		JSONObject cInfo = new JSONObject();
		cInfo.put("isCollecting", stateManager.currentState().getName().equals("CONNECTED:COLLECTING"));
		cInfo.put("canControl", true);
		json.put("collection", cInfo);
	}

	// DataSink support
	private ArrayList<DataCollection> collections = new ArrayList<DataCollection>();
	private long lastCollectionAdded;
	
	@Override
	public void setLastPolledData(ExperimentConfig config, float[] data) {
		// If the sensors have changed from the current collection, start a new collection
		// and discard the current one if no data has been collected
		if (collections.size() == 0) {
			startNewCollection(config);
		} else {
			DataCollection c = collections.get(collections.size()-1);
			if (!c.isPristine(config) && c.getNumberOfSamples() == 0) {
				// sensors changed, but no data collected
				// collections.remove(c);
				c.updateSensors(config);
			}
			c.setLastPolledData(data);
		}
	}

	@Override
	public float[] getLastPolledData() {
		if (collections.size() > 0) {
			return collections.get(collections.size()-1).getLastPolledData();
		} else {
			return new float[] {};
		}
	}

	@Override
	public void appendCollectedData(int numSamples, float[] data) {
		collections.get(collections.size()-1).appendCollectedData(numSamples, data);
	}

	@Override
	public void startNewCollection(ExperimentConfig config) {
		// If sensors are the same, and no data was collected, skip creating a collection
		if (collections.size() > 0 && collections.get(collections.size()-1).isPristine(config)) {
			return;
		}
		lastCollectionAdded = System.currentTimeMillis();
		collections.add(new DataCollection(config));
	}

	public String getCurrentState() {
		return stateManager.currentState().getName();
	}

	public String[] getUnits() {
		if (collections.size() > 0) {
			DataCollection dataCollection = collections.get(collections.size()-1);
			return dataCollection.getUnits();
		} else {
			return new String[] {};
		}
	}

	public String getCurrentInterface() {
		return stateManager.currentInterface();
	}

}
