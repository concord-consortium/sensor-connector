package org.concord.usb;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class UsbDeviceList {
	private static final Logger logger = Logger.getLogger(UsbDeviceList.class.getName());

	public static ArrayList<UsbDevice> getAttachedDevices() throws UsbException{
		ArrayList<UsbDevice> devices = new ArrayList<UsbDevice>();
		
		try {
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
					UsbDevice usbDevice = new UsbDevice(descriptor.idVendor(), descriptor.idProduct());
					devices.add(usbDevice);
				}
			} finally {
				// Ensure the allocated device list is freed
				LibUsb.freeDeviceList(list, false);
			}
	
			// Deinitialize the libusb context
			LibUsb.exit(context);
		} catch (LibUsbException e){
			throw new UsbException(e);
		}
		return devices;
	}
	
	public static void main(String[] args) throws UsbException {
		ArrayList<UsbDevice> attachedDevices = getAttachedDevices();
		System.out.println("VendID ProdID");
		for (UsbDevice usbDevice : attachedDevices) {
			System.out.format("0x%04x 0x%04x\n", usbDevice.vendorId, usbDevice.productId);
		}
	}
}
