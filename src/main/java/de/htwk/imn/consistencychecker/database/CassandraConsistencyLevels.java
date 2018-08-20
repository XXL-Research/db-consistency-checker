package de.htwk.imn.consistencychecker.database;

public enum CassandraConsistencyLevels {

	ALL, QUORUM, LOCAL_QUORUM, EACH_QUORUM, THREE, TWO, ONE, LOCAL_ONE, ANY;
}
