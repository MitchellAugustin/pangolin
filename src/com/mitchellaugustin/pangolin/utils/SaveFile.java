package com.mitchellaugustin.pangolin.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.mitchellaugustin.pangolin.AvailabilityCalculator;
import com.mitchellaugustin.pangolin.Constants;

//@author Mitchell Augustin
//Part of Team Pangolin's GlobalHack VI Project
public class SaveFile {
	
	/**
	 * Saves the specified text to the specified file
	 * @param filename - The file to save the text to
	 * @param filetext - The text to write
	 */
	public static void saveFile(String filename, String filetext){
		PrintWriter writer;
		try {
			if(new File(filename).exists()){
				Log.info("Saving " + filename + " with the value \"" + filetext + "\"");
				writer = new PrintWriter(filename, "UTF-8");
		        writer.println(filetext);
		        writer.close();
			}
			else{
				try {
					new File(filename).createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Log.info("Saving " + filename + " with the value \"" + filetext + "\"");
				writer = new PrintWriter(filename.trim(), "UTF-8");
		        writer.println(filetext);
		        writer.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static String readFile(String filename){
		String file = "Unavailible";
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(filename));
			file = new String(encoded, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	public static void updateUserData(String username, String password, String location) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException{
		Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Constants.SERVER_DATA_LOCATION + "userinfo.db");
        Statement queryStatement = conn.createStatement();
        //This must be enabled to CREATE a new database
//        queryStatement.executeUpdate("drop table if exists passwords;");
//        queryStatement.executeUpdate("create table passwords (name, password, location);");
        PreparedStatement prep = conn.prepareStatement("insert into passwords values (?, ?, ?);"); 
 
        prep.setString(1, username);
        prep.setString(2, PasswordUtils.getSaltedHash(password));
        prep.setString(3, location);
        prep.addBatch();
 
        conn.setAutoCommit(false);
        prep.executeBatch();
        conn.setAutoCommit(true);
 
        ResultSet rs = queryStatement.executeQuery("select * from passwords;");
        while (rs.next()) {
			String currentName = rs.getString("name");
			String currentHash = rs.getString("password");
//			  String currentLocation = rs.getString("location");
//            System.out.println("usernamename = " + currentName);
//            System.out.println("password hash = " + currentHash);
            if(currentName.equals(username)){
            	Log.info("Match found! " + currentHash);
            }
        } 
        rs.close();
        conn.close();
	}
	
	public static void updateLiveShelterData(String username, String currentOccupancy, String maxOccupancy) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException{
		Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Constants.SERVER_DATA_LOCATION + "userinfo.db");
        Statement queryStatement = conn.createStatement();
        //This must be enabled to CREATE a new database
//        queryStatement.executeUpdate("drop table if exists occupancy;");
//        queryStatement.executeUpdate("create table occupancy (name, currentOccupancy, maxOccupancy);");
        PreparedStatement prep = conn.prepareStatement("insert into occupancy values (?, ?, ?);"); 
 
        prep.setString(1, username);
        prep.setString(2, currentOccupancy);
        prep.setString(3, maxOccupancy);
        prep.addBatch();
 
        conn.setAutoCommit(false);
        prep.executeBatch();
        conn.setAutoCommit(true);
 
        ResultSet rs = queryStatement.executeQuery("select * from occupancy;");
        while (rs.next()) {
			String currentName = rs.getString("name");
			String currentCurrentOccupancy = rs.getString("currentOccupancy");
			String currentMaxOccupancy = rs.getString("maxOccupancy");
//            System.out.println("usernamename = " + currentName);
//            System.out.println("password hash = " + currentHash);
            if(currentName.equals(username)){
            	Log.info("Table updated successfully! " + currentName + ": " + currentCurrentOccupancy + "/" + currentMaxOccupancy + " patrons.");
            }
        } 
        rs.close();
        conn.close();
	}
	
	public static String getLiveShelterDataFromDatabase(String username) throws ClassNotFoundException, SQLException{
		String passwordHash = "";
		Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Constants.SERVER_DATA_LOCATION + "userinfo.db");
        
		Statement queryStatement = conn.createStatement();
        
		ResultSet rs = queryStatement.executeQuery("select * from occupancy;");
		while (rs.next()) {
			String currentName = rs.getString("name");
			String currentCurrentOccupancy = rs.getString("currentOccupancy");
			String currentMaxOccupancy = rs.getString("maxOccupancy");
//            System.out.println("usernamename = " + currentName);
//            System.out.println("password hash = " + currentHash);
            if(currentName.equals(username)){
            	Log.info("Match found! " + currentName);
            	passwordHash = currentName + ": " + currentCurrentOccupancy + "/" + currentMaxOccupancy;
            }
        } 
        rs.close();
        conn.close();
        
        return passwordHash;
	}
	
	public static String getPasswordHashFromDatabase(String username) throws ClassNotFoundException, SQLException{
		String passwordHash = "";
		Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Constants.SERVER_DATA_LOCATION + "userinfo.db");
        
		Statement queryStatement = conn.createStatement();
        
		ResultSet rs = queryStatement.executeQuery("select * from passwords;");
		while (rs.next()) {
			String currentName = rs.getString("name");
			String currentHash = rs.getString("password");
//            System.out.println("usernamename = " + currentName);
//            System.out.println("password hash = " + currentHash);
            if(currentName.equals(username)){
            	Log.info("Match found! " + currentHash);
            	passwordHash = currentHash;
            }
        } 
        rs.close();
        conn.close();
        
        return passwordHash;
	}
	
	public static String[] getShelterList(String location) throws ClassNotFoundException, SQLException, IOException, ParseException{
		List<String> shelters = new ArrayList<String>();
		String[] shelterList = null;
		Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Constants.SERVER_DATA_LOCATION + "userinfo.db");
        
		Statement queryStatement = conn.createStatement();
        
		ResultSet rs = queryStatement.executeQuery("select * from passwords;");
	
		ResultSet rs2 = queryStatement.executeQuery("select * from passwords;");
		
		while(rs.next()){
			String currentName = rs.getString("name");
			
			shelters.add(currentName);
		}
		
		shelterList = new String[shelters.size()];
		
		for(int i = 0; i < shelters.size(); i++){
			shelterList[i] = shelters.get(i);
		}
		
        rs.close();
        rs2.close();
        conn.close();
        
        return shelterList;
	}
	
