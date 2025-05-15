package com.datastax.datastaxopoly.dal;

import java.util.Map;
import java.util.UUID;

public class Player {

	private UUID gameId;
	private UUID playerId;
	private String name;
	private String password;
	private int cash;
	private int tokenId;
	private int squareId;
	private String tokenColor;
	private Map<String,Integer> getOutOfJailCards;
	
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

	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public int getCash() {
		return cash;
	}
	
	public void setCash(int cash) {
		this.cash = cash;
	}
	
	public int getTokenId() {
		return tokenId;
	}
	
	public void setTokenId(int tokenId) {
		this.tokenId = tokenId;
	}
	
	public String getTokenColor() {
		return tokenColor;
	}
	
	public void setTokenColor(String tokenColor) {
		this.tokenColor = tokenColor;
	}
	
	public Map<String, Integer> getGetOutOfJailCards() {
		return getOutOfJailCards;
	}
	
	public void setGetOutOfJailCards(Map<String, Integer> getOutOfJailCards) {
		this.getOutOfJailCards = getOutOfJailCards;
	}
	
	public int getSquareId() {
		return squareId;
	}
	
	public void setSquareId(int squareId) {
		this.squareId = squareId;
	}
}
