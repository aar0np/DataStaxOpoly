package com.datastax.datastaxopoly;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Random;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datastax.datastaxopoly.dal.Property;
import com.datastax.datastaxopoly.dal.Card;
import com.datastax.datastaxopoly.dal.Game;
import com.datastax.datastaxopoly.dal.GameDAL;
import com.datastax.datastaxopoly.dal.Jail;
import com.datastax.datastaxopoly.dal.Player;
import com.datastax.datastaxopoly.dal.Square;

@RequestMapping("/datastaxopoly/data")
@RestController
public class DataStaxOpolyController {

	private GameDAL dataLayer;
	private Random random = new Random();
	private final UUID bankID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	public DataStaxOpolyController() {
		dataLayer = new GameDAL();
	}
	
	@PostMapping("/startgame/name/{name}")
	public UUID startGame(@PathVariable(value="name") String name) {
		return dataLayer.newGame(name);
	}
	
	@GetMapping("/jail/game/{gameid}")
	public Optional<List<Jail>> whoIsInJail(@PathVariable(value="gameid") UUID gameId) {
		return dataLayer.getJailOccupants(gameId);
	}
	
	@GetMapping("/jail/game/{gameid}/player/{playerid}")
	public boolean isPlayerInJail(@PathVariable(value="gameid") UUID gameId,
			@PathVariable(value="playerid") UUID playerId) {
		
		boolean returnVal = false;
		List<Jail> inmates = dataLayer.getJailOccupants(gameId).get();
		
		for (Jail inmate : inmates) {
			if (inmate.getPlayerId() == playerId) {
				returnVal = true;
				break;
			}
		}
		
		return returnVal;
	}
	
	@GetMapping("playerinfo/game/{gameid}/player/{playerid}")
	public Optional<Player> getPlayer(@PathVariable(value="gameid") UUID gameId,
			@PathVariable(value="playerid") UUID playerId) {
		
		return dataLayer.getPlayer(gameId, playerId);
	}
	
	@GetMapping("square/{squareid}")
	public Optional<Square> getSquare(@PathVariable(value="squareid") int squareId) {
		
		return dataLayer.getSquare(squareId);
	}
	
	@GetMapping("communitycard/card/{cardid}")
	public Optional<Card> drawCommunityCard(@PathVariable(value="cardid") int cardid) {
		
		return dataLayer.getCommunityCard(cardid);
	}

	@GetMapping("swagcard/card/{cardid}")
	public Optional<Card> drawSwagCard(@PathVariable(value="cardid") int cardid) {
		
		return dataLayer.getSwagCard(cardid);
	}
	
	@GetMapping("game/rolldice")
	public int[] rollDice() {
		int die1 = random.nextInt(6) + 1;
		int die2 = random.nextInt(6) + 1;
		
		return new int[] {die1, die2};
	}
	
	public Optional<UUID> authenticatePlayer(String username, String password) {
		
		BCryptPasswordEncoder pEncoder = new BCryptPasswordEncoder(10, new SecureRandom());
		Optional<Player> player = dataLayer.login(username);
		String hashedPassword = pEncoder.encode(password);
		
		if (player.isEmpty()) {
			// not found, create new
			UUID playerId = UUID.randomUUID();
			
			Player newPlayer = new Player();
			newPlayer.setPlayerId(playerId);
			newPlayer.setName(username);
			newPlayer.setPassword(hashedPassword);
			
			dataLayer.newUser(newPlayer);
			
			return Optional.of(playerId);
		} else {
			// found, check password
			String storedPassword = player.get().getPassword();
			if (pEncoder.matches(password, storedPassword)) {
				return Optional.of(player.get().getPlayerId());
			} else {
				return Optional.ofNullable(null);
			}
		}
	}
	
	public Optional<Game> getGame(UUID gameId) {
		return dataLayer.getGame(gameId);
	}
	
	public Optional<List<Game>> getNewGames() {
		Optional<List<Game>> games = dataLayer.getGames();
		
		if (games.isPresent()) {
			List<Game> returnVal = new ArrayList<>();
			for (Game game : games.get()) {
				if (game.isAcceptingPlayers()) {
					returnVal.add(game);
				}
			}
			
			return Optional.of(returnVal);
		}
		
		return Optional.ofNullable(null);
	}
	
	public Optional<List<Player>> getPlayers(UUID gameId) {
		return dataLayer.getPlayers(gameId);
	}
	
	public boolean IsPlayerInJail(UUID gameId, UUID playerId) {
		return dataLayer.getJailPlayer(gameId, playerId).isPresent();
	}
	
	public void addPlayerToJail(UUID gameId, UUID playerId, String jailName) {
		Jail jailPlayer = new Jail();
		jailPlayer.setGameId(gameId);
		jailPlayer.setPlayerId(playerId);
		jailPlayer.setName(jailName);
		jailPlayer.setTurnsRemainingInJail(3);
		
		dataLayer.insertJailPlayer(jailPlayer);
	}
	
