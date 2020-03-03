import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/******************************************************
* *
* Name: Logan Zehm *
* Class: CS 3421 *
* Assignment: Assignment 4 (a simulator of a subset of the *
* MIPS assembly language) *
* Compile: "javac Assignment4.java" *
* Run: "java Assignment4 filename.obj" *
* *
******************************************************/

public class Assignment4 {
	
	static HashMap<Integer, String> opcodes = new HashMap<>();
	static HashMap<Integer, String> func = new HashMap<>();
	static HashMap<Integer, String> registers = new HashMap<>();
	static HashMap<Integer, Integer> registerValues = new HashMap<>();
	
	//store the actual instructions read in and data memory.
	static ArrayList<String> instructions = new ArrayList<>();
	static ArrayList<Integer> dataMemory = new ArrayList<>();
	
	static int programCounter = 0;
	static int numberOfInstructions;
	static int numberOfDataSegments;
	static PrintStream stdout;

	//initialize all our constant decimal values for encoding.
	public static void initializeHashMaps() {
		opcodes.put(9, "addiu");
		opcodes.put(4, "beq");
		opcodes.put(5, "bne");
		opcodes.put(35, "lw");
		opcodes.put(43, "sw");
		opcodes.put(2, "j");
		func.put(33, "addu");
		func.put(36, "and");
		func.put(26, "div");
		func.put(16, "mfhi");
		func.put(18, "mflo");
		func.put(24, "mult");
		func.put(37, "or");
		func.put(42, "slt");
		func.put(35, "subu");
		func.put(12, "syscall");
		
		registers.put(0, "$zero");
		registers.put(1, "$at");
		registers.put(2, "$v0");
		registers.put(3, "$v1");
		registers.put(4, "$a0");
		registers.put(5, "$a1");
		registers.put(6, "$a2");
		registers.put(7, "$a3");
		registers.put(8, "$t0");
		registers.put(9, "$t1");
		registers.put(10, "$t2");
		registers.put(11, "$t3");
		registers.put(12, "$t4");
		registers.put(13, "$t5");
		registers.put(14, "$t6");
		registers.put(15, "$t7");
		registers.put(16, "$s0");
		registers.put(17, "$s1");
		registers.put(18, "$s2");
		registers.put(19, "$s3");
		registers.put(20, "$s4");
		registers.put(21, "$s5");
		registers.put(22, "$s6");
		registers.put(23, "$s7");
		registers.put(24, "$t8");
		registers.put(25, "$t9");
		registers.put(26, "$k0");
		registers.put(27, "$k1");
		registers.put(28, "$gp");
		registers.put(29, "$sp");
		registers.put(30, "$fp");
		registers.put(31, "$ra");
		registers.put(32, "$lo");
		registers.put(33, "$hi");
		
		//initialize all register values to 0.
		for(int i = 0; i < 34; i++) {
			registerValues.put(i, 0);
		}
	}
	
    public static void main(String[] args) throws IOException { 
    	initializeHashMaps();
    	
    	//makes sure a file is given as the first argument
    	if(args.length > 0) {
    		File file = new File(args[0]);
	    	Scanner s = new Scanner(file);
	    	
	    	//store the out stream so I can set the log to the default stream.
	    	stdout = System.out;
	    	
	    	//set the new log file to the default stream.
	    	PrintStream fileOut = new PrintStream("./log.txt");
	    	System.setOut(fileOut);
	    	
	    	String line;
	    	int lineNumber = 0;
	    	
	    	//grab the first line that contains the number of instructions and data segments
	    	line = s.nextLine();
	    	numberOfInstructions = Integer.parseInt(line.split("\\s+")[0]);
	    	numberOfDataSegments = Integer.parseInt(line.split("\\s+")[1]);
	    	registerValues.put(28, numberOfInstructions);
	    	
	    	while(s.hasNextLine() == true && lineNumber < (numberOfInstructions + numberOfDataSegments)) {
	    		//replace all the tabs, spaces, and other whitespace characters with a single space for easier formatting
	    		if(lineNumber < numberOfInstructions) {
		    		line = s.nextLine();
		    		line = line.trim();
		    		instructions.add(line);
		    		lineNumber++;	
	    		} else {
	    			//read in the data segment.
		    		line = s.nextLine();
		    		line = line.trim();
		    		dataMemory.add(Integer.parseInt(hexToBin(line),2));
		    		lineNumber++;	
	    		}
	    	}
	    	s.close();
	    	
	    	//loop through the initial instructions to print as a list
	    	printInsts();
	    	//actually simulate the program
	    	printCycle();
	    	
    	}
    }
    
