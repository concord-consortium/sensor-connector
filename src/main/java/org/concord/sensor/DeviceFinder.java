package org.concord.sensor;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.concord.sensor.device.impl.DeviceID;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;


public class DeviceFinder {
	private static final Logger logger = Logger.getLogger(DeviceFinder.class);
	
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
			logger.debug("There are " + list.getSize() + " devices.");
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
						switch(descriptor.idProduct()) {
							case 0x0001:
								logger.debug("Found a Vernier LabPro!");
								foundDevices.add(DeviceID.VERNIER_LAB_PRO);
								break;
							case 0x0002: // GoTemp
								logger.debug("Found a Vernier GoTemp!");
								foundDevices.add(DeviceID.VERNIER_GO_LINK_JNA);
								break;
							case 0x0003: // GoLink
								logger.debug("Found a Vernier GoLink!");
								foundDevices.add(DeviceID.VERNIER_GO_LINK_JNA);
								break;
							case 0x0004: // GoMotion
								logger.debug("Found a Vernier GoMotion!");
								foundDevices.add(DeviceID.VERNIER_GO_LINK_JNA);
								break;
							case 0x0005: // LabQuest
								logger.debug("Found a Vernier LabQuest!");
								foundDevices.add(DeviceID.VERNIER_LAB_QUEST);
								break;
							case 0x0008: // LabQuest Mini
								logger.debug("Found a Vernier LabQuest Mini!");
								foundDevices.add(DeviceID.VERNIER_LAB_QUEST);
								break;
						}
						break;
					case 0x0000: // Pasco
						// Right now, we don't differentiate between Pasco USB devices, so no need to check product ids
						logger.debug("Found a Pasco!");
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

}
