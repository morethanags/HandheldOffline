package com.huntloc.handheldoffline;

import java.util.UUID;

public class Journal {
	private String guid;
	private String badge;
	private long time;
	private String log;
	private String door;
	private boolean sent;
	private String name;
	private String descLog;
	public Journal(String badge, String log, String door, long time, String name, String descLog) {
		this.guid = UUID.randomUUID().toString();
		this.badge = badge;
		this.time = time;
		this.log = log;
		this.door = door;
		this.sent = false;
		this.name =  name;
		this.descLog = descLog;
	}

	public Journal(String guid, String badge, String log, String door, long time, boolean isSent, String name, String descLog) {
		this.guid = guid;
		this.badge = badge;
		this.time = time;
		this.log = log;
		this.door = door;
		this.sent = isSent;
		this.name =  name;
		this.descLog =  descLog;
	}

	public String getDescLog() {
		return descLog;
	}

	public void setDescLog(String descLog) {
		this.descLog = descLog;
	}

	public String getBadge() {
		return badge;
	}

	public void setBadge(String badge) {
		this.badge = badge;
	}

	public long getTime() {
		return time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public String getDoor() {
		return door;
	}

	public void setDoor(String door) {
		this.door = door;
	}

	public boolean isSent() {
		return sent;
	}

	public void setSent(boolean sent) {
		this.sent = sent;
	}

	public String getGuid() {
		return guid;
	}
	
	

}
