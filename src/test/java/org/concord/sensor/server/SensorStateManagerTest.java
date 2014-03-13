package org.concord.sensor.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.concord.sensor.ExperimentConfig;
import org.concord.sensor.server.data.DataCollection;
import org.concord.sensor.server.data.DataSink;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.continuent.tungsten.fsm.core.FiniteStateException;

@RunWith(JUnit4.class)
public class SensorStateManagerTest {
	private static final Logger logger = LogManager.getLogger(SensorStateManagerTest.class);
	private float[] lastPolledData;
	private ArrayList<DataCollection> collections;
	
	/**
	 * Test basic data collection cycle. Requires a sensor interface plugged into the computer, and at least one sensor plugged in.
	 * @throws FiniteStateException
	 * @throws InterruptedException
	 */
	@Test
	public void canInstantiate() throws FiniteStateException, InterruptedException {
		BasicConfigurator.configure();
		LogManager.getLogger("org.concord.sensor").setLevel(Level.TRACE);
		
		lastPolledData = new float[] {};
		collections = new ArrayList<DataCollection>();
		
		logger.info("Setting up state manager");
		SensorStateManager manager = new SensorStateManager(new DataSink() {
			
			@Override
			public void setLastPolledData(ExperimentConfig config, float[] data) {
				logger.info("Got polled data");
				lastPolledData = data;
			}

			@Override
			public float[] getLastPolledData() {
				return lastPolledData;
			}

			@Override
			public void appendCollectedData(int numSamples, float[] data) {
				logger.info("Got collected data");
				collections.get(collections.size()-1).appendCollectedData(numSamples, data);
			}

			@Override
			public void startNewCollection(ExperimentConfig config) {
				logger.info("Told to start new collection");
				collections.add(new DataCollection(config));
			}
		});
		logger.info("Done setting up state manager");
		assertEquals("DISCONNECTED:NORMAL", manager.currentState().getName());
		
		manager.connect();
		Thread.sleep(4000);
		assertEquals("CONNECTED:POLLING", manager.currentState().getName());
		logger.info("Checking polled data: " + Arrays.toString(lastPolledData));
		assertEquals(1, lastPolledData.length);
		assertEquals(0, collections.size());
		
		manager.start();
		Thread.sleep(4000);
		assertEquals("CONNECTED:COLLECTING", manager.currentState().getName());
		assertEquals(1, lastPolledData.length);
		assertEquals(1, collections.size());
		assertTrue(collections.get(0).getCollectedData()[0].length > 10);
		
		manager.stop();
		Thread.sleep(1000);
		assertEquals("CONNECTED:POLLING", manager.currentState().getName());
		
		manager.start();
		Thread.sleep(4000);
		assertEquals("CONNECTED:COLLECTING", manager.currentState().getName());
		assertEquals(1, lastPolledData.length);
		assertEquals(2, collections.size());
		assertTrue(collections.get(1).getCollectedData()[0].length > 10);
		
		manager.stop();
		Thread.sleep(1000);
		assertEquals("CONNECTED:POLLING", manager.currentState().getName());
		
		manager.terminate();
		Thread.sleep(2000);
		assertEquals("FINISHED", manager.currentState().getName());
	}

}