	public void getOutOfJail(UUID gameId, UUID playerId) {
		dataLayer.deleteJailPlayer(gameId, playerId);
	}
	
	public Optional<Property> getProperty(UUID gameId, int squareId) {
		return dataLayer.getGameProperty(gameId, squareId);
	}
	
	public void moveBoardPlayer(UUID gameId, UUID playerId, int squareId) {
		dataLayer.updatePlayerSpace(gameId, playerId, squareId);
	}
	
	public void addPlayerToGame(UUID playerId, String playerName, UUID gameId) {
		// initialize player
		
		// check if player already is "in" the game, they will have a non-negative ordinal
		int ordinal = computePlayerGameOrdinal(gameId, playerId);
		
		if (ordinal < 0) {
			int tokenId = random.nextInt(8);
			
			dataLayer.addNewPlayer(gameId, playerId, playerName, tokenId);
		}
	}
	
	public void payFromBank(UUID gameId, UUID playerId, int amount) {
		Optional<Player> player = dataLayer.getPlayer(gameId, playerId);
		
		if (player.isPresent()) {
			int newBalance = player.get().getCash() + amount;
			dataLayer.updatePlayerBalance(gameId, playerId, newBalance);
		}		
	}
	
	public void closeGame(UUID gameId) {
		dataLayer.closeGame(gameId);
	}
	
	public String payRent(UUID gameId, UUID playerId, UUID ownerId, int squareId) {
		Optional<Property> propertyOpt = dataLayer.getGameProperty(gameId, squareId);
		Optional<Player> playerOpt = dataLayer.getPlayer(gameId, playerId);
		Optional<Player> ownerOpt = dataLayer.getPlayer(gameId, ownerId);
		if (propertyOpt.isPresent() && playerOpt.isPresent() && ownerOpt.isPresent()) {
			Property property = propertyOpt.get();
			
			List<Integer> rentList = property.getRent();
			
			int numHouses = 0;
			// here is where we would query the number of houses on the property
			int rent = rentList.get(numHouses);
			
			// Accord
			String cql = "BEGIN TRANSACTION "
					+ "UPDATE players SET cash -= " + rent + " WHERE game_id = " + gameId
					+ " AND player_id = " + playerId + "; "
					+ "UPDATE players SET cash += " + rent + " WHERE game_id = " + gameId
					+ " AND player_id = " + ownerId + "; "
					+ "COMMIT TRANSACTION;";
			dataLayer.executeAdhocTransaction(cql);
			//dataLayer.payRent(gameId, playerId, ownerId, rent);
			
			return cql;
		}
		return "Error: Unable to process rent payment.";
	}
	
	public void processSimpleCardTransaction(UUID gameId, UUID playerId, int playerCash, int amount) {
		dataLayer.updatePlayerBalance(gameId, playerId, playerCash + amount);
	}
	
	public String processCardTransactionWMultiplier(UUID gameId, UUID playerId, int amount,
			int multiplier, Map<UUID,String> playerMap) {
		Optional<Player> player = dataLayer.getPlayer(gameId, playerId);
		
		if (player.isPresent()) {

		}
		return "Error: Unable to process card transaction.";
	}
	
	public void buyProperty(UUID gameId, UUID playerId, int squareId) {
		Optional<Property> property = dataLayer.getGameProperty(gameId, squareId);
		Optional<Player> player = dataLayer.getPlayer(gameId, playerId);
		
		if (property.isPresent() && player.isPresent()) {
			UUID originalOwnerId = property.get().getPlayer_id();
			int price = property.get().getPrice();
			int newBalance = player.get().getCash() - price;
			
			if (originalOwnerId.equals(bankID)) {
				// owned by Bank
				
				// TODO Accord
				dataLayer.updatePlayerBalance(gameId, playerId, newBalance);
				dataLayer.updatePropertyOwner(gameId, squareId, playerId);
			} else {
			
				Optional<Player> originalOwner = dataLayer.getPlayer(gameId, originalOwnerId);
				int originalOwnerBalance = originalOwner.get().getCash() + price;

				// TODO Accord
				dataLayer.updatePlayerBalance(gameId, playerId, newBalance);
				dataLayer.updatePropertyOwner(gameId, squareId, playerId);
				dataLayer.updatePlayerBalance(gameId, originalOwnerId, originalOwnerBalance);
			}
		}
	}
	
	private int computePlayerGameOrdinal(UUID gameId, UUID playerId) {
		
		boolean found = false;
		int ordinal = 0;

		Optional<List<Player>> players = dataLayer.getPlayers(gameId);
		
		for (Player player : players.get()) {

			if (player.getPlayerId().equals(playerId)) {
				found = true;
				break;
			}			

			ordinal++;
		}

		if (found) {
			return ordinal;
		}
		
		return -1;
	}
}
