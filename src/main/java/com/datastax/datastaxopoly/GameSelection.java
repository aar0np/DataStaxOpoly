package com.datastax.datastaxopoly;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.datastax.datastaxopoly.dal.Game;

@PageTitle("DataStaxOpoly")
@Route("/gameselection")
public class GameSelection extends VerticalLayout implements HasUrlParameter<String> {

	private static final long serialVersionUID = -398226258201687288L;
	
	private String playerName;
	private UUID playerId;
	private UUID gameId;
	private DataStaxOpolyController serviceLayer;
	
	public GameSelection() {
		serviceLayer = new DataStaxOpolyController();
		
		Grid<Game> games = new Grid<>();
		games.addColumn(Game::getName).setHeader("Game");
		games.addColumn(Game::getGameId).setHeader("ID");
		
		Optional<List<Game>> currentGames = serviceLayer.getNewGames();
		
		if (currentGames.isPresent()) {
			games.setItems(currentGames.get());
			
			games.addSelectionListener(selection -> {
				Game game = selection.getFirstSelectedItem().get();
				gameId = game.getGameId();
			});
		}
		
		add(
			new H1("Please select an existing game or create a new one."),
			games,
			new Button("Join Game", event -> {
				serviceLayer.addPlayerToGame(playerId,playerName,gameId);
				UI.getCurrent().navigate("/gameboard/" + gameId + "," + playerId);
			}),
			new Button("New Game", event -> {
				gameId = serviceLayer.startGame(playerName + "'s game on " + Instant.now());
				serviceLayer.addPlayerToGame(playerId,playerName,gameId);
				UI.getCurrent().navigate("/gameboard/" + gameId + "," + playerId);
			})
		);
	}
	
	@Override
	public void setParameter(BeforeEvent event, String playerNameAndIds) {
    	if (playerNameAndIds == null) {
    		UI.getCurrent().navigate("/");
    	} else {
    		String[] playerNameIdArray = playerNameAndIds.split(",");
    		this.playerName = playerNameIdArray[0];
    		this.playerId = UUID.fromString(playerNameIdArray[1]);
    	}
	}
}
