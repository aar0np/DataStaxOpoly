package com.datastax.datastaxopoly.dal;

import java.util.UUID;

public class Jail {

	private UUID gameId;
	private UUID playerId;
	private String name;
	private int turnsRemainingInJail;
	
	public UUID getGameId() {
		return gameId;
	}
	
	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(UUID playerId) {
		this.playerId = playerId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getTurnsRemainingInJail() {
		return turnsRemainingInJail;
	}
	
	public void setTurnsRemainingInJail(int turnsRemainingInJail) {
		this.turnsRemainingInJail = turnsRemainingInJail;
	}
}
