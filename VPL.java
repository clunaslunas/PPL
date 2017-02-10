import java.io.*;
import java.util.*;

public class VPL
{
  static final int max = 100000;
  static int[] mem = new int[max];
  static int instruct[] = new int[max];
  static int ip, bp, sp, rv, hp, numPassed, gp;

  static String fileName;

  public static void main(String[] args) throws Exception
  {
    BufferedReader keys = new BufferedReader(
                           new InputStreamReader( System.in));
    System.out.print("enter name of file containing VPLstart program: ");
    fileName = keys.readLine();

    // load the program into the front part of
    // memory
    BufferedReader input = new BufferedReader( new FileReader( fileName ));
    String line;
    StringTokenizer st;
    int opcode;

    ArrayList<IntPair> labels, holes;
    labels = new ArrayList<IntPair>();
    holes = new ArrayList<IntPair>();
    int label;

    int k=0;
    do {
      line = input.readLine();
      // System.out.println("parsing line [" + line + "]");
      if( line != null )
      {// extract any tokens
        st = new StringTokenizer( line );
        if( st.countTokens() > 0 )
        {// have a token, so must be an instruction (as opposed to empty line)

          opcode = Integer.parseInt(st.nextToken());

          // load the instruction into memory:

          if( opcode == labelCode )
          {// note index that comes where label would go
            label = Integer.parseInt(st.nextToken());
            labels.add( new IntPair( label, k ) );
          }
          else
          {// opcode actually gets stored
            mem[k] = opcode;  ++k;

            if( opcode == callCode || opcode == jumpCode ||
                opcode == condJumpCode )
            {// note the hole immediately after the opcode to be filled in later
              label = Integer.parseInt( st.nextToken() );
              mem[k] = label;  holes.add( new IntPair( k, label ) );
              ++k;
            }

            // load correct number of arguments (following label, if any):
            for( int j=0; j<numArgs(opcode); ++j )
            {
              mem[k] = Integer.parseInt(st.nextToken());
              ++k;
            }

          }// not a label

        }// have a token, so must be an instruction
      }// have a line
    }while( line != null );

    //System.out.println("after first scan:");
    //showMem( 0, k-1 );

    // fill in all the holes:
    int index;
    for( int m=0; m<holes.size(); ++m )
    {
      label = holes.get(m).second;
      index = -1;
      for( int n=0; n<labels.size(); ++n )
        if( labels.get(n).first == label )
          index = labels.get(n).second;
      mem[ holes.get(m).first ] = index;
    }

    // System.out.println("after replacing labels:");
    // showMem( 0, k-1 );

    // initialize registers:
    bp = k;  sp = k+2;  ip = 0;  rv = 0;  hp = max;
    numPassed = 0;

    int codeEnd = bp-1;

    System.out.println("Code is " );
    showMem( 0, codeEnd );

    gp = codeEnd + 1;

    int a, b, c, n, L;
    Boolean end = false;
    int numCount = 0;
    while( end == false ){  //end == false

      int op;
      op = mem[ ip ];

      if( op==0 ){        //Instruction 0
        System.out.println("You shouldn't have come here... ");
        ip++;
      }

      /*
      1 L label
      During program loading this instruction disappears,
      and all occurrences of L are replaced by
      the actual index in mem where the opcode 1 would
      have been stored
      */
      else if( op==1 ){            //Instruction 1;
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        ip += 2;
      }

      /*
      2 L call
      Do all the steps necessary to set up for execution
      of the subprogram that begins at label L.
      */
      else if( op==2 ){            //Instruction 2
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        mem[sp - 2 - numPassed] = bp;
        mem[sp - 1 - numPassed] = ip + 2;
        bp = sp - 2 - numPassed;
        ip = mem[ip + 1];
        System.out.println("Moving to new IP: " + ip + "\nNew BP: " + bp + "\nCurrent SP: " + sp);
        instruct[numCount] = op;
        numCount += 1;
        numPassed = 0;
      }

      //
      // /*
      // 3 a pass
      // Push the contents of cell a on the stack.
      else if( op==3){            //Instruction 3
        numPassed += 1;
        sp += numPassed;
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        mem[sp - numPassed] = mem[bp + 2 + a];
        System.out.println("Pushed " + mem[bp + 2 + a] + " onto to stack at: " + (sp - numPassed));
        ip += 2;
        instruct[numCount] = op;
        numCount += 1;
      }

      //
      // /*
      // 4 n locals
      // Increase sp by n to make space for local variables
      // in the current stack frame
      else if( op==4 ){   //Instruction 4
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        n = 0;
        n += mem[ ip+1 ]; //Variable n
        sp += n +2; //Increase sp by n and plus two for reserve bp info
        System.out.println("SP: \t" + sp + "\nBP: \t" + bp);
        ip += 2;
        instruct[numCount] = op;
        numCount += 1;
      }

      /*
      5 a return
      Do all the steps necessary to return from the
      current subprogram, including putting the value
      stored in cell a in rv.
      */
      else if( op==5){            //Instruction 5
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        System.out.println("Pulling rv from: " + (bp + 2 + a));
        rv = mem[bp + 2 + a];
        ip = mem[bp + 1];
        sp = bp;
        bp = mem[bp];
        System.out.println("\nRV will be : " + rv + "\nIP will be set to: " +
                            ip + "\nSP will be: " + sp + "\nBP will be set to: "
                            + bp);
        instruct[numCount] = op;
        numCount += 1;
      }

      else if( op==6 ){            //Instruction 6
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp + "\tRV: " + rv);
        a = mem[ip + 1];
        System.out.println("A: " + a);
        mem[bp + 2 + a] = rv;
        ip += 2;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==7 ){            //Instruction 7
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        L = ip + 1;
        ip = L;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==8 ){            //Instruction 8

        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 2];
        L = mem[ip + 1];
        int zeroVal = mem[bp + numPassed + 2 + a];
        System.out.println("Looking in : " + (bp + numPassed + 2 + a) + " Value is: " + zeroVal);

        if(zeroVal != 0){
          ip = L;
          System.out.println("Moving to IP : " + ip);
        }
        else{
          ip += 3;
          System.out.println("Moved to IP : " + ip);
        }
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==9 ){            //Instruction 9
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];
        mem[bp + 2 + a] = mem[bp + 2 + b] + mem[bp + 2 + c];
        ip += 4;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==10 ){            //Instruction 10

        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];
        mem[bp + 2 + a] = mem[bp + 2 + b] - mem[bp + 2 + c];
        ip += 4;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==11 ){            //Instruction 11
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];
        System.out.println("\nA: " + mem[bp + 2 + a] + "\nB: " + mem[bp + 2 + b] + "\nC: " + mem[bp + 2 + c]);
        System.out.println("\nA will be stored at: " + (bp + 2 + a) +
                           "\nB will be stored at: " + (bp + 2 + b) +
                           "\nC will be stored at: " + (bp + 2 + c) );
        mem[bp + 2 + a] = mem[bp + 2 + b] * mem[bp + 2 + c];
        ip += 4;
        instruct[numCount] = op;
        numCount += 1;
        System.out.println("RV: " + rv);
      }
      else if( op==12 ){            //Instruction 12
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];

        mem[bp + 2 + a] = mem[bp + 2 + b] / mem[bp + 2 + c];
        ip += 4;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==13 ){            //Instruction 13
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];

        mem[bp + 2 + a] = mem[bp + 2 + b] % mem[bp + 2 + c];
        ip += 4;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==14 ){            //Instruction 14
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];
        if(mem[bp + 2 + b] == mem[bp + 2 + a]){
          System.out.println("B is: " + b + " C is: " + c);
          mem[bp + 2 + a] = 1;
        }
        else{
          mem[bp + 2 + a] = 0;
        }
        ip += 4;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==15 ){            //Instruction 15
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];
        if(mem[bp + 2 + b] != mem[bp + 2 + a]){
          System.out.println("B is: " + b + " C is: " + c);
          mem[bp + 2 + a] = 1;
        }
        else{
          mem[bp + 2 + a] = 0;
        }
        ip += 4;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op == 16){            //Instruction 16
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];
        System.out.println("Cell A is: " + a + "\nB: " + mem[bp + 2 + b]  + "\nC: " + mem[bp + 2 + c] );
        if(mem[bp + 2 + b] < mem[bp + 2 + c]){

          mem[bp + 2 + a] = 1;
          System.out.println("B is: " + b + " C is: " + c + " Storing 1 at: " +
                              (bp + 2 + a));
        }
        else{
          mem[bp + 2 + a] = 0;
          System.out.println("Store 0 at: " + (bp + 2 + a));
        }
        ip += 4;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==17 ){            //Instruction 17
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];
        if(mem[bp + 2 + b] <= mem[bp + 2 + a]){
          System.out.println("B is: " + b + " C is: " + c);
          mem[bp + 2 + a] = 1;
        }
        else{
          mem[bp + 2 + a] = 0;
        }
        ip += 4;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==18 ){            //Instruction 18
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];
        if(b == c & c ==b){
          System.out.println("B is: " + b + " C is: " + c);
          mem[bp + 2 + a] = 1;
        }
        else{
          mem[bp + 2 + a] = 0;
        }
        ip += 4;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==19 ){            //Instruction 19
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];
        if(b == c || c == b){
          System.out.println("B is: " + b + " C is: " + c);
          mem[bp + 2 + a] = 1;
        }
        else{
          mem[bp + 2 + a] = 0;
        }
        ip += 4;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==20 ){            //Instruction 20
        a = mem[ip + 1];
        b = mem[ip + 2];
        int zeroVal = mem[bp + 2 + b];
        if(zeroVal == 0){
          mem[bp + 2 + a] = 1;
        }
        else{
          mem[bp + 2 + a] = 0;
        }
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==21 ){            //Instruction 21
        a = mem[ip + 1];
        b = mem[ip + 2];
        mem[bp + 2 + a] = -(mem[bp + 2 + b]);
        ip += 3;
      }
      else if( op==22 ){            //Instruction 22
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        n = mem[ip + 2];
        a = mem[ip + 1];
        mem[bp + 2 + a] = n;
        System.out.println("Set index: " + (bp + 2 + a) + " to: " + n);
        ip += 3;
        System.out.println("New IP: " + ip);
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==23 ){            //Instruction 23
        a = mem[ip + 1];
        b = mem[ip + 2];
        mem[bp + 2 + a] = mem[bp + 2 + b];

        ip += 3;
      }
      else if( op==24 ){            //Instruction 24
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];
        mem[bp + 2 + a] = mem[bp + 2 + b] + mem[bp + 2 + c];
        int hIndex = mem[bp + 2 + a];
        int hVal = mem[hp - hIndex];
        mem[bp + 2 + a] = hVal;
        ip +=  4;

      }
      else if( op==25 ){            //Instruction 25
        a = mem[ip + 1];
        b = mem[ip + 2];
        c = mem[ip + 3];
        int hIndex = mem[bp + 2 + a] + mem[bp + 2 + b];
        mem[hp - hIndex] = mem[bp + 2 + c];
        ip += 4;
      }
      else if(op == 26){            //Instruction 26

        System.out.println("END: Thanks for playing... ");
        end = true;
        instruct[numCount] = op;
        numCount += 1;

      }
      else if( op==27 ){            //Instruction 27
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        Scanner in = new Scanner(System.in);
        System.out.println("? : ");

        int num = in.nextInt();
        a = mem[ip + 1];
        // System.out.println("Current bp: " + bp + " So I am gonna store this at: "
        //                     + (bp + 2 + a));
        mem[bp + 2 + a] = num;
        System.out.println("Pushed " + num + " Onto cell: " + a + " at index : " + (bp + 2 + a));
        ip += 2;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==28 ){            //Instruction 28
        System.out.println("\tIP: " + ip + "\tOP:  " + op);
        System.out.println("\tBP: " + bp + "\tSP: " + sp);
        a = mem[ip + 1];
        // int display =
        System.out.println("Index: " + (bp + 2 + a) + " :" + mem[bp + 2 + a] );
        ip += 2;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==29 ){            //Instruction 29
        System.out.println("\n");
        ip += 1;
        instruct[numCount] = op;
        numCount += 1;
      }
      else if( op==30 ){            //Instruction 30
        a = mem[ip + 1];
        int asciRang = mem[bp + 2 + a];
        if(asciRang <= 126 & asciRang >= 32){
          char asc = (char) asciRang;
          System.out.println(asc);
        }
        else{
          System.out.println("Out of range");
        }
        ip += 2;
      }
      else if( op==31 ){            //Instruction 31
        a = mem[ip + 1];
        b = mem[ip + 2];
        int m = mem[bp + 2 + b];
        hp -= m;
        mem[bp + 2 + a] = hp;
        ip += 3;
      }
      else if( op==32 ){            //Instruction 32
        n = mem[ip + 1];
        gp = bp;
        sp = bp + n;
        ip += 2;
      }
      else if( op==33 ){            //Instruction 33
        n = mem[ip + 1];
        a = mem[ip + 2];
        mem[gp + n] = mem[bp + 2 + a];
        ip += 3;
      }
      else if( op==34 ){            //Instruction 34
        n = mem[ip + 1];
        a = mem[ip + 2];
        mem[bp + 2 + a] = mem[gp + n];
        ip += 3;
      }

    }// end of the fetch-execute loop
    // for(int i = 0; i<90;i++){
    //   System.out.println("Index: " + i + " Value: " + mem[i]);
    // }




  }// main

  // use symbolic names for all opcodes:

  // op to produce comment on a line by itself
  private static final int noopCode = 0;

  // ops involved with registers
  private static final int labelCode = 1;
  private static final int callCode = 2;
  private static final int passCode = 3;
  private static final int allocCode = 4;
  private static final int returnCode = 5;  // return a means "return and put
           // copy of value stored in cell a in register rv
  private static final int getRetvalCode = 6;//op a means "copy rv into cell a"
  private static final int jumpCode = 7;
  private static final int condJumpCode = 8;

  // arithmetic ops
  private static final int addCode = 9;
  private static final int subCode = 10;
  private static final int multCode = 11;
  private static final int divCode = 12;
  private static final int remCode = 13;
  private static final int equalCode = 14;
  private static final int notEqualCode = 15;
  private static final int lessCode = 16;
  private static final int lessEqualCode = 17;
  private static final int andCode = 18;
  private static final int orCode = 19;
  private static final int notCode = 20;
  private static final int oppCode = 21;

  // ops involving transfer of data
  private static final int litCode = 22;  // litCode a b means "cell a gets b"
  private static final int copyCode = 23;// copy a b means "cell a gets cell b"
  private static final int getCode = 24; // op a b means "cell a gets
                                                // contents of cell whose
                                                // index is stored in b"
  private static final int putCode = 25;  // op a b means "put contents
     // of cell b in cell whose offset is stored in cell a"

  // system-level ops:
  private static final int haltCode = 26;
  private static final int inputCode = 27;
  private static final int outputCode = 28;
  private static final int newlineCode = 29;
  private static final int symbolCode = 30;
  private static final int newCode = 31;

  // global variable ops:
  private static final int allocGlobalCode = 32;
  private static final int toGlobalCode = 33;
  private static final int fromGlobalCode = 34;

  // debug ops:
  private static final int debugCode = 35;

  // return the number of arguments after the opcode,
  // except ops that have a label return number of arguments
  // after the label, which always comes immediately after
  // the opcode
  private static int numArgs( int opcode )
  {
    // highlight specially behaving operations
    if( opcode == labelCode ) return 1;  // not used
    else if( opcode == jumpCode ) return 0;  // jump label
    else if( opcode == condJumpCode ) return 1;  // condJump label expr
    else if( opcode == callCode ) return 0;  // call label

    // for all other ops, lump by count:

    else if( opcode==noopCode ||
             opcode==haltCode ||
             opcode==newlineCode ||
             opcode==debugCode
           )
      return 0;  // op

    else if( opcode==passCode || opcode==allocCode ||
             opcode==returnCode || opcode==getRetvalCode ||
             opcode==inputCode ||
             opcode==outputCode || opcode==symbolCode ||
             opcode==allocGlobalCode
           )
      return 1;  // op arg1

    else if( opcode==notCode || opcode==oppCode ||
             opcode==litCode || opcode==copyCode || opcode==newCode ||
             opcode==toGlobalCode || opcode==fromGlobalCode

           )
      return 2;  // op arg1 arg2

    else if( opcode==addCode ||  opcode==subCode || opcode==multCode ||
             opcode==divCode ||  opcode==remCode || opcode==equalCode ||
             opcode==notEqualCode ||  opcode==lessCode ||
             opcode==lessEqualCode || opcode==andCode ||
             opcode==orCode || opcode==getCode || opcode==putCode
           )
      return 3;

    else
    {
      System.out.println("Fatal error: unknown opcode [" + opcode + "]" );
      System.exit(1);
      return -1;
    }

  }// numArgs

  private static void showMem( int a, int b )
  {
    for( int k=a; k<=b; ++k )
    {
      System.out.println( k + ": " + mem[k] );
    }
  }// showMem

}// VPLstart
