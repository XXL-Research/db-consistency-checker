package de.htwk.imn.consistencychecker.database;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import de.htwk.imn.consistencychecker.utils.SelectedCassandraOptions;

public class Cassandra implements DataBase {

	private Cluster cluster;
	private Session session;
	private ConsistencyLevel writeConsistencyLevel;
	private ConsistencyLevel readConsistencyLevel;
	private PreparedStatement preparedWriteStatement;
	private PreparedStatement preparedReadStatement;
	private static final String KEYSPACE = "ycsb";
	private static final String TABLE = "usertable";

	public Cassandra(String IPCassandra, int[] ports, SelectedCassandraOptions selectedCassandraOptions,
			Boolean newDataBase) throws UnknownHostException {
		InetAddress inetaddress = InetAddress.getByName(IPCassandra);
		List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
		for (int port : ports) {
			addresses.add(new InetSocketAddress(inetaddress, port));
		}

		this.cluster = Cluster.builder().addContactPointsWithPorts(addresses)
				.withSocketOptions(new SocketOptions().setConnectTimeoutMillis(180000).setReadTimeoutMillis(180000))
				.build();
		this.session = cluster.connect();
		this.writeConsistencyLevel = selectedCassandraOptions.getWriteConsistencyLevel();
		this.readConsistencyLevel = selectedCassandraOptions.getReadConsistencyLevel();
		this.initDataBase(selectedCassandraOptions.getReplicationStrategy(),
				selectedCassandraOptions.getReplicationFactor(), newDataBase);
	}

	private void initPreparedStatements() {
		preparedWriteStatement = session.prepare("UPDATE ycsb.usertable SET field0 = ? WHERE y_id = ?;");
		preparedReadStatement = session.prepare("SELECT field0 FROM ycsb.usertable WHERE y_id = ?;");
		preparedWriteStatement.setConsistencyLevel(writeConsistencyLevel);
		preparedReadStatement.setConsistencyLevel(readConsistencyLevel);
		preparedWriteStatement.setIdempotent(true);
		preparedReadStatement.setIdempotent(true);
	}

	private void initDataBase(String replicationStrategy, String replicationFactor, Boolean newDataBase) {
		if (replicationStrategy.equals("SimpleStrategy")) {
			session.execute("CREATE KEYSPACE IF NOT EXISTS ycsb WITH REPLICATION = {'class' : '" + replicationStrategy
					+ "', 'replication_factor' : " + replicationFactor + "}");
		} else {
			session.execute("CREATE KEYSPACE IF NOT EXISTS ycsb WITH REPLICATION = {'class' : '" + replicationStrategy
					+ "', 'datacenter1' : " + replicationFactor + "}");
		}

		session.execute(
				"CREATE TABLE IF NOT EXISTS ycsb.usertable (y_id varchar, field0 varchar, field1 varchar, field2 varchar, field3 varchar, field4 varchar, field5 varchar, field6 varchar, field7 varchar, field8 varchar, field9 varchar, PRIMARY KEY (y_id))");
		if (newDataBase) {
			Statement insertQuery = QueryBuilder.insertInto(KEYSPACE, TABLE).value("y_id", "user1").value("field0",
					"1");
			session.execute(insertQuery);
		}
		this.initPreparedStatements();
	}

	@Override
	public void closeConnection() {
		this.session.close();
		this.cluster.close();
	}

	@Override
	public void writeData(String y_id, String field, String newValue) {
		session.execute(preparedWriteStatement.bind(newValue, y_id));
	}

	@Override
	public String readData(String y_id, String field) {
		ResultSet result = session.execute(preparedReadStatement.bind(y_id));
		Row row = result.one();
		return row.getString(0);
	}

}
