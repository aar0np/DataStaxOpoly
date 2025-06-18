package com.datastax.datastaxopoly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.datastax.datastaxopoly.dal.Card;
import com.datastax.datastaxopoly.dal.Player;
import com.datastax.datastaxopoly.dal.Property;
import com.datastax.datastaxopoly.dal.Square;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

@PageTitle("DataStaxOpoly")
@Route("/gameboard")
public class DataStaxOpolyBoard extends HorizontalLayout implements HasUrlParameter<String> {

	private static final long serialVersionUID = 948032255252928441L;

	private DataStaxOpolyController serviceLayer;
	
	private Map<UUID,String> playerMap = new HashMap<>();
	private Map<Integer,Square> squares;
	private Map<UUID,Integer> playerTokenOrdinals = new HashMap<>();
	private Map<UUID,String> skipTurnMap = new HashMap<>();
	private List<UUID> playerIds = new ArrayList<>();
	
	// token streams
	private StreamResource whiteSquare = new StreamResource("",
			() -> getClass().getResourceAsStream("/tokens/white_square.png"));
	private StreamResource airplane = new StreamResource("",
			() -> getClass().getResourceAsStream("/tokens/airplane.png"));
	private StreamResource pin = new StreamResource("",
			() -> getClass().getResourceAsStream("/tokens/pin.png"));
	private StreamResource rocket = new StreamResource("",
			() -> getClass().getResourceAsStream("/tokens/rocket.png"));
	private StreamResource scissors = new StreamResource("",
			() -> getClass().getResourceAsStream("/tokens/scissors.png"));
	private StreamResource star = new StreamResource("",
			() -> getClass().getResourceAsStream("/tokens/star.png"));
	private StreamResource suitcase = new StreamResource("",
			() -> getClass().getResourceAsStream("/tokens/suitcase.png"));
	private StreamResource sword = new StreamResource("",
			() -> getClass().getResourceAsStream("/tokens/sword.png"));
	private StreamResource wrench = new StreamResource("",
			() -> getClass().getResourceAsStream("/tokens/wrench.png"));

	private Image squareImage = new Image();
	private Grid<Player> playerGrid = new Grid<>(Player.class);
	private Paragraph statusText = new Paragraph();
	private Paragraph transactionText = new Paragraph();
	private Paragraph ownerText = new Paragraph();

	// token icon locations by square
	private Image goIcon = new Image(whiteSquare, null);
	private Image space1Icon = new Image(whiteSquare, null);
	private Image space2Icon = new Image(whiteSquare, null);
	private Image space3Icon = new Image(whiteSquare, null);
	private Image space4Icon = new Image(whiteSquare, null);
	private Image space5Icon = new Image(whiteSquare, null);
	private Image space6Icon = new Image(whiteSquare, null);
	private Image space7Icon = new Image(whiteSquare, null);
	private Image space8Icon = new Image(whiteSquare, null);
	private Image space9Icon = new Image(whiteSquare, null);
	private Image jailIcon = new Image(whiteSquare, null);
	private Image space11Icon = new Image(whiteSquare, null);
	private Image space12Icon = new Image(whiteSquare, null);
	private Image space13Icon = new Image(whiteSquare, null);
	private Image space14Icon = new Image(whiteSquare, null);
	private Image space15Icon = new Image(whiteSquare, null);
	private Image space16Icon = new Image(whiteSquare, null);
	private Image space17Icon = new Image(whiteSquare, null);
	private Image space18Icon = new Image(whiteSquare, null);
	private Image space19Icon = new Image(whiteSquare, null);
	private Image hibernatedDBIcon = new Image(whiteSquare, null);
	private Image space21Icon = new Image(whiteSquare, null);
	private Image space22Icon = new Image(whiteSquare, null);
	private Image space23Icon = new Image(whiteSquare, null);
	private Image space24Icon = new Image(whiteSquare, null);
	private Image space25Icon = new Image(whiteSquare, null);
	private Image space26Icon = new Image(whiteSquare, null);
	private Image space27Icon = new Image(whiteSquare, null);
	private Image space28Icon = new Image(whiteSquare, null);
	private Image space29Icon = new Image(whiteSquare, null);
	private Image goToJailIcon = new Image(whiteSquare, null);
	private Image space31Icon = new Image(whiteSquare, null);
	private Image space32Icon = new Image(whiteSquare, null);
	private Image space33Icon = new Image(whiteSquare, null);
	private Image space34Icon = new Image(whiteSquare, null);
	private Image space35Icon = new Image(whiteSquare, null);
	private Image space36Icon = new Image(whiteSquare, null);
	private Image space37Icon = new Image(whiteSquare, null);
	private Image space38Icon = new Image(whiteSquare, null);
	private Image space39Icon = new Image(whiteSquare, null);
		
