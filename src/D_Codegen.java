import java.util.List;
import java.util.Stack;

/**
 * Created by Dattlee on 04/12/2016.
 */
class D_Codegen implements Codegen{

    public int labelCount = 0;
    public Stack<String> startLoopStack = new Stack<>();
    public Stack<String> exitLoopStack = new Stack<>();


    @Override
    public String codegen(Program p) throws CodegenException {
        String s = "";

        //check stack pointer and fp equal each other
        s += String.format("jal %s_entry\n", p.decls.get(0).id);//  jump and link to first declaration
        s += "li $v0, 10\n";                // li      $v0, 10
        s += "syscall\n" ;                   // syscall 10

        // syscall automatically makes a call to $v0
        // and syscall 10 means end program.

        for (Declaration decl: p.decls) {
            s += genDecl(decl);
        }
        return String.format(s);

    }


    public String genDecl(Declaration d) throws CodegenException {
        int sizeAR = ( 2 + d.numOfArgs ) * 4;
        // each procedure argument takes 4 bytes,
        // in addition the AR stores the return
        // address and old FP
        String s = d.id + "_entry:\n" +    // label to jump to
        "move $fp $sp\n" +      // FP points to (future) top of AR
        "sw $ra 0($sp)\n" +     // put return address on stack
        "addiu $sp $sp -4\n"+   // now AR is fully created
        genExp(d.body) +
        "lw $ra 4($sp)\n" +     // load return address into $ra
                                // could also use $fp
        "addiu $sp $sp " + sizeAR + "\n" +  // pop AR off stack in one go
        "lw $fp 0($sp)\n" +     // restore old FP
        "jr $ra\n";             // hand back control to caller

        return s;
    }

    public String genExp (Exp e) throws CodegenException {

        if (e instanceof IntLiteral){
            return String.format("li $a0 %s\n", ((IntLiteral) e).n);

        } else if (e instanceof Variable){
            int offset = 4 * ((Variable) e).x;
            return "lw $a0 " + offset + "($fp)\n";

        } else if (e instanceof If){
            If ifstat = (If) e;
            String[] labels = newLabel(Label.IF);
            String elseBranch = labels[0];
            String thenBranch = labels[1];
            String exitLabel = labels[2];
            String s = genComp(ifstat.l,ifstat.comp,ifstat.r) + thenBranch + "\n" +
                    elseBranch + ":\n" +
                    genExp(ifstat.elseBody) +
                    "b " + exitLabel + "\n" +
                    thenBranch + ":\n" +
                    genExp(ifstat.thenBody) +
                    exitLabel + ":\n";
            return s;

        } else if (e instanceof Binexp){
            Binexp bexp = (Binexp) e;
            Binop bop = bexp.binop;
            String s = "";

            if (bop instanceof Plus) {
                s += genExp(bexp.l);
                s += "sw $a0 0($sp)\n";
                s += "addiu $sp $sp -4\n";
                s += genExp(bexp.r);
                s += "lw $t1 4($sp)\n";
                s += "add $a0 $t1 $a0\n";
                s += "addiu $sp $sp 4\n";
                return String.format(s);

            } else if (bop instanceof Minus) {
                s += genExp(bexp.l);
                s += "sw $a0 0($sp)\n";
                s += "addiu $sp $sp -4\n";
                s += genExp(bexp.r);
                s += "lw $t1 4($sp)\n";
                s += "sub $a0 $t1 $a0\n";
                s += "addiu $sp $sp 4\n";
                return String.format(s);

            } else if (bop instanceof Times) {
                s += genExp(bexp.l);
                s += "sw $a0 0($sp)\n";
                s += "addiu $sp $sp -4\n";
                s += genExp(bexp.r);
                s += "lw $t1 4($sp)\n";
                s += "mul $a0 $t1 $a0\n";
                s += "addiu $sp $sp 4\n";
                return String.format(s);

            } else if (bop instanceof Div) {
                s += genExp(bexp.l);
                s += "sw $a0 0($sp)\n";
                s += "addiu $sp $sp -4\n";
                s += genExp(bexp.r);
                s += "lw $t1 4($sp)\n";
                s += "div $a0 $t1 $a0\n";
                s += "addiu $sp $sp 4\n";
                return String.format(s);

            } else {
                throw new CodegenException("Binary Expression Error");

            }

        } else if (e instanceof Invoke) {
            Invoke invoke = (Invoke) e;
            String s = "sw $fp 0($sp)\n" +
                    "addiu $sp $sp -4\n";
            List<Exp> args = invoke.args;
            for (int i = args.size() - 1; i >= 0; i--) {
                s += genExp(args.get(i)) +
                        "sw $a0 0($sp)\n" +              // save nth argument on stack
                        "addiu $sp $sp -4\n";
            }
            s += String.format("jal %s_entry\n", invoke.name);
            return String.format(s);

        } else if (e instanceof While){
            While whileVar = (While) e;
            String[] labels = newLabel(Label.WHILE);
            String whileStart = labels[0];
            String whileLoop = labels[1];
            String whileExit = labels[2];

            startLoopStack.push(whileStart);
            exitLoopStack.push(whileExit);

            String s = whileStart + ":\n";
            s += genComp(whileVar.l, whileVar.comp, whileVar.r) + whileLoop + "\n";
            s += "j " + whileExit + "\n";
            s += whileLoop + ":\n";
            s += genExp(whileVar.body);
            s += "j " + whileStart + "\n";
            s += whileExit + ":\n";

            startLoopStack.pop();
            exitLoopStack.pop();

            return s;

        } else if (e instanceof RepeatUntil) {
            RepeatUntil repeatVar = (RepeatUntil) e;
            String[] labels = newLabel(Label.REPEAT);
            String repeatStart = labels[0];
            String repeatLoop = labels[1];
            String repeatExit = labels[2];

            startLoopStack.push(repeatStart);
            exitLoopStack.push(repeatExit);

            String s = "";
            s += repeatLoop + ":\n";
            s += genExp(repeatVar.body);
            s += repeatStart + ":\n";
            s += genComp(repeatVar.l, repeatVar.comp, repeatVar.r) + repeatLoop + "\n";
            s += repeatExit + ":\n";

            startLoopStack.pop();
            exitLoopStack.pop();

            return s;


        } else if (e instanceof Assign){
            int offset = 4 * ((Assign) e).x;
            String s = genExp(((Assign) e).e);
            s += "sw $a0 " + offset + "($fp)\n";
            return s;

        } else if (e instanceof Seq){
            String s = genExp(((Seq) e).l);
            s += genExp(((Seq) e).r);
            return s;

        } else if (e instanceof Skip){
            return "";

        } else if (e instanceof Break){
            if(!exitLoopStack.empty()) {
                return "j " + exitLoopStack.peek() + "\n";
            } else {
                throw new CodegenException("Break statement not in loop");
            }
        } else if (e instanceof Continue){
            if(!startLoopStack.empty()) {
                return "j " + startLoopStack.peek() + "\n";
            } else {
                throw new CodegenException("Continue statement not in loop");
            }
        } else {
            throw new CodegenException("Unidentified Expression");
        }
    }

