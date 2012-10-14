package org.openspaces.eviction.test.data;


//@Embeddable
public class Payload {
	//@Id
	private Integer id;
	private byte[] payload;
	
	public Payload(Integer id, int size) {
		this.id = id;
		this.payload = new byte[size];
	}
	
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public byte[] getPayload() {
		return payload;
	}
	
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	
}
