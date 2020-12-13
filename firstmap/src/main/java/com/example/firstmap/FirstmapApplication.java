package com.example.firstmap;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class FirstmapApplication {
	Scanner sc = new Scanner(System.in);
	JSONParser jsonParser = new JSONParser();
	DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");


	private static double getFileSizeMegaBytes(File file) {
		return  file.length() / (1024 * 1024);
	}

	private static double getFileSizeKiloBytes(File file) {
		return file.length() / 1024;
	}

	private void createDataStore() throws Exception {
		System.out.println("Enter the Key capped at 32chars");
		String key = sc.next();

		if (key.length() > 32) {
			System.out.println("key should be capped with 32 chars");
			return;
		}
		String fileName =  "dataStore.json";
		File myObj = new File(fileName);
		if (myObj.createNewFile()) {
			FileWriter myWriter = new FileWriter(fileName);
			myWriter.write("{\n" +
					"    \"time\": []   \n" +
					"}");
			myWriter.close();
		}
		if(getFileSizeMegaBytes(myObj)>1024){
			System.out.println("Data store Memory limit exceeds");
			return;
		}

		Object dataStoreObject = jsonParser.parse(new FileReader("dataStore.json"));
		String jsonInString = new Gson().toJson(dataStoreObject);
		JSONObject mJSONObject = new JSONObject(jsonInString);

		if(mJSONObject.has(key)){
			System.out.println(" This key already exists");
			return;
		}

		System.out.println("Enter time to live (optional) || Not need give 0");
		int time=sc.nextInt();

		System.out.println("Enter the json Object file path");
		String path = sc.next();

		File file = new File(path);
		if(getFileSizeKiloBytes(file)>16){
			System.out.println("Json should be capped with 16 KB");
			return;
		}

		Object obj = jsonParser.parse(new FileReader(path));
		mJSONObject.put(key,obj);

		JSONArray jsonArray=mJSONObject.getJSONArray("time");
		JSONObject time1=new JSONObject();
		time1.put(key,dateFormat.format(new Date())+","+time);
		jsonArray.put(time1);

		mJSONObject.put("time",jsonArray);

		FileWriter myWriter = new FileWriter(fileName);
		myWriter.write(mJSONObject.toString());
		myWriter.close();
		System.out.println("Data Store created and inserted successfully !!!");
	}

	private boolean timeToLive(String key) throws IOException, ParseException, java.text.ParseException, JSONException {
		Object obj = jsonParser.parse(new FileReader( "dataStore.json"));
		String jsonInString = new Gson().toJson(obj);
		JSONObject mJSONObject = new JSONObject(jsonInString);
		JSONArray jsonArray = mJSONObject.getJSONArray("time");
		Object value=null;
		for(int i=0;i<jsonArray.length();i++){
			if(jsonArray.getJSONObject(i).has(key)){
				value=jsonArray.getJSONObject(i).get(key);
				break;
			}
		}
		int sec=Integer.parseInt(value.toString().split(",")[1]);
		if(sec==0){
			return true;
		}else{
			Date d= dateFormat.parse(value.toString().split(",")[0]);
			Date nowd=new Date();
			long diffInMillies = Math.abs(d.getTime() - nowd.getTime());
			if(TimeUnit.MILLISECONDS.toSeconds(diffInMillies)<sec){
				return true;
			}else{
				return false;
			}
		}
	}

	private Boolean retrieveDataStore() throws IOException, ParseException, java.text.ParseException, JSONException {
		Object s=null;
		System.out.println("Enter key to retrieve data store");
		String key = sc.next();
		File f = new File("dataStore.json");
		if (!f.exists()) {
			System.out.println("Data store file not found");
			return false;
		}
		Object obj = jsonParser.parse(new FileReader( "dataStore.json"));
		String jsonInString = new Gson().toJson(obj);
		JSONObject mJSONObject = new JSONObject(jsonInString);

		if(mJSONObject.has(key)){
			s= mJSONObject.get(key);
		}else{
			System.out.println("key not found");
			return false;
		}
		if(this.timeToLive(key)){
			System.out.println(s.toString());
		}else{
			System.out.println("Time to Live completed");
		}
		return true;
	}

	private boolean deleteDataStore() throws ParseException, java.text.ParseException, IOException, JSONException {

		System.out.println("Enter the key");
		String key = sc.next();
		File f = new File("dataStore.json");
		if (!f.exists()) {
			System.out.println("Data store file not found");
			return false;
		}

		Object obj = jsonParser.parse(new FileReader( "dataStore.json"));
		String jsonInString = new Gson().toJson(obj);
		JSONObject mJSONObject = new JSONObject(jsonInString);

		if(!mJSONObject.has(key)){
			System.out.println("key not found");
			return false;
		}
		if(this.timeToLive(key)){
			mJSONObject.remove(key);
			FileWriter myWriter = new FileWriter("dataStore.json");
			myWriter.write(mJSONObject.toString());
			myWriter.close();
			System.out.println("Removed Successfully !!!");
		}else{
			System.out.println("Time to like exceeds for this key");
			return false;
		}

		return true;
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(FirstmapApplication.class, args);
		FirstmapApplication product = new FirstmapApplication();
		Scanner sc = new Scanner(System.in);

		while (true) {
			System.out.println("press 1 to create data store");
			System.out.println("press 2 to retrieve from data store");
			System.out.println("press 3 to delete data store");
			System.out.println("press 0 to run out of program");
			int s = sc.nextInt();
			if (s == 0)
				break;
			switch (s) {
				case 1: {
					product.createDataStore();
				}
				break;

				case 2: {
					product.retrieveDataStore();
				}
				break;

				case 3: {
					product.deleteDataStore();
				}
				break;

				default: {
					System.out.println("please check your input");
				}
				break;
			}
		}

	}
}



