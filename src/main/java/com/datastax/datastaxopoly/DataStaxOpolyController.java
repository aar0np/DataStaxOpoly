package com.datastax.datastaxopoly;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Random;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datastax.datastaxopoly.dal.BoardPlayer;
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
	private List<String> colorList;
	private Random random = new Random();
	
	public DataStaxOpolyController() {
		dataLayer = new GameDAL();
		generateColors();
	}
	
	@PostMapping("/startgame/name/{name}")
	public UUID startGame(@PathVariable(value="name") String name) {
		int monthBucket = getMonth();
		return dataLayer.newGame(name,monthBucket);
	}
	
	@GetMapping("/jail/game/{gameid}")
	public List<Jail> whoIsInJail(@PathVariable(value="gameid") UUID gameId) {
		return dataLayer.getJailOccupants(gameId).get();
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
	public Player getPlayer(@PathVariable(value="gameid") UUID gameId,
			@PathVariable(value="playerid") UUID playerId) {
		
		return dataLayer.getPlayer(gameId, playerId).get();
	}
	
	@GetMapping("square/{squareid}")
	public Square getSquare(@PathVariable(value="squareid") int squareId) {
		
		return dataLayer.getSquare(squareId).get();
	}
	
	@GetMapping("communitycard/card/{cardid}")
	public Card getCommunityCard(@PathVariable(value="cardid") int cardid) {
		
		return dataLayer.getCommunityCard(cardid).get();
	}

	@GetMapping("swagcard/card/{cardid}")
	public Card getSwagCard(@PathVariable(value="cardid") int cardid) {
		
		return dataLayer.getSwagCard(cardid).get();
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
		return dataLayer.getGame(gameId,getMonth());
	}
	
	public Optional<List<Game>> getNewGames() {
		int month = getMonth();
		Optional<List<Game>> games = dataLayer.getGames(month);
		
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
	
	public void addPlayerToGame(UUID playerId, String playerName, UUID gameId) {
		// initialize player
		int tokenId = random.nextInt(8);
		int colorNum = random.nextInt(colorList.size());
		String tokenColor = colorList.get(colorNum);
		
		// check if player already is "in" the game, they will have a non-negative ordinal
		int ordinal = computePlayerGameOrdinal(gameId, playerId);
		
		if (ordinal > -1) {
			// compute offsets
			int offsetX = computeOffsetX(ordinal);
			int offsetY = computeOffsetY(ordinal);
			
			dataLayer.addNewPlayer(gameId, playerId, playerName, tokenId, tokenColor);
			dataLayer.addNewPlayerBoard(gameId, playerId, tokenId, tokenColor, offsetX, offsetY);
		}
	}
	
	private int computePlayerGameOrdinal(UUID gameId, UUID playerId) {
		
		boolean found = false;
		int ordinal = 0;

		Optional<List<BoardPlayer>> players = dataLayer.getBoardPlayers(gameId);
		
		for (BoardPlayer player : players.get()) {

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
	
	private int computeOffsetX(int ordinal) {

//		int offset = 0;
//		int posNeg = 1;
//		
//		if (ordinal % 2 != 0) {
//			// odd, subtract 1 and make negative
//			offset = (ordinal - 1);
//			posNeg = -1;
//		}
//		
//		offset = 6 + (offset * 6);
//		
//		return offset * posNeg;
		return 0;
	}

	private int computeOffsetY(int ordinal) {
		
		int offset = 0;
		int posNeg = 1;
		
		if (ordinal % 2 != 0) {
			// odd, subtract 1 and make negative
			offset = (ordinal - 1);
			posNeg = -1;
		}

		offset = 6 + (offset * 6);
		
		if (offset > 24) {
			offset = 24;
		} else if (offset < -24) {
			offset = -24;
		}
		
		return offset * posNeg;
	}
	
	private int getMonth() {
		Instant timestamp = Instant.now();
		ZonedDateTime date = ZonedDateTime.parse(timestamp.toString());
		Integer year = date.getYear();
		Integer month = date.getMonthValue();
		StringBuilder bucket = new StringBuilder(year.toString());
		
		if (month < 10) {
			bucket.append("0");
		}
		bucket.append(month);

		return Integer.parseInt(bucket.toString());
	}
	
	private void generateColors() {
		colorList = new ArrayList<>();
		
		colorList.add("red");
		colorList.add("magenta");
		colorList.add("orange");
		colorList.add("gray");
		colorList.add("yellow");
		colorList.add("cyan");
		colorList.add("green");
		colorList.add("blue");
		colorList.add("purple");
	}
}
