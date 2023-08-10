package com.datastax.datastaxopoly.dal;

import java.util.List;
import java.util.UUID;

public class Property {

	private UUID gameId;
	private int square_id;
	private String name;
	private String image;
	private int price;
	private UUID player_id;
	private String special;
	private String type;
	private List<Integer> rent;
	private int rentDatabase;
	private int mortgage;
	private int structureCost;
	
	public UUID getGameId() {
		return gameId;
	}
	
	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}
	
	public int getSquare_id() {
		return square_id;
	}
	
	public void setSquare_id(int square_id) {
		this.square_id = square_id;
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
	
	public int getPrice() {
		return price;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}
	
	public UUID getPlayer_id() {
		return player_id;
	}
	
	public void setPlayer_id(UUID player_id) {
		this.player_id = player_id;
	}
	
	public String getSpecial() {
		return special;
	}
	
	public void setSpecial(String special) {
		this.special = special;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public List<Integer> getRent() {
		return rent;
	}
	
	public void setRent(List<Integer> rent) {
		this.rent = rent;
	}
	
	public int getRentDatabase() {
		return rentDatabase;
	}
	
	public void setRentDatabase(int rentDatabase) {
		this.rentDatabase = rentDatabase;
	}
	
	public int getMortgage() {
		return mortgage;
	}
	
	public void setMortgage(int mortgage) {
		this.mortgage = mortgage;
	}
	
	public int getStructureCost() {
		return structureCost;
	}
	
	public void setStructureCost(int structureCost) {
		this.structureCost = structureCost;
	}
}
