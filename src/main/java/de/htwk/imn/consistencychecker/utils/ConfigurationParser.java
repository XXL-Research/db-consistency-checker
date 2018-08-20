package de.htwk.imn.consistencychecker.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.datastax.driver.core.ConsistencyLevel;

import de.htwk.imn.consistencychecker.database.CassandraConsistencyLevels;
import de.htwk.imn.consistencychecker.database.DataBases;

public class ConfigurationParser {

	private static final String RYWC = "RYWC";
	private static final String MWC = "MWC";
	private static final String MRC = "MRC";

	@SuppressWarnings("unlikely-arg-type")
	public SelectedOptions parse(String filename) throws ParseException {

		String jsonData = readFile(filename);
		JSONObject jsonObject = new JSONObject(jsonData);

		String database = jsonObject.getString("Database");
		if (database.equals(DataBases.Cassandra.toString())) {
			SelectedCassandraOptions selectedCassandraOptions = new SelectedCassandraOptions();
			selectedCassandraOptions.setDatabase(database);

			String replicationStrategy = jsonObject.getJSONObject("DatabaseConfig").getString("ReplicationStrategy");
			String replicationFactor = jsonObject.getJSONObject("DatabaseConfig").getString("ReplicationFactor");

			if (!replicationStrategy.equals("SimpleStrategy")
					&& !replicationStrategy.equals("NetworkTopologyStrategy")) {
				throw new ParseException(replicationStrategy
						+ " is not supported. Only SimpleStrategy and NetworkTopologyStrategy are valid values.");
			}

			selectedCassandraOptions.setReplicationStrategy(replicationStrategy);
			selectedCassandraOptions.setReplicationFactor(replicationFactor);

			String writeConsistencyLevel = jsonObject.getJSONObject("DatabaseConfig")
					.getString("WriteConsistencyLevel");
			String readConsistencyLevel = jsonObject.getJSONObject("DatabaseConfig").getString("ReadConsistencyLevel");
			if (!Arrays.stream(CassandraConsistencyLevels.values())
					.anyMatch(e -> e.name().equals(writeConsistencyLevel))
					|| !Arrays.stream(CassandraConsistencyLevels.values())
							.anyMatch(e -> e.name().equals(readConsistencyLevel))
					|| readConsistencyLevel.equals(ConsistencyLevel.ANY)) {
				throw new ParseException(writeConsistencyLevel
						+ " is not supported. Only ALL, QUORUM, LOCAL_QUORUM, EACH_QUORUM, THREE, TWO, ONE, LOCAL_ONE are supported for write and read consistency level. ANY is just supported for write consistency level.");
			}

			selectedCassandraOptions.setWriteConsistencyLevel(writeConsistencyLevel);
			selectedCassandraOptions.setReadConsistencyLevel(readConsistencyLevel);

			setOtherOptions(selectedCassandraOptions, jsonObject);

			return selectedCassandraOptions;

		} else if (database.equals(DataBases.MongoDB.toString())) {

			SelectedMongoDBOptions selectedMongoDBOptions = new SelectedMongoDBOptions();
			selectedMongoDBOptions.setDatabase(database);

			JSONObject databaseConfig = jsonObject.getJSONObject("DatabaseConfig");
			JSONObject writeConcern = databaseConfig.getJSONObject("WriteConcern");
			String w = writeConcern.getString("w");
			if ((w.equals(SelectedMongoDBOptions.WRITE_CONCERN_W0) || w.equals(SelectedMongoDBOptions.WRITE_CONCERN_W1)
					|| w.equals(SelectedMongoDBOptions.WRITE_CONCERN_W2)
					|| w.equals(SelectedMongoDBOptions.WRITE_CONCERN_W3)
					|| w.equals(SelectedMongoDBOptions.WRITE_CONCERN_MAJORITY))) {
				selectedMongoDBOptions.setWriteConcern(w, writeConcern.getBoolean("j"),
						writeConcern.getLong("wtimeout"));
			} else {
				throw new ParseException("WriteConcern: \"w\" must be \"0\", \"1\", \"2\", \"3\" or \"majority\".");
			}

			JSONObject readConcern = databaseConfig.getJSONObject("ReadConcern");
			Boolean causallyConsistent = readConcern.getBoolean("causalConsistent");
			String readC = readConcern.getString("level");
			if (!(readC.equals(SelectedMongoDBOptions.READ_CONCERN_DEFAULT))
					&& !(readC.equals(SelectedMongoDBOptions.READ_CONCERN_LINEARIZABLE))
					&& !(readC.equals(SelectedMongoDBOptions.READ_CONCERN_LOCAL))
					&& !(readC.equals(SelectedMongoDBOptions.READ_CONCERN_MAJORITY))) {
				throw new ParseException(
						"ReadConcern: \"level\" must be \"local\", \"default\", \"majority\" or \"linearizable\".");
			} else {
				if (causallyConsistent && (!(readC.equals(SelectedMongoDBOptions.READ_CONCERN_MAJORITY))
						&& !(readC.equals(SelectedMongoDBOptions.READ_CONCERN_LOCAL)))) {
					throw new ParseException(
							"For a causally constistent session only \"majority\" and \"local\" are allowed as read preference mode.");
				}
				selectedMongoDBOptions.setReadConcern(readC);
			}
			selectedMongoDBOptions.setCausallyConsistent(causallyConsistent);

			JSONObject readPreference = databaseConfig.getJSONObject("ReadPreference");
			if (readPreference.length() > 1) {
				Long maxStalenessSeconds = readPreference.getLong("maxStalenessSeconds");
				if (maxStalenessSeconds >= 90) {
					selectedMongoDBOptions.setReadPreferenceWithMaxStaleness(readPreference.getString("mode"),
							readPreference.getLong("maxStalenessSeconds"));
				} else {
					throw new ParseException("maxStalenessSeconds must be at least 90 seconds.");
				}
			}
			String mode = readPreference.getString("mode");
			if (!(mode.equals(SelectedMongoDBOptions.READ_PREFERENCE_NEAREST))
					&& !(mode.equals(SelectedMongoDBOptions.READ_PREFERENCE_PRIMARY))
					&& !(mode.equals(SelectedMongoDBOptions.READ_PREFERENCE_PRIMARY_PREFERRED))
					&& !(mode.equals(SelectedMongoDBOptions.READ_PREFERENCE_SECONDARY))
					&& !(mode.equals(SelectedMongoDBOptions.READ_PREFERENCE_SECONDARY_PREFERRED))) {
				throw new ParseException(
						"ReadReadPreference: \"mode\" must be \"nearest\", \"primary\", \"primaryPreferred\", \"secondary\" or \"secondaryPreferred\".");
			} else {
				selectedMongoDBOptions.setReadPreference(mode);
			}

			setOtherOptions(selectedMongoDBOptions, jsonObject);

			return selectedMongoDBOptions;

		} else if (database.equals(DataBases.Redis.toString())) {

			SelectedRedisOptions selectedRedisOptions = new SelectedRedisOptions();
			selectedRedisOptions.setDatabase(database);

			String replication = jsonObject.getJSONObject("DatabaseConfig").getString("Replication");
			if (!replication.equals("synchron") && !replication.equals("asynchron")) {
				throw new ParseException(
						replication + " is not supported. Only 'synchron' and 'asynchron' are valid values.");
			}
			selectedRedisOptions.setSynchronReplication(replication);
			if (replication.equals("synchron")) {
				try {
					int numslaves = jsonObject.getJSONObject("DatabaseConfig").getInt("numslaves");
					int timeout = jsonObject.getJSONObject("DatabaseConfig").getInt("timeout");
					selectedRedisOptions.setNumslaves(numslaves);
					selectedRedisOptions.setTimeout(timeout);
				} catch (JSONException e) {
					throw new ParseException("synchron replication: 'numslaves' and 'timeout' are mandatory.");
				}
			}

			String readMode = jsonObject.getJSONObject("DatabaseConfig").getString("ReadMode");
			if (!readMode.equals("SLAVE") && !readMode.equals("MASTER") && !readMode.equals("MASTER_SLAVE")) {
				throw new ParseException(
						readMode + " is not supported. Only 'MASTER', 'SLAVE' and 'MASTER_SLAVE' are valid values.");
			}
			selectedRedisOptions.setReadMode(readMode);

			setOtherOptions(selectedRedisOptions, jsonObject);

			return selectedRedisOptions;

		} else {
			throw new ParseException(database
					+ " is not supported. At the moment only 'Cassandra', 'MongoDB' and 'Redis' are supported.");
		}

	}

	private void setOtherOptions(SelectedOptions selectedOptions, JSONObject jsonObject) throws ParseException {

		selectedOptions.setIP(jsonObject.getString("ClusterIP"));

		JSONArray portArray = jsonObject.getJSONArray("ClusterPorts");
		int[] ports = new int[portArray.length()];
		for (int i = 0; i < portArray.length(); ++i) {
			ports[i] = portArray.optInt(i);
		}
		selectedOptions.setPorts(ports);

		String modelToCheck = jsonObject.getString("ConsistencyModel");
		JSONObject consistencyModelConfig = jsonObject.getJSONObject("ConsistencyModelConfig");
		if (!modelToCheck.equals(RYWC) && !modelToCheck.equals(MWC) && !modelToCheck.equals(MRC)) {
			throw new ParseException(modelToCheck + " is not supported. Choose 'RYWC', 'MRC' or 'MWC'.");
		}
		if (modelToCheck.equals(MWC)) {
			selectedOptions.setWrite(consistencyModelConfig.getBoolean("MWCWrite"));
		}
		selectedOptions.setModelToCheck(modelToCheck);

		selectedOptions.setNumberOfTests(consistencyModelConfig.getInt("NumberOfTests"));
	}

	private String readFile(String filePath) {
		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}

}
