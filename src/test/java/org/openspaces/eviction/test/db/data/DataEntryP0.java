package org.openspaces.eviction.test.db.data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.openspaces.eviction.OrderBy;
import org.openspaces.eviction.SpaceEvictionPriority;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
@SpaceEvictionPriority(priority = 0, orderBy = OrderBy.NONE)
@SpaceClass
@Entity
public class DataEntryP0 implements Serializable{
	private static final long serialVersionUID = 7584368569212146521L;

	@Id
	private Integer id;
	private	String payload;
	
	public DataEntryP0(){}
	
	public DataEntryP0(Integer id) {
		super();
		this.id = id;
	}

	public DataEntryP0(Integer id, String payload) {
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
