package com.mitchellaugustin.pangolin;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.mitchellaugustin.pangolin.utils.Log;
import com.mitchellaugustin.pangolin.utils.SaveFile;

public class AvailabilityCalculator {
	private double distance;
	private double occupancy;
	private double maxOccupancy;
	private double availibility;
	private double propability = 0;
	
	public AvailabilityCalculator(double distance, double occupancy, double maxOccupancy){
		this.distance = distance;
		this.occupancy = occupancy;
		this.maxOccupancy = maxOccupancy;
	}
	
	public double getPropabilityValue(){
		availibility = maxOccupancy / occupancy;
		propability = (0.4 * availibility) / (0.6 * distance/2);
		return propability * 100;
	}
	
	/**
	 * Returns the driving/walking distance to the shelter from the specified location.
	 * @param currentAddress - The current address of the user
	 * @param shelterName - The username of the homeless shelter (NOT the address)
	 * @return distance - The distance to the homeless shelter
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static double getDistanceToShelter(String currentAddress, String shelterName) throws ClassNotFoundException, SQLException, IOException, ParseException{
		double distance = 0;
		String serverResponse = "";
	    try{
            HttpClient client = new DefaultHttpClient();
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" + currentAddress.replace(" ", "%20").replace(",", "%2C").replace(".", "%2E") + "&destinations=" + SaveFile.getShelterAddressFromDatabase(shelterName).replace(" ", "%20").replace(",", "%2C").replace(".", "%2E") + "&key=" + Constants.GOOGLE_MAPS_API_KEY;
            Log.info(url);
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                serverResponse = EntityUtils.toString(resEntity);
                Log.info(serverResponse);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
	    
	    
	    JSONObject jsonRespRouteDistance = new JSONObject(serverResponse)
                .getJSONArray("rows")
                .getJSONObject(0)
                .getJSONArray ("elements")
                .getJSONObject(0)
                .getJSONObject("distance");

	    
	    String returnedDistance = jsonRespRouteDistance.get("text").toString();
	    if(returnedDistance.contains("ft")){
	    	returnedDistance = returnedDistance.replace(" ft", "");
	    	distance = Double.parseDouble(returnedDistance) / 5280;
	    }
	    else{
	    	returnedDistance = returnedDistance.replace(" mi", "");
	    	distance = Double.parseDouble(returnedDistance);
	    }
	    
	    
	    
		return distance;
	}
}
