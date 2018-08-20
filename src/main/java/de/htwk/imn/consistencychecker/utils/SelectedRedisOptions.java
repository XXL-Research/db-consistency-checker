package de.htwk.imn.consistencychecker.utils;

import org.redisson.config.ReadMode;

public class SelectedRedisOptions extends SelectedOptions {

	private Boolean synchronReplication;
	private int numslaves, timeout;
	private ReadMode readMode;

	private static final String SYNCHRON = "synchron";
	private static final String ASYNCHRON = "asynchron";

	private static final String MASTER = "MASTER";
	private static final String SLAVE = "SLAVE";
	private static final String MASTER_SLAVE = "MASTER_SLAVE";

	public Boolean getSynchronReplication() {
		return synchronReplication;
	}

	public void setSynchronReplication(String replication) {
		switch (replication) {
		case SYNCHRON:
			this.synchronReplication = true;
			break;
		case ASYNCHRON:
			this.synchronReplication = false;
			break;
		default:
			this.synchronReplication = false;
			break;
		}
	}

	public ReadMode getReadMode() {
		return readMode;
	}

	public void setReadMode(String readMode) {
		switch (readMode) {
		case MASTER:
			this.readMode = ReadMode.MASTER;
			break;
		case SLAVE:
			this.readMode = ReadMode.SLAVE;
			break;
		case MASTER_SLAVE:
			this.readMode = ReadMode.MASTER_SLAVE;
			break;
		default:
			this.readMode = ReadMode.SLAVE;
			break;
		}
	}

	public int getNumslaves() {
		return numslaves;
	}

	public void setNumslaves(int numslaves) {
		this.numslaves = numslaves;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
