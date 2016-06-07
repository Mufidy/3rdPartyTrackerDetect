/**
 * Last Step.
 * Generate report about trackers.
 */
package mhf.graduate.analyzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JTextArea;

import com.google.common.net.InternetDomainName;

/**
 * @author Haifei
 *
 */
public class Report {
	private String countryType;
	
	public Report(String countryType){
		this.countryType = countryType;
	}
	
	@SuppressWarnings("unchecked")
	public void generateReport(JTextArea text) {
		//for GUI
		text.setText("日志显示区：\n正在分析识别追踪者，请稍候...\n");
		text.paintImmediately(text.getBounds());

		WekaClassifier classifier= new WekaClassifier(countryType);
		ArrayList<Integer> requestsToTracker = classifier.classify();
		
		HashMap<String, ArrayList<String>> addressAndTrackers = new HashMap<String, ArrayList<String>>();
		
		HashMap<String, ArrayList<Integer>> requestMap = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:result/fourthparty"+countryType+".sqlite");
			String pSql = "select value from http_request_headers where name = 'Host' and http_request_id = ? ";
			pstmt = conn.prepareStatement(pSql);
			
			//get requestMap from disk
			String seriFile = "result/requestMap"+countryType+".serialize";
			FileInputStream fileIn = new FileInputStream(seriFile);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			requestMap = (HashMap<String, ArrayList<Integer>>) in.readObject();
	        in.close();
	        fileIn.close();

			Iterator<Entry<String, ArrayList<Integer>>> iter = requestMap.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, ArrayList<Integer>> entry = iter.next();
				String address = entry.getKey();
				ArrayList<Integer> requestIdList = entry.getValue();
				requestIdList.retainAll(requestsToTracker);//get requestIdList ∩ requestsToTracker
				
				if(!requestIdList.isEmpty()){
//					System.out.println(address+": "+requestIdList);
					ArrayList<String> trackers = new ArrayList<String>();
					for (int requestId : requestIdList){
						pstmt.setInt(1, requestId);
						ResultSet rs = pstmt.executeQuery();
						String trackerHost;
						if(rs.next()){
							trackerHost = rs.getString("value");
						}else {
							continue;
						}
						
						//now get trakcer's top level domain
						if(InternetDomainName.isValid(trackerHost)){
				        	 InternetDomainName idm = InternetDomainName.from(trackerHost);
				        	 if(idm.isUnderPublicSuffix())
				        		 trackerHost = idm.topPrivateDomain().toString();		        	 
				         }
						if (!trackers.contains(trackerHost)){
							trackers.add(trackerHost);
						}
					}
					addressAndTrackers.put(address, trackers);
				}
			}
			System.out.println(addressAndTrackers);
			
			// analyze trackers
			HashMap<String,Integer> trackerNums = new HashMap<String,Integer>();
			Iterator<Entry<String, ArrayList<String>>> iter1 = addressAndTrackers.entrySet().iterator();
			while(iter1.hasNext()){
				Entry<String, ArrayList<String>> entry = iter1.next();
				String address = entry.getKey();
				ArrayList<String> trackers = entry.getValue();
				System.out.println(address+": tracker numbers is "+trackers.size());
//				System.out.println(trackers.size());
				// for GUI
				text.append(address+"上识别出"+trackers.size()+"个追踪者。\n");
				text.paintImmediately(text.getBounds());
				
				for (String tracker :trackers){
					if(trackerNums.containsKey(tracker)){
						int num = trackerNums.get(tracker).intValue();
						num++;
						trackerNums.put(tracker, num);
					} else {
						trackerNums.put(tracker, 1);
					}
				}
			}
			System.out.println(trackerNums);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
