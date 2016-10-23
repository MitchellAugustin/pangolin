package com.mitchellaugustin.pangolin;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import javax.ws.rs.core.Response;

import com.mitchellaugustin.pangolin.utils.PasswordUtils;
import com.mitchellaugustin.pangolin.utils.SaveFile;

//@author Mitchell Augustin
//Part of Team Pangolin's GlobalHack VI Project
public class WebpageGenerator {
	public static Response createAccount(String username, String password, String location) throws NoSuchAlgorithmException, InvalidKeySpecException, ClassNotFoundException, SQLException{
		//String hash = PasswordUtils.getSaltedHash(password);
		Response response = Response.status(500).build();
		SaveFile.updateUserData(username, password, location);
		response = authenticateUser(username, password, WebPages.LOGIN);
		return response;
	}
	
	public static Response authenticateUser(String username, String password, WebPages page) throws ClassNotFoundException, SQLException{
		Response response = Response.status(500).build();
		try {
			if(PasswordUtils.confirmPasswordAuthenticity(password, SaveFile.getPasswordHashFromDatabase(username))){
				response = getWebPage(username, SaveFile.getPasswordHashFromDatabase(username), WebPages.LOGIN);
			}
			else{
				response = getIncorrectPasswordPage();
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (IllegalStateException f){
			//We will return the incorrect password page here because the user probably just entered an invalid username.
			response = getIncorrectPasswordPage();
		}
		  
		return response;
	}
	
	public static boolean authenticateWithHash(String username, String hashedPassword, WebPages page) throws ClassNotFoundException, SQLException{
		boolean response = false;
		try {
			if(hashedPassword.equals(SaveFile.getPasswordHashFromDatabase(username))){
				response = true;
			}
			else{
				response = false;
			}
		} catch (IllegalStateException f){
			//We will return the incorrect password page here because the user probably just entered an invalid username.
			response = false;
		}
		  
		return response;
	}
	
	private static Response getWebPage(String username, String hashedPassword, WebPages page) throws ClassNotFoundException, SQLException{
		Response defaultResponse = Response.status(500).build();
		switch(page){
		case LOGIN:
			return Response.status(200).entity(SaveFile.readFile(Constants.TEMPLATE_START_LOCATION) + "Login successful! You are signed in as " + username + getManagerSubpage(username, hashedPassword) + SaveFile.readFile(Constants.TEMPLATE_END_LOCATION)).build();
		case LOGOUT:
			return Response.status(200).entity("Logout Successful").build();
		case SHELTER_STATUS_EDITOR:
			return Response.status(200).entity("Editor").build();
		default:
			return defaultResponse;
		}
	}
	
	private static String getManagerSubpage(String username, String hashedPassword) throws ClassNotFoundException, SQLException{
		return "<form action=\"http://pangolin.mitchellaugustin.com:8080/PangolinServer/dynamic/updatedata\" method=\"post\" enctype=\"multipart/form-data\"> <input type=\"hidden\" name=\"username\" value=\"" + username + "\"> <input type=\"hidden\" name=\"hashedpassword\" value=\"" + hashedPassword + "\"> <p>Currently, there are " + SaveFile.getLiveShelterDataFromDatabase(username).replace(username + ": ", "") + " patrons in your shelter. <br> New Current Occupancy: </p>" +   "<input type=\"text\" name=\"currentoccupancy\"> <br> <p>New Maximum Occupancy: </p> <input type=\"text\" name=\"maxoccupancy\" value=\"\"> <br>  <input type=\"submit\" value=\"Update\" class=\"button\"> </form>";
	}
	
	private static Response getIncorrectPasswordPage(){
		Response failedLoginResponse = Response.status(401).entity(SaveFile.readFile(Constants.TEMPLATE_START_LOCATION) + "<h1>Error: Incorrect username or password. Please try again.</h1> <form action=\"http://pangolin.mitchellaugustin.com:8080/PangolinServer/login\" method=\"post\" enctype=\"multipart/form-data\"> Username: <input type=\"text\" name=\"username\"/> <br> <br> Password:   <input type=\"text\" name=\"password\" /> <input type=\"submit\" value=\"Login\" class=\"button\"/> </form>" + SaveFile.readFile(Constants.TEMPLATE_END_LOCATION)).build();
		return failedLoginResponse;
	}
}
