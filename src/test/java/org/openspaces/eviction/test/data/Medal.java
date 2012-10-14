/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All
 rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.openspaces.eviction.test.data;

import java.io.Serializable;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

@SpaceClass
public class Medal implements Serializable {
	private static final long serialVersionUID = -5787064331155789837L;

	@Id
	private Integer id;
	@Embedded
	private Payload weight;
	private String winnerName;
	private double diameter;
	private String sport;
	private String contest;
	
	public Medal(){}
	
	public Medal(Integer id){
		this.id = id;
	}

	@SpaceId
	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getWinnerName() {
		return winnerName;
	}


	public void setWinnerName(String winnerName) {
		this.winnerName = winnerName;
	}


	public double getDiameter() {
		return diameter;
	}


	public void setDiameter(double diameter) {
		this.diameter = diameter;
	}

	@OneToOne
	public Payload getWeight() {
		return weight;
	}

	public void setWeight(Payload weight) {
		this.weight = weight;
	}

	public String getSport() {
		return sport;
	}


	public void setSport(String sport) {
		this.sport = sport;
	}


	public String getContest() {
		return contest;
	}


	public void setContest(String contest) {
		this.contest = contest;
	}
	
}