    //simple function to print all elements in an arraylist
    public static void printArrayList(ArrayList<String> a) {
    	for(int i = 0; i < a.size(); i++) {
    		System.out.println(a.get(i));
    	}
    }
    
    //print all the current register values
    public static void printRegs() {
    	System.out.println();
    	System.out.println("regs:");
    	int counter = 0;
    	for(int i = 0; i < registerValues.size(); i++) {
    		if(counter == 3) {
				System.out.format("%8s" + " =" + "%6d%n",
						registers.get(i), registerValues.get(i));
				counter = 0;
    		} else {
				System.out.format("%8s" + " =" + "%6d",
						registers.get(i), registerValues.get(i));
				counter++;
    		}
    	}
    	//twice is needed because i used string format
    	System.out.println();
    	System.out.println();
    }
    
    //print all the current data memory values
    public static void printDataMemory() {
    	System.out.println("data memory:");
    	int counter = 0;
    	for(int i = 0; i < dataMemory.size(); i++) {
    		if(counter == 2 || i == (dataMemory.size() - 1)) {
    			System.out.format("   data[%3d] =%6d%n", i, dataMemory.get(i));
    			counter = 0;
    		} else {
    			System.out.format("   data[%3d] =%6d", i, dataMemory.get(i));
    			counter++;
    		}
    	}
    	System.out.println();
    	System.out.println();
    }
    
    //convert a hexidecimal string to a 32 bit binary string
    public static String hexToBin(String s) {
    	return String.format("%32s", new BigInteger(s, 16).toString(2)).replace(" ", "0");
    }
    
    //set data to the data memory at a specified index
    public static void setData(int index, int value) {
    	int pointer = index - numberOfInstructions;
    	if(pointer < 0 || pointer > (numberOfDataSegments - 1)) {
    		//throw exception if index is out of bounds
    		System.err.println("illegal data address: PC = " + programCounter + ", address = " + index);
    		System.exit(0);
    	} else {
    		dataMemory.set(pointer, value);
    	}
    }
    
    //get data from the data memory at a specified index
    public static int getData(int index) {
    	int pointer = index - numberOfInstructions;
    	if(pointer < 0 || pointer > (numberOfDataSegments - 1)) {
    		//throw exception if index is out of bounds
    		System.err.println("illegal data address: PC = " + programCounter + ", address = " + index);
    		System.exit(0);
    		return -1;
    	} else {
    		return dataMemory.get(pointer);
    	}
    }
    
    //used to check if the address is inside the text segment
    public static void checkAddressInsideTextSeg(int i) {
    	if(i >= numberOfInstructions) {
    		System.err.println("illegal instruction address: at address = " + i);
    		System.exit(0);
    	}
    }
    
