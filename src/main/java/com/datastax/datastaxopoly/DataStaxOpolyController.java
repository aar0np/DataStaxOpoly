package com.datastax.datastaxopoly;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datastax.datastaxopoly.dal.Card;
import com.datastax.datastaxopoly.dal.GameDAL;
import com.datastax.datastaxopoly.dal.Jail;
import com.datastax.datastaxopoly.dal.Player;
import com.datastax.datastaxopoly.dal.Square;

@RequestMapping("/datastaxopoly/data")
@RestController
public class DataStaxOpolyController {

	private GameDAL dataLayer;
	
	public DataStaxOpolyController() {
		dataLayer = new GameDAL();
	}
	
	@PostMapping("/startgame")
	public UUID startGame() {
		return dataLayer.newGame();
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
}