	private UUID gameId;
	private UUID currentPlayerId;
	private final UUID bankID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	public DataStaxOpolyBoard() {

		serviceLayer = new DataStaxOpolyController();
		squares = loadSquares();
		
		setSpacing(false);
		add(buildBoard());
		add(buildRightNav());		
	}
	
    @Override
    public void setParameter(BeforeEvent event, String gameAndPlayerIds) {
        
    	if (gameAndPlayerIds == null) {
    		UI.getCurrent().navigate("/");
    	} else {
    		String[] gamePlayerParams = gameAndPlayerIds.split(",");
    		gameId = UUID.fromString(gamePlayerParams[0]);
    		UUID playerId = UUID.fromString(gamePlayerParams[1]);
    		
    		Optional<Player> playerOpt = serviceLayer.getPlayer(gameId,playerId);
    		if (playerOpt.isPresent()) {
    			Player player = playerOpt.get();
				currentPlayerId = player.getPlayerId();
	    		String playerName = player.getName();
	    		
	    		// add player to game
	    		playerMap.put(playerId, playerName);
	    		addPlayerID(playerId);
	    		serviceLayer.addPlayerToGame(playerId, playerName, gameId);
    		}    		
    		loadPlayerGrid();
    		drawTokens();
    		setNextPlayer();
    	}
    }
	
	private Map<Integer,Square> loadSquares() {
		
		Map<Integer,Square> returnVal = new HashMap<>();
		
		// squares
		for (int squareCounter = 0; squareCounter < 40; squareCounter++) {
			// add square
			Optional<Square> square = serviceLayer.getSquare(squareCounter);
			if (square.isPresent()) {
				returnVal.put(squareCounter, square.get());
			}
		}
		
		return returnVal;
	}

