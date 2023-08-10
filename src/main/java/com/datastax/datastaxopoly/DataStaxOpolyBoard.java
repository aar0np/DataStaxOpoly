package com.datastax.datastaxopoly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.datastaxopoly.dal.Square;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.server.StreamResource;

@PageTitle("DataStaxOpoly")
@Route("")
public class DataStaxOpolyBoard extends VerticalLayout {

	private static final long serialVersionUID = 948032255252928441L;

	private DataStaxOpolyController serviceLayer;
	private Map<Integer,Square> squares;
	private List<Image> squareImages;
	private List<Image> spacers;
	private Image logoSpacer;
	
	public DataStaxOpolyBoard() {

		squareImages = new ArrayList<>();
		spacers = new ArrayList<>();
		serviceLayer = new DataStaxOpolyController();
		squares = loadSquares();
		
		setSpacing(false);
		add(buildTopOfBoard());
		
		for (int squareOffset = 1; squareOffset < 10; squareOffset++) {
			add(buildMiddleOfBoard(squareOffset));
		}
		
		add(buildBottomOfBoard());
	}
	
	private Map<Integer,Square> loadSquares() {
		
		Map<Integer,Square> returnVal = new HashMap<>();
		
		// square images
		for (int squareCounter = 0; squareCounter < 40; squareCounter++) {
			// add square
			Square square = serviceLayer.getSquare(squareCounter);
			returnVal.put(squareCounter, square);
			
			// load square's image
			loadImage("/images/" + square.getImage(), square.getImage());
		}
		
		// spacer images
		StreamResource spacerResource = new StreamResource("",
				() -> getClass().getResourceAsStream("/images/black_spacer.png"));
		StreamResource logoResource = new StreamResource("",
				() -> getClass().getResourceAsStream("/images/datastax_spacer.png"));

		logoSpacer = new Image(logoResource,"DataStaxOpoly");

		for (int counter = 0; counter < 10; counter++) {
			if (counter == 5) {
				spacers.add(logoSpacer);
			} else {
				Image spacer = new Image(spacerResource,"");
				spacers.add(spacer);
			}
		}
		return returnVal;
	}
	
	private void loadImage(String source, String name) {

		StreamResource resource = new StreamResource(name, () -> getClass().getResourceAsStream(source));
		
		squareImages.add(new Image(resource, name));
	}
	
	private Component buildTopOfBoard() {
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(false);
		
		layout.add(squareImages.get(20),squareImages.get(21),squareImages.get(22),
				squareImages.get(23),squareImages.get(24),squareImages.get(25),
				squareImages.get(26),squareImages.get(27),squareImages.get(28),
				squareImages.get(29),squareImages.get(30));
		
		return layout;
	}
	
	private Component buildBottomOfBoard() {
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(false);
		
		layout.add(squareImages.get(10),squareImages.get(9),squareImages.get(8),
				squareImages.get(7),squareImages.get(6),squareImages.get(5),
				squareImages.get(4),squareImages.get(3),squareImages.get(2),
				squareImages.get(1),squareImages.get(0));
		
		return layout;
	}
	
	private Component buildMiddleOfBoard(int squareOffset) {
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(false);
		int leftStart = 20;
		int rightStart = 30;

		layout.add(squareImages.get(leftStart - squareOffset),
				spacers.get(squareOffset - 1),
				squareImages.get(rightStart + squareOffset));
		
		return layout;
	}
}
