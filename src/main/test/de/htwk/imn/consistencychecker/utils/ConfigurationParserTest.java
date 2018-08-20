package de.htwk.imn.consistencychecker.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.redisson.config.ReadMode;

import com.mongodb.ReadConcernLevel;
import com.mongodb.ReadPreference;

public class ConfigurationParserTest {

	private static final String DATABASEC = "Cassandra";
	private static final String DATABASEM = "MongoDB";
	private static final String DATABASER = "Redis";
	private static final String MWC = "MWC";
	private static final String IP = "192.168.99.100";
	private static final int[] PORTS_CASSANDRA = { 9042, 9142, 9242 };
	private static final int[] PORTS_MONGODB = { 27017, 27018, 27019 };
	private static final int[] PORTS_REDIS = { 26379, 26380, 26381 };
	private static final int NUMTEST = 100000;
	private static final Boolean WRITE = true;

	private ConfigurationParser parser = new ConfigurationParser();
	private SelectedOptions selectedOptions;

	@Test
	public void parseConfigurationCassandraSuccessful() throws ParseException, URISyntaxException {

		selectedOptions = parser.parse(
				Paths.get(this.getClass().getResource("/CassandraCorrectConfiguration.json").toURI()).toString());

		assertThat(selectedOptions.getIP(), equalTo(IP));
		assertThat(selectedOptions.getPorts(), equalTo(PORTS_CASSANDRA));
		assertThat(selectedOptions.getDatabase(), equalTo(DATABASEC));
		assertThat(selectedOptions.getModelToCheck(), equalTo(MWC));
		assertThat(selectedOptions.getNumberOfTests(), equalTo(NUMTEST));
		assertThat(selectedOptions.getWrite(), equalTo(WRITE));
		assertThat(((SelectedCassandraOptions) selectedOptions).getWriteConsistencyLevel().toString(), equalTo("ALL"));
		assertThat(((SelectedCassandraOptions) selectedOptions).getReadConsistencyLevel().toString(), equalTo("ALL"));
	}