	private Component buildBoard() {
		VerticalLayout layout = new VerticalLayout();
		
		HorizontalLayout boardLayout = new HorizontalLayout();
		boardLayout.setSpacing(false);
		boardLayout.setMargin(false);

		// build left icon spaces
		VerticalLayout leftColumn = new VerticalLayout();
		leftColumn.setSpacing(false);
		leftColumn.getStyle().set("margin-top", "126px");
		leftColumn.getStyle().set("margin-right", "0px");
		
		space19Icon.setHeight("52px");
		leftColumn.add(space19Icon);
		
		space18Icon.setHeight("52px");
		leftColumn.add(space18Icon);
		
		space17Icon.setHeight("52px");
		leftColumn.add(space17Icon);
		
		space16Icon.setHeight("52px");
		leftColumn.add(space16Icon);
		
		space15Icon.setHeight("52px");
		leftColumn.add(space15Icon);
		
		space14Icon.setHeight("52px");
		leftColumn.add(space14Icon);
		
		space13Icon.setHeight("52px");
		leftColumn.add(space13Icon);
		
		space12Icon.setHeight("52px");
		leftColumn.add(space12Icon);
		
		space11Icon.setHeight("52px");
		leftColumn.add(space11Icon);
		
		// build middle of board
		VerticalLayout middle = new VerticalLayout();
		middle.setSpacing(false);
		middle.setMargin(false);
		
		// build top row of icon spaces
		HorizontalLayout topRow = new HorizontalLayout();
		topRow.setSpacing(false);
		
		hibernatedDBIcon.setWidth("67px");
		topRow.add(hibernatedDBIcon);
	
		space21Icon.setWidth("52px");
		topRow.add(space21Icon);
		
		space22Icon.setWidth("52px");
		topRow.add(space22Icon);
		
		space23Icon.setWidth("52px");
		topRow.add(space23Icon);
		
		space24Icon.setWidth("52px");
		topRow.add(space24Icon);
		
		space25Icon.setWidth("52px");
		topRow.add(space25Icon);
		
		space26Icon.setWidth("52px");
		topRow.add(space26Icon);
		
		space27Icon.setWidth("52px");
		topRow.add(space27Icon);
		
		space28Icon.setWidth("52px");
		topRow.add(space28Icon);
		
		space29Icon.setWidth("52px");
		topRow.add(space29Icon);
		
		goToJailIcon.setWidth("67px");
		topRow.add(goToJailIcon);
		
		middle.add(topRow);
		
		StreamResource boardResource = new StreamResource("",
				() -> getClass().getResourceAsStream("/images/board_ibm.png"));
		Image boardImage = new Image(boardResource,"");
		boardImage.getStyle().set("border", "1px solid Black");
		middle.add(boardImage);
		
		// build bottom row of icon spaces
		HorizontalLayout bottomRow = new HorizontalLayout();
		bottomRow.setSpacing(false);
		
		jailIcon.setWidth("67px");
		bottomRow.add(jailIcon);
		
		space9Icon.setWidth("52px");
		bottomRow.add(space9Icon);

		space8Icon.setWidth("52px");
		bottomRow.add(space8Icon);

		space7Icon.setWidth("52px");
		bottomRow.add(space7Icon);
		
		space6Icon.setWidth("52px");
		bottomRow.add(space6Icon);

		space5Icon.setWidth("52px");
		bottomRow.add(space5Icon);
		
		space4Icon.setWidth("52px");
		bottomRow.add(space4Icon);
				
		space3Icon.setWidth("52px");
		bottomRow.add(space3Icon);

		space2Icon.setWidth("52px");
		bottomRow.add(space2Icon);
		
		space1Icon.setWidth("52px");
		bottomRow.add(space1Icon);
		
		goIcon.setWidth("67px");
		bottomRow.add(goIcon);
		
		middle.add(bottomRow);
		
		// build right icon spaces
		VerticalLayout rightColumn = new VerticalLayout();
		rightColumn.setSpacing(false);
		rightColumn.getStyle().set("margin-top", "126px");
		rightColumn.getStyle().set("margin-left", "0px");
		
		space31Icon.setHeight("52px");
		rightColumn.add(space31Icon);
		
		space32Icon.setHeight("52px");
		rightColumn.add(space32Icon);
		
		space33Icon.setHeight("52px");
		rightColumn.add(space33Icon);
		
		space34Icon.setHeight("52px");
		rightColumn.add(space34Icon);
		
		space35Icon.setHeight("52px");
		rightColumn.add(space35Icon);
		
		space36Icon.setHeight("52px");
		rightColumn.add(space36Icon);
		
		space37Icon.setHeight("52px");
		rightColumn.add(space37Icon);
		
		space38Icon.setHeight("52px");
		rightColumn.add(space38Icon);
		
		space39Icon.setHeight("52px");
		rightColumn.add(space39Icon);
		
		boardLayout.add(leftColumn);
		boardLayout.add(middle);
		boardLayout.add(rightColumn);
		
		layout.add(boardLayout);
		
		// build status bar
		statusText.setWidth("600px");
		statusText.setHeight("100px");
		statusText.getStyle().set("border", "1px solid Black");
		statusText.getStyle().set("padding-left", "10px");
		//statusText.getStyle().set("margin-top", "-40px");
		statusText.getStyle().set("margin-left", "100px");
		transactionText.getStyle().set("padding-right", "10px");
		statusText.getStyle().set("white-space", "pre-line");
		layout.add(statusText);
		
		return layout;
	}	
	
