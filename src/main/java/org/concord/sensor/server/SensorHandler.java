package org.concord.sensor.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.concord.sensor.ExperimentConfig;
import org.concord.sensor.server.data.DataCollection;
import org.concord.sensor.server.data.DataSink;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.continuent.tungsten.fsm.core.FiniteStateException;

import net.minidev.json.JSONObject;

public class SensorHandler extends AbstractHandler implements DataSink {
	private SensorStateManager stateManager;
	private final String sessionId;
	private boolean initialized = false;
	private String currentClient = "default";
	private String currentClientFriendlyName = null;

	public SensorHandler() {
		sessionId = UUID.randomUUID().toString();
	}
	
	public void init() {
		if (initialized) { return; }
		initialized = true;
		startNewCollection(null);
		try {
			stateManager = new SensorStateManager(this);
		} catch (FiniteStateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if (!initialized) { init(); }
		baseRequest.setHandled(true);
		JSONObject json = new JSONObject();

		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "GET");

		if (target.equals("/") || target.equals("/status")) {
			json.put("sessionID", sessionId);
			json.put("sessionDesc", "");
			json.put("requestTimeStamp", System.currentTimeMillis());
			json.put("columnListTimeStamp", lastCollectionAdded);
			json.put("currentInterface", stateManager.currentInterface());
			appendColumnsInfo(json, getCurrentClient(request));
			appendCollectionInfo(json, getCurrentClient(request));
			appendVersionInfo(json);
		} else if (target.equals("/connect")) {
			stateManager.connect();
		} else if (target.equals("/disconnect")) {
			stateManager.disconnect();
		} else if (target.equals("/control/start")) {
			currentClient = getCurrentClient(request);
			currentClientFriendlyName = getCurrentClientFriendlyName(request);
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
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(json.toJSONString());
	}

	private void appendVersionInfo(JSONObject json) {
		JSONObject serverInfo = new JSONObject();
		serverInfo.put("version", SensorConnector.readVersionString());
		serverInfo.put("arch", SensorConnector.getJVMArch(false));
		json.put("server", serverInfo);

		JSONObject osInfo = new JSONObject();
		osInfo.put("name", System.getProperty("os.name"));
		osInfo.put("version", System.getProperty("os.version"));
		json.put("os", osInfo);
		
	}

	private String getCurrentClient(HttpServletRequest request) {
		String client = request.getParameter("client");
		if (client == null) { client = "default"; }
		return client;
	}

	private String getCurrentClientFriendlyName(HttpServletRequest request) {
		String clientName = request.getParameter("clientName");
		return clientName;
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

	private void appendColumnsInfo(JSONObject json, String client) {
		JSONObject sInfo = new JSONObject();
		JSONObject cInfo = new JSONObject();
		int run = 0;
		for (int i = 0; i < collectionToClientMap.size(); i++) {
			DataCollection c = collections.get(i);
			if (collectionToClientMap.get(i).equals(client) || c == currentPollingCollection) {
				run = appendRunToColumnInfo(sInfo, cInfo, run, c);
			}
		}
		// If we get to this point, we are currently collecting, but a new client
		// is connecting and doesn't have any valid collections to show.
		// currentPollingCollection is a clone of the most recent collection, so show
		// it to the new client.
		if (run == 0 && currentPollingCollection != null) {
			run = appendRunToColumnInfo(sInfo, cInfo, run, currentPollingCollection);
		}
		json.put("columns", cInfo);
		json.put("sets", sInfo);
	}

	private int appendRunToColumnInfo(JSONObject sInfo, JSONObject cInfo, int run, DataCollection c) {
		run++;
		JSONObject setInfo = new JSONObject();
		setInfo.put("name", "Run " + run);
		
		JSONObject columnInfo = c.getColumnInfo();
		cInfo.merge(columnInfo);
		
		ArrayList<Integer> colIDs = new ArrayList<Integer>();
		for (int j = 0; j < c.getNumberOfSensors(); j++) {
			colIDs.add(c.getId() + j);
		}
		setInfo.put("colIDs", colIDs);
		sInfo.put(""+c.getId(), setInfo);
		return run;
	}

	private void appendCollectionInfo(JSONObject json, String clientId) {
		if (!initialized) { init(); }
		
		boolean isCollecting = stateManager.currentState().getName().equals("CONNECTED:COLLECTING") && currentClient.equals(clientId);
		boolean controlBlocked = stateManager.currentState().getName().equals("CONNECTED:COLLECTING") && !currentClient.equals(clientId);
		
		JSONObject cInfo = new JSONObject();
		cInfo.put("isCollecting", isCollecting);
		cInfo.put("canControl", !controlBlocked);
		if (controlBlocked) {
			JSONObject blockInfo = new JSONObject();
			blockInfo.put("clientId", currentClient);
			if (currentClientFriendlyName != null) {
				blockInfo.put("clientName", currentClientFriendlyName);
			}
			cInfo.put("inControl", blockInfo);
		}
		json.put("collection", cInfo);
	}

	// DataSink support
	private ArrayList<DataCollection> collections = new ArrayList<DataCollection>();
	private ArrayList<String> collectionToClientMap = new ArrayList<String>();
	private long lastCollectionAdded;
	private DataCollection currentPollingCollection;
	
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
		DataCollection dc = collections.get(collections.size()-1);
		
		if (dc.getNumberOfSamples() == 0) {
			collectionToClientMap.set(collections.size()-1, currentClient);
			currentPollingCollection = currentPollingCollection.clone();
		}

		dc.appendCollectedData(numSamples, data);
	}

	@Override
	public void startNewCollection(ExperimentConfig config) {
		// If sensors are the same, and no data was collected, skip creating a collection
		if (collections.size() > 0 && collections.get(collections.size()-1).isPristine(config)) {
			return;
		}
		lastCollectionAdded = System.currentTimeMillis();
		DataCollection dataCollection = new DataCollection(config);
		collections.add(dataCollection);
		collectionToClientMap.add("default");
		currentPollingCollection = dataCollection;
	}

	public String getCurrentState() {
		if (!initialized) { init(); }
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
		if (!initialized) { init(); }
		return stateManager.currentInterface();
	}

	public void shutdown() {
		if (initialized) {
			stateManager.terminate();
		}
	}

}
