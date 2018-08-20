package de.htwk.imn.consistencychecker.utils;

import com.datastax.driver.core.ConsistencyLevel;

public class SelectedCassandraOptions extends SelectedOptions {

	private ConsistencyLevel writeConsistencyLevel, readConsistencyLevel;
	private String replicationStrategy, replicationFactor;

	public ConsistencyLevel getWriteConsistencyLevel() {
		return writeConsistencyLevel;
	}

	public void setWriteConsistencyLevel(String writeConsistencyLevel) {
		this.writeConsistencyLevel = ConsistencyLevel.valueOf(writeConsistencyLevel);
	}

	public ConsistencyLevel getReadConsistencyLevel() {
		return readConsistencyLevel;
	}

	public void setReadConsistencyLevel(String readConsistencyLevel) {
		this.readConsistencyLevel = ConsistencyLevel.valueOf(readConsistencyLevel);
	}

	public String getReplicationStrategy() {
		return replicationStrategy;
	}

	public void setReplicationStrategy(String replicationStrategy) {
		this.replicationStrategy = replicationStrategy;
	}

	public String getReplicationFactor() {
		return replicationFactor;
	}

	public void setReplicationFactor(String replicationFactor) {
		this.replicationFactor = replicationFactor;
	}

}
