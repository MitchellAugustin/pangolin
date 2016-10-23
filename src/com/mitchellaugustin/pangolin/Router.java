package com.mitchellaugustin.pangolin;
 
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.mitchellaugustin.pangolin.utils.SaveFile;
import com.sun.jersey.multipart.FormDataParam;

//@author Mitchell Augustin
//Part of Team Pangolin's GlobalHack VI Project
@Path("/")
public class Router {
	String API_VERSION = "1.0";
	
	@Path("/")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response returnIndexPage(){
		return Response.status(200).entity("Pangolin Server").build();
	}
	
	@Path("/version")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response returnVersion() {
		return Response.status(200).entity("<p>Pangolin Server " + API_VERSION + "</p>").build();
	}
	
	@Path("/propability/distance={distance}&currentoccupancy={occupancy}&max={max}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response returnVersion(@PathParam("distance") double distance, @PathParam("occupancy") double currentOccupancy, @PathParam("max") double maximumOccupancy){
		AvailabilityCalculator calc = new AvailabilityCalculator(distance, currentOccupancy, maximumOccupancy);
		return Response.status(200).entity("The propability of visiting this shelter is: " + calc.getPropabilityValue() + "%.").build();
	}
	
	@Path("/shelters/location={location}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getShelterList(@PathParam("location") String location) throws ClassNotFoundException, SQLException, IOException, ParseException{
		Response response = Response.status(500).build();
		String page = SaveFile.readFile(Constants.TEMPLATE_START_LOCATION) + "<head>Shelters near " + location + "</head>";
		String[] shelters = SaveFile.getShelterList(location);
		double[] distances = SaveFile.getDistanceList(location);
		String[] addresses = SaveFile.getAddressList(location);

		double currentLowest = 1000;
		int lowestShelter = 0;
		for(int i = 0; i < shelters.length; i++){
			if(distances[i] < currentLowest){
				currentLowest = distances[i];
				lowestShelter = i;
			}
		}
		
		page += "<a href=\"" + Constants.GOOGLE_MAPS_URL + addresses[lowestShelter] + "\"><p>The nearest open shelter is " + shelters[lowestShelter] + " - " + distances[lowestShelter] + " miles from here.  (" + SaveFile.getLiveShelterDataFromDatabase(shelters[0]).replace(shelters[0] + ": ", "") + " occupants) </p></a><br><p>Other options include:</p>";
		
		for(int i = 0; i < shelters.length; i++){
			if(Integer.parseInt(SaveFile.getLiveShelterDataFromDatabase(shelters[i]).replace(shelters[i] + ": ", "").split("/")[0]) != Integer.parseInt(SaveFile.getLiveShelterDataFromDatabase(shelters[i]).replace(shelters[i] + ": ", "").split("/")[1])){
				page += "<a href=\"" + Constants.GOOGLE_MAPS_URL + addresses[i] + "\"><p>" + shelters[i] + " - " + distances[i] + " miles from here (" + SaveFile.getLiveShelterDataFromDatabase(shelters[i]).replace(shelters[i] + ": ", "") + " occupants)</p></a><br>";
			}
		}
		
		
		page += SaveFile.readFile(Constants.TEMPLATE_END_LOCATION);
		response = Response.status(200).entity(page).build();
		return response;
	}
	
	@Path("/shelters-public/location={location}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getPublicShelterList(@PathParam("location") String location) throws ClassNotFoundException, SQLException, IOException, ParseException{
		Response response = Response.status(500).build();
		String page = SaveFile.readFile(Constants.STATIC_DATA_LOCATION + "template_start_nonav.html") + "<head>Shelters near " + location + "</head>";
		String[] shelters = SaveFile.getShelterList(location);
		double[] distances = SaveFile.getDistanceList(location);
		String[] addresses = SaveFile.getAddressList(location);

		double currentLowest = 1000;
		int lowestShelter = 0;
		for(int i = 0; i < shelters.length; i++){
			if(distances[i] < currentLowest){
				currentLowest = distances[i];
				lowestShelter = i;
			}
		}
		
		page += "<a href=\"" + Constants.GOOGLE_MAPS_URL + addresses[lowestShelter] + "\"><p>The nearest open shelter is " + shelters[lowestShelter] + " - " + distances[lowestShelter] + " miles from here.  (" + SaveFile.getLiveShelterDataFromDatabase(shelters[0]).replace(shelters[0] + ": ", "") + " occupants) </p></a><br><p>Other options include:</p>";
		
		for(int i = 0; i < shelters.length; i++){
			if(Integer.parseInt(SaveFile.getLiveShelterDataFromDatabase(shelters[i]).replace(shelters[i] + ": ", "").split("/")[0]) != Integer.parseInt(SaveFile.getLiveShelterDataFromDatabase(shelters[i]).replace(shelters[i] + ": ", "").split("/")[1])){
				page += "<a href=\"" + Constants.GOOGLE_MAPS_URL + addresses[i] + "\"><p>" + shelters[i] + " - " + distances[i] + " miles from here.  (" + SaveFile.getLiveShelterDataFromDatabase(shelters[i]).replace(shelters[i] + ": ", "") + " occupants) </p></a><br>";
			}
		}
		
		
		page += SaveFile.readFile(Constants.TEMPLATE_END_LOCATION);
		response = Response.status(200).entity(page).build();
		return response;
	}
	
	@Path("/calculatedistance/currentlocation={current}&shelterlocation={shelter}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response calculateDistanceToShelter(@PathParam("current") String currentLocation, @PathParam("shelter") String shelterLocation) throws ClassNotFoundException, SQLException, IOException, ParseException{
		Response response = Response.status(500).build();
		response = Response.status(200).entity(AvailabilityCalculator.getDistanceToShelter(currentLocation, shelterLocation) + " miles").build();
		return response;
	}
	
	@Path("/login")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response login(@FormDataParam("username") String username, @FormDataParam("password") String password) throws ClassNotFoundException, SQLException{
		Response response = Response.status(500).build();
		response = WebpageGenerator.authenticateUser(username, password, WebPages.LOGIN);
		return response;
	}
	
	@Path("/create")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response createAccount(@FormDataParam("username") String username, @FormDataParam("password") String password, @FormDataParam("location") String location) throws NoSuchAlgorithmException, InvalidKeySpecException, ClassNotFoundException, SQLException{
		Response response = Response.status(500).build();
		response = WebpageGenerator.createAccount(username, password, location);
		return response;
	}
	
	@Path("/updatedata")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response updateOccupancyData(@FormDataParam("username") String username, @FormDataParam("hashedpassword") String hashedPassword, @FormDataParam("currentoccupancy") String currentOccupancy, @FormDataParam("maxoccupancy") String maxOccupancy) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException{
		Response response = Response.status(500).build();
		if(WebpageGenerator.authenticateWithHash(username, hashedPassword, WebPages.LOGIN)){
			SaveFile.updateLiveShelterData(username, currentOccupancy, maxOccupancy);
			response = Response.status(200).entity(SaveFile.readFile(Constants.TEMPLATE_START_LOCATION) + "<p>Success! Your data has been updated.</p><br><p>" + SaveFile.getLiveShelterDataFromDatabase(username)).build();
		}
		else{
			response = Response.status(401).entity(SaveFile.readFile(Constants.TEMPLATE_START_LOCATION) + "<p>Error: Incorrect password hash.</p><br><p>Current number of patrons: " + SaveFile.readFile(Constants.TEMPLATE_END_LOCATION)).build();
		}
		return response;
	}
}