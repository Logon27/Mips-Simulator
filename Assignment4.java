import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/******************************************************
* *
* Name: Logan Zehm *
* Class: CS 3421 *
* Assignment: Assignment 2 (an assembler of a subset of the *
* MIPS assembly language) *
* Compile: "javac Assignment2.java" *
* Run: "java Assignment2 < filename.asm > filename.obj" *
* *
******************************************************/

public class Assignment2 {
	
	//used static maps just because the program is only run one file at a time.
	static HashMap<String, Integer> opcodes = new HashMap<>();
	static HashMap<String, Integer> func = new HashMap<>();
	static HashMap<String, Integer> registers = new HashMap<>();
	static HashMap<String, Integer> labels = new HashMap<>();
	static HashMap<String, Integer> words = new HashMap<>();
	static HashMap<String, Character> format = new HashMap<>();
	static ArrayList<String> instructions = new ArrayList<>();
	static ArrayList<String> instructionsWithoutLabel = new ArrayList<>();
	
	static ArrayList<String> outputInstructions = new ArrayList<String>();
	static ArrayList<String> output = new ArrayList<String>();
	
	//initialize all our constant decimal values for encoding.
	public static void initializeHashMaps() {
		opcodes.put("addiu", 9);
		opcodes.put("beq", 4);
		opcodes.put("bne", 5);
		opcodes.put("lw", 35);
		opcodes.put("sw",43);
		opcodes.put("j", 2);
		func.put("addu", 33);
		func.put("and", 36);
		func.put("div", 26);
		func.put("mfhi", 16);
		func.put("mflo", 18);
		func.put("mult", 24);
		func.put("or", 37);
		func.put("slt", 42);
		func.put("subu", 35);
		func.put("syscall", 12);
		
		format.put("addiu", 'I');
		format.put("addu", 'R');
		format.put("and", 'R');
		format.put("beq", 'I');
		format.put("bne", 'I');
		format.put("div", 'R');
		format.put("j", 'J');
		format.put("lw", 'I');
		format.put("mfhi", 'R');
		format.put("mflo", 'R');
		format.put("mult", 'R');
		format.put("or", 'R');
		format.put("slt", 'R');
		format.put("subu", 'R');
		format.put("sw", 'I');
		format.put("syscall", 'R');
		
		registers.put("$zero", 0);
		registers.put("$at", 1);
		registers.put("$v0", 2);
		registers.put("$v1", 3);
		registers.put("$a0", 4);
		registers.put("$a1", 5);
		registers.put("$a2", 6);
		registers.put("$a3", 7);
		registers.put("$t0", 8);
		registers.put("$t1", 9);
		registers.put("$t2", 10);
		registers.put("$t3", 11);
		registers.put("$t4", 12);
		registers.put("$t5", 13);
		registers.put("$t6", 14);
		registers.put("$t7", 15);
		registers.put("$s0", 16);
		registers.put("$s1", 17);
		registers.put("$s2", 18);
		registers.put("$s3", 19);
		registers.put("$s4", 20);
		registers.put("$s5", 21);
		registers.put("$s6", 22);
		registers.put("$s7", 23);
		registers.put("$t8", 24);
		registers.put("$t9", 25);
		registers.put("$k0", 26);
		registers.put("$k1", 27);
		registers.put("$gp", 28);
		registers.put("$sp", 29);
		registers.put("$fp", 30);
		registers.put("$ra", 31);
	}
	
