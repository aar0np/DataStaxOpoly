package com.datastax.datastaxopoly;

import java.util.Optional;
import java.util.UUID;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("DataStaxOpoly")
@Route("/")
public class LoginScreen extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	
	private DataStaxOpolyController serviceLayer;
	
	public LoginScreen() {
		
		serviceLayer = new DataStaxOpolyController();
		
		setId("login-view");
		TextField username = new TextField("Username");
		PasswordField password = new PasswordField("Password");
		
		add(
			new H1("Welcome to DataStaxOpoly!"),
			username,
			password,
			new Button("Login", event -> {
				Optional<UUID> playerId = serviceLayer
						.authenticatePlayer(username.getValue(), password.getValue());

				if (playerId.isPresent()) {
					UI.getCurrent().navigate("/gameselection/"
							+ username.getValue() + "," + playerId.get());
				} else {
					Notification.show("Incorrect credentials.");
				}
			})
		);
	}
}
