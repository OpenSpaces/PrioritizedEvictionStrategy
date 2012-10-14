package org.openspaces.test.db.data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

@SpaceClass
@Entity
public class DataEntry implements Serializable{
	private static final long serialVersionUID = 7584368569212146521L;

	@Id
	private Integer id;
	private	String payload;
	
	public DataEntry(){}
	
	public DataEntry(Integer id) {
		super();
		this.id = id;
	}

	public DataEntry(Integer id, String payload) {
		super();
		this.id = id;
		this.payload = payload;
	}


	@SpaceId
	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getPayload() {
		return payload;
	}


	public void setPayload(String payload) {
		this.payload = payload;
	}
	
}
