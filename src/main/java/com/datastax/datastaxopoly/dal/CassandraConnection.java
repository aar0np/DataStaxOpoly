package com.datastax.datastaxopoly.dal;

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.List;

import com.datastax.oss.driver.api.core.CqlSession;

public class CassandraConnection {
	private CqlSession cqlSession;
	
	private static String USERNAME = System.getenv("CASSANDRA_USERNAME");
	private static String PASSWORD = System.getenv("CASSANDRA_PASSWORD");
	private static String ENDPOINT = System.getenv("CASSANDRA_ENDPOINT");
	private static String KEYSPACE = System.getenv("CASSANDRA_KEYSPACE");
	private static String DATACENTER = System.getenv("CASSANDRA_DATACENTER");
	
	public CassandraConnection() {
        // Connect to open source Apache Cassandra w/ default endpoint and auth
        try {
        	cqlSession = CqlSession.builder()
                .withAuthCredentials(USERNAME, PASSWORD)
                .addContactPoint(new InetSocketAddress(ENDPOINT,9042))
                .withKeyspace(KEYSPACE)
                .withLocalDatacenter(DATACENTER)
                .build();
        	
        	System.out.println("[OK] Success");
        	System.out.printf("[OK] Welcome to Apache Cassandra! Connected to Keyspace %s\n", cqlSession.getKeyspace().get());
        } catch (Exception ex) {
        	System.out.println(ex.getMessage());
        }
	}
	
	public CassandraConnection(String username, String pwd, List<InetSocketAddress> endpointList, String keyspace, String datacenter) {
        // Connect to open source Apache Cassandra
        try {
        	cqlSession = CqlSession.builder()
                .addContactPoints(endpointList)
                .withAuthCredentials(username, pwd)
                .withKeyspace(keyspace)
                .withLocalDatacenter(datacenter)
                .build();

        	System.out.println("[OK] Success");
        	System.out.printf("[OK] Welcome to Apache Cassandra! Connected to Keyspace %s\n", cqlSession.getKeyspace().get());
        } catch (Exception ex) {
        	System.out.println(ex.getMessage());
        }
	}
	
	public CassandraConnection(String username, String pwd, String secureBundleLocation, String keyspace) {
        // Connect to Astra DB with a secure bundle
        try {
        	cqlSession = CqlSession.builder()
                .withCloudSecureConnectBundle(Paths.get(secureBundleLocation))
                .withAuthCredentials(username, pwd)
                .withKeyspace(keyspace)
                .build();
        	
        	System.out.println("[OK] Success");
        	System.out.printf("[OK] Welcome to Astra DB! Connected to Keyspace %s\n", cqlSession.getKeyspace().get());
        } catch (Exception ex) {
        	System.out.println(ex.getMessage());
        }
	}
	
	public CqlSession getCqlSession() {
		return cqlSession;
	}
	
	protected void finalize() {
		cqlSession.close();
		System.out.println("[shutdown_driver] Closing connection");
		System.out.println();
	}
}
