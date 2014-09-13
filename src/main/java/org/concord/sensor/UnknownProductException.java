package org.concord.sensor;

@SuppressWarnings("serial")
public class UnknownProductException extends Exception {

	private String vendorName;
	private short productId;

	public UnknownProductException(String vendorName, short productId) {
		this.vendorName = vendorName;
		this.productId = productId;
	}
	
	@Override
	public String getMessage() {
		return "vendor: " + vendorName + 
			   " productId: 0x" + Integer.toHexString( productId & 0xffff);
	}

}