	private Component buildRightNav() {
		VerticalLayout layout = new VerticalLayout();
		
		HorizontalLayout gameControls = new HorizontalLayout();
		
		Button addPlayer = new Button("Add Player", addPlayerEvent -> {
			Dialog newPlayerDialog = new Dialog();
			newPlayerDialog.setHeaderTitle("Add New Player");
			
			VerticalLayout dialogLayout = new VerticalLayout();
			HorizontalLayout dialogButtons = new HorizontalLayout();
			
			Paragraph dialogText = new Paragraph("Name");
			TextField nameField = new TextField();
			
			Button addButton = new Button("Add", addButtonEvent -> {
				String playerName = nameField.getValue();
				
				if (!playerName.isBlank() && !playerName.isEmpty()) {
					UUID playerId = UUID.randomUUID();
					serviceLayer.addPlayerToGame(playerId, playerName, gameId);
					playerMap.put(playerId, playerName);
					addPlayerID(playerId);
					loadPlayerGrid();
					drawTokens();
				}
				
				newPlayerDialog.close();
			});
			Button cancelButton = new Button("Cancel", cancelEvent -> newPlayerDialog.close());
			dialogButtons.add(addButton);
			dialogButtons.add(cancelButton);
			
			dialogLayout.add(dialogText);
			dialogLayout.add(nameField);
			dialogLayout.add(dialogButtons);
			
			newPlayerDialog.add(dialogLayout);
			newPlayerDialog.open();
		});
		
		Button closeGame = new Button("Start Game", closeEvent -> {
			addPlayer.setEnabled(false);
			setEnabled(false);
			serviceLayer.closeGame(gameId);
			currentPlayerId = null;
			setNextPlayer();
		});
		
		gameControls.add(addPlayer);
		gameControls.add(closeGame);
		
		layout.add(gameControls);
		
		playerGrid.setWidth("300px");
		playerGrid.getStyle().set("font", "10px");
		playerGrid.getStyle().set("border", "1px solid Black");
		playerGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
		playerGrid.setColumns("name","cash");
		playerGrid.setAllRowsVisible(true);
		
		// build dice roller
		VerticalLayout diceControls = new VerticalLayout();
		HorizontalLayout diceLayout = new HorizontalLayout();
		Paragraph die0 = new Paragraph();
		Paragraph die1 = new Paragraph();
		Paragraph diePlus = new Paragraph("+");
		Paragraph dieEquals = new Paragraph("=");
		Paragraph diceResult = new Paragraph();
		
		die0.getStyle().set("border", "1px solid Black");
		die0.getStyle().set("padding-left", "6px");
		die0.getStyle().set("padding-right", "6px");
		die0.setHeight("25px");
		die1.getStyle().set("border", "1px solid Black");
		die1.getStyle().set("padding-left", "6px");
		die1.getStyle().set("padding-right", "6px");
		die1.setHeight("25px");
		diceResult.getStyle().set("font-weight", "bold");
		diceResult.getStyle().set("font-size", "36px");
		diceResult.getStyle().set("margin-top", "-8px");
		
		Button rollDiceButton = new Button("Roll Dice", rollEvent -> {
			if (skipPlayer(currentPlayerId)) {
				
				Dialog skipDialog = new Dialog();
				skipDialog.setHeaderTitle("SKIP TURN!");
				Paragraph skipDialogText = new Paragraph("You are skipping your turn");
				
				VerticalLayout skipDialogLayout = new VerticalLayout();
				skipDialogLayout.add(skipDialogText);
				Button okButton = new Button("OK", skipEvent -> {
					skipDialog.close();
				});
				skipDialogLayout.add(okButton);
				skipDialog.add(skipDialogLayout);
				
				skipDialog.open();
				
				statusText.setText("You are skipping your turn.");
				removeFromSkipTurn(currentPlayerId);
				setNextPlayer();
				return;
			}
			
			int[] resultArray = serviceLayer.rollDice();
			die0.setText(resultArray[0] + "");
			die1.setText(resultArray[1] + "");
			Integer result = resultArray[0] + resultArray[1];
			diceResult.setText(result.toString());
			moveToken(resultArray);
		});

		diceLayout.add(die0);
		diceLayout.add(diePlus);
		diceLayout.add(die1);
		diceLayout.add(dieEquals);
		diceLayout.add(diceResult);
		
		diceControls.add(rollDiceButton);
		diceControls.add(diceLayout);
		
		// build refresh button
		VerticalLayout refreshLayout = new VerticalLayout();
		Button refreshButton = new Button("Refresh", event -> {
			refreshPlayerGrid();
		});
		
		refreshLayout.add(refreshButton);

		HorizontalLayout diceAndRefreshLayout = new HorizontalLayout();
		diceAndRefreshLayout.add(diceControls);
		diceAndRefreshLayout.add(refreshLayout);

		// build square image
		squareImage.setWidth("300px");
		squareImage.setHeight("300px");
		squareImage.getStyle().set("border", "2px solid Black");
		HorizontalLayout squareImageLayout = new HorizontalLayout();
		squareImageLayout.add(squareImage);
		
		// add to layout and return
		layout.add(playerGrid);
		layout.add(diceAndRefreshLayout);
		layout.add(ownerText);
		layout.add(squareImageLayout);
		
		transactionText.getStyle().set("white-space", "pre-line");
		transactionText.getStyle().set("border", "1px solid Black");
		transactionText.getStyle().set("margin-left", "-100px");
		transactionText.getStyle().set("margin-top", "1px");
		transactionText.getStyle().set("padding-left", "10px");
		transactionText.getStyle().set("padding-right", "10px");
		transactionText.setWidth("500px");
		layout.add(transactionText);
		return layout;
	}
	
