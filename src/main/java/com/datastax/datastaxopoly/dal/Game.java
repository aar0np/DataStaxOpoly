package com.datastax.datastaxopoly.dal;

import java.util.UUID;

public class Game {

	private UUID gameId;
	private String name;
	private boolean active;
	private boolean acceptingPlayers;
	
	public UUID getGameId() {
		return gameId;
	}
	
	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isAcceptingPlayers() {
		return acceptingPlayers;
	}
	
	public void setAcceptingPlayers(boolean acceptingPlayers) {
		this.acceptingPlayers = acceptingPlayers;
	}
}
