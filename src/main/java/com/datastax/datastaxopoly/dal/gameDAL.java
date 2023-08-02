package com.datastax.datastaxopoly.dal;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

public class gameDAL {

	private CqlSession session;
	private UUID empty;
	
	public gameDAL() {
		this.session = new CassandraConnection().getCqlSession();
		
		empty = UUID.fromString("00000000-0000-0000-0000-000000000000");
	}
	
	public UUID newGame() {
		UUID gameId = UUID.randomUUID();
		
		// add bank as a player
		session.execute("INSERT INTO players (game_id, name, player_id)"
				+ "VALUES (,'Bank',0)");
		
		// initialize property ownership table
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader("/resourceDir/data/initializeOwnership.cql"));
			
			String cqlInsert = fileReader.readLine();
			
			while (cqlInsert != null) {
				cqlInsert = cqlInsert.replace("GAMEID", gameId.toString());
				session.execute(cqlInsert);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return empty;
		}
		
		return gameId;
	}
	
	public 
}