	private Component drawTokens() {
		HorizontalLayout layout = new HorizontalLayout();
		
		Optional<List<Player>> players = serviceLayer.getPlayers(gameId);
		
		if (players.isPresent()) {

			int ordinal = 0;
			for (Player player : players.get()) {
				StreamResource token = getToken(player.getTokenId());
				setSpaceIcon(token, player.getSquareId());
				playerTokenOrdinals.put(player.getPlayerId(), ordinal);
				
				ordinal++;
			}
		}
		
		return layout;
	}
	
	private void setSpaceIcon(StreamResource token, int squareId) {
		
		switch (squareId) {
		case 0:
			goIcon.setSrc(token);
			break;
		case 1:
			space1Icon.setSrc(token);
			break;
		case 2:
			space2Icon.setSrc(token);
			break;
		case 3:
			space3Icon.setSrc(token);
			break;
		case 4:
			space4Icon.setSrc(token);
			break;
		case 5:
			space5Icon.setSrc(token);
			break;
		case 6:
			space6Icon.setSrc(token);
			break;
		case 7:
			space7Icon.setSrc(token);
			break;
		case 8:
			space8Icon.setSrc(token);
			break;
		case 9:
			space9Icon.setSrc(token);
			break;
		case 10:
			jailIcon.setSrc(token);
			break;
		case 11:
			space11Icon.setSrc(token);
			break;
		case 12:
			space12Icon.setSrc(token);
			break;
		case 13:
			space13Icon.setSrc(token);
			break;
		case 14:
			space14Icon.setSrc(token);
			break;
		case 15:
			space15Icon.setSrc(token);
			break;
		case 16:
			space16Icon.setSrc(token);
			break;
		case 17:
			space17Icon.setSrc(token);
			break;
		case 18:
			space18Icon.setSrc(token);
			break;
		case 19:
			space19Icon.setSrc(token);
			break;
		case 20:
			hibernatedDBIcon.setSrc(token);
			break;
		case 21:
			space21Icon.setSrc(token);
			break;
		case 22:
			space22Icon.setSrc(token);
			break;
		case 23:
			space23Icon.setSrc(token);
			break;
		case 24:
			space24Icon.setSrc(token);
			break;
		case 25:
			space25Icon.setSrc(token);
			break;
		case 26:
			space26Icon.setSrc(token);
			break;
		case 27:
			space27Icon.setSrc(token);
			break;
		case 28:
			space28Icon.setSrc(token);
			break;
		case 29:
			space29Icon.setSrc(token);
			break;
		case 30:
			goToJailIcon.setSrc(token);
			break;
		case 31:
			space31Icon.setSrc(token);
			break;
		case 32:
			space32Icon.setSrc(token);
			break;
		case 33:
			space33Icon.setSrc(token);
			break;
		case 34:
			space34Icon.setSrc(token);
			break;
		case 35:
			space35Icon.setSrc(token);
			break;
		case 36:
			space36Icon.setSrc(token);
			break;
		case 37:
			space37Icon.setSrc(token);
			break;
		case 38:
			space38Icon.setSrc(token);
			break;
		case 39:
			space39Icon.setSrc(token);
			break;
		}
	}
	