	public static String[] getAddressList(String location) throws ClassNotFoundException, SQLException, IOException, ParseException{
		List<String> shelters = new ArrayList<String>();
		String[] shelterList = null;
		Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Constants.SERVER_DATA_LOCATION + "userinfo.db");
        
		Statement queryStatement = conn.createStatement();
        
		ResultSet rs = queryStatement.executeQuery("select * from passwords;");
	
		ResultSet rs2 = queryStatement.executeQuery("select * from passwords;");
		
		while(rs.next()){
			String currentName = rs.getString("location");
			
			shelters.add(currentName);
		}
		
		shelterList = new String[shelters.size()];
		
		for(int i = 0; i < shelters.size(); i++){
			shelterList[i] = shelters.get(i);
		}
		
        rs.close();
        rs2.close();
        conn.close();
        
        return shelterList;
	}
	
	public static double[] getDistanceList(String location) throws ClassNotFoundException, SQLException, IOException, ParseException{
		List<String> shelters = new ArrayList<String>();
		List<Double> distances = new ArrayList<Double>();
		String[] shelterList = null;
		double[] distanceList = null;
		Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Constants.SERVER_DATA_LOCATION + "userinfo.db");
        
		Statement queryStatement = conn.createStatement();
        
		ResultSet rs = queryStatement.executeQuery("select * from passwords;");
	
		ResultSet rs2 = queryStatement.executeQuery("select * from passwords;");
		
		while(rs.next()){
			String currentName = rs.getString("name");
			double currentDistance = AvailabilityCalculator.getDistanceToShelter(location, currentName);

			shelters.add(currentName);
			distances.add((double) currentDistance);
		}
		
		shelterList = new String[shelters.size()];
		distanceList = new double[distances.size()];
		
		for(int i = 0; i < shelters.size(); i++){
			distanceList[i] = distances.get(i);
			shelterList[i] = shelters.get(i);
		}
		
        rs.close();
        rs2.close();
        conn.close();
        
        return distanceList;
	}
	
	public static String getShelterAddressFromDatabase(String username) throws ClassNotFoundException, SQLException{
		String passwordHash = "";
		Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Constants.SERVER_DATA_LOCATION + "userinfo.db");
        
		Statement queryStatement = conn.createStatement();
        
		ResultSet rs = queryStatement.executeQuery("select * from passwords;");
		while (rs.next()) {
			String currentName = rs.getString("name");
			String currentAddress = rs.getString("location");
//            System.out.println("usernamename = " + currentName);
//            System.out.println("password hash = " + currentHash);
            if(currentName.equals(username)){
            	Log.info("Match found! " + currentAddress);
            	passwordHash = currentAddress;
            }
        } 
        rs.close();
        conn.close();
        
        return passwordHash;
	}
	
}