    //print the instructions and initial data values
    public static void printInsts() {
    	System.out.println("insts:");
    	for(int i = 0; i < instructions.size(); i++) {
    		//conver the instruction to binary
    		String binary = hexToBin(instructions.get(i));
    		String opcodeBinary = binary.substring(0, 6);
    		int opcode = Integer.parseInt(opcodeBinary, 2);
    		//R-type instruction
			if(opcode == 0) {
				int funct = Integer.parseInt(binary.substring(26),2);
				int rs = Integer.parseInt(binary.substring(6,11),2);
				int rt = Integer.parseInt(binary.substring(11,16),2);
				int rd = Integer.parseInt(binary.substring(16,21),2);
				//shift should be 0 for R instruction
				int shift = Integer.parseInt(binary.substring(21,26),2);
				
				switch(funct) {
					//addu
					case 33:
						System.out.format("%4d" + ": " + "%s%c" + "%s," + "%s," + "%s%n",
								i, func.get(funct), '\t', registers.get(rd), registers.get(rs), registers.get(rt));
						break;
					//and
					case 36:
						System.out.format("%4d" + ": " + "%s%c" + "%s," + "%s," + "%s%n",
								i, func.get(funct), '\t', registers.get(rd), registers.get(rs), registers.get(rt));
						break;
					//div
					case 26:
						System.out.format("%4d" + ": " + "%s%c" + "%s," + "%s%n",
								i, func.get(funct), '\t', registers.get(rs), registers.get(rt));
						break;
					//mfhi
					case 16:
						System.out.format("%4d" + ": " + "%s%c" + "%s%n",
								i, func.get(funct), '\t', registers.get(rd));
						break;
					//mflo
					case 18:
						System.out.format("%4d" + ": " + "%s%c" + "%s%n",
								i, func.get(funct), '\t', registers.get(rd));
						break;
					//mult
					case 24:
						System.out.format("%4d" + ": " + "%s%c" + "%s," + "%s%n",
								i, func.get(funct), '\t', registers.get(rs), registers.get(rt));
						break;
					//or
					case 37:
						System.out.format("%4d" + ": " + "%s%c" + "%s," + "%s," + "%s%n",
								i, func.get(funct), '\t', registers.get(rd), registers.get(rs), registers.get(rt));
						break;
					//slt
					case 42:
						System.out.format("%4d" + ": " + "%s%c" + "%s," + "%s," + "%s%n",
								i, func.get(funct), '\t', registers.get(rd), registers.get(rs), registers.get(rt));
						break;
					//subu
					case 35:
						System.out.format("%4d" + ": " + "%s%c" + "%s," + "%s," + "%s%n",
								i, func.get(funct), '\t', registers.get(rd), registers.get(rs), registers.get(rt));
						break;
					//syscall
					case 12:
						System.out.format("%4d" + ": " + "%s%n",
								i, func.get(funct));
						break;
		            default:
		            	break;
				}
				
			} else {
			//I or J type instruction
				if(opcode != 2) {
					int rs = Integer.parseInt(binary.substring(6,11),2);
					int rt = Integer.parseInt(binary.substring(11,16),2);
					int imm = Integer.parseInt(binary.substring(16),2);
					switch(opcode) {
						//addiu
						case 9:
							System.out.format("%4d" + ": " + "%s%c" + "%s," + "%s," + "%d%n",
									i, opcodes.get(opcode), '\t', registers.get(rt), registers.get(rs), imm);
							break;
						//beq
						case 4:
							System.out.format("%4d" + ": " + "%s%c" + "%s," + "%s," + "%d%n",
									i, opcodes.get(opcode), '\t', registers.get(rs), registers.get(rt), imm);
							break;
						//bne
						case 5:
							System.out.format("%4d" + ": " + "%s%c" + "%s," + "%s," + "%d%n",
									i, opcodes.get(opcode), '\t', registers.get(rs), registers.get(rt), imm);
							break;
						//lw
						case 35:
							System.out.format("%4d" + ": " + "%s%c" + "%s," + "%d(" + "%s)%n",
									i, opcodes.get(opcode), '\t', registers.get(rt), imm, registers.get(rs));
							break;
						//sw
						case 43:
							System.out.format("%4d" + ": " + "%s%c" + "%s," + "%d(" + "%s)%n",
									i, opcodes.get(opcode), '\t', registers.get(rt), imm, registers.get(rs));
							break;
						default:
							break;
					}
				} else {
					//j
					int address = Integer.parseInt(binary.substring(6),2);
					System.out.format("%4d" + ": " + "%s%c" + "%d%n",
							i, opcodes.get(opcode), '\t', address);
				}
			}
    	}
    	
    	//print all the initial data.
    	System.out.println();
    	System.out.println("data:");
    	for(int i = 0; i < dataMemory.size(); i++) {
    		System.out.format("%4d: " + "%d%n", (instructions.size() + i), dataMemory.get(i));
    	}
    	System.out.println();
    }
    
