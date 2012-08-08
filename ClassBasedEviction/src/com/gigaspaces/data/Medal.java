package com.gigaspaces.data;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

@SpaceClass
public class Medal {
	Integer id;
	String winnerName;
	double diameter;
	String sport;
	String contest;
	
	public Medal(){}
	
	public Medal(int id){
		this.id = id;
	}

	@SpaceId
	public int getId() {
		return id;
	}


	public void setId(int id) {
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