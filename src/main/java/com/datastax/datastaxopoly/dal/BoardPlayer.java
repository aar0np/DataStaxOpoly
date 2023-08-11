package com.datastax.datastaxopoly.dal;

import java.util.UUID;

public class BoardPlayer {
    private UUID gameId;
    private UUID playerId;
    private int offsetX;
    private int offsetY;
    private int squareId;
    private String tokenColor;
    private int tokenId;
    
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
	
	public int getOffsetX() {
		return offsetX;
	}
	
	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}
	
	public int getOffsetY() {
		return offsetY;
	}
	
	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}
	
	public int getSquareId() {
		return squareId;
	}
	
	public void setSquareId(int squareId) {
		this.squareId = squareId;
	}
	
	public String getTokenColor() {
		return tokenColor;
	}
	
	public void setTokenColor(String tokenColor) {
		this.tokenColor = tokenColor;
	}
	
	public int getTokenId() {
		return tokenId;
	}
	
	public void setTokenId(int tokenId) {
		this.tokenId = tokenId;
	}
}