	@Test
	public void parseCassandraWrongReplicationStrategy() throws URISyntaxException {
		try {
			selectedOptions = parser.parse(Paths
					.get(this.getClass().getResource("/CassandraWrongReplicationStrategy.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "XYZ is not supported. Only SimpleStrategy and NetworkTopologyStrategy are valid values.";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

	@Test
	public void parseCassandraWrongConsistencyLevel() throws URISyntaxException {
		try {
			selectedOptions = parser.parse(
					Paths.get(this.getClass().getResource("/CassandraWrongConsistencyLevel.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "XYZ is not supported. Only ALL, QUORUM, LOCAL_QUORUM, EACH_QUORUM, THREE, TWO, ONE, LOCAL_ONE are supported for write and read consistency level. ANY is just supported for write consistency level.";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

	@Test
	public void parseConfigurationMongoDBSuccessful() throws ParseException, URISyntaxException {

		selectedOptions = parser
				.parse(Paths.get(this.getClass().getResource("/MongoDBCorrectConfiguration.json").toURI()).toString());

		assertThat(selectedOptions.getIP(), equalTo(IP));
		assertThat(selectedOptions.getPorts(), equalTo(PORTS_MONGODB));
		assertThat(selectedOptions.getDatabase(), equalTo(DATABASEM));
		assertThat(selectedOptions.getModelToCheck(), equalTo(MWC));
		assertThat(selectedOptions.getNumberOfTests(), equalTo(NUMTEST));
		assertThat(selectedOptions.getWrite(), equalTo(WRITE));
		assertThat(((SelectedMongoDBOptions) selectedOptions).getWriteConcern().getW(), equalTo(3));
		assertThat(((SelectedMongoDBOptions) selectedOptions).getReadConcern().getLevel(),
				equalTo(ReadConcernLevel.MAJORITY));
		assertThat(((SelectedMongoDBOptions) selectedOptions).getReadPreference(), equalTo(ReadPreference.secondary()));
		assertThat(((SelectedMongoDBOptions) selectedOptions).getCausallyConsistent(), equalTo(false));
	}

	@Test
	public void parseMongoDBWrongWriteConcern() throws URISyntaxException, ParseException {
		try {
			selectedOptions = parser
					.parse(Paths.get(this.getClass().getResource("/MongoDBWrongWriteConcern.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "WriteConcern: \"w\" must be \"0\", \"1\", \"2\", \"3\" or \"majority\".";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

	@Test
	public void parseMongoDBWrongReadConcern() throws URISyntaxException, ParseException {
		try {
			selectedOptions = parser
					.parse(Paths.get(this.getClass().getResource("/MongoDBWrongReadConcern.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "ReadConcern: \"level\" must be \"local\", \"default\", \"majority\" or \"linearizable\".";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

	@Test
	public void parseMongoDBWrongRCforCC() throws URISyntaxException, ParseException {
		try {
			selectedOptions = parser
					.parse(Paths.get(this.getClass().getResource("/MongoDBWrongRCforCC.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "For a causally constistent session only \"majority\" and \"local\" are allowed as read preference mode.";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

	@Test
	public void parseMongoDBWrongMaxStaleness() throws URISyntaxException, ParseException {
		try {
			selectedOptions = parser
					.parse(Paths.get(this.getClass().getResource("/MongoDBWrongMaxStaleness.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "maxStalenessSeconds must be at least 90 seconds.";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

	@Test
	public void parseMongoDBWrongReadPreference() throws URISyntaxException, ParseException {
		try {
			selectedOptions = parser.parse(
					Paths.get(this.getClass().getResource("/MongoDBWrongReadPreference.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "ReadReadPreference: \"mode\" must be \"nearest\", \"primary\", \"primaryPreferred\", \"secondary\" or \"secondaryPreferred\".";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

	@Test
	public void parseConfigurationRedisSuccessful() throws ParseException, URISyntaxException {

		selectedOptions = parser
				.parse(Paths.get(this.getClass().getResource("/RedisCorrectConfiguration.json").toURI()).toString());

		assertThat(selectedOptions.getIP(), equalTo(IP));
		assertThat(selectedOptions.getPorts(), equalTo(PORTS_REDIS));
		assertThat(selectedOptions.getDatabase(), equalTo(DATABASER));
		assertThat(selectedOptions.getModelToCheck(), equalTo(MWC));
		assertThat(selectedOptions.getNumberOfTests(), equalTo(NUMTEST));
		assertThat(selectedOptions.getWrite(), equalTo(WRITE));
		assertThat(((SelectedRedisOptions) selectedOptions).getSynchronReplication(), equalTo(true));
		assertThat(((SelectedRedisOptions) selectedOptions).getNumslaves(), equalTo(1));
		assertThat(((SelectedRedisOptions) selectedOptions).getTimeout(), equalTo(60));
		assertThat(((SelectedRedisOptions) selectedOptions).getReadMode(), equalTo(ReadMode.MASTER));
	}

	@Test
	public void parseRedisWrongReplication() throws URISyntaxException, ParseException {
		try {
			selectedOptions = parser
					.parse(Paths.get(this.getClass().getResource("/RedisWrongReplication.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "xyz is not supported. Only 'synchron' and 'asynchron' are valid values.";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

	@Test
	public void parseRedisWrongReadMode() throws URISyntaxException, ParseException {
		try {
			selectedOptions = parser
					.parse(Paths.get(this.getClass().getResource("/RedisWrongReadMode.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "XYZ is not supported. Only 'MASTER', 'SLAVE' and 'MASTER_SLAVE' are valid values.";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

	@Test
	public void parseRedisNumslavesMissing() throws URISyntaxException, ParseException {
		try {
			selectedOptions = parser
					.parse(Paths.get(this.getClass().getResource("/RedisNumslavesMissing.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "synchron replication: 'numslaves' and 'timeout' are mandatory.";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

	@Test
	public void parseWrongDatabase() throws URISyntaxException, ParseException {
		try {
			selectedOptions = parser
					.parse(Paths.get(this.getClass().getResource("/WrongDatabase.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "Database is not supported. At the moment only 'Cassandra', 'MongoDB' and 'Redis' are supported.";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

	@Test
	public void parseWrongModelToCheck() throws URISyntaxException, ParseException {
		try {
			selectedOptions = parser
					.parse(Paths.get(this.getClass().getResource("/WrongModelToCheck.json").toURI()).toString());
		} catch (ParseException exception) {
			String message = "XYZ is not supported. Choose 'RYWC', 'MRC' or 'MWC'.";
			assertThat(exception.getMessage(), equalTo(message));
		}
	}

}