	private StreamResource getToken(int tokenIndex) {
		
		switch (tokenIndex) {
		case 0:
			return wrench;
		case 1:
			return sword;
		case 2:
			return airplane;
		case 3:
			return suitcase;
		case 4:
			return star;
		case 5:
			return rocket;
		case 6:
			return pin;
		case 7:
			return scissors;
		default:
			return star;
		}		
	}
	
	private void loadPlayerGrid() {
		// build player grid
		Optional<List<Player>> players = serviceLayer.getPlayers(gameId);
		List<String> playerNames = new ArrayList<>();
		
		if (players.isPresent()) {
			playerNames.clear();
			
			for (Player player : players.get()) {

				StreamResource token = getToken(player.getTokenId());
				setSpaceIcon(token, player.getSquareId());
				String playerName = player.getName();
				UUID playerId = player.getPlayerId();
				playerNames.add(playerName);
				playerMap.put(playerId, playerName);
				addPlayerID(playerId);
			}
			
			// add players to grid
			playerGrid.setItems(players.get());
		}		
	}

	private void refreshPlayerGrid() {
		// build player grid
		Optional<List<Player>> players = serviceLayer.getPlayers(gameId);

		if (players.isPresent()) {
			playerGrid.setItems(players.get());
		}
	}
	
	private void moveToken(int[] diceRolled) {
		
		Integer result = diceRolled[0] + diceRolled[1];
		Optional<Player> playerOpt = serviceLayer.getPlayer(gameId, currentPlayerId);
		if (playerOpt.isPresent()) {
			Player player = playerOpt.get();
			String playerName = player.getName();
			int squarePosition = player.getSquareId();
			int newSquarePosition = squarePosition + result;
			
			if (newSquarePosition > 39) {
				newSquarePosition = newSquarePosition - 40;
				passGo();
			}
			
			serviceLayer.moveBoardPlayer(gameId, currentPlayerId, newSquarePosition);
			Square square = squares.get(newSquarePosition);
			player.setSquareId(newSquarePosition);
			
			adjustSquareAndTokenGraphics(squarePosition, newSquarePosition,
					square.getImage(), player.getTokenId(), playerName);
			
			statusText.setText(playerName + " rolled a " + result.toString() 
				+ " and moved to " + square.getName() + ".");
			
			processSquare(square);
			refreshPlayerGrid();
			setNextPlayer();
		}
	}
	
	private void adjustSquareAndTokenGraphics(int squareId, int newSquareId,
			String squareImageFile,	int playerTokenId, String playerName) {
		// remove token from old square/space
		setSpaceIcon(whiteSquare, squareId);
		
		// draw token on new square/space
		StreamResource token = getToken(playerTokenId);
		setSpaceIcon(token, newSquareId);
		
		// show square image
		StreamResource resource = new StreamResource(squareImageFile,
				() -> getClass().getResourceAsStream("/fullSizeImages/" + squareImageFile));
		squareImage.setSrc(resource);
		squareImage.setAlt(playerName);
	}
	
	private void passGo() {
		UUID playerId = currentPlayerId;
		String playerName = playerMap.get(playerId);
		
		Dialog dialog = new Dialog();
		VerticalLayout dialogLayout = new VerticalLayout();
		Paragraph dialogText = new Paragraph();

		dialog.setHeaderTitle("Pass GO!");
		dialogText.setText(playerName + " passed GO and collected $200.");
		
		dialogLayout.add(dialogText);
		Button okButton = new Button("OK", event -> {
			dialog.close();
		});
		dialogLayout.add(okButton);
		dialog.add(dialogLayout);
		
		dialog.open();
		
		serviceLayer.payFromBank(gameId, playerId, 200);
		//playerGrid.setItems(serviceLayer.getPlayers(gameId).get());
		refreshPlayerGrid();
	}
	
