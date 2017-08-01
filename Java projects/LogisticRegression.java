//Evaristo Koyama
//ek4ks

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class LogisticRegression extends Classifier {
	
	private ArrayList<String> classes;
	private HashMap<Integer, String> numeric;
	private ArrayList<Double> theta;
	private HashMap<String, Integer> tIndex;
	private final double learningRate = 2;
	private final int iterations = 1500;
	
	public LogisticRegression(String namesFilepath) {
		super(namesFilepath);
		Scanner sc = null;
		try {
			sc = new Scanner(new File(namesFilepath));
			
		} catch (FileNotFoundException e) {
			System.out.print("File not found");
			System.exit(1);
		}
		classes = new ArrayList<String>();
		if (sc.hasNextLine()) {
			String line = sc.nextLine().trim();
			String[] temp = line.split("  ");
			for (String str : temp) {
				classes.add(str);
			}
			sc.nextLine();
		}
		numeric = new HashMap<Integer, String>();
		tIndex = new HashMap<String, Integer>();
		theta = new ArrayList<Double>();
		
		theta.add(1.0);
		int index = 0;
		int ti = 1;
		
		while(sc.hasNextLine()) {
			String line = sc.nextLine().trim();
			String[] temp = line.split("\t");
			if (temp[1].equals("numeric")) {
				numeric.put(index, temp[0]);
				theta.add(0.0);
				tIndex.put(temp[0], ti);
				ti++;
			}
			else {
				String[] temp2 = temp[1].split("  ");
				for (int i = 0; i < temp2.length; i++) {
					if(!temp2[i].isEmpty()) {
						theta.add(0.0);
						tIndex.put(temp2[i], ti);
						ti++;
					}
				}
			}
			index++;
		}
		sc.close();
	}

	@Override
	public void train(String trainingDataFilpath) {
		Scanner sc = null;
		try {
			sc = new Scanner(new File(trainingDataFilpath));
			
		} catch (FileNotFoundException e) {
			System.out.print("File not found");
			System.exit(1);
		}
		ArrayList<String> lines = new ArrayList<String>();
		while (sc.hasNextLine()) {
			String line = sc.nextLine().trim();
			if (!line.isEmpty()){
				lines.add(line);
			}
		}
		for (int i = 0; i < iterations; i++) {
			ArrayList<Double> gradDescent = getGradDescent(lines);
			for (int j = 0; j < theta.size(); j++) {
				double t = theta.get(j);
				t = t - learningRate*gradDescent.get(j);
				theta.set(j, t);
			}
		}
		sc.close();
	}
	private ArrayList<Double> getGradDescent(ArrayList<String> trainingSet) {
		ArrayList<Double> ret = new ArrayList<Double>(theta.size());
		for (int i = 0; i < theta.size(); i++) {
			ret.add(0.0);
		}
		for (String line : trainingSet) {
			ArrayList<Double> xVal = getXVal(line);
			double prediction = getPrediction(xVal);
			double value = 0.0;
			if (line.contains(">50K")) {
				value = 1.0;
			}
			double cost = prediction - value;
			for (int i = 0; i < theta.size(); i++) {
				double x = ret.get(i);
				x += cost*xVal.get(i);
				ret.set(i, x);
			}
		}
		for (int i = 0; i < theta.size(); i++) {
			double x = ret.get(i);
			x /= trainingSet.size();
			ret.set(i, x);
		}
		return ret;
	}
	
	private ArrayList<Double> getXVal(String line) {
		ArrayList<Double> xVal = new ArrayList<Double>(theta.size());
		for (int i = 0; i < theta.size(); i++) {
			xVal.add(0.0);
		}
		xVal.set(0, 1.0);
		String[] vals = line.split(" ");
		for (int i = 0; i < vals.length-1; i++) {
			if (numeric.containsKey(i)) {
				String feature = numeric.get(i);
				xVal.set(tIndex.get(feature), Double.parseDouble(vals[i]));					
			}
			else {
				if (tIndex.containsKey(vals[i])) {
					xVal.set(tIndex.get(vals[i]), 1.0);
				}
			}
		}
		return xVal;
	}
	
	private double getPrediction(ArrayList<Double> xVal) {
		double prediction = 0.0;
		for (int i = 0; i < theta.size(); i++) {
			prediction += (theta.get(i)*xVal.get(i));
		}
		return 1.0/(1.0 + Math.pow(Math.E, -prediction));
	}
	
	private double totalCost(ArrayList<String> trainingSet) {
		double total = 0.0;
		for (String line : trainingSet) {
			ArrayList<Double> xVal = getXVal(line);
			double prediction = getPrediction(xVal);
			double value = 0.0;
			if (line.contains(">50K")) {
				value = 1.0;
			}
			total += cost(prediction, value);
		}
		return total;
	}
	
	private double cost(double prediction, double value) {
		return -value*Math.log(prediction)-(1.0-value)*Math.log(1.0-prediction);
	}

	@Override
	public void makePredictions(String testDataFilepath) {
		Scanner sc = null;
		try {
			sc = new Scanner(new File(testDataFilepath));
			
		} catch (FileNotFoundException e) {
			System.out.print("File not found");
			System.exit(1);
		}
		while (sc.hasNextLine()) {
			String line = sc.nextLine().trim();
			if (line.isEmpty()) continue;
			ArrayList<Double> xVal = getXVal(line);
			double prediction = getPrediction(xVal);
			if (prediction >= 0.5) {
				System.out.println(">50K" + ": " + line);				
			}
			else {
				System.out.println("<=50K" + ": " + line);
			}
		}
		sc.close();
	}

	public static void main(String[] args) {
		LogisticRegression lr = new LogisticRegression("src/census.names");
		lr.train("src/census.train");
		lr.makePredictions("src/census.train");
	}
}
