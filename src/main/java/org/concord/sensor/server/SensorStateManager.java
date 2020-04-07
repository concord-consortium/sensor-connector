package org.concord.sensor.server;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import java.util.logging.Logger;
import org.concord.sensor.DeviceFinder;
import org.concord.sensor.ExperimentConfig;
import org.concord.sensor.ExperimentRequest;
import org.concord.sensor.SensorConfig;
import org.concord.sensor.SensorDefaults;
import org.concord.sensor.SensorRequest;
import org.concord.sensor.device.SensorDevice;
import org.concord.sensor.device.impl.DeviceConfigImpl;
import org.concord.sensor.device.impl.DeviceID;
import org.concord.sensor.device.impl.JavaDeviceFactory;
import org.concord.sensor.device.impl.SensorConfigImpl;
import org.concord.sensor.impl.ExperimentConfigImpl;
import org.concord.sensor.impl.ExperimentRequestImpl;
import org.concord.sensor.impl.Range;
import org.concord.sensor.impl.SensorRequestImpl;
import org.concord.sensor.pasco.PascoUsbSensorDevice;
import org.concord.sensor.server.data.DataSink;
import org.concord.sensor.vernier.labquest.LabQuestSensorDevice;
import org.concord.usb.UsbException;
import org.usb4java.LibUsbException;

import com.continuent.tungsten.fsm.core.Action;
import com.continuent.tungsten.fsm.core.Entity;
import com.continuent.tungsten.fsm.core.Event;
import com.continuent.tungsten.fsm.core.EventTypeGuard;
import com.continuent.tungsten.fsm.core.FiniteStateException;
import com.continuent.tungsten.fsm.core.PositiveGuard;
import com.continuent.tungsten.fsm.core.State;
import com.continuent.tungsten.fsm.core.StateMachine;
import com.continuent.tungsten.fsm.core.StateTransitionMap;
import com.continuent.tungsten.fsm.core.StateType;
import com.continuent.tungsten.fsm.core.Transition;
import com.continuent.tungsten.fsm.core.TransitionFailureException;
import com.continuent.tungsten.fsm.core.TransitionRollbackException;
import com.continuent.tungsten.fsm.event.EventDispatcherTask;

public class SensorStateManager {
	private StateMachine stateMachine;
	private EventDispatcherTask dispatcher;
	private final HashMap<String, Action> actions = new HashMap<String, Action>();

	private static final Logger logger = Logger.getLogger(SensorStateManager.class.getName());

	private static final int MAX_READ_ERRORS = 7;
	private static final int NULL_INTERFACE_TYPE = -1;

	private JavaDeviceFactory deviceFactory;
	private SensorDevice device;
	private ScheduledExecutorService executor;
	private ScheduledFuture<?> collectionTask;
	private String executorThreadName;

	private ExperimentConfig reportedConfig = null;
	private long reportedConfigLoadedAt = 0;
	private int currentInterfaceType = NULL_INTERFACE_TYPE;
	private boolean zeroSamplesIsAnError = true;

	private DataSink datasink;

	// measured time since last device connection starts at launch
	private Date lastConnectedTime = new Date();
	// measured time since last data collection starts at launch
	private Date lastCollectedTime = new Date();

	public SensorStateManager(DataSink datasink) throws FiniteStateException, Exception {
		this.datasink = datasink;

		StateTransitionMap map = generateStateTransitionMap();
		Entity sensorEntity = new Entity(){};
		stateMachine = new StateMachine(map, sensorEntity);
		dispatcher = new EventDispatcherTask(stateMachine);
		dispatcher.start("state-machine-dispatcher");
		// Trigger the transition to DISCONNECTED:NORMAL
		dispatcher.put(new Event(null));
	}

	public void start() {
		try {
			dispatcher.put(new StartEvent());
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.severe("SensorStateManager start exception!");
		}
	}

