package org.concord.sensor;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.concord.sensor.device.impl.DeviceID;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class DeviceFinder {
	private static final Logger logger = Logger.getLogger(DeviceFinder.class.getName());

	public static int[] getAttachedDeviceTypes() throws LibUsbException {
		ArrayList<Integer> foundDevices = new ArrayList<Integer>();

		// Create the libusb context
		final Context context = new Context();

		// Initialize the libusb context
		int result = LibUsb.init(context);
		if (result < 0) {
			throw new LibUsbException("Unable to initialize libusb", result);
		}

		// Read the USB device list
		DeviceList list = new DeviceList();
		result = LibUsb.getDeviceList(context, list);
		if (result < 0) {
			throw new LibUsbException("Unable to get device list", result);
		}

		try {
			logger.fine("There are " + list.getSize() + " devices.");
			// Iterate over all devices and dump them
			for (Device device : list) {
				// Read the device descriptor
				DeviceDescriptor descriptor = new DeviceDescriptor();
				result = LibUsb.getDeviceDescriptor(device, descriptor);
				if (result < 0) {
					throw new LibUsbException("Unable to read device descriptor", result);
				}
				switch (descriptor.idVendor()) {
				case 0x08F7: // Vernier
					switch (descriptor.idProduct()) {
					case 0x0001:
						logger.fine("Found a Vernier LabPro!");
						foundDevices.add(DeviceID.VERNIER_LAB_PRO);
						break;
					case 0x0002: // GoTemp
						logger.fine("Found a Vernier GoTemp!");
						foundDevices.add(DeviceID.VERNIER_GO_LINK_JNA);
						break;
					case 0x0003: // GoLink
						logger.fine("Found a Vernier GoLink!");
						foundDevices.add(DeviceID.VERNIER_GO_LINK_JNA);
						break;
					case 0x0004: // GoMotion
						logger.fine("Found a Vernier GoMotion!");
						foundDevices.add(DeviceID.VERNIER_GO_LINK_JNA);
						break;
					case 0x0005: // LabQuest
						logger.fine("Found a Vernier LabQuest!");
						foundDevices.add(DeviceID.VERNIER_LAB_QUEST);
						break;
					case 0x0008: // LabQuest Mini
						logger.fine("Found a Vernier LabQuest Mini!");
						foundDevices.add(DeviceID.VERNIER_LAB_QUEST);
						break;
					}
					break;
				case 0x0000: // Pasco
					// Right now, we don't differentiate between Pasco USB
					// devices, so no need to check product ids
					logger.fine("Found a Pasco!");
					foundDevices.add(DeviceID.PASCO_USB);
					break;
				default:
					break;
				}
			}
		} finally {
			// Ensure the allocated device list is freed
			LibUsb.freeDeviceList(list, false);
		}

		// Deinitialize the libusb context
		LibUsb.exit(context);

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
}
