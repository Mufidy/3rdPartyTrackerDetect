package mhf.graduate.gui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import mhf.graduate.analyzer.WekaClassifier;

public class TestForPaper {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WekaClassifier c = new WekaClassifier("China");
		ArrayList<Integer> requestsToTrackers = c.classify();
		Connection conn = null;
		Statement stmt = null;
		ArrayList<Integer> trackerCookies = new ArrayList<Integer>();
		int[] trackerlengths = new int[500];
		int[] nontrackerlengths = new int[500];
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:result/fourthpartyChina.sqlite");
			stmt = conn.createStatement();
		
			for(int requestId : requestsToTrackers){
	
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
					//System.out.println(cookieId);
					if(!trackerCookies.contains(cookieId))
						trackerCookies.add(cookieId);
				}
			}
			
			ArrayList<Integer> nonTracker = new ArrayList<Integer>();
			for(int i=0;i<2436;i++){
				if(!trackerCookies.contains(i)){
					if ( !nonTracker.contains(i)){
						nonTracker.add(i);
					}
				}
					
			}
			String pSql = "SELECT creationTime, expiry, value FROM cookies WHERE is_session = 0 and id = ?";
			PreparedStatement pstmt = conn.prepareStatement(pSql);
			for (int ckid:trackerCookies){
				pstmt.setInt(1, ckid);
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
				long lifeTime = (expiry - creationTime/1000000)/3600;
				int valueLen = value.length();
				//System.out.println(lifeTime+" "+valueLen);
				trackerlengths[valueLen]++;
				System.out.println("lifeTime "+lifeTime);
			}
			
			for (int ckid:nonTracker){
				pstmt.setInt(1, ckid);
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
				long lifeTime = (expiry - creationTime/1000000)/3600;
				int valueLen = value.length();
				//System.out.println(lifeTime+" "+valueLen);
				nontrackerlengths[valueLen]++;
				System.out.println("Non Tracker lifeTime "+lifeTime);
			}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(trackerCookies);
//		int tempT = 0, tempN = 0;
//		for(int i=0; i<500; i++){			
//			if(trackerlengths[i]!=0 || nontrackerlengths[i]!=0)
//			{
//				tempT += trackerlengths[i];
//				tempN += nontrackerlengths[i];
//				System.out.println(i+" "+tempT+" "+tempN);
//			}
//		}
		
	}

}