	public void stop() {
		try {
			dispatcher.put(new StopEvent());
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.severe("SensorStateManager stop exception!");
		}
	}

	public void connect() {
		try {
			dispatcher.put(new ConnectEvent());
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.severe("SensorStateManager connect exception!");
		}
	}

	public void disconnect() {
		try {
			dispatcher.put(new DisconnectEvent());
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.severe("SensorStateManager disconnect exception!");
		}
	}

	public void terminate() {
		// Do this with applyEvent so that control doesn't return until the transition is complete.
		try {
			stateMachine.applyEvent(new TerminateEvent());
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.severe("SensorStateManager terminate exception!");
		} catch (FiniteStateException e) {
			e.printStackTrace();
			logger.severe("SensorStateManager terminate exception!");
		}
	}

	public State currentState() {
		return stateMachine.getState();
	}

	public boolean hasCurrentInterface() {
		return currentInterfaceType != NULL_INTERFACE_TYPE;
	}

	public String currentInterface() {
		if (currentInterfaceType == NULL_INTERFACE_TYPE) {
			return "None Found";
		}
		return DeviceFinder.getDeviceName(currentInterfaceType);
	}

	// returns the time since the last device connection in seconds (as a double)
	public double timeSinceConnection() {
		return ((new java.util.Date()).getTime() - lastConnectedTime.getTime()) / 1000;
	}

	// returns the time since the last data collection in seconds (as a double)
	public double timeSinceCollection() {
		return ((new java.util.Date()).getTime() - lastCollectedTime.getTime()) / 1000;
	}

	private StateTransitionMap generateStateTransitionMap() throws FiniteStateException {
		initializeActions();
		StateTransitionMap map = new StateTransitionMap();

		// First, set up the various states
		State initializing = new State("INITIALIZING", StateType.START);

		State disconnected = new State("DISCONNECTED", StateType.ACTIVE);
		State disconnectedNormal = new State("NORMAL", StateType.ACTIVE, disconnected, actions.get("transitionToConnect"), null);
		State disconnectedError  = new State("ERROR",  StateType.ACTIVE, disconnected, actions.get("reinitialize"), null);

		State connected = new State("CONNECTED", StateType.ACTIVE, actions.get("connect"), actions.get("disconnect"));
		State connectedPolling = new State("POLLING", StateType.ACTIVE, connected, actions.get("startPolling"), actions.get("stopPolling"));
		State connectedCollecting = new State("COLLECTING", StateType.ACTIVE, connected, actions.get("startCollecting"), actions.get("stopCollecting"));

		State finished = new State("FINISHED", StateType.END, actions.get("terminate"), null);

		map.addState(initializing);
		map.addState(disconnected);
		map.addState(disconnectedNormal);
		map.addState(disconnectedError);
		map.addState(connected);
		map.addState(connectedPolling);
		map.addState(connectedCollecting);
		map.addState(finished);

		// Then, define the transitions between states
		EventTypeGuard startGuard = new EventTypeGuard(StartEvent.class);
		EventTypeGuard stopGuard = new EventTypeGuard(StopEvent.class);
		EventTypeGuard connectGuard = new EventTypeGuard(ConnectEvent.class);
		EventTypeGuard disconnectGuard = new EventTypeGuard(DisconnectEvent.class);
		EventTypeGuard terminateGuard = new EventTypeGuard(TerminateEvent.class);
		EventTypeGuard errorGuard = new EventTypeGuard(ErrorEvent.class);

		Transition init2disco = new Transition("INITIALIZING-TO-DISCONNECTED", new PositiveGuard(), initializing, actions.get("initialize"), disconnectedNormal);
		Transition disco2connected = new Transition("DISCONNECTED-TO-CONNECTED", connectGuard, disconnectedNormal, null, connectedPolling);
		Transition error2connected = new Transition("ERROR-TO-CONNECTED", connectGuard, disconnectedError, null, connectedPolling);
		Transition polling2collecting = new Transition("POLLING-TO-COLLECTING", startGuard, connectedPolling, null, connectedCollecting);
		Transition collecting2polling = new Transition("COLLECTING-TO-POLLING", stopGuard, connectedCollecting, null, connectedPolling);

		Transition collecting2disconnected = new Transition("COLLECTING-TO-DISCONNECTED", disconnectGuard, connectedCollecting, null, disconnectedNormal);
		Transition polling2disconnected = new Transition("POLLING-TO-DISCONNECTED", disconnectGuard, connectedPolling, null, disconnectedNormal);

		Transition connected2error = new Transition("CONNECTED-TO-ERROR", errorGuard, connected, null, disconnectedError);
		Transition disconnected2error = new Transition("DISCONNECTED-TO-ERROR", errorGuard, disconnected, null, disconnectedError);

		Transition connected2finished = new Transition("CONNECTED-TO-FINISHED", terminateGuard, connected, null, finished);
		Transition disconnected2finished = new Transition("DISCONNECTED-TO-FINISHED", terminateGuard, disconnected, null, finished);

		map.addTransition(init2disco);
		map.addTransition(disco2connected);
		map.addTransition(error2connected);
		map.addTransition(polling2collecting);
		map.addTransition(collecting2polling);
		map.addTransition(collecting2disconnected);
		map.addTransition(polling2disconnected);
		map.addTransition(connected2finished);
		map.addTransition(disconnected2finished);

		map.addTransition(connected2error);
		map.addTransition(disconnected2error);

		map.setErrorState(disconnectedError);

		map.build();

		return map;
	}