    public static void main(String[] args) throws IOException { 
    	initializeHashMaps();
    	
    	Scanner s = new Scanner(System.in);
    	String line;
    	int lineNumber = 0;
    	while(s.hasNextLine() == true) {
    		//replace all the tabs, spaces, and other whitespace characters with a single space for easier formatting
    		line = s.nextLine();
    		line = line.replaceAll("\\s+"," ");
    		//remove all the comments from the assembly file
    		if(line.contains("#")) {
    			line = line.substring(0, line.indexOf("#"));
    		}
    		line = line.trim();
    		//add all our instructions to arraylists/hashmaps and store the labels
    		instructions.add(line);
    		instructionsWithoutLabel.add(removeLabel(line));
    		storeLabel(line, lineNumber);
    		lineNumber++;
    	}
    	s.close();
    	
    	//debug test prints
		//printArrayList(instructions);
		//System.out.println("-------------------");
		//printArrayList(instructionsWithoutLabel);
		//System.out.println("-------------------");
		
		//read instructions as long as you have not hit the data segement
		boolean dataSegment = false;
		
		//start index at i = 1 to skip .text
		for(int i = 1; i < instructionsWithoutLabel.size(); i++) {
			String[] arrOfStr = instructionsWithoutLabel.get(i).split(" ", 2);
			if(dataSegment == false) {
				switch(arrOfStr[0].trim()) {
					case ".data":
						dataSegment = true;
						break;
		            case "addiu":
		                //System.out.println("addiu");
		                if(arrOfStr.length > 1) {
		                String[] arrOfArgs = arrOfStr[1].split(",", 3);
		                outputInstructions.add(toHexIFormat(9,registers.get(arrOfArgs[1]),
		                		registers.get(arrOfArgs[0]),replaceReference(arrOfArgs[2])));
		                }
		                //Integer.parseInt(arrOfArgs[2])
		                break;
		            case "addu":
		                //System.out.println("addu");
		                if(arrOfStr.length > 1) {
		                String[] arrOfArgs = arrOfStr[1].split(",", 3);
		                outputInstructions.add(toHexRFormat(0,registers.get(arrOfArgs[1]),
		                		registers.get(arrOfArgs[2]),registers.get(arrOfArgs[0]),0,func.get("addu")));
		                //System.out.println("args: " + arrOfStr[1].trim());
		                }
		                break;
		            case "and":
		                //System.out.println("and");
		                if(arrOfStr.length > 1) {
		                String[] arrOfArgs = arrOfStr[1].split(",", 3);
		                outputInstructions.add(toHexRFormat(0,registers.get(arrOfArgs[1]),
		                		registers.get(arrOfArgs[2]),registers.get(arrOfArgs[0]),0,func.get("and")));
		                }
		                break;
		            case "beq":
		                //System.out.println("beq");
		                if(arrOfStr.length > 1) {
		                String[] arrOfArgs = arrOfStr[1].split(",", 3);
		                outputInstructions.add(toHexIFormat(opcodes.get(arrOfStr[0]),registers.get(arrOfArgs[0]),
		                		registers.get(arrOfArgs[1]),labels.get(arrOfArgs[2]) - i));
		                }
		                break;
		            case "bne":
		                //System.out.println("bne");
		                if(arrOfStr.length > 1) {
		                String[] arrOfArgs = arrOfStr[1].split(",", 3);
		                //used to have a first arg of 5
		                outputInstructions.add(toHexIFormat(opcodes.get(arrOfStr[0]),registers.get(arrOfArgs[0]),
		                		registers.get(arrOfArgs[1]),labels.get(arrOfArgs[2]) - i));
		                }
		                break;
		            case "div":
		                //System.out.println("div");
		                if(arrOfStr.length > 1) {
		                String[] arrOfArgs = arrOfStr[1].split(",", 2);
		                outputInstructions.add(toHexRFormat(0,registers.get(arrOfArgs[0]),
		                		registers.get(arrOfArgs[1]),0,0,func.get("div")));
		                }
		                break;
		            case "j":
		                //System.out.println("j");
		            	// I guess I need to get the line number - 1 for the absolute location because of the .text line
		                if(arrOfStr.length > 1)
		                outputInstructions.add(toHexJFormat(2, labels.get(arrOfStr[1]) - 1));
		                break;
		            case "lw":
		                //System.out.println("lw");
		                if(arrOfStr.length > 1) {
		                String[] arrOfArgs = arrOfStr[1].split(",", 2);
		                outputInstructions.add(toHexIFormat(opcodes.get(arrOfStr[0]),
		                		registers.get(arrOfArgs[1].substring(arrOfArgs[1].indexOf('(') + 1, arrOfArgs[1].indexOf(')'))),
		                		registers.get(arrOfArgs[0]),
		                		replaceReference(arrOfArgs[1].substring(0,arrOfArgs[1].indexOf('(')))
		                		));
		                }
		                break;
		            case "mfhi":
		                //System.out.println("mfhi");
		                if(arrOfStr.length > 1) {
		                outputInstructions.add(toHexRFormat(0,0,
		                		0,registers.get(arrOfStr[1].trim()),0,func.get("mfhi")));
		                }
		                break;
		            case "mflo":
		                //System.out.println("mflo");
		                if(arrOfStr.length > 1) {
		                outputInstructions.add(toHexRFormat(0,0,
		                		0,registers.get(arrOfStr[1].trim()),0,func.get("mflo")));
		                }
		                break;
		            case "mult":
		                //System.out.println("mult");
		                if(arrOfStr.length > 1) {
		                String[] arrOfArgs = arrOfStr[1].split(",", 2);
		                outputInstructions.add(toHexRFormat(0,registers.get(arrOfArgs[0]),
		                		registers.get(arrOfArgs[1]),0,0,func.get("mult")));
		                }
		                break;
		            case "or":
		                //System.out.println("or");
		                if(arrOfStr.length > 1) {
		                String[] arrOfArgs = arrOfStr[1].split(",", 3);
		                outputInstructions.add(toHexRFormat(0,registers.get(arrOfArgs[1]),
		                		registers.get(arrOfArgs[2]),registers.get(arrOfArgs[0]),0,func.get("or")));
		                }
		                break;
		            case "slt":
		                //System.out.println("slt");
		                if(arrOfStr.length > 1) {
			            String[] arrOfArgs = arrOfStr[1].split(",", 3);
			            outputInstructions.add(toHexRFormat(0,registers.get(arrOfArgs[1]),
		                		registers.get(arrOfArgs[2]),registers.get(arrOfArgs[0]),0,func.get("slt")));
		                }
		                break;
		            case "sw":
		                //System.out.println("sw");
		                if(arrOfStr.length > 1) {
		                String[] arrOfArgs = arrOfStr[1].split(",", 2);	            
		                outputInstructions.add(toHexIFormat(opcodes.get(arrOfStr[0]),
		                		registers.get(arrOfArgs[1].substring(arrOfArgs[1].indexOf('(') + 1, arrOfArgs[1].indexOf(')'))),
		                		registers.get(arrOfArgs[0]),
		                		replaceReference(arrOfArgs[1].substring(0,arrOfArgs[1].indexOf('(')))
		                		));
		                }
		                break;
		            case "subu":
		                //System.out.println("subu");
		                if(arrOfStr.length > 1) {
		                String[] arrOfArgs = arrOfStr[1].split(",", 3);
		                outputInstructions.add(toHexRFormat(0,registers.get(arrOfArgs[1]),
		                		registers.get(arrOfArgs[2]),registers.get(arrOfArgs[0]),0,func.get("subu")));
		                }
		            case "syscall":
		                //System.out.println("syscall");
		            	outputInstructions.add(toHexSyscallFormat(12));
		                break;
		            default:
		            	outputInstructions.add("no match");
		        }
			}
		}
		
		//add the first line with the number of instructions followed by the number of words.
		output.add(outputInstructions.size() + " " + words.size());
		//input all the instructions to our output list
		for(String x : outputInstructions) {
			output.add(x);
		}
		//input all the data words to our output list
		for(int y : words.values()) {
			output.add(toWord(y));
		}
		//print our output list to standard output
		for(String z : output) {
			System.out.println(z);
		}
    }
    
