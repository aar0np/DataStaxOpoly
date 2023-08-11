package com.datastax.datastaxopoly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.datastax.datastaxopoly.dal.Square;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style.Position;
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
	private Map<Integer,Square> squares;
	private List<Image> squareImages;
	//private List<Image> spacers;
	//private Image logoSpacer;
	
	private UUID playerId;
	private UUID gameId;
	
	public DataStaxOpolyBoard() {

		squareImages = new ArrayList<>();
		//spacers = new ArrayList<>();
		serviceLayer = new DataStaxOpolyController();
		squares = loadSquares();
		
		setSpacing(false);
		add(buildBoard());
		
		//add(buildTopOfBoard());
		
		//for (int squareOffset = 1; squareOffset < 10; squareOffset++) {
		//	add(buildMiddleOfBoard(squareOffset));
		//}

		//add(buildBottomOfBoard());

		// testing
		//Icon token = new Icon(VaadinIcon.AIRPLANE);
		//token.setColor("blue");
		//token.getStyle().setPosition(Position.ABSOLUTE);
		//token.getStyle().setLeft("570px");
		//token.getStyle().setTop("570px");
		//addComponentAtIndex(0, token);

		
		
	}
	
    @Override
    public void setParameter(BeforeEvent event, String gameAndPlayerIds) {
        
    	if (gameAndPlayerIds == null) {
    		UI.getCurrent().navigate("/");
    	} else {
    		String[] gamePlayerArray = gameAndPlayerIds.split(",");
    		
    		this.gameId = UUID.fromString(gamePlayerArray[0]);
    		this.playerId = UUID.fromString(gamePlayerArray[1]);
    	}
    }
	
	private Map<Integer,Square> loadSquares() {
		
		Map<Integer,Square> returnVal = new HashMap<>();
		
		// squares
		for (int squareCounter = 0; squareCounter < 40; squareCounter++) {
			// add square
			Square square = serviceLayer.getSquare(squareCounter);
			returnVal.put(squareCounter, square);
			
			// load square's image
			loadImage("/fullSizeImages/" + square.getImage(), square.getImage());
		}
		
//		// spacer images
//		StreamResource spacerResource = new StreamResource("",
//				() -> getClass().getResourceAsStream("/images/black_spacer.png"));
//		StreamResource logoResource = new StreamResource("",
//				() -> getClass().getResourceAsStream("/images/datastax_spacer.png"));
//
//		logoSpacer = new Image(logoResource,"DataStaxOpoly");
//
//		for (int counter = 0; counter < 10; counter++) {
//			if (counter == 4) {
//				spacers.add(logoSpacer);
//			} else {
//				Image spacer = new Image(spacerResource,"");
//				spacers.add(spacer);
//			}
//		}
		
		return returnVal;
	}
	
	private void loadImage(String source, String name) {

		StreamResource resource = new StreamResource(name, () -> getClass().getResourceAsStream(source));
		
		squareImages.add(new Image(resource, name));
	}
	
	private Component buildBoard() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(false);
		
		StreamResource boardResource = new StreamResource("",
				() -> getClass().getResourceAsStream("/images/board.png"));
		Image boardImage = new Image(boardResource,"");
		layout.add(boardImage);
		
		return layout;
	}
	
//	private Component buildTopOfBoard() {
//		
//		HorizontalLayout layout = new HorizontalLayout();
//		layout.setSpacing(false);
//		
//		layout.add(squareImages.get(20),squareImages.get(21),squareImages.get(22),
//				squareImages.get(23),squareImages.get(24),squareImages.get(25),
//				squareImages.get(26),squareImages.get(27),squareImages.get(28),
//				squareImages.get(29),squareImages.get(30));
//		
//		return layout;
//	}
//	
//	private Component buildBottomOfBoard() {
//		
//		HorizontalLayout layout = new HorizontalLayout();
//		layout.setSpacing(false);
//		
//		layout.add(squareImages.get(10),squareImages.get(9),squareImages.get(8),
//				squareImages.get(7),squareImages.get(6),squareImages.get(5),
//				squareImages.get(4),squareImages.get(3),squareImages.get(2),
//				squareImages.get(1),squareImages.get(0));
//		
//		return layout;
//	}
//	
//	private Component buildMiddleOfBoard(int squareOffset) {
//		
//		HorizontalLayout layout = new HorizontalLayout();
//		layout.setSpacing(false);
//		int leftStart = 20;
//		int rightStart = 30;
//
//		layout.add(squareImages.get(leftStart - squareOffset),
//				spacers.get(squareOffset - 1),
//				squareImages.get(rightStart + squareOffset));
//		
//		return layout;
//	}
}
