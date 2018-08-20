package de.htwk.imn.consistencychecker.database;

import java.util.concurrent.TimeUnit;

import org.redisson.Redisson;
import org.redisson.api.RBatch;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import de.htwk.imn.consistencychecker.utils.SelectedRedisOptions;

public class Redis implements DataBase {

	private RedissonClient redisson;
	private Boolean synchronReplication;
	private int numslaves, timeout;

	public Redis(String IPRedis, int[] ports, SelectedRedisOptions selectedRedisOptions, Boolean newDataBase) {

		Config config = new Config();
		config.useSentinelServers().setMasterName("master")
				.addSentinelAddress("redis://" + IPRedis + ":" + String.valueOf(ports[0]))
				.addSentinelAddress("redis://" + IPRedis + ":" + String.valueOf(ports[1]))
				.addSentinelAddress("redis://" + IPRedis + ":" + String.valueOf(ports[2]))
				.setReadMode(selectedRedisOptions.getReadMode());

		synchronReplication = selectedRedisOptions.getSynchronReplication();
		if (synchronReplication) {
			numslaves = selectedRedisOptions.getNumslaves();
			timeout = selectedRedisOptions.getTimeout();
		}

		redisson = Redisson.create(config);
	}

	@Override
	public void writeData(String y_id, String field, String newValue) {
		RBatch batch = redisson.createBatch();
		batch.getMap(y_id).putAsync(field, newValue);
		if (synchronReplication) {
			batch.syncSlaves(numslaves, timeout, TimeUnit.SECONDS);
		}
		batch.retryAttempts(3);
		batch.retryInterval(30, TimeUnit.SECONDS);
		batch.timeout(360, TimeUnit.SECONDS);
		batch.execute();
	}

	@Override
	public String readData(String y_id, String field) {
		RBatch batch = redisson.createBatch();
		batch.getMap(y_id).getAsync(field);
		batch.retryAttempts(3);
		batch.retryInterval(30, TimeUnit.SECONDS);
		batch.timeout(360, TimeUnit.SECONDS);
		return batch.execute().getResponses().get(0).toString();
	}

	@Override
	public void closeConnection() {
		redisson.shutdown(0, 1, TimeUnit.MILLISECONDS);
		System.exit(1);
	}

}
