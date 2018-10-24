package org.concord.sensor;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.concord.sensor.device.impl.DeviceID;
import org.concord.usb.UsbDevice;
import org.concord.usb.UsbDeviceList;
import org.concord.usb.UsbException;
import org.concord.usb.jna.JNAUsbDeviceList;

import com.sun.jna.Platform;

public class DeviceFinder {
	private static final Logger logger = Logger.getLogger(DeviceFinder.class.getName());

	public static int[] getAttachedDeviceTypes() throws UsbException {
		ArrayList<Integer> foundDevices = new ArrayList<Integer>();
		ArrayList<UsbDevice> attachedDevices = null;
		if (Platform.isMac()){
			attachedDevices = JNAUsbDeviceList.getAttachedDevices();
		} else {
			attachedDevices = UsbDeviceList.getAttachedDevices();
		}
		
		for (UsbDevice device : attachedDevices) {
			
			try {
				int sensorDeviceID = getSensorDeviceID(device.vendorId, device.productId);
				if(sensorDeviceID != -1){
					foundDevices.add(sensorDeviceID);
				}
			} catch (UnknownProductException e) {
				// this should be exposed to the user
				logger.warning(e.getMessage());
			}
		}
		
		if (foundDevices.size() > 0) {
			int[] foundIDs = new int[foundDevices.size()];
			for (int i = 0; i < foundIDs.length; i++) {
				foundIDs[i] = foundDevices.get(i);
			}
			return foundIDs;
		} else {
			return new int[] {};
		}
	}

	/**
	 * 
	 * @param vendorId
	 * @param productId
	 * @return the id of the device from org.concord.sensor.DeviceID or -1 for 
	 *   an unknown vendor
	 * @throws UnknownProductException if the vendor is known but the product is not
	 */
	public static int getSensorDeviceID(short vendorId, short productId) throws UnknownProductException {
		switch (vendorId) {
		case 0x08F7: // Vernier
			switch (productId) {
			case 0x0001:
				logger.fine("Found a Vernier LabPro!");
				return DeviceID.VERNIER_LAB_PRO;
			case 0x0002: // GoTemp
				logger.fine("Found a Vernier GoTemp!");
				return DeviceID.VERNIER_GO_LINK_JNA;
			case 0x0003: // GoLink
				logger.fine("Found a Vernier GoLink!");
				return DeviceID.VERNIER_GO_LINK_JNA;
			case 0x0004: // GoMotion
				logger.fine("Found a Vernier GoMotion!");
				return DeviceID.VERNIER_GO_LINK_JNA;
			case 0x0005: // LabQuest
				logger.fine("Found a Vernier LabQuest!");
				return DeviceID.VERNIER_LAB_QUEST;
			case 0x0008: // LabQuest Mini
				logger.fine("Found a Vernier LabQuest Mini!");
				return DeviceID.VERNIER_LAB_QUEST;
			case 0x000B: // LabQuest 2
				logger.fine("Found a Vernier LabQuest 2!");
				return DeviceID.VERNIER_LAB_QUEST;
			case 0x0010: // Go Direct
				logger.fine("Found a Vernier Go Direct!");
				return DeviceID.VERNIER_GO_DIRECT;				
			}
			throw new UnknownProductException("Vernier", productId);
		case 0x0945: // Pasco
			// Right now, we don't differentiate between Pasco USB
			// devices, so no need to check product ids
			logger.fine("Found a Pasco!");
			return DeviceID.PASCO_USB;
		}
		return -1;
	}
	
	public static String getDeviceName(int deviceID) {
		switch (deviceID) {
		case DeviceID.PSEUDO_DEVICE:
		case DeviceID.PSEUDO_DEVICE_VARIABLE_TIME:
			return "Pseudo Sensor Device";
		case DeviceID.VERNIER_LAB_PRO:
			return "Vernier LabPro";
		case DeviceID.VERNIER_LAB_QUEST:
			return "Vernier LabQuest";
		case DeviceID.VERNIER_GO_LINK:
		case DeviceID.VERNIER_GO_LINK_JNA:
		case DeviceID.VERNIER_GO_LINK_NATIVE:
			return "Vernier GoLink!";
		case DeviceID.VERNIER_GO_DIRECT:
			return "Vernier Go Direct";			
		case DeviceID.TI_CONNECT:
			return "TI Connect"; // ???
		case DeviceID.FOURIER:
			return "Fourier EcoLog"; // ???
		case DeviceID.DATA_HARVEST_USB:
		case DeviceID.DATA_HARVEST_CF:
		case DeviceID.DATA_HARVEST_ADVANCED:
		case DeviceID.DATA_HARVEST_QADVANCED:
			return "Data Harvest"; // ???
		case DeviceID.IMAGIWORKS_SERIAL:
		case DeviceID.IMAGIWORKS_SD:
			return "Imagiworks"; // ???
		case DeviceID.PASCO_SERIAL:
		case DeviceID.PASCO_AIRLINK:
		case DeviceID.PASCO_USB:
			return "Pasco";
		case DeviceID.CCPROBE_VERSION_0:
		case DeviceID.CCPROBE_VERSION_1:
		case DeviceID.CCPROBE_VERSION_2:
			return "CC Probe";
		case DeviceID.COACH:
			return "Coach"; // ???
		default:
			return "Unknown Device!";
		}
	}

	public static void main(String[] args) throws UsbException {
		int[] attachedDeviceTypes = getAttachedDeviceTypes();
		for (int deviceType : attachedDeviceTypes) {
			System.out.println("" + getDeviceName(deviceType));
		}
	}
}
