package com.gigaspaces.data;

import com.gigaspaces.annotation.pojo.SpaceClass;

@SpaceClass
public class Medal {
	String winnerName;
	double diameter;
	String sport;
	String contest;
	
	
	public Medal(){}


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