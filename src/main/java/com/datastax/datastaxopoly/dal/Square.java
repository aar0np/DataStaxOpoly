package com.datastax.datastaxopoly.dal;

import java.util.List;

public class Square {
	private int squareId;
	private String name;
	private String image;
	private int price;
	private String type;
	private List<Integer> rent;
	private int rentDatabase;
	private int mortgage;
	private int structureCost;
	private int centerX;
	private int centerY;
	private String special;

	public int getSquareId() {
		return squareId;
	}
	
	public void setSquareId(int squareId) {
		this.squareId = squareId;
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

	public int getCenterX() {
		return centerX;
	}
	
	public void setCenterX(int centerX) {
		this.centerX = centerX;
	}

	public int getCenterY() {
		return centerY;
	}
	
	public void setCenterY(int centerY) {
		this.centerY = centerY;
	}

	public String getSpecial() {
		return special;
	}
	
	public void setSpecial(String special) {
		this.special = special;
	}
}
