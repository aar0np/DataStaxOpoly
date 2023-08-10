package com.datastax.datastaxopoly.dal;

public class Token {

	private int tokenId;
	private String name;
	private String image;
	
	public int getTokenId() {
		return tokenId;
	}
	
	public void setTokenId(int tokenId) {
		this.tokenId = tokenId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getImage() {
		return image;
	}
	
	public void setImage(String image) {
		this.image = image;
	}
}
