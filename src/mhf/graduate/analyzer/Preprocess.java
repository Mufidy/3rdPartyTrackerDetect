/*
 * Classify Http Requests to first party, second party and special second party requests.
 * This class will add a column called "request_type" in table "http_requests".
 */
package mhf.graduate.analyzer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;

import com.google.common.net.InternetDomainName;

/**
 * @author Haifei
 *
 */
public class Preprocess {
	private String countryType;
	
	public Preprocess(){
	}
	
	public Preprocess(String countryType){
		this.countryType = countryType;
	}
	
	public void classify(JTextArea text){
		//for GUI
		text.setText("日志显示区：\n正在分析HTTP请求，请稍候...\n");
		text.paintImmediately(text.getBounds());

		if(countryType == null || (!countryType.equals("China") && !countryType.equals("Global"))){
			System.out.println("countryType error...\nexit unexceptedly...");
			return;
		}
		ArrayList<Integer> hIds3rd = new ArrayList<Integer>();
		int cookieTotalNo = 0;
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:result/fourthparty"+countryType+".sqlite");
			stmt = conn.createStatement();
			
			//Get Cookie Total numbers firstly
			ResultSet rs0 = stmt.executeQuery("SELECT count(*) FROM cookies");
			cookieTotalNo = rs0.getInt("count(*)");
			
			//Add column "request_type" in table "http_requests"
			String sql = "SELECT * FROM http_requests";
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			boolean has_request_type = false;
			for(int i=1; i<=rsmd.getColumnCount(); i++) {
				if(rsmd.getColumnName(i).equals("request_type"))
					has_request_type = true;
			}
			if(!has_request_type) {
				Statement stmt1 = conn.createStatement();
				stmt1.executeUpdate( "ALTER TABLE \"main\".\"http_requests\" ADD COLUMN \"request_type\" INTEGER DEFAULT 0" );
			}
			
			String psql = "UPDATE http_requests SET request_type = ? WHERE id= ? ";
			PreparedStatement pstmt = conn.prepareStatement(psql);
			
			//classify http type
			int web1st = 0, web3rd = 0, webSp3rd = 0;
			while (rs.next()) {
		         int id = rs.getInt("id");
		         String url = rs.getString("url");
		         String referrer = rs.getString("referrer");
		         int page_id = rs.getInt("page_id");
		         String urlDomain = url;
		         String referrerDomain = referrer;		         
		         
		         Pattern pattern = Pattern.compile("https*://(.*?)/");
		         Matcher matcher1 = pattern.matcher(url);
		         if (matcher1.find())
		        	 urlDomain = matcher1.group(1);
		         Matcher matcher2 = pattern.matcher(referrer);
		         if (matcher2.find())
		        	 referrerDomain = matcher2.group(1);
		         
		         if(InternetDomainName.isValid(referrerDomain)){
		        	 InternetDomainName idm = InternetDomainName.from(referrerDomain);
		        	 if(idm.isUnderPublicSuffix())
		        		 referrerDomain = idm.topPrivateDomain().toString();		        	 
		         }
		         if(InternetDomainName.isValid(urlDomain)){
		        	 InternetDomainName idm = InternetDomainName.from(urlDomain);
		        	 if(idm.isUnderPublicSuffix())
		        		 urlDomain = idm.topPrivateDomain().toString();
		         }
		         
		         //URL and Referrer (domain) Not Equal -> third party. include page_id=-1
		         if(!urlDomain.equals(referrerDomain) || page_id==-1){
		        	 web3rd++;
		        	 pstmt.setInt(1, 3);
		        	 pstmt.setInt(2, id);
		        	 pstmt.addBatch();
		        	 //pstmt.executeUpdate();
		        	 System.out.println("Request #"+id+" is third party request.");
		        	 hIds3rd.add(id);
		        	 continue;
		         }
		         
		         //Judge href address ?= Referrer
		         int p_id=-1;
		         String address = null;
		         while(true){
		        	 sql = "SELECT id,location,parent_id FROM pages WHERE id=" +page_id;
		        	 Statement stmt1 = conn.createStatement();
		        	 ResultSet rs1 = stmt1.executeQuery(sql);
		        	 if(rs1.next()){
		        		 page_id = rs1.getInt("id");
		        		 address = rs1.getString("location");
		        		 p_id = rs1.getInt("parent_id");
		        	 }else{
		        		 break;
		        	 }
		        	 if(page_id == p_id){
		        		 break;
		        	 }else{
		        		 page_id = p_id;
		        	 }
		        	 stmt1.close();
		         }
		         if(address==null || address.equals("")){
		        	 continue;
		         }
		         
		         //get address's top-level-domain which shown in browser
		         String addressDomain = address;
		         Matcher matcher3 = pattern.matcher(address);
		         if (matcher3.find())
		        	 addressDomain = matcher3.group(1);
		         if(InternetDomainName.isValid(addressDomain)){
		        	 InternetDomainName idm = InternetDomainName.from(addressDomain);
		        	 if(idm.isUnderPublicSuffix())
		        		 addressDomain = idm.topPrivateDomain().toString();		        	 
		         }
		         
		         if(addressDomain.equals(referrerDomain)){
		        	 web1st++;
		        	 pstmt.setInt(1, 1);
		        	 pstmt.setInt(2, id);
		        	 pstmt.addBatch();
		        	 //pstmt.executeUpdate();
		        	 System.out.println("Request #"+id+" is first party request.");
		         }else{
		        	 webSp3rd++;
		        	 pstmt.setInt(1, 4);
		        	 pstmt.setInt(2, id);
		        	 pstmt.addBatch();
		        	 //pstmt.executeUpdate();
		        	 System.out.println("Request #"+id+" is special third party request.");
		        	 hIds3rd.add(id);
		         }
			}
			
			//pstmt.executeBatch(); //<-- Do not write to Database when Demo
			pstmt.close();
			stmt.close();
			conn.close();
			System.out.println("Total:\nThere are "+web1st+" first party requests, "+web3rd+" third party requests and "
			+webSp3rd+" special third party requests.");
			
			//for GUI
			String c = countryType.equals("China")?"国内":"国外";
			text.append(c+"网站数据（HTTP请求）分析结果汇总：\n");
			text.append("一共收集到"+(web1st+web3rd+webSp3rd)+"个HTTP请求，\n其中有"+web1st+"个第一方HTTP请求，\n"
			+web3rd+"个第三方HTTP请求，\n"+webSp3rd+"个特殊的第三方HTTP请求。\n\n");
			text.append("正在分析Cookie，请稍候...\n");
			text.paintImmediately(text.getBounds());
			
			ArrayList<Integer> thirdCookies = classifyCookies(hIds3rd);
			text.append(c+"网站数据（Cookie）分析结果汇总：\n");
			text.append("一共收集到"+cookieTotalNo+"个Cookie，\n其中有"+thirdCookies.size()+"被发往第三方网站。");
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<Integer> classifyCookies(ArrayList<Integer> requestIds){
		ArrayList<Integer> thirdCookies = new ArrayList<Integer>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		int httpReq3rdWithCk = 0;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:result/fourthparty"+countryType+".sqlite");
			String psql = "SELECT value FROM http_request_headers WHERE name ='Cookie' and http_request_id= ? ";
			pstmt = conn.prepareStatement(psql);
			int tmp = 1;
			for(int requestId : requestIds){
				System.out.println("now search "+(tmp++)+"/"+requestIds.size());
				pstmt.setInt(1, requestId);
				ResultSet rs = pstmt.executeQuery();
				String cks = null;
				if(rs.next()){
					cks = rs.getString("value");
					httpReq3rdWithCk ++;
				}else{
					continue;
				}
				String pSql = "SELECT id FROM cookies WHERE is_session=0 and name= ? and value= ? ";
				PreparedStatement pstmt1 = conn.prepareStatement(pSql);
				String[] cksArr = cks.split(";");
				for(String ck : cksArr){
					ck = ck.trim();
					String[] ckNV = ck.split("=", 2);
					String ckName = ckNV[0];
					String ckValue = ckNV[1];							
					pstmt1.setString(1, ckName);
					pstmt1.setString(2, ckValue);
					ResultSet rs1 = pstmt1.executeQuery();
					while (rs1.next()){
						int cookieId = rs1.getInt("id");
						if ( !thirdCookies.contains(cookieId)) {
							thirdCookies.add(cookieId);
						}
					}
				}
			}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(httpReq3rdWithCk);
		return thirdCookies;
	}
	
}
