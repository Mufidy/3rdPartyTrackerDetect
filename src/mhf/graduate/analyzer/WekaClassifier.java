/**
 * Generate a classifier using WEKA machine learning suite.
 * Using Support Vector Machine(SVM) to classify cookies.
 */
package mhf.graduate.analyzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.functions.LibSVM;


/**
 * @author Haifei
 *
 */
public class WekaClassifier {
	private String countryType;
	
	public WekaClassifier(String countryType){
		this.countryType = countryType;
	}
	
	private LibSVM generateClassifier(){
		LibSVM svm = null;
		try {
			Instances trainData = DataSource.read("origin/trainDataset.arff");
			
			//set the class be attribute 'isTracker'
			trainData.setClassIndex(trainData.numAttributes() - 1);
			
	        //initialize SVM classifier
	        svm = new LibSVM();
	        svm.buildClassifier(trainData);
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return svm;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> classify() {
		// return an ArrayList of http request ids which sent to trackers
		if(countryType == null || (!countryType.equals("China") && !countryType.equals("Global"))){
			System.out.println("countryType error...\nexit unexceptedly...");
			return null;
		}
		
		ArrayList<Integer> requestsToTrackers = new ArrayList<Integer>();
		
		//deserialize requestsArray from disk to detect which http request is tracker
		ArrayList<Integer> requests = null;
		try {
			String seriFile = "result/requestsArray"+countryType+".serialize";
			FileInputStream fileIn = new FileInputStream(seriFile);
			ObjectInputStream in = new ObjectInputStream(fileIn);
	        requests = (ArrayList<Integer>) in.readObject();
	        in.close();
	        fileIn.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int tracker = 0;
		LibSVM svm = generateClassifier();
		try {
			String fileStr = "result/dataset"+countryType+".arff";
			Instances data = DataSource.read(fileStr);
			data.setClassIndex(data.numAttributes() - 1);
			
	        for(int i=0; i<data.numInstances(); i++){
	        	
	        	Instance instance = data.instance(i);
	        	double result = svm.classifyInstance(instance);
	        	
	        	int requestId = requests.get(i);
	        	
	        	if(result == 1.0){
	        	}else{
	        		tracker++;
	        		//System.out.println(requestId+"is trakcer");
	        		requestsToTrackers.add(requestId);
	        	}
	        }
	        System.out.println(countryType+":Total trackers: "+tracker);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return requestsToTrackers;
	}
}
