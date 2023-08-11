package com.datastax.datastaxopoly.dal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
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
	private PreparedStatement gameByIdPrepared;
	private PreparedStatement newGamePrepared;
	private PreparedStatement loginPrepared;
	private PreparedStatement newLoginPrepared;
	private PreparedStatement addPlayerPrepared;
	private PreparedStatement insertBoardPlayerPrepared;
	private PreparedStatement updateBoardPlayerPrepared;
	private PreparedStatement playersOnBoardPrepared;
	private PreparedStatement playerOnBoardPrepared;
	private PreparedStatement playersInGamePrepared;
	
	public GameDAL() {
		this.session = new CassandraConnection().getCqlSession();
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
		String gamesByIdCQL = "SELECT * FROM games WHERE month_bucket = ? AND game_id = ?";
		String newGameCQL = "INSERT INTO games (game_id,month_bucket,game_name,active,accepting_players) VALUES (?,?,?,true,true)";
		String loginCQL = "SELECT * FROM player_login WHERE player_name = ?";
		String newLoginCQL = "INSERT INTO player_login (player_id, player_name, password) VALUES (?,?,?)";
		String addPlayerCQL = "INSERT INTO players (game_id,player_id,name,cash,token_id,token_color) VALUES(?,?,?,?,?,?)";
		String insertPlayerBoardCQL = "INSERT INTO board (game_id,player_id,square_id,token_id,token_color,offset_x,offset_y) VALUES (?,?,?,?,?,?,?)";
		String updatePlayerBoardCQL = "INSERT INTO board (game_id,player_id,square_id) VALUES (?,?,?)";
		String playersOnBoardCQL = "SELECT * FROM board WHERE game_id = ?";
		String playerOnBoardCQL = "SELECT * FROM board WHERE game_id = ? AND player_id = ?";
		String playersInGameCQL = "SELECT COUNT(*) FROM board WHERE game_id = ?";
		
		squareByIdPrepared = session.prepare(squareByIdCQL);
		communityCardByIdPrepared = session.prepare(communityCardByIdCQL);
		swagCardByIdPrepared = session.prepare(swagCardByIdCQL);
		tokenByIdPrepared = session.prepare(tokenByIdCQL);
		playerByIdPrepared = session.prepare(playerByIdCQL);
		playersByGamePrepared = session.prepare(playersByGameCQL);
		jailOccupantsByGameIdPrepared = session.prepare(jailOccupantsByGameIdCQL);
		gameByIdPrepared = session.prepare(gamesByIdCQL);
		newGamePrepared = session.prepare(newGameCQL);
		loginPrepared = session.prepare(loginCQL);
		newLoginPrepared = session.prepare(newLoginCQL);
		addPlayerPrepared = session.prepare(addPlayerCQL);
		insertBoardPlayerPrepared = session.prepare(insertPlayerBoardCQL);
		updateBoardPlayerPrepared = session.prepare(updatePlayerBoardCQL);
		playersOnBoardPrepared = session.prepare(playersOnBoardCQL);
		playerOnBoardPrepared = session.prepare(playerOnBoardCQL);
		playersInGamePrepared = session.prepare(playersInGameCQL);
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
	
	public UUID newGame(String name, int monthBucket) {
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
			
			BoundStatement newGame = newGamePrepared.bind(gameId, monthBucket, name);
			session.execute(newGame);

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
		
		return Optional.ofNullable(null);
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
		
		return Optional.ofNullable(null);
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
		
		return Optional.ofNullable(null);
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
		
		return Optional.ofNullable(null);
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
		
		return Optional.ofNullable(null);
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
		
		return Optional.ofNullable(null);
	}
	
	public Optional<Game> getGame(UUID gameId, int monthBucket) {
		
		BoundStatement getGame = gameByIdPrepared.bind(monthBucket,gameId);
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

	public Optional<List<Game>> getGames(int monthBucket) {

		ResultSet rSet = session.execute("SELECT * FROM games WHERE month_bucket = "
		+ monthBucket);
		
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
				
		BoundStatement postLogin = newLoginPrepared.bind(
				player.getPlayerId(), player.getName(), player.getPassword());
		session.execute(postLogin);
	}
	
	public void addNewPlayer(UUID gameId, UUID playerId, String playerName,
			int tokenId, String tokenColor) {
		
		// INSERT INTO players (game_id,player_id,name,cash,token_id,token_color)
		BoundStatement playerStatement = addPlayerPrepared.bind(
				gameId, playerId, playerName, 1500, tokenId, tokenColor);
		session.execute(playerStatement);
	}
	
	public void addNewPlayerBoard(UUID gameId, UUID playerId,
			int tokenId, String tokenColor,
			int offsetX, int offsetY) {
		// INSERT INTO board (game_id,player_id,square_id,token_id,token_color)
		BoundStatement boardStatement = insertBoardPlayerPrepared.bind(
				gameId, playerId, 0, tokenId, tokenColor);
		session.execute(boardStatement);
	}
	
	public void updatePlayerBoard(UUID gameId, UUID playerId, int squareId) {
		
		BoundStatement boardStatement = updateBoardPlayerPrepared.bind(
				gameId, playerId, squareId);
		session.execute(boardStatement);
	}
	
	public Optional<List<BoardPlayer>> getBoardPlayers(UUID gameId) {
		
		BoundStatement boardPlayersStatement = playerOnBoardPrepared.bind(
				gameId);
		List<Row> rows = session.execute(boardPlayersStatement).all();
		List<BoardPlayer> returnVal = new ArrayList<>();
		
		if (rows != null) {
			for (Row row : rows) {
				BoardPlayer player = new BoardPlayer();
				player.setGameId(gameId);
				player.setPlayerId(row.getUuid("player_id"));
				player.setOffsetX(row.getInt("offset_x"));
				player.setOffsetY(row.getInt("offset_y"));
				player.setSquareId(row.getInt("sqaure_id"));
				player.setTokenColor(row.getString("token_color"));
				player.setTokenId(row.getInt("token_id"));
				
				returnVal.add(player);
			}
		}
		
		return Optional.of(returnVal);
	}
	
	public Optional<BoardPlayer> getBoardPlayer(UUID gameId, UUID playerId) {
		
		BoundStatement boardPlayerStatement = playerOnBoardPrepared.bind(
				gameId, playerId);
		Row row = session.execute(boardPlayerStatement).one();
		
		if (row != null) {
			BoardPlayer player = new BoardPlayer();
			player.setGameId(gameId);
			player.setPlayerId(playerId);
			player.setOffsetX(row.getInt("offset_x"));
			player.setOffsetY(row.getInt("offset_y"));
			player.setSquareId(row.getInt("sqaure_id"));
			player.setTokenColor(row.getString("token_color"));
			player.setTokenId(row.getInt("token_id"));
		}
		
		return Optional.ofNullable(null);
	}
	
	public Long getBoardPlayersCount(UUID gameId) {
		
		BoundStatement countBoardPlayers = playersInGamePrepared.bind(gameId);
		Row row = session.execute(countBoardPlayers).one();
		
		if (row != null) {
			return row.getLong("count");
		}
		
		return 0L;
	}
}
