package com.datastax.datastaxopoly.dal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

public class GameDAL {

	private CqlSession session;
	private UUID empty;
	
	private PreparedStatement squareByIdPrepared;
	private PreparedStatement communityCardByIdPrepared;
	private PreparedStatement swagCardByIdPrepared;
	private PreparedStatement tokenByIdPrepared;
	private PreparedStatement playerByIdPrepared;
	private PreparedStatement playersByGamePrepared;
	private PreparedStatement jailOccupantsByGameIdPrepared;
	
	public GameDAL() {
		this.session = new CassandraConnection().getCqlSession();
		
		empty = UUID.fromString("00000000-0000-0000-0000-000000000000");
		prepareStatements();
	}
	
	private void prepareStatements() {
		String squareByIdCQL = "SELECT * FROM squares WHERE square_id = ?";
		String communityCardByIdCQL = "SELECT * FROM community_cards WHERe card_id = ?";
		String swagCardByIdCQL = "SELECT * FROm swag_cards WHERE card_id = ?";
		String tokenByIdCQL = "SELECT * FROM tokens WHERe token_id = ?";
		String playerByIdCQL = "SELECT * FROM players WHERE game_id = ? AND player_id = ?";
		String playersByGameCQL = "SELECT * FROM players WHERE game_id = ?";
		String jailOccupantsByGameIdCQL = "SELECT * FROM jail WHERE game_id = ?";
		
		squareByIdPrepared = session.prepare(squareByIdCQL);
		communityCardByIdPrepared = session.prepare(communityCardByIdCQL);
		swagCardByIdPrepared = session.prepare(swagCardByIdCQL);
		tokenByIdPrepared = session.prepare(tokenByIdCQL);
		playerByIdPrepared = session.prepare(playerByIdCQL);
		playersByGamePrepared = session.prepare(playersByGameCQL);
		jailOccupantsByGameIdPrepared = session.prepare(jailOccupantsByGameIdCQL);
	}
	