    public  String genComp (Exp l, Comp comp, Exp r) throws CodegenException {
        if (comp instanceof Equals){
            String s = genExp(l) +
                    "sw $a0 0($sp)\n" +
                    "addiu $sp $sp -4\n" +
                    genExp(r) +
                    "lw $t1 4($sp)\n" +
                    "addiu $sp $sp 4\n" +
                    "beq $a0 $t1 ";
            return s;
        } else if (comp instanceof Less){
            String s = genExp(l) +
                    "sw $a0 0($sp)\n" +     // put l on stack
                    "addiu $sp $sp -4\n" +  // increment stack
                    genExp(r) +
                    "lw $t1 4($sp)\n" +
                    "addiu $sp $sp 4\n" +
                    "slt $a0 $t1 $a0\n" +   // if $t1 (or l) is less than $a0 (or r) set t2
                    "li $t1 1 \n" +
                    "beq $t1 $a0 ";
            return s;
        } else if (comp instanceof Greater){
            String s = genExp(l) +
                    "sw $a0 0($sp)\n" +     // put l on stack
                    "addiu $sp $sp -4\n" +  // increment stack
                    genExp(r) +
                    "lw $t1 4($sp)\n" +
                    "addiu $sp $sp 4\n" +
                    "slt $a0 $a0 $t1\n" +   // if $t1 (or l) is less than $a0 (or r) set t2
                    "li $t1 1 \n" +
                    "beq $t1 $a0 ";
            return s;
        } else if (comp instanceof LessEq){
            String s = genExp(l) +
                    "sw $a0 0($sp)\n" +     // put l on stack
                    "addiu $sp $sp -4\n" +  // increment stack
                    genExp(r) +
                    "lw $t1 4($sp)\n" +
                    "addiu $sp $sp 4\n" +
                    "slt $a0 $a0 $t1\n" +   // if $t1 (or l) is less than $a0 (or r) set t2
                    "li $t1 1 \n" +
                    "bne $t1 $a0 ";
            return s;
        } else if (comp instanceof GreaterEq){
            String s = genExp(l) +
                    "sw $a0 0($sp)\n" +     // put l on stack
                    "addiu $sp $sp -4\n" +  // increment stack
                    genExp(r) +
                    "lw $t1 4($sp)\n" +
                    "addiu $sp $sp 4\n" +
                    "slt $a0 $t1 $a0\n" +   // if $t1 (or l) is less than $a0 (or r) set t2
                    "li $t1 1 \n" +
                    "bne $t1 $a0 ";
            return s;
        } else {
            throw new CodegenException("Unidentified Comp");
        }
    }


    public enum Label {
        IF, WHILE, REPEAT
    }

    public String[] newLabel(Label l) throws CodegenException {
        labelCount += 1;
        switch (l) {
            case IF:
                String[] iflabel = {("elseBranch"+labelCount), ("thenBranch"+labelCount), ("exitLabel"+labelCount)};
                return iflabel;
            case WHILE:
                String[] whileLabel = {("whileStart"+labelCount), ("whileLoop"+labelCount), ("whileExit"+labelCount)};
                return whileLabel;
            case REPEAT:
                String[] repeatLabel = {("repeatStart"+labelCount), ("repeatLoop"+labelCount), ("repeatExit"+labelCount)};
                return repeatLabel;
            default:
                throw new CodegenException("Non-existent label");
        }
    }

}
