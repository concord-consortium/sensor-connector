package org.concord.sensor.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.usb4java.ConfigDescriptor;
import org.usb4java.Context;
import org.usb4java.DescriptorUtils;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

@RunWith(JUnit4.class)
public class UsbEnumeratorTest {
	@Test
	public void enumerateUSB() {
		// Create the libusb context
		final Context context = new Context();

		// Initialize the libusb context
		int result = LibUsb.init(context);
		if (result < 0) {
			throw new LibUsbException("Unable to initialize libusb", result);
		}

		// Read the USB device list
		final DeviceList list = new DeviceList();
		result = LibUsb.getDeviceList(context, list);
		if (result < 0) {
			throw new LibUsbException("Unable to get device list", result);
		}

		try {
			System.out.println("There are " + list.getSize() + " devices.");
			// Iterate over all devices and dump them
			for (Device device : list) {
				dumpDevice(device);
			}
		} finally {
			// Ensure the allocated device list is freed
			LibUsb.freeDeviceList(list, true);
		}

		// Deinitialize the libusb context
		LibUsb.exit(context);
	}

	/**
	 * Dumps all configuration descriptors of the specified device. Because
	 * libusb descriptors are connected to each other (Configuration descriptor
	 * references interface descriptors which reference endpoint descriptors)
	 * dumping a configuration descriptor also dumps all interface and endpoint
	 * descriptors in this configuration.
	 * 
	 * @param device
	 *            The USB device.
	 * @param numConfigurations
	 *            The number of configurations to dump (Read from the device
	 *            descriptor)
	 */
	public static void dumpConfigurationDescriptors(final Device device, final int numConfigurations) {
		for (byte i = 0; i < numConfigurations; i += 1) {
			final ConfigDescriptor descriptor = new ConfigDescriptor();
			final int result = LibUsb.getConfigDescriptor(device, i, descriptor);
			if (result < 0) {
				System.err.println("Unable to read config descriptor " + i);
			} else {
				try {
					System.out.println(descriptor.dump().replaceAll("(?m)^", "  "));
				} finally {
					// Ensure that the config descriptor is freed
					LibUsb.freeConfigDescriptor(descriptor);
				}
			}
		}
	}

	/**
	 * Dumps the specified device to stdout.
	 * 
	 * @param device
	 *            The device to dump.
	 */
	public static void dumpDevice(final Device device) {
		// Dump device address and bus number
		final int address = LibUsb.getDeviceAddress(device);
		final int busNumber = LibUsb.getBusNumber(device);
		System.out.println(String.format("Device %03d/%03d", busNumber, address));

		// Dump port number if available
		final int portNumber = LibUsb.getPortNumber(device);
		if (portNumber != 0)
			System.out.println("Connected to port: " + portNumber);

		// Dump parent device if available
		final Device parent = LibUsb.getParent(device);
		if (parent != null) {
			final int parentAddress = LibUsb.getDeviceAddress(parent);
			final int parentBusNumber = LibUsb.getBusNumber(parent);
			System.out.println(String.format("Parent: %03d/%03d", parentBusNumber, parentAddress));
		}

		// Dump the device speed
		System.out.println("Speed: " + DescriptorUtils.getSpeedName(LibUsb.getDeviceSpeed(device)));

		// Read the device descriptor
		final DeviceDescriptor descriptor = new DeviceDescriptor();
		int result = LibUsb.getDeviceDescriptor(device, descriptor);
		if (result < 0) {
			throw new LibUsbException("Unable to read device descriptor", result);
		}

		// Try to open the device. This may fail because user has no
		// permission to communicate with the device. This is not
		// important for the dumps, we are just not able to resolve string
		// descriptor numbers to strings in the descriptor dumps.
		DeviceHandle handle = new DeviceHandle();
		result = LibUsb.open(device, handle);
		if (result < 0) {
			System.out.println(String.format("Unable to open device: %s. " + "Continuing without device handle.", LibUsb.strError(result)));
			handle = null;
		}

		// Dump the device descriptor
		System.out.print(descriptor.dump(handle));

		// Dump all configuration descriptors
		try {
			dumpConfigurationDescriptors(device, descriptor.bNumConfigurations());
		} catch (LibUsbException e) {

		}

		// Close the device if it was opened
		if (handle != null) {
			LibUsb.close(handle);
		}
	}
}
