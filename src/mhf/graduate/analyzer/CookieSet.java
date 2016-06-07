/**
 * Generate CookieSet(third party cookies) whose key is web site address(A);
 * for each value,
 * it also is a HashMap, and key is HTPP request ID
 * value is an ArrayList, which contains all cookies' ID contained by request#ID
 * Firstly, it will generate HashMap<String,ArrayList<Integer>> requestsMap.
 */
package mhf.graduate.analyzer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @author Haifei
 *
 */
public class CookieSet {
	private String countryType;
	
	public CookieSet(){
	}
	
	public CookieSet(String countryType){
		this.countryType = countryType;
	}
	
	public HashMap<String, HashMap<Integer, ArrayList<Integer>>> getCookieSet(){
		return generateCookieSet();
	}
	
	private HashMap<String, ArrayList<Integer>> generateRequestMap(){
		HashMap<String,ArrayList<Integer>> result = new HashMap<String, ArrayList<Integer>>();
		if(countryType == null || (!countryType.equals("China") && !countryType.equals("Global"))){
			System.out.println("countryType error...\nexit unexceptedly...");
			return null;
		}
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:result/fourthparty"+countryType+".sqlite");
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id,page_id FROM http_requests WHERE request_type!=1");
			
			while(rs.next()){
				int id = rs.getInt("id");
				int pageId = rs.getInt("page_id");
				         
				int pId = -1;
				String address = "";
				while (true) {
					Statement stmt1 = conn.createStatement();
					ResultSet rs1 = stmt1.executeQuery("SELECT id,location,parent_id FROM pages WHERE id="+pageId);
					if(rs1.next()){
						pageId = rs1.getInt("id");
						address = rs1.getString("location");
						pId = rs1.getInt("parent_id");
					}else{
						break;
					}
					
					if(pageId == pId){
						break;
					}else{
						pageId = pId;
					}					
				}
				
				//handle the case which address is empty
				if(address.equals("")){
					continue;
				}
				
				if(result.containsKey(address)){
					result.get(address).add(id);
				}else{
					ArrayList<Integer> rId = new ArrayList<Integer>();
					rId.add(id);
					result.put(address, rId);
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//serialize RequestMap for later use
		try {
			String seriFile = "result/requestMap"+countryType+".serialize";
			FileOutputStream fileOut = new FileOutputStream(seriFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(result);
			out.close();
			fileOut.close();
			System.out.println("Serialized data is saved in "+seriFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	//Generate cookieMap(third party cookies) whose key is web site address
	//CookieSet is HashMap: <address,HashMap for <requestID, array for cookieID>>;
	private HashMap<String,HashMap<Integer,ArrayList<Integer>>> generateCookieSet(){
		if(countryType == null || (!countryType.equals("China") && !countryType.equals("Global"))){
			System.out.println("countryType error...\nexit unexceptedly...");
			return null;
		}
		HashMap<String,HashMap<Integer,ArrayList<Integer>>> CookieSet = new HashMap<String,HashMap<Integer,ArrayList<Integer>>>();
		
		Connection conn = null;
		Statement stmt = null;
		
		HashMap<String, ArrayList<Integer>> requestMap = generateRequestMap();
		System.out.println("requestMap has been created successfully, now creating CookieSet...");
		
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:result/fourthparty"+countryType+".sqlite");
			stmt = conn.createStatement();
			Iterator<Entry<String, ArrayList<Integer>>> iter = requestMap.entrySet().iterator();
			
			while(iter.hasNext()){
				Entry<String, ArrayList<Integer>> entry = iter.next();
				String address = entry.getKey();
				ArrayList<Integer> requestIdList = entry.getValue();
				CookieSet.put(address, new HashMap<Integer,ArrayList<Integer>>());
				
				for(int requestId : requestIdList){

					CookieSet.get(address).put(requestId, new ArrayList<Integer>());
					
					String sql = "SELECT value FROM http_request_headers WHERE name ='Cookie' and http_request_id="+requestId;
					ResultSet rs = stmt.executeQuery(sql);
					String cks = null;
					if(rs.next()){
						cks = rs.getString("value");
					}else{
						continue;
					}
					String pSql = "SELECT id FROM cookies WHERE is_session=0 and name= ? and value= ? ";
					PreparedStatement pstmt = conn.prepareStatement(pSql);
					String[] cksArr = cks.split(";");
					for(String ck : cksArr){
						ck = ck.trim();
						String[] ckNV = ck.split("=", 2);
						String ckName = ckNV[0];
						String ckValue = ckNV[1];							
						pstmt.setString(1, ckName);
						pstmt.setString(2, ckValue);
						ResultSet rs1 = pstmt.executeQuery();
						int cookieId = -1;
						if(rs1.next()){
							cookieId = rs1.getInt("id");
						}else{
							continue;
						}
						
						//add this cookieId into CookieSet
						CookieSet.get(address).get(requestId).add(cookieId);
						
					}
				}
			}
			stmt.close();
			conn.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return CookieSet;
	}
}