    //simple function to remove a label from a given string / instruction
    public static String removeLabel(String s) {
    	//might need to increment the index by 1
    	if(s.contains(":")) {
        	return s.substring(s.indexOf(":") + 1).trim();	
    	} else {
    		return s;
    	}
    }
    
    //store a label to our hashmap with its line number/instruction number.
    //check for data segments and parse them to the words hashmap
    public static void storeLabel(String s, int lineNumber) {
    	if(s.contains(":")) {
        	labels.put(s.substring(0, s.indexOf(":")).trim(), lineNumber);
        	
        	//add to the words hashmap
        	if(s.contains(".word")) {
	        	String[] arrOfStr = s.split(" ", 3);
	        	words.put(s.substring(0, s.indexOf(":")).trim(), Integer.parseInt(arrOfStr[2].trim()));
        	}
        	//need to add extra logic for space to initialize n words to 0
        	if(s.contains(".space")) {
	        	String[] arrOfStr = s.split(" ", 3);
	        	for(int i = 0; i < Integer.parseInt(arrOfStr[2].trim()); i++) {
		        	words.put(s.substring(0, s.indexOf(":")).trim() + Integer.toString(i), 0);
	        	}
        	}
    	}
    }
    
    //simple function to print all elements in an arraylist
    public static void printArrayList(ArrayList<String> a) {
    	for(int i = 0; i < a.size(); i++) {
    		System.out.println(a.get(i));
    	}
    }
    
