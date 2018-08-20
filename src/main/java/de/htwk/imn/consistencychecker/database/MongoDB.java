package de.htwk.imn.consistencychecker.database;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.ClientSessionOptions;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.session.ClientSession;

import de.htwk.imn.consistencychecker.utils.SelectedMongoDBOptions;

public class MongoDB implements DataBase {

	private MongoClient client;
	private MongoClientOptions mongoClientOptions;
	private ClientSession clientSession;
	private ClientSessionOptions clientSessionOptions;
	private MongoDatabase database;
	private MongoCollection<Document> collection;
	private Boolean causallyConsistent;

	public MongoDB(String IPMongoDB, int[] ports, SelectedMongoDBOptions selectedMongoDBOptions, Boolean newDataBase)
			throws UnknownHostException {

		this.mongoClientOptions = MongoClientOptions.builder().connectTimeout(300000).socketTimeout(300000)
				.maxConnectionIdleTime(300000).writeConcern(selectedMongoDBOptions.getWriteConcern())
				.readConcern(selectedMongoDBOptions.getReadConcern())
				.readPreference(selectedMongoDBOptions.getReadPreference()).build();

		List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();
		for (int port : ports) {
			serverAddresses.add(new ServerAddress(InetAddress.getByName(IPMongoDB), port));
		}

		this.client = new MongoClient(serverAddresses, mongoClientOptions);
		this.causallyConsistent = selectedMongoDBOptions.getCausallyConsistent();

		if (causallyConsistent) {
			this.clientSessionOptions = ClientSessionOptions.builder()
					.causallyConsistent(selectedMongoDBOptions.getCausallyConsistent()).build();
			this.clientSession = client.startSession(clientSessionOptions);
		}

		this.initDataBase(newDataBase);
	}

	private void initDataBase(Boolean newDataBase) {
		database = client.getDatabase("ycsb");
		if (newDataBase) {
			collection = database.getCollection("usertable");
			collection.createIndex(Indexes.ascending("y_id"));
			Document document = new Document("y_id", "user1").append("field0", "1");
			collection.insertOne(document);
		}
	}

	@Override
	public void writeData(String y_id, String field, String newValue) {
		collection = database.getCollection("usertable");

		if (causallyConsistent) {
			collection.updateMany(clientSession, Filters.eq("y_id", y_id), Updates.set(field, newValue),
					new UpdateOptions().upsert(true));
		} else {
			collection.updateMany(Filters.eq("y_id", y_id), Updates.set(field, newValue),
					new UpdateOptions().upsert(true));
		}

	}

	@Override
	public String readData(String y_id, String field) {
		collection = database.getCollection("usertable");
		List<Document> documents;

		if (causallyConsistent) {
			documents = (List<Document>) collection.find(clientSession, Filters.eq("y_id", y_id))
					.into(new ArrayList<Document>());
		} else {
			documents = (List<Document>) collection.find(Filters.eq("y_id", y_id)).into(new ArrayList<Document>());
		}

		return documents.get(0).getString(field);
	}

	@Override
	public void closeConnection() {
		if (causallyConsistent) {
			clientSession.close();
		}
		client.close();
	}

}
