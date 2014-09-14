/*
 * Created by Daniel Marell 2011-08-28 10:19
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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface LibUsb extends Library {
  LibUsb libUsb = (LibUsb) Native.loadLibrary("usb-1.0", LibUsb.class);

  /*
   Library initialization/deinitialization
  */

  /**
   * Set message verbosity.
   * <p/>
   * Level 0: no messages ever printed by the library (default)
   * Level 1: error messages are printed to stderr
   * Level 2: warning and error messages are printed to stderr
   * Level 3: informational messages are printed to stdout, warning and error messages are printed to stderr
   * The default level is 0, which means no messages are ever printed. If you choose to increase the message
   * verbosity level, ensure that your application does not close the stdout/stderr file descriptors.
   * <p/>
   * You are advised to set level 3. libusb is conservative with its message logging and most of the time, will
   * only log messages that explain error conditions and other oddities. This will help you debug your software.
   * <p/>
   * If the LIBUSB_DEBUG environment variable was set when libusb was initialized, this function does nothing:
   * the message verbosity is fixed to the value in the environment variable.
   * <p/>
   * If libusb was compiled without any message logging, this function does nothing: you'll never get any messages.
   * <p/>
   * If libusb was compiled with verbose debug message logging, this function does nothing: you'll always get
   * messages from all levels.
   *
   * @param context the context to operate on, or null for the default context
   * @param level   debug level to set
   */
  void libusb_set_debug(Pointer context, int level);

  /**
   * Initialize libusb.
   * <p/>
   * This function must be called before calling any other libusb function.
   *
   * @param context Optional output location for context pointer. Only valid on return code 0.
   * @return 0 on success, or a LIBUSB_ERROR code on failure
   */
  int libusb_init(Pointer[] context);

  /**
   * Deinitialize libusb.
   * <p/>
   * Should be called after closing all open devices and before your application terminates.
   *
   * @param context the context to deinitialize, or null for the default context
   */
  void libusb_exit(Pointer context);

  /*
   Device handling and enumeration
  */

  /**
   * Returns a list of USB devices currently attached to the system.
   * <p/>
   * This is your entry point into finding a USB device to operate.
   * <p/>
   * You are expected to unreference all the devices when you are done with them, and then free the list
   * with libusb_free_device_list(). Note that libusb_free_device_list() can unref all the devices for you.
   * Be careful not to unreference a device you are about to open until after you have opened it.
   * <p/>
   * This return value of this function indicates the number of devices in the resultant list. The list is
   * actually one element larger, as it is NULL-terminated.
   *
   * @param context the context to operate on, or null for the default context
   * @param list    output location for a list of devices. Must be later freed with libusb_free_device_list().
   * @return the number of devices in the outputted list, or LIBUSB_ERROR_NO_MEM on memory allocation failure.
   */
  int libusb_get_device_list(Pointer context, Pointer[] list);

  /**
   * Frees a list of devices previously discovered using libusb_get_device_list().
   * <p/>
   * If the unref_devices parameter is set, the reference count of each device in the list is decremented by 1.
   *
   * @param context       the context to operate on, or null for the default context
   * @param list          the list to free
   * @param unref_devices whether to unref the devices in the list
   */
  void libusb_free_device_list(Pointer context, Pointer list, int unref_devices);

  /**
   * Get the number of the bus that a device is connected to.
   *
   * @param usb_device a device
   * @return the bus number
   */
  int libusb_get_bus_number(Pointer usb_device);

  /**
   * Get the address of the device on the bus it is connected to.
   *
   * @param usb_device a device
   * @return the device address
   */
  int libusb_get_device_address(Pointer usb_device);

  /**
   * Convenience function to retrieve the wMaxPacketSize value for a particular endpoint in the active
   * device configuration.
   * <p/>
   * This function was originally intended to be of assistance when setting up isochronous transfers,
   * but a design mistake resulted in this function instead. It simply returns the wMaxPacketSize value
   * without considering its contents. If you're dealing with isochronous transfers, you probably want
   * libusb_get_max_iso_packet_size() instead.
   *
   * @param usb_device a device
   * @param endpoint   address of the endpoint in question
   * @return the wMaxPacketSize value
   *         LIBUSB_ERROR_NOT_FOUND if the endpoint does not exist
   *         LIBUSB_ERROR_OTHER on other failure
   */
  int libusb_get_max_packet_size(Pointer usb_device, int endpoint);

  /**
   * Calculate the maximum packet size which a specific endpoint is capable is sending or receiving in the
   * duration of 1 microframe.
   * <p/>
   * Only the active configution is examined. The calculation is based on the wMaxPacketSize field in the
   * endpoint descriptor as described in section 9.6.6 in the USB 2.0 specifications.
   * <p/>
   * If acting on an isochronous or interrupt endpoint, this function will multiply the value found in bits
   * 0:10 by the number of transactions per microframe (determined by bits 11:12). Otherwise, this function
   * just returns the numeric value found in bits 0:10.
   * <p/>
   * This function is useful for setting up isochronous transfers, for example you might pass the return
   * value from this function to libusb_set_iso_packet_lengths() in order to set the length field of every
   * isochronous packet in a transfer.
   * <p/>
   * Since v1.0.3.
   *
   * @param usb_device a device
   * @param endpoint   address of the endpoint in question
   * @return the maximum packet size which can be sent/received on this endpoint
   *         LIBUSB_ERROR_NOT_FOUND if the endpoint does not exist
   *         LIBUSB_ERROR_OTHER on other failure
   */
  int libusb_get_max_iso_packet_size(Pointer usb_device, int endpoint);

  /**
   * Increment the reference count of a device.
   *
   * @param usb_device the device to reference
   * @return the same device
   */
  Pointer libusb_ref_device(Pointer usb_device);

  /**
   * Decrement the reference count of a device. If the decrement operation causes the reference count
   * to reach zero, the device shall be destroyed.
   *
   * @param usb_device the device to unreference
   */
  void libusb_unref_device(Pointer usb_device);

  /**
   * Open a device and obtain a device handle.
   * <p/>
   * A handle allows you to perform I/O on the device in question.
   * <p/>
   * Internally, this function adds a reference to the device and makes it available to you through
   * libusb_get_device(). This reference is removed during libusb_close().
   * <p/>
   * This is a non-blocking function; no requests are sent over the bus.
   *
   * @param usb_device the device to open
   * @param dev_handle output location for the returned device handle pointer. Only populated when the return code is 0.
   * @return 0 on success
   *         LIBUSB_ERROR_NO_MEM on memory allocation failure
   *         LIBUSB_ERROR_ACCESS if the user has insufficient permissions
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         another LIBUSB_ERROR code on other failure
   */
  int libusb_open(Pointer usb_device, Pointer[] dev_handle);

  /**
   * Convenience function for finding a device with a particular idVendor/idProduct combination.
   * <p/>
   * This function is intended for those scenarios where you are using libusb to knock up a quick test
   * application - it allows you to avoid calling libusb_get_device_list() and worrying about
   * traversing/freeing the list.
   * <p/>
   * This function has limitations and is hence not intended for use in real applications: if multiple   7
   * devices have the same IDs it will only give you the first one, etc.
   *
   * @param context    the context to operate on, or null for the default context
   * @param vendor_id  the idVendor value to search for
   * @param product_id the idProduct value to search for
   * @return a handle for the first found device, or null on error or if the device could not be found.
   */
  Pointer libusb_open_device_with_vid_pid(Pointer context, int vendor_id, int product_id);

  /**
   * Close a device handle.
   * <p/>
   * Should be called on all open handles before your application exits.
   * <p/>
   * Internally, this function destroys the reference that was added by libusb_open() on the given device.
   * <p/>
   * This is a non-blocking function; no requests are sent over the bus.
   *
   * @param dev_handle the handle to close
   */
  void libusb_close(Pointer dev_handle);

  /**
   * Get the underlying device for a handle.
   * <p/>
   * This function does not modify the reference count of the returned device, so do not feel compelled to
   * unreference it when you are done.
   *
   * @param dev_handle a device handle
   * @return the underlying device
   */
  Pointer libusb_get_device(Pointer dev_handle);

  /**
   * Determine the bConfigurationValue of the currently active configuration.
   * <p/>
   * You could formulate your own control request to obtain this information, but this function has the
   * advantage that it may be able to retrieve the information from operating system caches (no I/O involved).
   * <p/>
   * If the OS does not cache this information, then this function will block while a control transfer is
   * submitted to retrieve the information.
   * <p/>
   * This function will return a value of 0 in the config output parameter if the device is in unconfigured
   * state.
   *
   * @param dev_handle a device handle
   * @param config     output location for the bConfigurationValue of the active configuration (only valid for return code 0)
   * @return 0 on success
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         another LIBUSB_ERROR code on other failure
   */
  int libusb_get_configuration(Pointer dev_handle, int[] config);

  /**
   * Set the active configuration for a device.
   * <p/>
   * The operating system may or may not have already set an active configuration on the device. It is up to
   * your application to ensure the correct configuration is selected before you attempt to claim interfaces
   * and perform other operations.
   * <p/>
   * If you call this function on a device already configured with the selected configuration, then this
   * function will act as a lightweight device reset: it will issue a SET_CONFIGURATION request using the
   * current configuration, causing most USB-related device state to be reset (altsetting reset to zero,
   * endpoint halts cleared, toggles reset).
   * <p/>
   * You cannot change/reset configuration if your application has claimed interfaces - you should free them
   * with libusb_release_interface() first. You cannot change/reset configuration if other applications or
   * drivers have claimed interfaces.
   * <p/>
   * A configuration value of -1 will put the device in unconfigured state. The USB specifications state
   * that a configuration value of 0 does this, however buggy devices exist which actually have a configuration 0.
   * <p/>
   * You should always use this function rather than formulating your own SET_CONFIGURATION control request.
   * This is because the underlying operating system needs to know when such changes happen.
   * <p/>
   * This is a blocking function.
   *
   * @param dev_handle    a device handle
   * @param configuration the bConfigurationValue of the configuration you wish to activate, or -1 if you wish to put the device in unconfigured state
   * @return 0 on success
   *         LIBUSB_ERROR_NOT_FOUND if the requested configuration does not exist
   *         LIBUSB_ERROR_BUSY if interfaces are currently claimed
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         another LIBUSB_ERROR code on other failure
   */
  int libusb_set_configuration(Pointer dev_handle, int configuration);

  /**
   * Claim an interface on a given device handle.
   * <p/>
   * You must claim the interface you wish to use before you can perform I/O on any of its endpoints.
   * <p/>
   * It is legal to attempt to claim an already-claimed interface, in which case libusb just returns
   * 0 without doing anything.
   * <p/>
   * Claiming of interfaces is a purely logical operation; it does not cause any requests to be sent
   * over the bus. Interface claiming is used to instruct the underlying operating system that your
   * application wishes to take ownership of the interface.
   * <p/>
   * This is a non-blocking function.
   *
   * @param dev_handle       a device handle
   * @param interface_number the bInterfaceNumber of the interface you wish to claim
   * @return 0 on success
   *         LIBUSB_ERROR_NOT_FOUND if the requested interface does not exist
   *         LIBUSB_ERROR_BUSY if another program or driver has claimed the interface
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         a LIBUSB_ERROR code on other failure
   */
  int libusb_claim_interface(Pointer dev_handle, int interface_number);

  /**
   * Release an interface previously claimed with libusb_claim_interface().
   * <p/>
   * You should release all claimed interfaces before closing a device handle.
   * <p/>
   * This is a blocking function. A SET_INTERFACE control request will be sent to the device,
   * resetting interface state to the first alternate setting.
   *
   * @param dev_handle       a device handle
   * @param interface_number the bInterfaceNumber of the previously-claimed interface
   * @return 0 on success
   *         LIBUSB_ERROR_NOT_FOUND if the interface was not claimed
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         another LIBUSB_ERROR code on other failure
   */
  int libusb_release_interface(Pointer dev_handle, int interface_number);

  /**
   * Activate an alternate setting for an interface.
   * <p/>
   * The interface must have been previously claimed with libusb_claim_interface().
   * <p/>
   * You should always use this function rather than formulating your own SET_INTERFACE control request.
   * This is because the underlying operating system needs to know when such changes happen.
   * <p/>
   * This is a blocking function.
   *
   * @param dev_handle        a device handle
   * @param interface_number  the bInterfaceNumber of the previously-claimed interface
   * @param alternate_setting the bAlternateSetting of the alternate setting to activate
   * @return 0 on success
   *         LIBUSB_ERROR_NOT_FOUND if the interface was not claimed, or the requested alternate setting does not exist
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         another LIBUSB_ERROR code on other failure
   */
  int libusb_set_interface_alt_setting(Pointer dev_handle, int interface_number, int alternate_setting);

  /**
   * Clear the halt/stall condition for an endpoint.
   * <p/>
   * Endpoints with halt status are unable to receive or transmit data until the halt condition is stalled.
   * <p/>
   * You should cancel all pending transfers before attempting to clear the halt condition.
   * <p/>
   * This is a blocking function.
   *
   * @param dev_handle a device handle
   * @param endpoint   the endpoint to clear halt status
   * @return 0 on success
   *         LIBUSB_ERROR_NOT_FOUND if the endpoint does not exist
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         another LIBUSB_ERROR code on other failure
   */
  int libusb_clear_halt(Pointer dev_handle, byte endpoint);

  /**
   * Perform a USB port reset to reinitialize a device.
   * <p/>
   * The system will attempt to restore the previous configuration and alternate settings
   * after the reset has completed.
   * <p/>
   * If the reset fails, the descriptors change, or the previous state cannot be restored,
   * the device will appear to be disconnected and reconnected. This means that the device
   * handle is no longer valid (you should close it) and rediscover the device. A return
   * code of LIBUSB_ERROR_NOT_FOUND indicates when this is the case.
   * <p/>
   * This is a blocking function which usually incurs a noticeable delay.
   *
   * @param dev_handle a handle of the device to reset
   * @return 0 on success
   *         LIBUSB_ERROR_NOT_FOUND if re-enumeration is required, or if the device has been disconnected
   *         another LIBUSB_ERROR code on other failure
   */
  int libusb_reset_device(Pointer dev_handle);

  /**
   * Determine if a kernel driver is active on an interface.
   * <p/>
   * If a kernel driver is active, you cannot claim the interface, and libusb will be unable to perform I/O.
   *
   * @param dev_handle       a device handle
   * @param interface_number the interface to check
   * @return 0 if no kernel driver is active
   *         1 if a kernel driver is active
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         another LIBUSB_ERROR code on other failure
   */
  int libusb_kernel_driver_active(Pointer dev_handle, int interface_number);

  /**
   * Detach a kernel driver from an interface.
   * <p/>
   * If successful, you will then be able to claim the interface and perform I/O.
   *
   * @param dev_handle       a device handle
   * @param interface_number the interface to detach the driver from
   * @return 0 on success
   *         LIBUSB_ERROR_NOT_FOUND if no kernel driver was active
   *         LIBUSB_ERROR_INVALID_PARAM if the interface does not exist
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         another LIBUSB_ERROR code on other failure
   */
  int libusb_detach_kernel_driver(Pointer dev_handle, int interface_number);

  /**
   * Re-attach an interface's kernel driver, which was previously detached using libusb_detach_kernel_driver().
   *
   * @param dev_handle       a device handle
   * @param interface_number the interface to attach the driver from
   * @return 0 on success
   *         LIBUSB_ERROR_NOT_FOUND if no kernel driver was active
   *         LIBUSB_ERROR_INVALID_PARAM if the interface does not exist
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         LIBUSB_ERROR_BUSY if the driver cannot be attached because the interface is claimed by a program or driver
   *         another LIBUSB_ERROR code on other failure
   */
  int libusb_attach_kernel_driver(Pointer dev_handle, int interface_number);

  /**
   * Get the USB device descriptor for a given device.
   * <p/>
   * This is a non-blocking function; the device descriptor is cached in memory.
   *
   * @param usb_device the device
   * @param desc       output location for the descriptor data
   * @return 0 on success or a LIBUSB_ERROR code on failure
   */
  int libusb_get_device_descriptor(Pointer usb_device, libusb_device_descriptor[] desc);

  /**
   * Retrieve a string descriptor in C style ASCII.
   * <p/>
   * Wrapper around libusb_get_string_descriptor(). Uses the first language supported by the device.
   *
   * @param dev_handle a device handle
   * @param desc_index the index of the descriptor to retrieve
   * @param data       output buffer for ASCII string descriptor
   * @param length     size of data buffer
   * @return number of bytes returned in data, or LIBUSB_ERROR code on failure
   */
  int libusb_get_string_descriptor_ascii(Pointer dev_handle, byte desc_index, byte[] data, int length);

  /*
  Asynchronous device I/O.
  Todo: Implement
  struct libusb_transfer * 	libusb_alloc_transfer (int iso_packets)
  void 	libusb_free_transfer (struct libusb_transfer *transfer)
  int 	libusb_submit_transfer (struct libusb_transfer *transfer)
  int 	libusb_cancel_transfer (struct libusb_transfer *transfer)
  static unsigned char * 	libusb_control_transfer_get_data (struct libusb_transfer *transfer)
  static struct libusb_control_setup * 	libusb_control_transfer_get_setup (struct libusb_transfer *transfer)
  static void 	libusb_fill_control_setup (unsigned char *buffer, uint8_t bmRequestType, uint8_t bRequest, uint16_t wValue, uint16_t wIndex, uint16_t wLength)
  static void 	libusb_fill_control_transfer (struct libusb_transfer *transfer, libusb_device_handle *dev_handle, unsigned char *buffer, libusb_transfer_cb_fn callback, void *user_data, unsigned int timeout)
  static void 	libusb_fill_bulk_transfer (struct libusb_transfer *transfer, libusb_device_handle *dev_handle, unsigned char endpoint, unsigned char *buffer, int length, libusb_transfer_cb_fn callback, void *user_data, unsigned int timeout)
  static void 	libusb_fill_interrupt_transfer (struct libusb_transfer *transfer, libusb_device_handle *dev_handle, unsigned char endpoint, unsigned char *buffer, int length, libusb_transfer_cb_fn callback, void *user_data, unsigned int timeout)
  static void 	libusb_fill_iso_transfer (struct libusb_transfer *transfer, libusb_device_handle *dev_handle, unsigned char endpoint, unsigned char *buffer, int length, int num_iso_packets, libusb_transfer_cb_fn callback, void *user_data, unsigned int timeout)
  static void 	libusb_set_iso_packet_lengths (struct libusb_transfer *transfer, unsigned int length)
  static unsigned char * 	libusb_get_iso_packet_buffer (struct libusb_transfer *transfer, unsigned int packet)
  static unsigned char * 	libusb_get_iso_packet_buffer_simple (struct libusb_transfer *transfer, unsigned int packet)
  */

  /*
  Polling and timing:
  Todo: Implement
  int 	libusb_try_lock_events (libusb_context *ctx)
  void 	libusb_lock_events (libusb_context *ctx)
  void 	libusb_unlock_events (libusb_context *ctx)
  int 	libusb_event_handling_ok (libusb_context *ctx)
  int 	libusb_event_handler_active (libusb_context *ctx)
  void 	libusb_lock_event_waiters (libusb_context *ctx)
  void 	libusb_unlock_event_waiters (libusb_context *ctx)
  int 	libusb_wait_for_event (libusb_context *ctx, struct timeval *tv)
  int 	libusb_handle_events_timeout (libusb_context *ctx, struct timeval *tv)
  int 	libusb_handle_events (libusb_context *ctx)
  int 	libusb_handle_events_locked (libusb_context *ctx, struct timeval *tv)
  int 	libusb_pollfds_handle_timeouts (libusb_context *ctx)
  int 	libusb_get_next_timeout (libusb_context *ctx, struct timeval *tv)
  void 	libusb_set_pollfd_notifiers (libusb_context *ctx, libusb_pollfd_added_cb added_cb, libusb_pollfd_removed_cb removed_cb, void *user_data)
  struct libusb_pollfd ** 	libusb_get_pollfds (libusb_context *ctx)
  */

  /*
  Synchronous device I/O:
  */

  /**
   * Perform a USB control transfer.
   * <p/>
   * The direction of the transfer is inferred from the bmRequestType field of the setup packet.
   * <p/>
   * The wValue, wIndex and wLength fields values should be given in host-endian byte order.
   *
   * @param dev_handle    a handle for the device to communicate with
   * @param bmRequestType the request type field for the setup packet
   * @param bRequest      the request field for the setup packet
   * @param wValue        the value field for the setup packet
   * @param wIndex        the index field for the setup packet
   * @param data          a suitably-sized data buffer for either input or output (depending on direction bits within bmRequestType)
   * @param wLength       the length field for the setup packet. The data buffer should be at least this size.
   * @param timeout       timeout (in millseconds) that this function should wait before giving up due to no response
   *                      being received. For an unlimited timeout, use value 0.
   * @return on success, the number of bytes actually transferred
   *         LIBUSB_ERROR_TIMEOUT if the transfer timed out
   *         LIBUSB_ERROR_PIPE if the control request was not supported by the device
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         another LIBUSB_ERROR code on other failures
   */
  int libusb_control_transfer(Pointer dev_handle, byte bmRequestType, byte bRequest, short wValue, short wIndex, byte[] data, short wLength, int timeout);

  /**
   * Perform a USB bulk transfer.
   * <p/>
   * The direction of the transfer is inferred from the direction bits of the endpoint address.
   * <p/>
   * For bulk reads, the length field indicates the maximum length of data you are expecting to receive.
   * If less data arrives than expected, this function will return that data, so be sure to check the
   * transferred output parameter.
   * <p/>
   * You should also check the transferred parameter for bulk writes. Not all of the data may have been
   * written.
   * <p/>
   * Also check transferred when dealing with a timeout error code. libusb may have to split your transfer
   * into a number of chunks to satisfy underlying O/S requirements, meaning that the timeout may expire
   * after the first few chunks have completed. libusb is careful not to lose any data that may have been
   * transferred; do not assume that timeout conditions indicate a complete lack of I/O.
   *
   * @param dev_handle  a handle for the device to communicate with
   * @param endpoint    the address of a valid endpoint to communicate with
   * @param data        a suitably-sized data buffer for either input or output (depending on endpoint)
   * @param length      for bulk writes, the number of bytes from data to be sent. for bulk reads, the maximum number of bytes to receive into the data buffer.
   * @param transferred output location for the number of bytes actually transferred.
   * @param timeout     timeout (in millseconds) that this function should wait before giving up due to no response being received. For an unlimited timeout, use value 0.
   * @return 0 on success (and populates transferred)
   *         LIBUSB_ERROR_TIMEOUT if the transfer timed out (and populates transferred)
   *         LIBUSB_ERROR_PIPE if the endpoint halted
   *         LIBUSB_ERROR_OVERFLOW if the device offered more data, see Packets and overflows
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         another LIBUSB_ERROR code on other failures
   */
  int libusb_bulk_transfer(Pointer dev_handle, byte endpoint, byte[] data, int length, int[] transferred, int timeout);

  /**
   * Perform a USB interrupt transfer.
   * <p/>
   * The direction of the transfer is inferred from the direction bits of the endpoint address.
   * <p/>
   * For interrupt reads, the length field indicates the maximum length of data you are expecting to receive.
   * If less data arrives than expected, this function will return that data, so be sure to check the transferred
   * output parameter.
   * <p/>
   * You should also check the transferred parameter for interrupt writes. Not all of the data may have been written.
   * <p/>
   * Also check transferred when dealing with a timeout error code. libusb may have to split your transfer into
   * a number of chunks to satisfy underlying O/S requirements, meaning that the timeout may expire after the
   * first few chunks have completed. libusb is careful not to lose any data that may have been transferred;
   * do not assume that timeout conditions indicate a complete lack of I/O.
   * <p/>
   * The default endpoint bInterval value is used as the polling interval.*
   *
   * @param dev_handle  a handle for the device to communicate with
   * @param endpoint    the address of a valid endpoint to communicate with
   * @param data        a suitably-sized data buffer for either input or output (depending on endpoint)
   * @param length      for bulk writes, the number of bytes from data to be sent. for bulk reads, the maximum
   *                    number of bytes to receive into the data buffer.
   * @param transferred output location for the number of bytes actually transferred.
   * @param timeout     timeout (in millseconds) that this function should wait before giving up due to no response
   *                    being received. For an unlimited timeout, use value 0.
   * @return 0 on success (and populates transferred)
   *         LIBUSB_ERROR_TIMEOUT if the transfer timed out
   *         LIBUSB_ERROR_PIPE if the endpoint halted
   *         LIBUSB_ERROR_OVERFLOW if the device offered more data, see Packets and overflows
   *         LIBUSB_ERROR_NO_DEVICE if the device has been disconnected
   *         another LIBUSB_ERROR code on other error
   */
  int libusb_interrupt_transfer(Pointer dev_handle, byte endpoint, byte[] data, int length, int[] transferred, int timeout);
}