    //loops through and prints all the values at each step of the program counter
    public static void printCycle() {
    	while(programCounter <= numberOfInstructions) {
    		System.out.println("PC: " + programCounter);
    		checkAddressInsideTextSeg(programCounter);
    		
    		//a boolean to check if a register modified the PC so I know not to increment.
    		boolean programCounterChanged = false;
    		
    		//convert instruction to binary
    		String binary = hexToBin(instructions.get(programCounter));
    		String opcodeBinary = binary.substring(0, 6);
    		int opcode = Integer.parseInt(opcodeBinary, 2);
    		//R-type instruction
			if(opcode == 0) {
				int funct = Integer.parseInt(binary.substring(26),2);
				int rs = Integer.parseInt(binary.substring(6,11),2);
				int rt = Integer.parseInt(binary.substring(11,16),2);
				int rd = Integer.parseInt(binary.substring(16,21),2);
				//shift should be 0 for R instruction
				int shift = Integer.parseInt(binary.substring(21,26),2);
				
				switch(funct) {
					//addu
					case 33:
						System.out.format("inst: " + "%s%c" + "%s," + "%s," + "%s%n",
								func.get(funct), '\t', registers.get(rd), registers.get(rs), registers.get(rt));
						registerValues.put(rd, registerValues.get(rs) + registerValues.get(rt));
						break;
					//and
					case 36:
						System.out.format("inst: " + "%s%c" + "%s," + "%s," + "%s%n",
								func.get(funct), '\t', registers.get(rd), registers.get(rs), registers.get(rt));
						registerValues.put(rd, registerValues.get(rs) & registerValues.get(rt));
						break;
					//div
					case 26:
						System.out.format("inst: " + "%s%c" + "%s," + "%s%n",
								func.get(funct), '\t', registers.get(rs), registers.get(rt));
						if(registerValues.get(rt) == 0) {
							System.err.println("Divide by zero error: PC = " + programCounter);
							System.exit(0);
						}
						registerValues.put(32, registerValues.get(rs) / registerValues.get(rt));
						registerValues.put(33, registerValues.get(rs) % registerValues.get(rt));
						break;
					//mfhi
					case 16:
						System.out.format("inst: " + "%s%c" + "%s%n",
								func.get(funct), '\t', registers.get(rd));
						registerValues.put(rd, registerValues.get(33));
						break;
					//mflo
					case 18:
						System.out.format("inst: " + "%s%c" + "%s%n",
								func.get(funct), '\t', registers.get(rd));
						registerValues.put(rd, registerValues.get(32));
						break;
					//mult
					case 24:
						System.out.format("inst: " + "%s%c" + "%s," + "%s%n",
								func.get(funct), '\t', registers.get(rs), registers.get(rt));
						registerValues.put(32, registerValues.get(rs) * registerValues.get(rt));
						break;
					//or
					case 37:
						System.out.format("inst: " + "%s%c" + "%s," + "%s," + "%s%n",
								func.get(funct), '\t', registers.get(rd), registers.get(rs), registers.get(rt));
						registerValues.put(rd, registerValues.get(rs) | registerValues.get(rt));
						break;
					//slt
					case 42:
						System.out.format("inst: " + "%s%c" + "%s," + "%s," + "%s%n",
								func.get(funct), '\t', registers.get(rd), registers.get(rs), registers.get(rt));
						if(registerValues.get(rs) < registerValues.get(rt)) {
							registerValues.put(rd, 1);
						} else {
							registerValues.put(rd, 0);
						}
						break;
					//subu
					case 35:
						System.out.format("inst: " + "%s%c" + "%s," + "%s," + "%s%n",
								func.get(funct), '\t', registers.get(rd), registers.get(rs), registers.get(rt));
						registerValues.put(rd, registerValues.get(rs) - registerValues.get(rt));
						break;
					//syscall
					case 12:
						System.out.format("inst: " + "%s%n",
								func.get(funct));
						if(registerValues.get(2) == 1) {
							//print an integer followed by new line character
							stdout.println(registerValues.get(4));
						} else if (registerValues.get(2) == 5) {
							//read an integer from standard input.
							Scanner in = new Scanner(System.in);
							int numb = in.nextInt();
							registerValues.put(2, numb);
						} else if(registerValues.get(2) == 10) {
							System.out.println("exiting simulator");
							System.exit(0);
							return;
						}
						break;
		            default:
						System.err.println("illegal instruction: PC = " + programCounter);
						System.exit(0);
		            	break;
				}
				
			} else {
			//I or J type instruction
				if(opcode != 2) {
					int rs = Integer.parseInt(binary.substring(6,11),2);
					int rt = Integer.parseInt(binary.substring(11,16),2);
					int imm = Integer.parseInt(binary.substring(16),2);
					switch(opcode) {
						//addiu
						case 9:
							System.out.format("inst: " + "%s%c" + "%s," + "%s," + "%d%n",
									opcodes.get(opcode), '\t', registers.get(rt), registers.get(rs), imm);
							registerValues.put(rt, registerValues.get(rs) + imm);
							break;
						//beq
						case 4:
							System.out.format("inst: " + "%s%c" + "%s," + "%s," + "%d%n",
									opcodes.get(opcode), '\t', registers.get(rs), registers.get(rt), imm);
							//might need to change how the offset is handled.
							if(registerValues.get(rs) == registerValues.get(rt)) {
								programCounter = programCounter + imm;
								programCounterChanged = true;
								checkAddressInsideTextSeg(programCounter);
							}
							break;
						//bne
						case 5:
							System.out.format("inst: " + "%s%c" + "%s," + "%s," + "%d%n",
									opcodes.get(opcode), '\t', registers.get(rs), registers.get(rt), imm);
							//might need to change how the offset is handled.
							if(registerValues.get(rs) != registerValues.get(rt)) {
								programCounter = programCounter + imm;
								programCounterChanged = true;
								checkAddressInsideTextSeg(programCounter);
							}
							break;
						//lw
						case 35:
							System.out.format("inst: " + "%s%c" + "%s," + "%d(" + "%s)%n",
									opcodes.get(opcode), '\t', registers.get(rt), imm, registers.get(rs));
							//load from data memory
							registerValues.put(rt, getData(registerValues.get(rs) + imm));
							break;
						//sw
						case 43:
							System.out.format("inst: " + "%s%c" + "%s," + "%d(" + "%s)%n",
									opcodes.get(opcode), '\t', registers.get(rt), imm, registers.get(rs));
							//store to data memory
							setData(registerValues.get(rs) + imm, registerValues.get(rt));
							break;
						default:
							System.err.println("illegal instruction: PC = " + programCounter);
							System.exit(0);
							break;
					}
				} else {
					//j
					int address = Integer.parseInt(binary.substring(6),2);
					System.out.format("inst: " + "%s%c" + "%d%n",
							opcodes.get(opcode), '\t', address);
					programCounter = address;
					programCounterChanged = true;
					checkAddressInsideTextSeg(programCounter);
				}
			}
			printRegs();
			printDataMemory();
			//if no registers changed the PC then increment it.
			if(programCounterChanged == false) {
		    	programCounter++;
			}
    	}
    }
    
}
