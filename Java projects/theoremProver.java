/*Evaristo Koyama
 * ek4ks
 * 7/19/15
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class theoremProver {

	public static Scanner sc = new Scanner(System.in);
	public static HashMap<String, String> varToString = new HashMap<String, String>();
	public static LinkedHashMap<String, Boolean> root = new LinkedHashMap<String, Boolean>(16, 0.75f, false);
	public static LinkedList<String> rules = new LinkedList<String>();
	public static LinkedList<String> facts = new LinkedList<String>();
	public static HashSet<String> printed = new HashSet<String>();

	public static void main(String[] args) {
		String command = "";
		while (sc.hasNext()) {
			command = sc.next().trim();
			if (command.equals("Terminate")) {
				break;
			}
			else if (command.equals("Teach")) {
				String str = sc.next().trim();
				if (str.equals("-R") || str.equals("-L")) {
					teachVariable(str);
				}	
				else {
					String operator = sc.next().trim();
					if (operator.equals("=")) {
						teachRoot(str);
					}
					if (operator.equals("->")){
						teachRules(str);
					}
				}
			}
			else if (command.equals("List")) {
				print();
			}

			else if (command.equals("Learn")) {
				learn();
			}
			
			else if (command.equals("Query")) {
				String exp = sc.nextLine().trim();
				query(exp);
			}
			else if (command.equals("Why")) {
				String exp = sc.nextLine().trim();
				why(exp);
			}

			else {
				sc.nextLine();
			}
		}
		sc.close();
	}
	public static void teachVariable(String flag) {
		String var = sc.next().trim();
		if (varToString.containsKey(var)) {
			System.out.println("Variable name already in use");
			sc.nextLine();
			return;
		}
		sc.next();
		String statement = sc.nextLine().trim();					
		varToString.put(var, statement);
		if (flag.equals("-R")) {
			root.put(var, true);
		}
		else {
			root.put(var, false);
		}
	}

	public static void teachRoot(String var) {
		if (!root.containsKey(var) || !root.get(var)) {
			System.out.println(var + " is not a root variable");
			sc.nextLine();
			return;
		}
		boolean b = sc.nextBoolean();
		if (b && !facts.contains(var)) {
			facts.add(var);
		}
		if (!b && facts.contains(var)) {
			facts.remove(var);
		}
		for (String s : root.keySet()) {
			if (!root.get(s) && facts.contains(s)) {
				facts.remove(s);
			}
		}
	}

	public static void teachRules(String exp) {
		String var = sc.nextLine().trim();
		if (!root.containsKey(var) || root.get(var)) {
			System.out.println(var + " is not a learned variable");
			return;
		}
		exp = exp + " -> " + var;
		rules.add(exp);
	}

	public static void print() {
		System.out.println("Root Variables:");
		for (String var : root.keySet()) {
			if (root.get(var)) {
				System.out.println("\t" + var + " = " + varToString.get(var));
			}
		}
		System.out.println("Learned Variables:");
		for (String var : root.keySet()) {
			if (!root.get(var)) {
				System.out.println("\t" + var + " = " + varToString.get(var));
			}
		}
		System.out.println("Facts:");
		for (String f : facts) {
			System.out.println("\t" + f);
		}

		System.out.println("Rules:");
		for (String r : rules) {
			System.out.println("\t" + r);
		}
	}

	public static void learn() {
		boolean updated = false;
		for (String exp : rules) {
			String learned = exp.substring(exp.indexOf("->")+2).trim();
			String expression = exp.substring(0, exp.indexOf("->")).trim();
			TreeNode root = new TreeNode(expression);
			makeTree(root);
			boolean result = calculate(root);
			if (result && !facts.contains(learned)) {
				updated = true;
				facts.add(learned);
				break;
			}
			if (!result && facts.contains(learned)) {
				updated = true;
				facts.remove(learned);
				break;
			}
		}
		if (updated) {
			learn();
		}
	}
	
	public static void query(String exp) {
		TreeNode tn = new TreeNode(exp);
		makeTree(tn);
		System.out.println(backtrack(tn));
	}
	
	public static void why(String exp) {
		query(exp);
		TreeNode tn = new TreeNode(exp);
		makeTree(tn);
		printed = new HashSet<String>();
		explain(tn);
	}
	
	public static void makeTree(TreeNode tn) {
		ArrayList<String> tokens = tokenize(tn.token);
		boolean not = false;
		int index = -1;
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("!")) {
				not = true;
				index = i;
			}
		}
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("&")) {
				not = false;
				index = i;
			}
		}
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("|")) {
				not = false;
				index = i;
			}
		}
		if (index == -1 && !tn.token.contains("(")) {
			return;
		}
		else if (index == -1 && tn.token.contains("(")) {
			tn.token = tn.token.substring(1, tn.token.length()-1);
			makeTree(tn);
		}
		else if (not) {
			String child = tn.token.substring(1);
			tn.token = "!";
			tn.right = new TreeNode(child);
			makeTree(tn.right);
		}
		else {
			String l = "";
			String r = "";
			for (int i = 0; i < index; i++) {			
				l = l + tokens.get(i);
			}
			for (int i = index+1; i < tokens.size(); i++) {
				r = r + tokens.get(i);
			}
			tn.token = tokens.get(index);
			tn.left = new TreeNode(l);
			tn.right = new TreeNode(r);
			makeTree(tn.left);
			makeTree(tn.right);
		}
	}
	
	public static ArrayList<String> tokenize(String exp) {
		ArrayList<String> tokens = new ArrayList<String>();
		String s = "";
		int paren = 0;
		for (int i = 0; i < exp.length(); i++) {
			if (exp.charAt(i) == '(') {
				paren++;
				if (paren == 1) {
					s = s + "(";
					continue;	
				}
			}
			if (exp.charAt(i) == ')') {
				paren--;
				if (paren == 0) {
					s = s + ")";
					continue;
				}
			}
			if (paren == 0 && exp.charAt(i) == '!') {
				tokens.add("!");
			}
			else if (paren == 0 && exp.charAt(i) == '&') {
				if (s.length() > 0) {
					tokens.add(s);
					s = "";
				}
				tokens.add("&");
			}
			else if (paren == 0 && exp.charAt(i) == '|') {
				if (s.length() > 0) {
					tokens.add(s);
					s = "";
				}
				tokens.add("|");
			}
			else {
				s = s + exp.substring(i, i+1);
			}
		}
		if (s.length() > 0) {
			tokens.add(s);
		}
		if (tokens.size() == 1 && tokens.get(0).charAt(0) == '(') {
			tokens = tokenize(exp.substring(1, exp.length()-1));
		}
		return tokens;
	}
	
	public static boolean calculate(TreeNode tn) {
		if (tn.token.equals("!")) {
			return !calculate(tn.right);
		}
		else if (tn.token.equals("|")) {
			return calculate(tn.left) || calculate(tn.right);
		}
		else if (tn.token.equals("&")) {
			return calculate(tn.left) && calculate(tn.right);
		}
		else {
			return facts.contains(tn.token);
		}
	}
	
	public static boolean backtrack(TreeNode tn) {
		if (tn.token.equals("!")) {
			return !backtrack(tn.right);
		}
		else if (tn.token.equals("|")) {
			return backtrack(tn.left) || backtrack(tn.right);
		}
		else if (tn.token.equals("&")) {
			return backtrack(tn.left) && backtrack(tn.right);
		}
		else {
			if(facts.contains(tn.token)) {
				return true;
			}
			else if (root.get(tn.token)) {
				return false;
			}	
			else {
				boolean result = false;
				for (String exp : rules) {
					String learned = exp.substring(exp.indexOf("->")+2).trim();
					if (learned.equals(tn.token)) {
						String expression = exp.substring(0, exp.indexOf("->")).trim();
						TreeNode root = new TreeNode(expression);
						makeTree(root);
						result = backtrack(root);
					}
					if (result) {
						break;
					}
				}
				return result;
			}
		}
	}
	public static boolean explain(TreeNode tn) {
		if (tn.token.equals("!")) {
			boolean result = !explain(tn.right);
			if (result) {
				System.out.print("I THUS KNOW THAT ");
				ArrayList<String> a = tokenize(tn.right.toString());
				for (String str : a) {
					printToken(str);
				}
				printed.add(tn.right.toString());
				System.out.println("IS FALSE");
			}
			else {
				System.out.print("THUS I CANNOT PROVE ");
				ArrayList<String> a = tokenize(tn.right.toString());
				for (String str : a) {
					printToken(str);
				}
				System.out.println();
			}
			return result;
		}
		else if (tn.token.equals("|")) {
			boolean result = explain(tn.left) || explain(tn.right);
			if (result) {
				System.out.print("I THUS KNOW THAT ");
				ArrayList<String> a = tokenize(tn.left.toString());
				for (String str : a) {
					printToken(str);
				}
				System.out.print("OR ");
				a = tokenize(tn.right.toString());
				for (String str : a) {
					printToken(str);
				}
				System.out.println();
			}
			else {
				System.out.print("THUS I CANNOT PROVE ");
				ArrayList<String> a = tokenize(tn.left.toString());
				for (String str : a) {
					printToken(str);
				}
				System.out.print("OR ");
				a = tokenize(tn.right.toString());
				for (String str : a) {
					printToken(str);
				}
				System.out.println();
			}
			return result;
		}
		else if (tn.token.equals("&")) {
			boolean result = explain(tn.left) && explain(tn.right);
			if (result) {
				System.out.print("I THUS KNOW THAT ");
				ArrayList<String> a = tokenize(tn.left.toString());
				for (String str : a) {
					printToken(str);
				}
				System.out.print("AND ");
				a = tokenize(tn.right.token);
				for (String str : a) {
					printToken(str);
				}
				System.out.println();
			}
			else {
				System.out.print("THUS I CANNOT PROVE ");
				ArrayList<String> a = tokenize(tn.left.toString());
				for (String str : a) {
					printToken(str);
				}
				System.out.print(" AND ");
				a = tokenize(tn.right.toString());
				for (String str : a) {
					printToken(str);
				}
				System.out.println();
			}
			return result;
		}
		else {
			if (facts.contains(tn.token)) {
				if (!printed.contains(tn.token)) {
					printed.add(tn.token);
					System.out.println("I KNOW THAT " + varToString.get(tn.token));
				}
				return true;
			}
			else if (root.get(tn.token)) {
				if (!printed.contains(tn.token)) {
					printed.add(tn.token);
					System.out.println("I KNOW IT IS NOT TRUE THAT " + varToString.get(tn.token));
				}
				return false;
			}	
			else {
				boolean result = false;
				String s = "";
				ArrayList<String> f = new ArrayList<String>();
				for (String exp : rules) {
					String learned = exp.substring(exp.indexOf("->")+2).trim();
					String expression = "";
					if (learned.equals(tn.token)) {
						expression = exp.substring(0, exp.indexOf("->")).trim();
						TreeNode root = new TreeNode(expression);
						makeTree(root);
						result = explain(root);
					}
					if (result) {
						s = expression;
						break;
					}
					else {
						f.add(expression);
					}
				}
				if (result) {
					System.out.print("BECAUSE ");
					ArrayList<String> a = tokenize(s);					
					for (String str : a) {
						printToken(str);
					}
					System.out.print("I KNOW THAT ");
					System.out.println(varToString.get(tn.token));
				}
				else {
					for (String str1 : f) {
						System.out.print("BECAUSE IT IS NOT TRUE THAT ");
						ArrayList<String> a = tokenize(str1);					
						for (String str2 : a) {
							printToken(str2);
						}
						System.out.print("I CANNOT PROVE ");
						System.out.println(varToString.get(tn.token));
					}
				}
				return result;
			}
		}
	}
	public static void printToken(String str) {
		String temp = "";
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '(') {
				System.out.print('(');
			}
			else if (str.charAt(i) == ')') {
				if (temp.length() > 0) {
					System.out.print(varToString.get(temp));
					temp = "";
				}
				System.out.print(") ");
			}
			else if (str.charAt(i) == '!') {
				System.out.print("NOT ");
			}
			else if (str.charAt(i) == '&') {
				if (temp.length() > 0) {
					System.out.print(varToString.get(temp) + " ");
					temp = "";
				}
				System.out.print("AND ");
			}
			else if (str.charAt(i) == '|') {
				if (temp.length() > 0) {
					System.out.print(varToString.get(temp) + " ");
					temp = "";
				}
				System.out.print("OR ");
			}
			else {
				temp = temp + str.substring(i, i+1);
			}
		}
		if (temp.length() > 0) {
			System.out.print(varToString.get(temp) + " ");
		}
	}	
}
class TreeNode {
	public String token;
	public TreeNode left, right;

	public TreeNode(String s) {
		token = s;
		left = null;
		right = null;
	}
	public void inorder() {
		if (left != null && right != null) {
			System.out.print("(");
		}
		if (left != null) {
			left.inorder();
		}
		System.out.print(token);
		if (right != null) {
			right.inorder();
		}
		if (left != null && right != null) {
			System.out.print(")");
		}
	}
	
	public String toString() {
		String ret = "";
		if (left != null && right != null) {
			ret = ret + "(";
		}
		if (left != null) {
			ret = ret + left.toString();
		}
		ret = ret + token;
		if (right != null) {
			ret = ret + right.toString();
		}
		if (left != null && right != null) {
			ret = ret + ")";
		}
		return ret;
	}
	
}

