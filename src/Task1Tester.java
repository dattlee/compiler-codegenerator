import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Dattlee on 05/12/2016.
 */
public class Task1Tester {

    D_Codegen t1;

    @Before
    public void setUp() {
        t1 = new D_Codegen();
    }

    @Test
    public void intLiteralTest() throws CodegenException {
        String x = t1.genExp(new IntLiteral(2));

        System.out.println("Test for IntLiteral:");
        System.out.println(x);

        assertEquals(String.format("li $a0 2\n"), x);
    }

    @Test
    public void plusBinexpTest() throws CodegenException {
        IntLiteral one = new IntLiteral(1);
        IntLiteral two = new IntLiteral(2);
        Plus plus = new Plus();
        Binexp b = new Binexp(one,plus,two);

        String x = t1.genExp(b);

        String correct = String.format("li $a0 1\n" +
                "sw $a0 0($sp)\n" +
                "addiu $sp $sp -4\n" +
                "li $a0 2\n" +
                "lw $t1 4($sp)\n" +
                "add $a0 $t1 $a0\n" +
                "addiu $sp $sp 4\n");

        System.out.println("Test for plus Binexp:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void minusBinexpTest() throws CodegenException {
        IntLiteral one = new IntLiteral(1);
        IntLiteral eight = new IntLiteral(8);
        Minus minus = new Minus();
        Binexp b = new Binexp(eight,minus,one);

        String x = t1.genExp(b);

        String correct = String.format("li $a0 8\n" +
                "sw $a0 0($sp)\n" +
                "addiu $sp $sp -4\n" +
                "li $a0 1\n" +
                "lw $t1 4($sp)\n" +
                "sub $a0 $t1 $a0\n" +
                "addiu $sp $sp 4\n");

        System.out.println("Test for minus Binexp:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void divBinexpTest() throws CodegenException {
        IntLiteral six = new IntLiteral(6);
        IntLiteral two = new IntLiteral(2);
        Div div = new Div();
        Binexp b = new Binexp(six,div,two);

        String x = t1.genExp(b);

        String correct = String.format("li $a0 6\n" +
                "sw $a0 0($sp)\n" +
                "addiu $sp $sp -4\n" +
                "li $a0 2\n" +
                "lw $t1 4($sp)\n" +
                "div $a0 $t1 $a0\n" +
                "addiu $sp $sp 4\n");

        System.out.println("Test for Div Binexp:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void multiplyBinexpTest() throws CodegenException {
        IntLiteral one = new IntLiteral(3);
        IntLiteral two = new IntLiteral(2);
        Times times = new Times();
        Binexp b = new Binexp(one,times,two);

        String x = t1.genExp(b);

        String correct = String.format("li $a0 3\n" +
                "sw $a0 0($sp)\n" +
                "addiu $sp $sp -4\n" +
                "li $a0 2\n" +
                "lw $t1 4($sp)\n" +
                "mul $a0 $t1 $a0\n" +
                "addiu $sp $sp 4\n");

        System.out.println("Test for Multiply Binexp:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void invokeTest() throws CodegenException {
        IntLiteral one = new IntLiteral(1);
        IntLiteral two = new IntLiteral(2);
        IntLiteral eight = new IntLiteral(8);
        ArrayList<Exp> arrayList= new ArrayList<>(Arrays.asList(one,two,eight));
        Invoke i = new Invoke("threeArgs", arrayList);

        String x = t1.genExp(i);

        String correct = String.format(
                "sw $fp 0($sp)\n" +
                "addiu $sp $sp -4\n" +
                "li $a0 8\n" +
                "sw $a0 0($sp)\n" +
                "addiu $sp $sp -4\n" +
                "li $a0 2\n" +
                "sw $a0 0($sp)\n" +
                "addiu $sp $sp -4\n" +
                "li $a0 1\n" +
                "sw $a0 0($sp)\n" +
                "addiu $sp $sp -4\n" +
                "jal threeArgs_entry\n");

        System.out.println("Test for Invoke:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void labelTest() throws CodegenException {
        String[] labels = t1.newLabel(D_Codegen.Label.IF);
        String[] labels2 = t1.newLabel(D_Codegen.Label.IF);

        String[] test1 = {("elseBranch1"), ("thenBranch1"), ("exitLabel1")};
        String[] test2 = {("elseBranch2"), ("thenBranch2"), ("exitLabel2")};
        
        assertTrue(test1[0].equals(labels[0]));
        assertTrue(test1[1].equals(labels[1]));
        assertTrue(test1[2].equals(labels[2]));
        assertTrue(test2[0].equals(labels2[0]));
        assertTrue(test2[1].equals(labels2[1]));
        assertTrue(test2[2].equals(labels2[2]));
    }

    @Test
    public void equalsIfTest() throws CodegenException {
        IntLiteral a = new IntLiteral(5);
        IntLiteral b = new IntLiteral(2);
        Plus p = new Plus();
        Minus m = new Minus();
        Equals e = new Equals();
        Binexp c = new Binexp(a,p,b);
        Binexp d = new Binexp(a,m,b);
        If i = new If(a,e,b,c,d);

        // if 2 = 2 then 2 + 2 else 2 - 2

        String x = t1.genExp(i);

        String correct = String.format(
                "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "addiu $sp $sp 4\n" +
                        "beq $a0 $t1 thenBranch1\n" +
                        "elseBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "sub $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "b exitLabel1\n" +
                        "thenBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "add $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "exitLabel1:\n");

        System.out.println("Test for If:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void lessIfTest() throws CodegenException {
        IntLiteral a = new IntLiteral(5);
        IntLiteral b = new IntLiteral(2);
        Plus p = new Plus();
        Minus m = new Minus();
        Less e = new Less();
        Binexp c = new Binexp(a,p,b);
        Binexp d = new Binexp(a,m,b);
        If i = new If(a,e,b,c,d);

        // if 2 = 2 then 2 + 2 else 2 - 2

        String x = t1.genExp(i);

        String correct = String.format(
                "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "addiu $sp $sp 4\n" +
                        "slt $a0 $t1 $a0\n" +
                        "li $t1 1 \n" +
                        "beq $t1 $a0 thenBranch1\n" +
                        "elseBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "sub $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "b exitLabel1\n" +
                        "thenBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "add $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "exitLabel1:\n");

        System.out.println("Test for Less:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void greaterIfTest() throws CodegenException {
        IntLiteral a = new IntLiteral(5);
        IntLiteral b = new IntLiteral(2);
        Plus p = new Plus();
        Minus m = new Minus();
        Greater e = new Greater();
        Binexp c = new Binexp(a,p,b);
        Binexp d = new Binexp(a,m,b);
        If i = new If(a,e,b,c,d);

        // if 2 = 2 then 2 + 2 else 2 - 2

        String x = t1.genExp(i);

        String correct = String.format(
                "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "addiu $sp $sp 4\n" +
                        "slt $a0 $a0 $t1\n" +
                        "li $t1 1 \n" +
                        "beq $t1 $a0 thenBranch1\n" +
                        "elseBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "sub $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "b exitLabel1\n" +
                        "thenBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "add $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "exitLabel1:\n");

        System.out.println("Test for GreaterThan:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void lessEqualIfTest() throws CodegenException {
        IntLiteral a = new IntLiteral(5);
        IntLiteral b = new IntLiteral(2);
        Plus p = new Plus();
        Minus m = new Minus();
        LessEq e = new LessEq();
        Binexp c = new Binexp(a,p,b);
        Binexp d = new Binexp(a,m,b);
        If i = new If(a,e,b,c,d);

        // if 2 = 2 then 2 + 2 else 2 - 2

        String x = t1.genExp(i);

        String correct = String.format(
                "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "addiu $sp $sp 4\n" +
                        "slt $a0 $a0 $t1\n" +
                        "li $t1 1 \n" +
                        "bne $t1 $a0 thenBranch1\n" +
                        "elseBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "sub $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "b exitLabel1\n" +
                        "thenBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "add $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "exitLabel1:\n");

        System.out.println("Test for LessEqual:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void greaterEqualIfTest() throws CodegenException {
        IntLiteral a = new IntLiteral(5);
        IntLiteral b = new IntLiteral(2);
        Plus p = new Plus();
        Minus m = new Minus();
        GreaterEq e = new GreaterEq();
        Binexp c = new Binexp(a,p,b);
        Binexp d = new Binexp(a,m,b);
        If i = new If(a,e,b,c,d);

        // if 2 = 2 then 2 + 2 else 2 - 2

        String x = t1.genExp(i);

        String correct = String.format(
                "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "addiu $sp $sp 4\n" +
                        "slt $a0 $t1 $a0\n" +
                        "li $t1 1 \n" +
                        "bne $t1 $a0 thenBranch1\n" +
                        "elseBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "sub $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "b exitLabel1\n" +
                        "thenBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "add $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "exitLabel1:\n");

        System.out.println("Test for GreaterEq:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void whileTest() throws CodegenException {
        IntLiteral a = new IntLiteral(5);
        IntLiteral b = new IntLiteral(2);
        Plus p = new Plus();
        Minus m = new Minus();
        Equals e = new Equals();
        Binexp d = new Binexp(a,m,b);
        While i = new While(a,e,b,d);

        // if 2 = 2 then 2 + 2 else 2 - 2

        String x = t1.genExp(i);

        String correct = String.format(
                "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "addiu $sp $sp 4\n" +
                        "beq $a0 $t1 thenBranch1\n" +
                        "elseBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "sub $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "b exitLabel1\n" +
                        "thenBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "add $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "exitLabel1:\n");

        System.out.println("Test for If:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void repeatTest() throws CodegenException {
        IntLiteral a = new IntLiteral(5);
        IntLiteral b = new IntLiteral(2);
        Plus p = new Plus();
        Minus m = new Minus();
        Equals e = new Equals();
        Binexp d = new Binexp(a,m,b);
        RepeatUntil i = new RepeatUntil(d,a,e,b);

        // if 2 = 2 then 2 + 2 else 2 - 2

        String x = t1.genExp(i);

        String correct = String.format(
                "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "addiu $sp $sp 4\n" +
                        "beq $a0 $t1 thenBranch1\n" +
                        "elseBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "sub $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "b exitLabel1\n" +
                        "thenBranch1:\n" +
                        "li $a0 5\n" +
                        "sw $a0 0($sp)\n" +
                        "addiu $sp $sp -4\n" +
                        "li $a0 2\n" +
                        "lw $t1 4($sp)\n" +
                        "add $a0 $t1 $a0\n" +
                        "addiu $sp $sp 4\n" +
                        "exitLabel1:\n");

        System.out.println("Test for If:");
        System.out.println(x);

        assertEquals(correct, x);
    }

    @Test
    public void declCheck() throws CodegenException {

        IntLiteral a = new IntLiteral(5);
        IntLiteral b = new IntLiteral(2);
        Plus p = new Plus();
        Minus m = new Minus();
        Equals e = new Equals();
        Binexp c = new Binexp(a,p,b);
        Binexp d = new Binexp(a,m,b);
        If i = new If(a,e,b,c,d);

        Declaration dec = new Declaration("Magnificent", 0, i);



        String correct = "";// String.format(

        System.out.println("Test for Declaration:");
        System.out.println(t1.genDecl(dec));
        //System.out.println(t1.genDecl(new Declaration("HelloWorld", 0,)));

        assertEquals(correct, d);
    }


}