	private void processSquare(Square square) {
		Optional<Property> propertyOpt = serviceLayer.getProperty(gameId, square.getSquareId());
		Property property = propertyOpt.get();

		int squareId = property.getSquare_id();
		String name = property.getName();
		String type = property.getType();
		UUID ownerId = property.getPlayer_id();
		// assign playerId to the currentPlayerId locally, because the global
		// currentPlayerId can change before this method is finished.
		UUID playerId = currentPlayerId;
		
		switch (type) {
		case "REST":
			// Check for Skip Turn square
			if (square.getSquareId() == 38) {
				addToSkipTurn(playerId,"skip");
			}
			// Could also be Hibernated Database square, which is a rest square
			break;
		case "CARD":
			// generate a random card 0-15
			int cardIndex = (int) (Math.random() * 16);
			Optional<Card> drawnCard = null;
			StringBuilder deck = new StringBuilder();
			if (property.getName().contains("SWAG")) {
				drawnCard = serviceLayer.drawSwagCard(cardIndex);
				deck.append("SWAG");
			} else {
				drawnCard = serviceLayer.drawCommunityCard(cardIndex);
				deck.append("Community Forum");
			}
			
			if (drawnCard.isEmpty()) {
				statusText.setText("No cards available.");
				return;
			}
			processCard(drawnCard, playerId, deck.toString());
			break;
		case "CHARGE":
			break;
		case "PAY":
			// Go! Already handled in passGo()
			break;
		case "GOTOJAIL":
			break;
		case "JAIL":
			break;
		default:
			// purchasable property
			if (ownerId.equals(bankID)) {
				// property is owned by the bank
				Dialog dialog = new Dialog();
				VerticalLayout dialogLayout = new VerticalLayout();
				Paragraph dialogText = new Paragraph();
				ownerText.setText("");
				
				dialog.setHeaderTitle("Buy Property");
				dialogText.setText("Do you want to buy " + name + " for $" + property.getPrice() + "?");
				
				dialogLayout.add(dialogText);
				Button yesButton = new Button("Yes", event -> {

					serviceLayer.buyProperty(gameId, playerId, squareId);
					refreshPlayerGrid();
					dialog.close();
				});
				Button noButton = new Button("No", event -> {
					dialog.close();
				});
				
				dialogLayout.add(yesButton);
				dialogLayout.add(noButton);
				
				dialog.add(dialogLayout);
				dialog.open();
			} else if (!ownerId.equals(playerId)) {
				// property is owned by another player
				int rent = property.getRent().get(0);
				Dialog dialog = new Dialog();
				VerticalLayout dialogLayout = new VerticalLayout();
				Paragraph dialogText = new Paragraph();
				ownerText.setText("Owner: " + playerMap.get(ownerId));
				
				dialog.setHeaderTitle("Pay Rent!");
				dialogText.setText("You (" + playerMap.get(playerId)
						+ ") landed on " + name + ", owned by " + playerMap.get(ownerId)
						+ ". You owe " + playerMap.get(ownerId) + " $" + rent + ".");
				
				dialogLayout.add(dialogText);
				Button okButton = new Button("Ok", event -> {
					String cql = serviceLayer.payRent(gameId, playerId, ownerId, squareId)
							.replace("; ",";\n")
							.replace(" WHERE","\nWHERE")
							.replace(" AND","\nAND")
							.replace("ACTION ","ACTION\n");

					transactionText.setText(cql);
					refreshPlayerGrid();
					dialog.close();
				});
				dialogLayout.add(okButton);
				
				dialog.add(dialogLayout);
				dialog.open();
			} else {
				// property is owned by the current player
				ownerText.setText("Owner: " + playerMap.get(ownerId));
			}
			break;
		}
	}
	
