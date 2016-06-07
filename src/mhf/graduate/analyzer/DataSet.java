/**
 * Generate Train and Test date set for WEKA machine learning suite.
 */
package mhf.graduate.analyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @author Haifei
 *
 */
public class DataSet {
	private String countryType;
	
	public DataSet(){
	}
	
	public DataSet(String countryType){
		this.countryType = countryType;
	}
	
	public String generateDataSet(){
		//return the absolute path of DataSet in string
		if(countryType == null || (!countryType.equals("China") && !countryType.equals("Global"))){
			System.out.println("countryType error...\nexit unexceptedly...");
			return null;
		}
		
		ArrayList<Integer> requestsArray = new ArrayList<Integer>();
		
		CookieSet classifyCk = new CookieSet(countryType);
		System.out.println("Generating CookieSet...Please wait a few minutes...");
		HashMap<String, HashMap<Integer, ArrayList<Integer>>> CookieSet = classifyCk.getCookieSet();
		System.out.println("Generate CookieSet successfully...Now generating ClassifyDataSet...");
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		File file = null;
		String pSql = "SELECT creationTime, expiry, value FROM cookies WHERE id = ? ";
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:result/fourthparty"+countryType+".sqlite");
			pstmt = conn.prepareStatement(pSql);
			Iterator<Entry<String, HashMap<Integer, ArrayList<Integer>>>> iter = CookieSet.entrySet().iterator();
			
			file = new File("result/dataset"+countryType+".arff");
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();
			BufferedWriter fw = new BufferedWriter(new FileWriter(file));
//			ArrayList<String> toWriteStr = new ArrayList<String>();
			fw.write("@relation classifyTracker\r\n\r\n@attribute cookieNum numeric\r\n"
					+"@attribute lifeTimeMin numeric\r\n@attribute lifeTimeAug numeric\r\n"
					+"@attribute isTracker {TRUE, FALSE}\r\n\r\n@data\r\n");
			
			while(iter.hasNext()){
				Entry<String, HashMap<Integer, ArrayList<Integer>>> entry = iter.next();
				String address = entry.getKey();
				HashMap<Integer, ArrayList<Integer>> cookieIdMap = entry.getValue();
				Iterator<Entry<Integer, ArrayList<Integer>>> iter1 = cookieIdMap.entrySet().iterator();
				
				while(iter1.hasNext()){
					Entry<Integer, ArrayList<Integer>> entry1 = iter1.next();
					Integer requestId = entry1.getKey();
					long lifeTimeMin = Long.MAX_VALUE;
					int cookieNum = 0;
					long lifeTimeAug = 0L;
					ArrayList<Integer> cookieIds = entry1.getValue();
					
					for (int cookieId : cookieIds) {
						pstmt.setInt(1, cookieId);
						ResultSet rs = pstmt.executeQuery();
						long creationTime, expiry;
						String value;
						
						if(rs.next()){
							creationTime = rs.getLong("creationTime");
							expiry = rs.getLong("expiry");
							value = rs.getString("value");
						}else {
							continue;
						}
						long lifeTime = expiry - creationTime/1000000;
						int valueLen = value.length();
						
						if(lifeTime<lifeTimeMin)
							lifeTimeMin = lifeTime;
						cookieNum ++;
						lifeTimeAug += lifeTime*valueLen;
					}
					
					lifeTimeMin = (lifeTimeMin == Long.MAX_VALUE ? 0 : lifeTimeMin);
					System.out.println(address+"->"+requestId+"  cookieNum:"+cookieNum
							+" lifeTimeMin:"+lifeTimeMin+" lifeTimeAug:"+lifeTimeAug);
//					fw.write(address+"->"+requestId+"  cookieNum:"+cookieNum
//							+" lifeTimeMin:"+lifeTimeMin+" lifeTimeAug:"+lifeTimeAug+"\r\n");
					fw.write(cookieNum+","+lifeTimeMin+","+lifeTimeAug+",?\r\n");	
					requestsArray.add(requestId);
				}
				
			}
			System.out.println("ClassifyDataSet created successfully. Please check file "+ file.getAbsolutePath());
			fw.flush();
			fw.close();
			pstmt.close();
			conn.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//serialize requestsArray for later use
		try {
			String seriFile = "result/requestsArray"+countryType+".serialize";
			FileOutputStream fileOut = new FileOutputStream(seriFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(requestsArray);
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
		
		return file.getAbsolutePath();

	}
	
	public void generateTrainDataSet(){
		if(countryType == null || (!countryType.equals("China") && !countryType.equals("Global"))){
			System.out.println("countryType error...\nexit unexceptedly...");
			return ;
		}
		
		CookieSet classifyCk = new CookieSet(countryType);
		System.out.println("Generating CookieSet...Please wait a few minutes...");
		HashMap<String, HashMap<Integer, ArrayList<Integer>>> CookieSet = classifyCk.getCookieSet();
		System.out.println("Generate CookieSet successfully...Now generating ClassifyDataSet...");
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		String pSql = "SELECT creationTime, expiry, value FROM cookies WHERE id = ? ";
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:result/fourthparty"+countryType+".sqlite");
			pstmt = conn.prepareStatement(pSql);
			Iterator<Entry<String, HashMap<Integer, ArrayList<Integer>>>> iter = CookieSet.entrySet().iterator();
			
			File file = new File("result/train.txt");
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();
			BufferedWriter fw = new BufferedWriter(new FileWriter(file));
			ArrayList<String> toWriteStr = new ArrayList<String>();
			fw.write("@relation classifyTracker\r\n\r\n@attribute cookieNum numeric\r\n"
					+"@attribute lifeTimeMin numeric\r\n@attribute lifeTimeAug numeric\r\n"
					+"@attribute isTracker {TRUE, FALSE}\r\n\r\n@data\r\n");
			
			while(iter.hasNext()){
				Entry<String, HashMap<Integer, ArrayList<Integer>>> entry = iter.next();
				HashMap<Integer, ArrayList<Integer>> cookieIdMap = entry.getValue();
				Iterator<Entry<Integer, ArrayList<Integer>>> iter1 = cookieIdMap.entrySet().iterator();
				
				while(iter1.hasNext()){
					Entry<Integer, ArrayList<Integer>> entry1 = iter1.next();
					Integer requestId = entry1.getKey();
					long lifeTimeMin = Long.MAX_VALUE;
					int cookieNum = 0;
					long lifeTimeAug = 0L;
					ArrayList<Integer> cookieIds = entry1.getValue();
					
					for (int cookieId : cookieIds) {
						pstmt.setInt(1, cookieId);
						ResultSet rs = pstmt.executeQuery();
						long creationTime, expiry;
						String value;
						
						if(rs.next()){
							creationTime = rs.getLong("creationTime");
							expiry = rs.getLong("expiry");
							value = rs.getString("value");
						}else {
							continue;
						}
						long lifeTime = expiry - creationTime/1000000;
						int valueLen = value.length();
						
						if(lifeTime<lifeTimeMin)
							lifeTimeMin = lifeTime;
						cookieNum ++;
						lifeTimeAug += lifeTime*valueLen;
					}
					
					if (lifeTimeMin != Long.MAX_VALUE)
						toWriteStr.add(cookieNum+","+lifeTimeMin+","+lifeTimeAug+",?,"+requestId+"\r\n");
				}
				
			}
			System.out.println("ClassifyDataSet created successfully. Please check file "+ file.getAbsolutePath());
			for(int i=0;i<300;i++){
				int index =(int) (Math.random()*toWriteStr.size());
				fw.write(toWriteStr.get(index));
				toWriteStr.remove(index);
			}
			fw.flush();
			fw.close();
			pstmt.close();
			conn.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
