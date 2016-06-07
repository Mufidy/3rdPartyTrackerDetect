/*
 * Collect Http Requests by Firefox/Selenium with fourthparty installed.
 * Data will be stored in fourthparty.sqlite.
 */
package mhf.graduate.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 * @author Haifei
 *
 */
public class Crawler {
	
	private WebDriver driver;
	private String countryType;
	ArrayList<String> websites = new ArrayList<String>();
	
	public Crawler(){		
	}
	
	public Crawler(String countryType){
		this.countryType = countryType;
	}
	
	private void clearProf(){
		/*
		The temporary profile Selenium/Firefox creates should be located at java.io.tmpdir.On Windows it is %TEMP%
		Ensure that there is a file in the profile directory which name is the same as the name of the profile.
		*/
		File sysTemp = new File(System.getProperty("java.io.tmpdir"));
		for (File t : sysTemp.listFiles()) {
			if (!t.isDirectory()) { 
				continue; 
			}
			
			try {
				if ((t.toString().contains("anonymous") && t.toString().contains("webdriver-profile"))) {
					FileUtils.forceDelete(t);
					System.out.println("Remove file " + t.toString() +" successfully.");
				}
			}
			catch(Exception e) {
				System.out.println("Failed to remove file " + t.toString());
			}
		}
	}
	
	private String getDriverProfile() {
		/*
		The temporary profile Selenium/Firefox creates should be located at java.io.tmpdir. On Windows it is %TEMP%
		Search is based on the presence of the file which name is the same as the name of the profile.
		*/
		File sysTemp = new File(System.getProperty("java.io.tmpdir"));		
		File pwd = null;
		for (File t : sysTemp.listFiles()) {
			if (!t.isDirectory()) { 
				continue;
			}			
			if ((t.toString().contains("anonymous") && t.toString().contains("webdriver-profile"))) {
				pwd = t;
				break;
			}
		}			
		return pwd.toString();
	}
	
	private String getResultDir() {
		File file =new File("result");    
		//如果文件夹不存在则创建    
		if  (!file .exists()  && !file .isDirectory()) {       
		    file .mkdir();    
		}
		return file.toString();
	}
	
	private ArrayList<String> getWebsite(){
		String fileName = "origin/websites"+countryType+".list";
		BufferedReader in = null;
	    String line = null;
		try {
			in = new BufferedReader(new FileReader(fileName));
			line = in.readLine();
			while (line != null) {
				String website = "http://"+line;
		    	websites.add(website);
		    	line = in.readLine();			
		    }
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return websites;
	}

    public void runCrawler(JTextArea text){
//        System.out.println("Clear firefox/selenium's driver profile...");
    	text.setText("日志显示区：\n");
    	//Insert log string to GUI(JTextArea)
    	text.insert("Clear firefox/selenium's driver profile...\n",7);
		text.paintImmediately(text.getBounds());
		
        clearProf();
//        System.out.println("Start Firefox with fourthparty...");
    	//Insert log string to GUI(JTextArea)
    	text.insert("Start Firefox with fourthparty...\n",7);
		text.paintImmediately(text.getBounds());
		
        File file = new File("origin/fourthparty.xpi");
        FirefoxProfile profile = new FirefoxProfile();
        try {
            profile.addExtension(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        profile.setPreference("extensions.fourthparty.currentVersion", "0.0.1");
        //active fourth_party extensions
        profile.setPreference("extensions.fourthparty.allPagesActivation", "on"); 
        
        driver = new FirefoxDriver(profile);
        String profileDir = getDriverProfile();
        String resultDir = getResultDir();
        websites = getWebsite();
        for(String website : websites){
        	driver.get(website);
//        	System.out.println("Visiting "+driver.getTitle());
        	//Insert log string to GUI(JTextArea)
        	text.insert("Visiting Website: "+driver.getTitle()+"\n",7);
    		text.paintImmediately(text.getBounds());
        }
        // copy the fourthparty database out.
		
		try {
			File source = new File(profileDir + "/fourthparty.sqlite");
			File target = new File(resultDir + "/fourthparty" + countryType + ".sqlite");
			FileUtils.copyFile(source, target);
//			System.out.println("Copy from "+source.toString()+" to "+target.toString()+" successfully.");
			//Insert log string to GUI(JTextArea)
        	text.insert("Copy from "+source.toString()+" to "+target.toString()+" successfully.\n",7);
    		text.paintImmediately(text.getBounds());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		driver.quit();
//        System.out.println("Collect websites' HTTP requests and cookies successfully.");
		//Insert log string to GUI(JTextArea)
    	text.insert("Collect "+ countryType+" websites' HTTP requests and cookies successfully.\n",7);
		text.paintImmediately(text.getBounds());
		text.setCaretPosition(0);
    }

}