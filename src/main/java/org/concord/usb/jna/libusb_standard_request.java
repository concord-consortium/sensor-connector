/*
 * Created by Daniel Marell 2011-08-28 16:18
 * http://marellbox.marell.se/bundles/libusb10j
 * http://blog.cag.se/?author=2
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011 Daniel Marell 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.concord.usb.jna;

public class libusb_standard_request {
  /**
   * Request status of the specific recipient
   */
  public static final int REQUEST_GET_STATUS = 0x00;

  /**
   * Clear or disable a specific feature
   */
  public static final int REQUEST_CLEAR_FEATURE = 0x01;

  /* 0x02 is reserved */

  /**
   * Set or enable a specific feature
   */
  public static final int REQUEST_SET_FEATURE = 0x03;

  /* 0x04 is reserved */

  /**
   * Set device address for all future accesses
   */
  public static final int REQUEST_SET_ADDRESS = 0x05;

  /**
   * Get the specified descriptor
   */
  public static final int REQUEST_GET_DESCRIPTOR = 0x06;

  /**
   * Used to update existing descriptors or add new descriptors
   */
  public static final int REQUEST_SET_DESCRIPTOR = 0x07;

  /**
   * Get the current device configuration value
   */
  public static final int REQUEST_GET_CONFIGURATION = 0x08;

  /**
   * Set device configuration
   */
  public static final int REQUEST_SET_CONFIGURATION = 0x09;

  /**
   * Return the selected alternate setting for the specified interface
   */
  public static final int REQUEST_GET_INTERFACE = 0x0A;

  /**
   * Select an alternate interface for the specified interface
   */
  public static final int REQUEST_SET_INTERFACE = 0x0B;

  /**
   * Set then report an endpoint's synchronization frame
   */
  public static final int REQUEST_SYNCH_FRAME = 0x0C;
}