	private void processNewPropertyInserts(BufferedReader fileReader, UUID gameId) {
		
		try {
			
			String cqlInsert = fileReader.readLine();
			
			while (cqlInsert != null) {
				cqlInsert = cqlInsert.replace("GAMEID", gameId.toString());
				session.execute(cqlInsert);
				cqlInsert = fileReader.readLine();
			}
			
			fileReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processNewInserts(BufferedReader fileReader) {
		
		try {
			
			String cqlInsert = fileReader.readLine();
			
			while (cqlInsert != null) {
				session.execute(cqlInsert);
				cqlInsert = fileReader.readLine();
			}
			
			fileReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public UUID newGame() {
		UUID gameId = UUID.randomUUID();
		
		// add bank as a player
		session.execute("INSERT INTO players (game_id, name, player_id)"
				+ "VALUES (" + gameId + ",'Bank'," + empty + ")");
		
		try {
			BufferedReader propertyReader = new BufferedReader(new FileReader("/resources/data/initializeOwnership.cql"));
			processNewPropertyInserts(propertyReader, gameId);
			
			Row commCardCount = session.execute("SELECT count(*) FROM community_cards").one();
			
			if (commCardCount != null && commCardCount.getInt("count") != 16) {
				BufferedReader communityCardReader = new BufferedReader(new FileReader("/resources/data/communityCards.cql"));
				processNewInserts(communityCardReader);				
			}

			Row swagCardCount = session.execute("SELECT count(*) FROM swag_cards").one();

			if (swagCardCount != null && swagCardCount.getInt("count") != 16) {
				BufferedReader swagCardReader = new BufferedReader(new FileReader("/resources/data/swagCards.cql"));
				processNewInserts(swagCardReader);				
			}
			
			Row squareCount = session.execute("SELECT count(*) FROM squares").one();
			
			if (squareCount != null && squareCount.getInt("count") != 40) {
				BufferedReader squareDataReader = new BufferedReader(new FileReader("/resources/data/squares.cql"));
				processNewInserts(squareDataReader);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return gameId;
	}
	
	public Optional<List<Jail>> getJailOccupants(UUID gameId) {
		
		BoundStatement getJail = jailOccupantsByGameIdPrepared.bind(gameId);
		ResultSet rSet = session.execute(getJail);
		
		List<Row> jailData = rSet.all();
		
		if (jailData != null && jailData.size() > 0) {
			
			List<Jail> returnVal = new ArrayList<>();
			
			for (Row row : jailData) {
				Jail inmate = new Jail();
			
				inmate.setGameId(gameId);
				inmate.setPlayerId(row.getUuid("player_id"));
				inmate.setName(row.getString("player_name"));
				inmate.setTurnsRemainingInJail(row.getInt("turns_remaining_in_jail"));
				
				returnVal.add(inmate);
			}
			
			return Optional.of(returnVal);
		}
		
		return Optional.of(null);
	}
	
	public Optional<List<Player>> getPlayers(UUID gameId) {
		List<Player> returnVal = new ArrayList<>();
		
		BoundStatement getPlayers = playersByGamePrepared.bind(gameId);
		ResultSet rSet = session.execute(getPlayers);
		
		List<Row> playerData = rSet.all();
		
		for (Row playerRow : playerData) {
			// don't add the bank
			String name = playerRow.getString("name");
			if (!name.equals("Bank")) {
				Player player = new Player();
				player.setPlayerId(playerRow.getUuid("player_id"));
				player.setGameId(gameId);
				player.setCash(playerRow.getInt("cash"));
				player.setGetOutOfJailCards(playerRow.getMap(
						"get_out_of_jail_cards", String.class, Integer.class));
				player.setName(playerRow.getString("name"));
				player.setTokenId(playerRow.getInt("token_id"));
				
				returnVal.add(player);
			}
		}
		
		return Optional.of(returnVal);
	}
	
	public Optional<Player> getPlayer(UUID gameId, UUID playerId) {
		
		BoundStatement getPlayer = playerByIdPrepared.bind(gameId,playerId);
		ResultSet rSet = session.execute(getPlayer);
		
		Row playerData = rSet.one();
		
		if (playerData != null) {
			
			Player returnVal = new Player();
			
			returnVal.setPlayerId(playerId);
			returnVal.setGameId(gameId);
			returnVal.setCash(playerData.getInt("cash"));
			returnVal.setGetOutOfJailCards(playerData.getMap(
					"get_out_of_jail_cards", String.class, Integer.class));
			returnVal.setName(playerData.getString("name"));
			returnVal.setTokenId(playerData.getInt("token_id"));
			
			return Optional.of(returnVal);
		}
		
		return Optional.of(null);
	}
	
	public Optional<Square> getSquare(int squareId) {
		
		BoundStatement getSquare = squareByIdPrepared.bind(squareId);
		ResultSet rSet = session.execute(getSquare);
		
		Row squareData = rSet.one();
		
		if (squareData != null) {
		
			Square returnVal = new Square();
			
			returnVal.setSquareId(squareId);
			returnVal.setImage(squareData.getString("image"));
			returnVal.setMortgage(squareData.getInt("mortgage"));
			returnVal.setName(squareData.getString("name"));
			returnVal.setPrice(squareData.getInt("price"));
			returnVal.setRent(squareData.getList("rent", Integer.class));
			returnVal.setRentDatabase(squareData.getInt("rent_database"));
			returnVal.setSpecial(squareData.getString("special"));
			returnVal.setStructureCost(squareData.getInt("structure_cost"));
			returnVal.setType(squareData.getString("type"));
			
			return Optional.of(returnVal);
		}
		
		return Optional.of(null);
	}
	
	public Optional<Card> getCommunityCard(int cardId) {
		
		BoundStatement getCard = communityCardByIdPrepared.bind(cardId);
		ResultSet rSet = session.execute(getCard);
		
		Row cardData = rSet.one();
		
		if (cardData != null) {
			Card returnVal = new Card();
			
			returnVal.setCardId(cardId);
			returnVal.setName(cardData.getString("name"));
			returnVal.setType(cardData.getString("type"));
			returnVal.setSpecial(cardData.getString("special"));
			returnVal.setValue(cardData.getInt("value"));
			
			return Optional.of(returnVal);
		}
		
		return Optional.of(null);
	}
	
	public Optional<Card> getSwagCard(int cardId) {
		
		BoundStatement getCard = swagCardByIdPrepared.bind(cardId);
		ResultSet rSet = session.execute(getCard);
		
		Row cardData = rSet.one();
		
		if (cardData != null) {
			Card returnVal = new Card();
			
			returnVal.setCardId(cardId);
			returnVal.setName(cardData.getString("name"));
			returnVal.setType(cardData.getString("type"));
			returnVal.setSpecial(cardData.getString("special"));
			returnVal.setValue(cardData.getInt("value"));
			
			return Optional.of(returnVal);
		}
		
		return Optional.of(null);
	}
	
	public Optional<Token> getToken(int tokenId) {
		
		BoundStatement getToken = tokenByIdPrepared.bind(tokenId);
		ResultSet rSet = session.execute(getToken);
		
		Row tokenData = rSet.one();
		
		if (tokenData != null) {
			Token returnVal = new Token();
			
			returnVal.setTokenId(tokenId);
			returnVal.setName(tokenData.getString("name"));
			returnVal.setImage(tokenData.getString("image"));
			
			return Optional.of(returnVal);
		}
		
		return Optional.of(null);
	}
}