	private void processCard(Optional<Card> drawnCard, UUID playerId, String deck) {

		Dialog cardDialog = new Dialog();
		VerticalLayout cardDialogLayout = new VerticalLayout();
		Paragraph cardDialogText = new Paragraph();

		Optional<Player> playerOpt = serviceLayer.getPlayer(gameId, playerId);
		
		if (playerOpt.isPresent()) {
			// display the card
			Card card = drawnCard.get();
			Player player = playerOpt.get();
			
			cardDialog.setHeaderTitle("Draw a " + deck + " Card");
			cardDialogText.setText(player.getName() + " - " + card.getName());
			
			cardDialogLayout.add(cardDialogText);
			Button okButton = new Button("OK", event -> {
				cardDialog.close();
			});
			cardDialogLayout.add(okButton);
			cardDialog.add(cardDialogLayout);
			
			cardDialog.open();
			// process the card
			switch (card.getType()) {
			case "MOVE":
				if (card.getSpecial() != null && !card.getSpecial().isEmpty()) {
					// parse the special field
					if (card.getSpecial().contains("back 3 spaces")) {
						int newSquareId = player.getSquareId() - 3;
						Square square = squares.get(newSquareId);
						
						if (newSquareId == 0) {
							// player landed on GO
							passGo();
						}
						
						adjustSquareAndTokenGraphics(player.getSquareId(), newSquareId,
								square.getImage(), player.getTokenId(), player.getName());
						player.setSquareId(newSquareId);
						processSquare(square);
					} else if (card.getSpecial().contains("square=")) {
						String special = card.getSpecial();
						int newSquareId = Integer.parseInt(special.substring(special.indexOf("=") + 1));
						Square square = squares.get(newSquareId);
						
						if (newSquareId < player.getSquareId()) {
							// player passed GO
							passGo();
						}
						
						adjustSquareAndTokenGraphics(player.getSquareId(), newSquareId,
								square.getImage(), player.getTokenId(), player.getName());
						player.setSquareId(newSquareId);
						processSquare(square);
					}
					serviceLayer.moveBoardPlayer(
							gameId, player.getPlayerId(), player.getSquareId());
				}
				break;
			case "CHARGE":
				if (card.getSpecial() != null && !card.getSpecial().isEmpty()) {
					// charge a player with a multiplier
					
				} else {
					int amount = card.getValue() * -1;
					serviceLayer.processSimpleCardTransaction(
							gameId, player.getPlayerId(), player.getCash(), amount);
				}
				break;
			case "CREDIT":
				if (card.getSpecial() != null && !card.getSpecial().isEmpty()) {
					// charge a player with a multiplier
				} else {
					serviceLayer.processSimpleCardTransaction(
						gameId, player.getPlayerId(), player.getCash(), card.getValue());
				}
				break;
			default:
				// HOLD
				break;
			}
		}
	}

	private void setNextPlayer() {
		
		List<Player> players = serviceLayer.getPlayers(gameId).get();
		UUID lastPlayerId = currentPlayerId;
		UUID firstPlayerId = players.get(0).getPlayerId();
		
		if (currentPlayerId == null) {
			currentPlayerId = firstPlayerId;
		} else {
			// find the next player
			boolean currentPlayerFound = false;
			for (Player player : players) {
				
				if (currentPlayerFound) {
					currentPlayerId = player.getPlayerId();
					break;
				}
				
				if (currentPlayerId.equals(player.getPlayerId())) {
					currentPlayerFound = true;
				}
			}
			
			if (currentPlayerId.equals(lastPlayerId)) {
				// end of the list, go back to the first player
				currentPlayerId = firstPlayerId;
			}
		}

		Optional<Player> player = serviceLayer.getPlayer(gameId, currentPlayerId);
		
		if (player.isPresent()) {
			statusText.setText(statusText.getText()
					+ "\n\nIt is now " + player.get().getName() + "'s turn.");
		}
	}
	
	private void addPlayerID(UUID playerId) {
		if (!playerIds.contains(playerId)) {
			playerIds.add(playerId);
		}
	}
	
	private void addToSkipTurn(UUID playerId, String action) {
		skipTurnMap.put(playerId, action);
	}
	
	private boolean skipPlayer(UUID playerId) {
		return skipTurnMap.containsKey(playerId);
	}
	
	private void removeFromSkipTurn(UUID playerId) {
		skipTurnMap.remove(playerId);
	}
	
	private void goToJail(UUID playerId, String name) {
		serviceLayer.addPlayerToJail(gameId, playerId, name);
	}
	
	private boolean isPlayerInJail(UUID playerId) {
		return serviceLayer.IsPlayerInJail(gameId, playerId);
	}
	
	private void getOutOfJail(UUID playerId) {
		serviceLayer.getOutOfJail(gameId, playerId);
	}
}
