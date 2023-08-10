package com.datastax.datastaxopoly.dal;

public class Card {
	
	private int cardId;
	private String name;
	private String type;
	private int value;
	private String special;
	
	public int getCardId() {
		return cardId;
	}
	
	public void setCardId(int cardId) {
		this.cardId = cardId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public String getSpecial() {
		return special;
	}
	
	public void setSpecial(String special) {
		this.special = special;
	}
}
