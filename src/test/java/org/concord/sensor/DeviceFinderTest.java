package org.concord.sensor;

import org.concord.usb.UsbException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DeviceFinderTest {

	@Test
	public void findDevices() throws UsbException {
		int[] attachedTypes = DeviceFinder.getAttachedDeviceTypes();
		
		System.err.println("Enumerating found types:");
		if (attachedTypes.length == 0) {
			System.err.println("  No interfaces found!");
		} else {
			for (int i = 0; i < attachedTypes.length; i++) {
				System.err.println("  Found type: " + attachedTypes[i]);
			}
		}
	}
}
