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
	//private final UUID empty = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	private PreparedStatement squareByIdPrepared;
	private PreparedStatement communityCardByIdPrepared;
	private PreparedStatement swagCardByIdPrepared;
	private PreparedStatement tokenByIdPrepared;
	private PreparedStatement playerByIdPrepared;
	private PreparedStatement playersByGamePrepared;
	private PreparedStatement jailOccupantsByGameIdPrepared;
	private PreparedStatement jailOccupantPrepared;
	private PreparedStatement addPlayerToJailPrepared;
	private PreparedStatement deletePlayerFromJailPrepared;
	private PreparedStatement gameByIdPrepared;
	private PreparedStatement newGamePrepared;
	private PreparedStatement loginPrepared;
	private PreparedStatement newLoginPrepared;
	private PreparedStatement addPlayerPrepared;
	private PreparedStatement updateCashPrepared;
	private PreparedStatement getGamePropertyPrepared;
	private PreparedStatement updatePropertyOwnerCQLPrepared;
	private PreparedStatement updatePlayerSpacePrepared;
	private PreparedStatement payRentPrepared;
	
	public GameDAL() {
		this.session = new CassandraConnection().getCqlSession();
		prepareStatements();
	}
	
	private void reconnect() {
		if (session == null || session.isClosed()) {
			session = new CassandraConnection().getCqlSession();
		}
	}
	
	private void prepareStatements() {
		
		String squareByIdCQL = "SELECT * FROM squares WHERE square_id = ?";
		String communityCardByIdCQL = "SELECT * FROM community_cards WHERe card_id = ?";
		String swagCardByIdCQL = "SELECT * FROm swag_cards WHERE card_id = ?";
		String tokenByIdCQL = "SELECT * FROM tokens WHERe token_id = ?";
		String playerByIdCQL = "SELECT * FROM players WHERE game_id = ? AND player_id = ?";
		String playersByGameCQL = "SELECT * FROM players WHERE game_id = ?";
		String jailOccupantsByGameIdCQL = "SELECT * FROM jail WHERE game_id = ?";
		String getJailOccupantCQL = "SELECT * FROM jail WHERE game_id = ? AND player_id = ?";
		String addPlayerToJail = "INSERT INTO jail (game_id, player_id, name, turns_remaining_in_jail) "
				+ "VALUES (?, ?, ?, ?)";
		String deletePlayerFromJail = "DELETE FROM jail WHERE game_id = ? AND player_id = ?";
		String gamesByIdCQL = "SELECT * FROM games WHERE game_id = ?";
		String newGameCQL = "INSERT INTO games (game_id,game_name,active,accepting_players) VALUES (?,?,true,true)";
		String loginCQL = "SELECT * FROM player_login WHERE player_name = ?";
		String newLoginCQL = "INSERT INTO player_login (player_id, player_name, password) VALUES (?,?,?)";
		String addPlayerCQL = "INSERT INTO players (game_id,player_id,name,cash,token_id) VALUES(?,?,?,?,?)";
		String updateCashCQL = "UPDATE players SET cash = ? WHERE game_id = ? AND player_id = ?";
		String getGamePropertyCQL = "SELECT * FROM properties_by_player WHERE game_id = ? AND square_id = ?";
		String updatePropertyOwnerCQL = "INSERT INTO properties_by_player (game_id,square_id,player_id) VALUES (?,?,?)";
		String updatePlayerSpaceCQL = "INSERT INTO players (game_id,player_id,square_id) VALUES (?,?,?)";
		String payRentCQL = "BEGIN TRANSACTION "
				+ "UPDATE players SET cash -= ? WHERE game_id = ?"
				+ " AND player_id = ?; "
				+ "UPDATE players SET cash += ? WHERE game_id = ?"
				+ " AND player_id = ?; "
				+ "COMMIT TRANSACTION;";
		
		squareByIdPrepared = session.prepare(squareByIdCQL);
		communityCardByIdPrepared = session.prepare(communityCardByIdCQL);
		swagCardByIdPrepared = session.prepare(swagCardByIdCQL);
		tokenByIdPrepared = session.prepare(tokenByIdCQL);
		playerByIdPrepared = session.prepare(playerByIdCQL);
		playersByGamePrepared = session.prepare(playersByGameCQL);
		jailOccupantsByGameIdPrepared = session.prepare(jailOccupantsByGameIdCQL);
		jailOccupantPrepared = session.prepare(getJailOccupantCQL);
		addPlayerToJailPrepared = session.prepare(addPlayerToJail);
		deletePlayerFromJailPrepared = session.prepare(deletePlayerFromJail);
		gameByIdPrepared = session.prepare(gamesByIdCQL);
		newGamePrepared = session.prepare(newGameCQL);
		loginPrepared = session.prepare(loginCQL);
		newLoginPrepared = session.prepare(newLoginCQL);
		addPlayerPrepared = session.prepare(addPlayerCQL);
		updateCashPrepared = session.prepare(updateCashCQL);
		getGamePropertyPrepared = session.prepare(getGamePropertyCQL);
		updatePropertyOwnerCQLPrepared = session.prepare(updatePropertyOwnerCQL);
		updatePlayerSpacePrepared = session.prepare(updatePlayerSpaceCQL);
		payRentPrepared = session.prepare(payRentCQL);
	}
	
	private void processNewPropertyInserts(BufferedReader fileReader, UUID gameId) {
		
		reconnect();
		
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
		
		reconnect();
		
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
	
	public UUID newGame(String name) {
		
		reconnect();
		
		UUID gameId = UUID.randomUUID();
		
		// add bank as a player
		//session.execute("INSERT INTO players (game_id, name, player_id)"
		//		+ "VALUES (" + gameId + ",'Bank'," + empty + ")");
		
		try {
			BufferedReader propertyReader = new BufferedReader(new FileReader("src/main/resources/data/initializeOwnership.cql"));
			processNewPropertyInserts(propertyReader, gameId);
			
			Row commCardCount = session.execute("SELECT count(*) FROM community_cards").one();
			
			if (commCardCount != null && commCardCount.getLong("count") != 16) {
				BufferedReader communityCardReader = new BufferedReader(new FileReader("src/main/resources/data/communityCards.cql"));
				processNewInserts(communityCardReader);				
			}

			Row swagCardCount = session.execute("SELECT count(*) FROM swag_cards").one();

			if (swagCardCount != null && swagCardCount.getLong("count") != 16) {
				BufferedReader swagCardReader = new BufferedReader(new FileReader("src/main/resources/data/swagCards.cql"));
				processNewInserts(swagCardReader);				
			}
			
			Row squareCount = session.execute("SELECT count(*) FROM squares").one();
			
			if (squareCount != null && squareCount.getLong("count") != 40) {
				BufferedReader squareDataReader = new BufferedReader(new FileReader("src/main/resources/data/squares.cql"));
				processNewInserts(squareDataReader);
			}

			Row tokenCount = session.execute("SELECT count(*) FROM tokens").one();
			
			if (tokenCount != null && squareCount.getLong("count") < 8) {
				BufferedReader squareDataReader = new BufferedReader(new FileReader("src/main/resources/data/tokens.cql"));
				processNewInserts(squareDataReader);
			}
			
			BoundStatement newGame = newGamePrepared.bind(gameId, name);
			session.execute(newGame);

		} catch (Exception e) {
			e.printStackTrace();
		}
				
		return gameId;
	}
	
	public Optional<List<Jail>> getJailOccupants(UUID gameId) {
		
		reconnect();
		
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
		
		return Optional.ofNullable(null);
	}
	
	public Optional<Jail> getJailPlayer(UUID gameId, UUID playerId) {
		
		reconnect();
		
		BoundStatement getJailPlayer = jailOccupantPrepared.bind(gameId, playerId);
		ResultSet rSet = session.execute(getJailPlayer);
		
		Row inmateData = rSet.one();
		
		if (inmateData != null) {
			Jail inmate = new Jail();
			
			inmate.setGameId(gameId);
			inmate.setPlayerId(playerId);
			inmate.setName(inmateData.getString("name"));
			inmate.setTurnsRemainingInJail(inmateData.getInt("turns_remaining_in_jail"));
			
			return Optional.of(inmate);
		}
		
		return Optional.ofNullable(null);
	}
	
	public void insertJailPlayer(Jail jailPlayer) {
		reconnect();
		
		BoundStatement addPlayerToJail = addPlayerToJailPrepared.bind(
				jailPlayer.getGameId(), jailPlayer.getPlayerId(), jailPlayer.getName(),
				jailPlayer.getTurnsRemainingInJail());
		session.execute(addPlayerToJail);
	}
	
	public void deleteJailPlayer(UUID gameId, UUID playerId) {
		reconnect();
		
		BoundStatement deletePlayerFromJail = deletePlayerFromJailPrepared.bind(gameId, playerId);
		session.execute(deletePlayerFromJail);
	}
	
	public Optional<List<Player>> getPlayers(UUID gameId) {
		
		reconnect();
		
		List<Player> returnVal = new ArrayList<>();
		
		BoundStatement getPlayers = playersByGamePrepared.bind(gameId);
		//getPlayers.setConsistencyLevel(ConsistencyLevel.ONE);
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
				player.setSquareId(playerRow.getInt("square_id"));
				
				returnVal.add(player);
			}
		}
		
		return Optional.of(returnVal);
	}
	
	public Optional<Player> getPlayer(UUID gameId, UUID playerId) {
		
		reconnect();
		
		BoundStatement getPlayer = playerByIdPrepared.bind(gameId,playerId);
		//getPlayer.setConsistencyLevel(ConsistencyLevel.ONE);
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
			returnVal.setSquareId(playerData.getInt("square_id"));
			
			return Optional.of(returnVal);
		}
		
		return Optional.ofNullable(null);
	}
	
	public Optional<Square> getSquare(int squareId) {
		
		reconnect();
		
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
			returnVal.setCenterX(squareData.getInt("center_x"));
			returnVal.setCenterY(squareData.getInt("center_y"));
			
			return Optional.of(returnVal);
		}
		
		return Optional.ofNullable(null);
	}
	
	public Optional<Card> getCommunityCard(int cardId) {
		
		reconnect();
		
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
		
		return Optional.ofNullable(null);
	}
	
	public Optional<Card> getSwagCard(int cardId) {
		
		reconnect();
		
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
		
		return Optional.ofNullable(null);
	}
	
	public Optional<Token> getToken(int tokenId) {
		
		reconnect();
		
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
		
		return Optional.ofNullable(null);
	}
	
	public Optional<Game> getGame(UUID gameId) {
		
		reconnect();
		
		BoundStatement getGame = gameByIdPrepared.bind(gameId);
		ResultSet rSet = session.execute(getGame);
		
		Row gameData = rSet.one();
		
		if (gameData != null) {
			Game returnVal = new Game();
			
			returnVal.setGameId(gameId);
			returnVal.setName(gameData.getString("game_name"));
			returnVal.setActive(gameData.getBoolean("active"));
			returnVal.setAcceptingPlayers(gameData.getBoolean("accepting_players"));
			
			return Optional.of(returnVal);
		}
		
		return Optional.ofNullable(null);
	}

	public Optional<List<Game>> getGames() {

		reconnect();
		
		ResultSet rSet = session.execute("SELECT * FROM games");
		
		List<Row> gamesData = rSet.all();
		
		if (gamesData != null) {
			List<Game> returnVal = new ArrayList<>();
			
			for (Row gameRow : gamesData) {
				Game game = new Game();
				
				game.setGameId(gameRow.getUuid("game_id"));
				game.setName(gameRow.getString("game_name"));
				game.setActive(gameRow.getBoolean("active"));
				game.setAcceptingPlayers(gameRow.getBoolean("accepting_players"));
				
				returnVal.add(game);
			}
			
			return Optional.of(returnVal);
		}
		
		return Optional.ofNullable(null);
	}
	
	public Optional<Player> login(String username) {
		reconnect();
		
		BoundStatement getLogin = loginPrepared.bind(username);
		ResultSet rSet = session.execute(getLogin);
		
		Row loginData = rSet.one();
		Player returnVal = new Player();
		
		if (loginData != null) {
			
			returnVal.setName(username);
			returnVal.setPlayerId(loginData.getUuid("player_id"));
			returnVal.setPassword(loginData.getString("password"));
			
			return Optional.of(returnVal);
		}
		
		return Optional.ofNullable(null);
	}
	
	public void newUser(Player player) {
		reconnect();
		
		BoundStatement postLogin = newLoginPrepared.bind(
				player.getPlayerId(), player.getName(), player.getPassword());
		session.execute(postLogin);
	}
	
	public void addNewPlayer(UUID gameId, UUID playerId, String playerName,
			int tokenId) {
		reconnect();
		
		// INSERT INTO players (game_id,player_id,name,cash,token_id,token_color)
		BoundStatement playerStatement = addPlayerPrepared.bind(
				gameId, playerId, playerName, 1500, tokenId);
		session.execute(playerStatement);
	}
	
	public void closeGame(UUID gameId) {
		reconnect();
		
		String closeGameCQL = "UPDATE games SET accepting_players = false WHERE game_id = ?";
		BoundStatement closeGameStatement = session.prepare(closeGameCQL).bind(gameId);
		
		session.execute(closeGameStatement);
	}
	
	public void updatePlayerBalance(UUID gameId, UUID playerId, int amount) {
		reconnect();
		
		BoundStatement payBank = updateCashPrepared.bind(amount, gameId, playerId);
		session.execute(payBank);
	}
	
	public Optional<Property> getGameProperty(UUID gameId, int squareId) {
		reconnect();
		
		BoundStatement getProperty = getGamePropertyPrepared.bind(gameId, squareId);
		ResultSet rSet = session.execute(getProperty);
		
		Row propertyData = rSet.one();
		
		if (propertyData != null) {
			Property returnVal = new Property();
			
			returnVal.setGameId(gameId);
			returnVal.setSquare_id(squareId);
			returnVal.setName(propertyData.getString("name"));
			returnVal.setImage(propertyData.getString("image"));
			returnVal.setMortgage(propertyData.getInt("mortgage"));
			returnVal.setPrice(propertyData.getInt("price"));
			returnVal.setPlayer_id(propertyData.getUuid("player_id"));
			returnVal.setSpecial(propertyData.getString("special"));
			returnVal.setType(propertyData.getString("type"));
			returnVal.setRent(propertyData.getList("rent", Integer.class));
			returnVal.setRentDatabase(propertyData.getInt("rent_database"));
			
			return Optional.ofNullable(returnVal);
		}
		
		return Optional.ofNullable(null);
	}
	
	public void updatePropertyOwner(UUID gameId, int squareId, UUID playerId) {
		reconnect();
		
		BoundStatement updateOwnerStatement = updatePropertyOwnerCQLPrepared.bind(
				gameId, squareId, playerId);
		
		session.execute(updateOwnerStatement);
	}
	
	public void updatePlayerSpace(UUID gameId, UUID playerId, int squareId) {
		reconnect();
		
		BoundStatement updatePlayerSpaceStatement = updatePlayerSpacePrepared.bind(
				gameId, playerId, squareId);
		
		session.execute(updatePlayerSpaceStatement);
	}
	
	public void payRent(UUID gameId, UUID playerId, UUID ownerId, int rent) {
		reconnect();
		
		BoundStatement payRentStatement = payRentPrepared.bind(
				rent, gameId, playerId, rent, gameId, ownerId);
		
		session.execute(payRentStatement);
	}
	
	public void executeAdhocTransaction(String cql) {
		reconnect();
		
		session.execute(cql);
	}	
}