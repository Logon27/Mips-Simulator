#  Mips Simulator

This program takes in a machine code object file from standard input and simulates the execution.

## Supported Directives:
* .text (switch to the text segment)
* .data (switch to the data segment)
* .word w1, ... , wn (store n 32-bit integer values in successive memory words)
* .space n (allocate n words)

## Supported instructions:
* addiu
* addu
* and
* beq
* bne
* div
* j
* lw
* mfhi
* mflo
* mult
* or
* slt
* subu
* sw
* syscall

---
System Call Code | Argument | Explanation
------------ | ------------- | -------------
1 | $a0 = integer | Print an integer value followed by a newline character to standard output
5 |  | Read an integer value from standard input and assign the value to $v0
10 |  | Exit the simulation

---
Type | Explanation
------------ | -------------
Illegal Instruction | Illegal combination of opcode and funct field values
Illegal Instruction Address | PC is referencing an addresss outside of the text segment
Illegal Data Address | Load or store referencing an address outside of the data segment
Divide By Zero | Integer divide by zero

---
## Example input file:

<img src="https://i.imgur.com/cxRwupl.png">

## Example output file:

The sample program reads an integer specifying the number of integers to sum. 
Then it reads those integers from stdin. Then prints the output.
For example, 3 integers, 5 + 10 + 15 = 30

<img src="https://i.imgur.com/2zEcQML.png">

## Generated Log.txt File
A log.txt file is also generated each time a program is simulated. This includes disassembled instructions, register values and current values of the words in the data segment for every instruction executed.

<img src="https://i.imgur.com/fgNQJJG.png">