	private void initializeActions() {
		// Define the actions that happen on state transitions
		actions.put("initialize", new Action() {
			@Override
			public void doAction(Event message, Entity entity, Transition transition, int actionType) throws TransitionRollbackException, TransitionFailureException, InterruptedException {
				// Then, set up all the pre-requisite classes, thread processors, etc.
				deviceFactory = new JavaDeviceFactory();
				executor = Executors.newSingleThreadScheduledExecutor();
				ScheduledFuture<?> task = executor.schedule(new Runnable() {
					public void run() {
						executorThreadName = Thread.currentThread().getName();
					}
				}, 0, TimeUnit.MILLISECONDS);
				try {
					task.get();
				} catch (Exception e) {
					logger.severe("initializeActions exception! " + e.toString());
					e.printStackTrace();
				}
			}
		});

		actions.put("transitionToConnect", new Action() {
			@Override
			public void doAction(Event message, Entity entity, Transition transition, int actionType) throws TransitionRollbackException, TransitionFailureException, InterruptedException {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						SensorStateManager.this.connect();
					}
				});
			}
		});

		actions.put("reinitialize", new Action() {
			@Override
			public void doAction(Event message, Entity entity, Transition transition, int actionType) throws TransitionRollbackException, TransitionFailureException, InterruptedException {
				// make sure we've destroyed any prior sessions
				actions.get("terminate").doAction(message, entity, transition, actionType);
				Thread.sleep(1000); // Wait for a second before trying again
				actions.get("initialize").doAction(message, entity, transition, actionType);
				actions.get("transitionToConnect").doAction(message, entity, transition, actionType);
			}
		});

		actions.put("connect", new Action() {
			@Override
			public void doAction(Event message, Entity entity, Transition transition, int actionType) throws TransitionRollbackException, TransitionFailureException, InterruptedException {
				Runnable r = new Runnable() {
					public void run() {
						logger.info("In SensorStateManager doAction run" + Thread.currentThread().getName());
						// Scan to see which devices are connected, and then connect with that device type
						currentInterfaceType = NULL_INTERFACE_TYPE;
						try {
							int[] types = DeviceFinder.getAttachedDeviceTypes();
							if (types.length > 0) {
								// Just pick the first interface for now
								// TODO Give the user the option to choose which one to connect to?
								currentInterfaceType = types[0];
							}
						} catch (UsbException e) {
							logger.severe("Failed to enumerate USB devices! " + e.toString());
						}

						if (currentInterfaceType == NULL_INTERFACE_TYPE) {
							// No devices found. Fail transition to go back to disconnected.
							throw new RuntimeException("No devices found!");
						}

						logger.info("Creating device: " + Thread.currentThread().getName());
						device = deviceFactory.createDevice(new DeviceConfigImpl(currentInterfaceType, "usb"));

						// Check if we're attached
						logger.info("Checking attached: " + Thread.currentThread().getName());
						boolean deviceIsAttached = device.isAttached();
						if (!deviceIsAttached) {
							// try re-opening the device
							try {
								logger.info("Re-opening device: " + Thread.currentThread().getName());
								device.close();
								device.open(null);
								deviceIsAttached = device.isAttached();
							} catch (Exception e) {
								logger.severe("Device connection exception! " + e.toString());
								deviceIsAttached = false;
							}
							// we're still not attached. Error.
							if (!deviceIsAttached) {
								throw new RuntimeException("Device not attached!");
							}
						}
					}
				};

				if (!executeAndWait(r)) {
					// device wasn't attached. Error.
					throw new TransitionFailureException("Device not attached!", message, entity, transition, actionType, null);
				}
			}
		});

		actions.put("disconnect", new Action() {
			@Override
			public void doAction(Event message, Entity entity, Transition transition, int actionType) throws TransitionRollbackException, TransitionFailureException, InterruptedException {
				logger.info("In SensorStateManager doAction disconnect" + Thread.currentThread().getName());
				// make sure we've destroyed any prior sessions
				if (device != null) {
					Runnable r = new Runnable() {
						public void run() {
							deviceFactory.destroyDevice(device);
							device = null;
							currentInterfaceType = NULL_INTERFACE_TYPE;
						}
					};
					executeAndWait(r);
				}
			}
		});

		actions.put("startPolling", new Action() {
			@Override
			public void doAction(Event message, Entity entity, Transition transition, int actionType) throws TransitionRollbackException, TransitionFailureException, InterruptedException {
				logger.info("In SensorStateManager doAction startPolling" + Thread.currentThread().getName());
				datasink.startNewCollection(getDeviceConfig());

				Runnable r2 = new Runnable() {
					// This is a balance between how quickly we recognize that a device
					// has been disconnected and how robust we are to the possibility of
					// occasional device errors. It was recently adjusted from 5 to 2
					// to make detection of device disconnect more responsive.
					private int ALLOWED_ERRORS_BEFORE_DISCONNECT = 2;
					private int errorCount = 0;
					@Override
					public void run() {
						try {
							if ((device != null) && device.supportsChannelPolling()) {
								pollChannelValues();
							}
							else {
								readSingleValue();
							}
							// update our time since last connection
							lastConnectedTime = new Date();
						} catch (Exception e) {
							errorCount++;
							logger.severe("Failed to read data from the device! " + e.toString());
							if (errorCount > ALLOWED_ERRORS_BEFORE_DISCONNECT) {
								try {
									dispatcher.put(new DisconnectEvent());
								} catch (InterruptedException e1) {
									logger.severe("Failed to transition to DISCONNECTED! " + e1.toString());
								}
							}
						}
					}

				};
				collectionTask = executor.scheduleAtFixedRate(r2, 1, 1, TimeUnit.SECONDS);
			}
		});

		actions.put("stopPolling", new Action() {
			@Override
			public void doAction(Event message, Entity entity, Transition transition, int actionType) throws TransitionRollbackException, TransitionFailureException, InterruptedException {
				logger.info("In SensorStateManager doAction stopPolling" + Thread.currentThread().getName());
				collectionTask.cancel(false);
			}
		});

		actions.put("startCollecting", new Action() {
			@Override
			public void doAction(final Event message, final Entity entity, final Transition transition, final int actionType) throws TransitionRollbackException, TransitionFailureException, InterruptedException {
				logger.info("In SensorStateManager doAction startCollecting" + Thread.currentThread().getName());
				Runnable start = new Runnable() {
					public void run() {
						ExperimentConfig config = getDeviceConfig();
						ExperimentConfig actualConfig;
						ExperimentRequest request;
						try {
							request = generateExperimentRequest(config);
						} catch (Exception e1) {
							try {
								dispatcher.putOutOfBand(new ErrorEvent(e1.getMessage()));
							} catch (InterruptedException e) {
								logger.severe("Run exception in dispatcher.putOutOfBand! " + e.toString());
								e.printStackTrace();
							}
							return;
						}
						try {
							actualConfig = device.configure(request);
							numSensors = actualConfig.getSensorConfigs().length;
//							SensorUtilJava.printExperimentConfig(actualConfig);
						} catch (RuntimeException e) {
							// force re-getting the currently attached sensors and try it again
							try {
								Thread.sleep(1000);
								config = getDeviceConfig(true);
								request = generateExperimentRequest(config);
							} catch (Exception e1) {
								try {
									dispatcher.putOutOfBand(new ErrorEvent(e1.getMessage()));
								} catch (InterruptedException e2) {
									logger.severe("Run exception in dispatcher.putOutOfBand! " + e2.toString());
									e2.printStackTrace();
								}
							}
							actualConfig = device.configure(request);
							SensorConfig[] sensorConfigs = actualConfig.getSensorConfigs();
							if (sensorConfigs == null) {
								try {
									dispatcher.putOutOfBand(new ErrorEvent("No sensors attached! Can't collect data."));
								} catch (InterruptedException e2) {
									logger.severe("Run exception in dispatcher.putOutOfBand! " + e2.toString());
									e2.printStackTrace();
								}
								return;
							}
							numSensors = sensorConfigs.length;
//							SensorUtilJava.printExperimentConfig(actualConfig);
						}

						// Make sure the sensor list is accurate in the datasink before we start collecting data.
						// When getting the last polled data, strip off the first value since that's the time value.
						float[] lastPolled = datasink.getLastPolledData();
						float[] strippedLastPolled = Arrays.copyOfRange(lastPolled, 1, lastPolled.length);
						datasink.setLastPolledData(actualConfig, strippedLastPolled);

						final float[] data = new float[1024];
						final Runnable r = new Runnable() {
							public void run() {
								try {
									int numSamples = device.read(data, 0, numSensors, null);
									if (numSamples > 0) {
										float[] dataCopy = new float[numSamples * numSensors];
										System.arraycopy(data, 0, dataCopy, 0, numSamples * numSensors);
										datasink.appendCollectedData(numSamples, dataCopy);

										numErrors = 0;
									} else {
										// some devices (ex: GoIO) report -1 samples to indicate an error, or
										// will just report 0 samples continuously after being unplugged
										if (numSamples != 0 || zeroSamplesIsAnError) numErrors++;
									}
								} catch (Exception e) {
									numErrors++;
									logger.severe("Error reading data from device! " + e.toString());
								}
								if (numErrors >= MAX_READ_ERRORS) {
									numErrors = 0;
									logger.severe("Too many collection errors! Stopping device.");
									try {
										dispatcher.put(new StopEvent());
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
						};
						numErrors = 0;
						long interval = (long) Math.floor(actualConfig.getDataReadPeriod() * 1000);
						if (interval <= 0) {
							interval = 100;
						}
						long adjustedInterval = interval;
						long initialDelay = actualConfig.getInitialReadDelay() + adjustedInterval;
						if (device instanceof PascoUsbSensorDevice) {
							zeroSamplesIsAnError = false;
						}
						boolean deviceIsRunning = device.start();
						if(deviceIsRunning) {
							System.out.println("started device");
							collectionTask = executor.scheduleAtFixedRate(r, initialDelay, adjustedInterval, TimeUnit.MILLISECONDS);
						} else {
							// we should send a notification here that something went wrong
							System.err.println("error starting the device");
							logger.severe("error starting the device!");
							try {
								dispatcher.put(new StopEvent());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						// update our time since last collection
						lastCollectedTime = new Date();
					}
				};
				execute(start, 0);
			}
		});

		actions.put("stopCollecting", new Action() {
			@Override
			public void doAction(Event message, Entity entity, Transition transition, int actionType) throws TransitionRollbackException, TransitionFailureException, InterruptedException {
				logger.info("In SensorStateManager doAction stopCollecting" + Thread.currentThread().getName());
				if (collectionTask != null && !collectionTask.isDone()) {
					collectionTask.cancel(false);
				}
				collectionTask = null;
				Runnable r = new Runnable() {
					public void run() {
						logger.info("Stopping device: " + Thread.currentThread().getName());
						device.stop(true);
						// update our time since last collection
						lastCollectedTime = new Date();
					}
				};

				if (!executeAndWait(r)) {
					// try closing and re-opening the device
					throw new TransitionFailureException("Stopping had errors! Closing and re-opening.", message, entity, transition, actionType, null);
				}
			}
		});

		actions.put("terminate", new Action() {
			@Override
			public void doAction(Event message, Entity entity, Transition transition, int actionType) throws TransitionRollbackException, TransitionFailureException, InterruptedException {
				logger.info("In SensorStateManager doAction terminate" + Thread.currentThread().getName());
				Runnable r = new Runnable() {
					public void run() {
						if (device != null) {
							deviceFactory.destroyDevice(device);
							device = null;
							currentInterfaceType = NULL_INTERFACE_TYPE;
						}
					}
				};
				executeAndWait(r);
				executor.shutdownNow();
				try {
					executor.awaitTermination(5, TimeUnit.SECONDS);
					System.err.println("Shutdown completed. All tasks terminated: " + executor.isTerminated());
				} catch (InterruptedException e) {
					logger.severe("Terminate exception! " + e.toString());
					e.printStackTrace();
				}
				executor = null;
				executorThreadName = null;
				deviceFactory = null;
				if (message instanceof TerminateEvent) {
					dispatcher.stop();
					dispatcher = null;
				}
			}
		});
	}

	private ExperimentRequest generateExperimentRequest(ExperimentConfig config) {
		logger.info("In SensorStateManager generateExperimentRequest" + Thread.currentThread().getName());
		if (config == null) { throw new RuntimeException("Couldn't fetch config from device! Restarting..."); }

		SensorRequest[] sensors = getSensorsFromCurrentConfig(config);
		numSensors = sensors.length;
		if (sensors == null || numSensors < 1) { throw new RuntimeException("No sensors attached! Restarting...");  }

		ExperimentRequestImpl request = new ExperimentRequestImpl();

		float period = config.getPeriod();
		if (config instanceof ExperimentConfigImpl) {
			Range r = ((ExperimentConfigImpl) config).getPeriodRange();
			if (r != null && r.minimum > 0) {
				period = r.minimum;
			}
		}
		if (period == 0) { period = SensorDefaults.DEFAULT_PERIOD; }
		if (period < SensorDefaults.MIN_PERIOD) { period = SensorDefaults.MIN_PERIOD; }
		request.setPeriod(period);
		request.setNumberOfSamples(-1);

		request.setSensorRequests(sensors);
		return request;
	}

	private ExperimentConfig getDeviceConfig() {
		return getDeviceConfig(false);
	}

	private ExperimentConfig getDeviceConfig(boolean force) {
		if (device != null && (force || reportedConfig == null || (System.currentTimeMillis() - reportedConfigLoadedAt) > 1000)) {
			Runnable r = new Runnable() {
				public void run() {
					logger.info("Getting device config: " + Thread.currentThread().getName());
					// Check what is attached, this isn't necessary if you know what you want
					// to be attached. But sometimes you want the user to see what is attached
					reportedConfig = device.getCurrentConfig();
					reportedConfigLoadedAt = System.currentTimeMillis();
				}
			};

			executeAndWait(r);

			logger.info("DONE getting device config: " + Thread.currentThread().getName());
		}
		return reportedConfig;
	}

	void pollChannelValues() {
		Runnable r = new Runnable() {
			public void run() {
				ExperimentConfig expConfig = getDeviceConfig();
				if (expConfig == null) {
					logger.severe("Error polling channel values!");
					throw new RuntimeException("Error polling channel values");
				}
				SensorConfig[] sensors = expConfig.getSensorConfigs();
				int sensorCount = sensors != null ? sensors.length : 0;
				float[] channelValues = new float[sensorCount];
				int valueCount = device.pollChannelValues(expConfig, channelValues);
				if (valueCount >= 0) {
					datasink.setLastPolledData(expConfig, channelValues);
				}
				else if (valueCount < 0) {
					logger.severe("Error polling channel values!");
					throw new RuntimeException("Error polling channel values");
				}
			}
		};
		if (! executeAndWait(r, 10)) {
			// exception trying to read channel values
			logger.severe("Error polling channel values!");
			throw new RuntimeException("Error polling channel values.");
		}
	}

	private int numErrors = 0;
	private int numSensors = 0;
	private long adjustedInterval = 100;
	private ExperimentConfig actualConfig;
	private float[] data;
	private void readSingleValue() {
		Runnable r = new Runnable() {
			public void run() {
				logger.info("Read single value: " + Thread.currentThread().getName());
				// There's probably a more efficient way of doing this.
				// GoIO devices, for instance, support one-shot data collection.
				// Perhaps other devices do as well?
				float[] buffer = new float[1024];
				ExperimentConfig config = getDeviceConfig();
				ExperimentRequest request = generateExperimentRequest(config);
				actualConfig = device.configure(request);

				if (device instanceof LabQuestSensorDevice && !device.isAttached()) {
					// Something dramatic happened during configure. Bail.
					// I've seen this when all sensors get unplugged from the LabQuest after getting opened with sensors plugged in.
					logger.severe("Device is no longer attached!");
					throw new RuntimeException("Device is no longer attached!");
				}

				SensorConfig[] sensorConfigs = actualConfig.getSensorConfigs();
				if (sensorConfigs == null) {
					logger.severe("No sensors attached to device!");
					throw new RuntimeException("No sensors attached to device!");
				}
				numSensors = sensorConfigs.length;
		//		SensorUtilJava.printExperimentConfig(actualConfig);

				data = new float[numSensors];

				int interval = (int) Math.floor(actualConfig.getDataReadPeriod() * 1000);
				if (interval <= 0) {
					interval = 100;
				}
				adjustedInterval = interval;

				logger.info("starting device");
				device.start();

				if (device instanceof PascoUsbSensorDevice) {
					zeroSamplesIsAnError = false;
				}

				try {
					Thread.sleep(adjustedInterval);
				} catch (InterruptedException e1) {
					logger.info("Interrupted while waiting for device to collect.");
				}

				int numCollected = 0;
				while (numErrors < MAX_READ_ERRORS && numCollected < 1) {
					logger.info(String.format("Attempt %d to read data from the device...", numErrors + 1));
					try {
						final int numSamples = device.read(buffer, 0, numSensors, null);
						if (numSamples > 0) {
							// read just the first value
							synchronized (data) {
								System.arraycopy(buffer, 0, data, 0, numSensors);
							}
							logger.info(String.format("Successfully read %d samples from the device after %d errors!",
										numSamples, numErrors));
							numCollected++;
							numErrors = 0;
						} else {
							// some devices (ex: GoIO) report -1 samples to indicate an error, or
							// will just report 0 samples continuously after being unplugged
							if (numSamples != 0 || zeroSamplesIsAnError) numErrors++;
							Thread.sleep(adjustedInterval);
						}
					} catch (Exception e) {
						numErrors++;
						logger.severe("Error reading data from device! " + e.toString());
					}
				}
				if (numErrors >= MAX_READ_ERRORS) {
					logger.severe("Too many collection errors while getting single value! Stopping device.");
				}
			}
		};
		try {
			numErrors = 0;
			if (! executeAndWait(r, 10)) {
				// exception trying to collect data
				throw new RuntimeException("Error reading single value.");
			}
			synchronized (data) {
				// A no-op, basically, just to ensure this synchronized block
				// doesn't get optimized away completely.
				numSensors = 0;
			}
			logger.info("Returning data: " + Arrays.toString(data));
			datasink.setLastPolledData(actualConfig, data);
			return;
		} finally {
			r = new Runnable() {
				public void run() {
					logger.info("Stopping device: " + Thread.currentThread().getName());
					device.stop(true);
				}
			};

			if (!executeAndWait(r)) {
				// try closing and re-opening the device
				logger.severe("Stopping had errors! Closing and re-opening.");
				try {
					dispatcher.putOutOfBand(new ErrorEvent("No sensors attached! Can't collect data."));
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	private SensorRequest[] getSensorsFromCurrentConfig(ExperimentConfig deviceConfig) {
		SensorConfig[] configs;
		if (deviceConfig == null || (configs = deviceConfig.getSensorConfigs()) == null) {
			return new SensorRequest[] {};
		}

		SensorRequest[] reqs = new SensorRequest[configs.length];
		for (int i = 0; i < reqs.length; i++) {
			SensorConfigImpl config = (SensorConfigImpl) configs[i];
			SensorRequestImpl sensorReq = new SensorRequestImpl();
			Range r = config.getValueRange();
			if (r == null) { r = new Range(-10000f, 10000f); }
			configureSensorRequest(sensorReq, 1, r.minimum, r.maximum, config.getPort(), config.getStepSize(), config.getType(), config.getUnit());
			reqs[i] = sensorReq;
		}
		return reqs;
	}

	private void configureSensorRequest(SensorRequestImpl sensor, int precision, float min, float max, int port, float step, int type, String unit) {
		sensor.setDisplayPrecision(precision);
		sensor.setUnit(unit);
		sensor.setRequiredMin(min);
		sensor.setRequiredMax(max);
		sensor.setPort(port);
		sensor.setStepSize(step);
		sensor.setType(type);
	}

	private ScheduledFuture<?> execute(Runnable r, int delay) {
		if (Thread.currentThread().getName().equals(executorThreadName)) {
			r.run();
			return null;
		} else {
			return executor.schedule(r, delay, TimeUnit.MILLISECONDS);
		}
	}

	private boolean executeAndWait(final Runnable r) {
		return executeAndWait(r, 0);
	}

	private boolean executeAndWait(final Runnable r, int delayMs) {
		try {
			ScheduledFuture<?> task = execute(r, delayMs);
			if (task != null) {
				task.get();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private class StopEvent extends Event {
		public StopEvent() {
			super(null);
		}
	}
	private class StartEvent extends Event {
		public StartEvent() {
			super(null);
		}
	}
	private class ConnectEvent extends Event {
		public ConnectEvent() {
			super(null);
		}
	}
	private class DisconnectEvent extends Event {
		public DisconnectEvent() {
			super(null);
		}
	}
	private class TerminateEvent extends Event {
		public TerminateEvent() {
			super(null);
		}
	}
	private class ErrorEvent extends Event {
		public ErrorEvent(String message) {
			super(message);
		}
	}
}