    //convert to hexidecimal r format
    public static String toHexRFormat(int opcode, int rs, int rt, int rd, int shift, int funct) {
    	String opcodeString = String.format("%6s", Integer.toBinaryString(opcode)).replace(' ', '0');
    	String rsString = String.format("%5s", Integer.toBinaryString(rs)).replace(' ', '0');
    	String rtString = String.format("%5s", Integer.toBinaryString(rt)).replace(' ', '0');
    	String rdString = String.format("%5s", Integer.toBinaryString(rd)).replace(' ', '0');
    	String shiftString = String.format("%5s", Integer.toBinaryString(shift)).replace(' ', '0');
    	String functString = String.format("%6s", Integer.toBinaryString(funct)).replace(' ', '0');
    	String result = opcodeString + rsString + rtString + rdString + shiftString + functString;
    	
		return String.format("%08x", Long.parseLong(result,2));
    }
    
    //convert to hexidecimal i format
    public static String toHexIFormat(int opcode, int rs, int rt, int imm) {
    	String opcodeString = String.format("%6s", Integer.toBinaryString(opcode)).replace(' ', '0');
    	String rsString = String.format("%5s", Integer.toBinaryString(rs)).replace(' ', '0');
    	String rtString = String.format("%5s", Integer.toBinaryString(rt)).replace(' ', '0');
    	String immString = String.format("%16s", Integer.toBinaryString(imm)).replace(' ', '0');
    	String result = opcodeString + rsString + rtString + immString;
    	
		return String.format("%08x", Long.parseLong(result,2));
    }
    
    //convert to hexidecimal j format
    public static String toHexJFormat(int opcode, int addr) {
    	String opcodeString = String.format("%6s", Integer.toBinaryString(opcode)).replace(' ', '0');
    	String addrString = String.format("%26s", Integer.toBinaryString(addr)).replace(' ', '0');
    	String result = opcodeString + addrString;
    	
		return String.format("%08x", Long.parseLong(result,2));
    }
    
    //take an input code and convert it to the Syscall encoded format. for some reason it is different from standard I format
    public static String toHexSyscallFormat(int code) {
    	String opcodeString = String.format("%6s", Integer.toBinaryString(0)).replace(' ', '0');
    	String addrString = String.format("%20s", Integer.toBinaryString(0)).replace(' ', '0');
    	String codeString = String.format("%6s", Integer.toBinaryString(code)).replace(' ', '0');
    	String result = opcodeString + addrString + codeString;
    	
		return String.format("%08x", Long.parseLong(result,2));
    }
    
    //searches the current data word set and looks for a key match to replace forward references
    public static int replaceReference(String ref) {
    	String temp = ref;
    	for (String key : words.keySet()) {
    	    if(temp.equals(key)) {
    	    	return words.get(key);
    	    }
    	}
		return Integer.parseInt(temp);
    }
    
    //converts a given integer into a hexidecimal string
    public static String toWord(int i) {
    	return String.format("%08x", i);
    }
}
