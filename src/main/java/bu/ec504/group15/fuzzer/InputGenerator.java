package bu.ec504.group15.fuzzer;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;

/*
 * Class is used to generate input for a source code file to test it for vulnerabilities.
 * The constructor creates an instance of the FileParser, and parses the file.
 * The createInput function is what generates inputs based on the parsed info
 */
public class InputGenerator {

    /* Constructor Class */
    public InputGenerator(String file) {
        FileParser parser = new FileParser(file);

        /* Parse the file */

        /* Get a guess for the type/style of input */
        argTypes = parser.FindArgType();

        /* Find variables dependant on args[0] */
        dependantArgNames = parser.FindInputDependentVariables();

        /* Find all conditional branches that we can manipulate */
        conditions = parser.FindExpressions();

        /* Find all integers */
        fuzzedInts = ints = parser.findInts();

        /* Find all doubles */
        fuzzedDoubles = doubles = parser.findDoubles();

        /* Puts intermediate values between each element of the number arrays */
        addIntermediateNums();

        /* Find the string info */
        fuzzedStrings = strings = parser.findStrings();

        /* combine numbers and strings into an array of possible inputs */
        createInputStrings();

        /* Find dependancies */
        dependancies = new ArrayList<>();
        for (int i=0; i<dependantArgNames.size(); i++) {
            parser.FindDependancies(dependantArgNames.get(i), dependancies);
        }

        g = 0;
        fuzzRange = 2;

        return;
    }

    /* Prints the information that we parsed from the file. Mostly used for debugging */
    public void displayParsedFileInfo() {
        System.out.println("argTypes");
        argTypes.forEach(at -> {
            System.out.println("   " + at);
        });

        System.out.println("DependantArgNames:");
        dependantArgNames.forEach((dan -> {
            System.out.println("   " + dan);
        }));

        System.out.println("conditions");
        conditions.forEach(c -> {
            System.out.println("   " + c);
        });

        System.out.println("Dependancies:");
        dependancies.forEach(d -> {
            System.out.println("   " + d);
        });

        System.out.print("Integers: ");
        ints.forEach(i -> System.out.print(i + ", "));
        System.out.print("\n");

        System.out.print("Doubles: ");
        doubles.forEach(d -> System.out.print(d + ", "));
        System.out.print("\n");

        System.out.print("Strings: ");
        strings.forEach(s -> System.out.print("'" + s + "', "));

        System.out.println("\nINPUT STRINGS: ");
        inputStrings.forEach(i -> System.out.println("----> " + i));

        System.out.println("\nNEG INPUT STRINGS: ");
        negativeInputStrings.forEach(i -> System.out.println("----> " + i));
    }

    /* Solves a binary expression for a string input */
    private String solveString(BinaryExpr b, String s, int how) {
        String updated = s;
        if ((!(b.getLeft() instanceof BinaryExpr)) && (!(b.getRight() instanceof BinaryExpr))) {
            if (b.getLeft().toString().compareTo("args[0]") != 0) {
                String useless = b.getLeft().toString();
                if (how == 1)
                    updated = updated.concat(useless.substring(1, useless.length() - 1));
                else
                    updated = updated.replaceFirst(useless.substring(1, useless.length() - 1), "");
            }
            if (b.getRight().toString().compareTo("args[0]") != 0) {
                String useless = b.getRight().toString();
                if (how == 1)
                    updated = updated.concat(useless.substring(1, useless.length() - 1));
                else
                    updated = updated.replaceFirst(useless.substring(1, useless.length() - 1), "");
            }
            return updated;
        } else if (b.getLeft() instanceof BinaryExpr) {
            updated = solveString(b.getLeft().asBinaryExpr(), s, how);
            if (b.getRight().toString().compareTo("args[0]") != 0) {
                String useless = b.getRight().toString();
                if (how == 1)
                    updated = updated.concat(useless.substring(1, useless.length() - 1));
                else
                    updated = updated.replaceFirst(useless.substring(1, useless.length() - 1), "");
            }
        } else if (b.getRight() instanceof BinaryExpr) {
            updated = solveString(b.getRight().asBinaryExpr(), s, how);
            if (b.getLeft().toString().compareTo("args[0]") != 0) {
                String useless = b.getLeft().toString();
                if (how == 1)
                    updated = updated.concat(useless.substring(1, useless.length() - 1));
                else
                    updated = updated.replaceFirst(useless.substring(1, useless.length() - 1), "");
            }
        }
        return updated;
    }

    private void addIntermediateNums() {

        if (ints != null && ints.size() > 0) {
            ArrayList<Integer> list = new ArrayList<>();
            TreeMap<Integer, Integer> listMap = new TreeMap<>();

            /* Put a random value between zero and the first element in the first slot */
            if (ints.get(0) < 0) {
                listMap.put(-(new Random().nextInt(Math.abs(ints.get(0)))), 1);
            } else if (ints.get(0) > 0) {
                listMap.put(new Random().nextInt(ints.get(0)), 1);
            }


            for (int i = 0; i < ints.size(); i++) {
                listMap.put(ints.get(i), 1);
                if (i < ints.size()-1) {
                    if (ints.get(i+1) < 0) {
                        listMap.put(new Random().nextInt(Math.abs(ints.get(i+1)))+ints.get(i), 1);
                    } else {
                        listMap.put(new Random().nextInt(ints.get(i + 1)) + ints.get(i), 1);
                    }
                } else {
                    listMap.put(new Random().nextInt(999) + ints.get(i), 1);
                }
            }
            list.addAll(listMap.keySet());
            ints = list;
        }

        /* Alter the doubles array */
        if (doubles != null && doubles.size() > 0) {
            ArrayList<Double> list = new ArrayList<>();
            TreeMap<Double, Double> listMap = new TreeMap<>();

            /* Put a random value between zero and the first element in the first slot */
            if (doubles.get(0) < 0) {
                listMap.put(-(new Random().nextDouble() + doubles.get(0)), 1D);
            } else if (doubles.get(0) > 0) {
                listMap.put(new Random().nextInt() + doubles.get(0), 1D);
            }


            for (int i = 0; i < doubles.size(); i++) {
                listMap.put(doubles.get(i), 1D);
                if (i < doubles.size()-1) {
                    listMap.put(doubles.get(i) + (doubles.get(i) + doubles.get(i+1)) * (new Random().nextDouble()), 1D);
                } else {
                    listMap.put(doubles.get(i) + (doubles.get(i) + 999) * (new Random().nextDouble()), 1D);
                }
            }
            list.addAll(listMap.keySet());
            doubles = list;
        }

    }

    /* Put all of the input variables into an array of strings */
    private void createInputStrings() {
        ArrayList<String> positiveList = new ArrayList<>();
        ArrayList<String> negativeList = new ArrayList<>();
        String s;

        /* Populate the positive strings */
        for (int i=0; i<fuzzedInts.size(); i++) {
            s = Integer.toString(fuzzedInts.get(i));
            if (s.length() > 3)
                positiveList.add(s.substring(0, 3));
            else
                positiveList.add(s);
        }
        for (int i=0; i<fuzzedDoubles.size(); i++) {
            s = Double.toString(fuzzedDoubles.get(i));
            if (s.length() > 3)
                positiveList.add(s.substring(0, 3));
            else
                positiveList.add(s);
        }

        positiveList.addAll(fuzzedStrings);

        /* Populate the negative strings */
        for (int i=0; i<fuzzedInts.size(); i++) {
            s = Integer.toString(fuzzedInts.get(i) * -1);
            if (s.length() > 3)
                negativeList.add(s.substring(0,3));
            else
                negativeList.add(s);
        }
        for (int i=0; i<fuzzedDoubles.size(); i++) {
            s = Double.toString(fuzzedDoubles.get(i) * -1);
            if (s.length() > 3)
                negativeList.add(s.substring(0,3));
            else
                negativeList.add(s);
        }

        inputStrings = positiveList;
        negativeInputStrings = negativeList;
    }

    /* Randomly alter the inputs slightly to retest */
    private void fuzzInputStrings() {
        /* fuzzRange is the amount that we fuzz by which gets larger with each call to the function */

        /* Fuzz the integer array */
        fuzzedInts = new ArrayList<>();
        int iTemp;
        for (int i=0; i<ints.size(); i++) {
            iTemp = ints.get(i);
            if (new Random().nextBoolean()) {
                /* add */
                iTemp = iTemp + (new Random().nextInt(fuzzRange));

            } else {
                /* sub */
                iTemp = iTemp - (new Random().nextInt(fuzzRange));

            }
            fuzzedInts.add(iTemp);

        }

        fuzzedDoubles = new ArrayList<>();
        double dTemp;
        for (int i=0; i<doubles.size(); i++) {
            dTemp = doubles.get(i);
            if (new Random().nextBoolean()) {
                /* add */
                dTemp = dTemp + (fuzzRange * (new Random().nextDouble()));

            } else {
                /* sub */
                dTemp = dTemp - (fuzzRange * (new Random().nextDouble()));

            }
            fuzzedDoubles.add(dTemp);

        }

        fuzzedStrings = new ArrayList<>();
        String sTemp;
        int ascii;
        char c;
        for (int i=0; i<strings.size(); i++) {
            sTemp = strings.get(i);
            for (int j=0; j<sTemp.length(); j++) {

                if (new Random().nextDouble() < 0.2) {
                    /* randomly change some chars in the string */
                    ascii = new Random().nextInt(95) + 32;
                    c = (char) ascii;
                    sTemp = sTemp.substring(0, j) + c + sTemp.substring(j+1);

                }
                if (new Random().nextDouble() < 0.15) {
                    /* randomly remove a char */
                    sTemp = sTemp.substring(0,j) + sTemp.substring(j+1);

                }
                if (new Random().nextDouble() < 0.10){
                    /* randomly add a char */
                    ascii = new Random().nextInt(95) + 32;
                    c = (char)ascii;
                    sTemp = sTemp.substring(0,j) + c + sTemp.substring(j);

                }
            }
            if (sTemp.length() > 3)
                fuzzedStrings.add(sTemp.substring(0, 3));
            else
                fuzzedStrings.add(sTemp);
        }

        fuzzRange += 2;

        /* Put the newly fuzzed values into the input strings */
        createInputStrings();
    }

    /* Generate inputs based on the information that was parsed */
    public String createInput() {
        String input = "";

        int genTestsSize = generalTestCases.size();
        int inputSize = inputStrings.size() + genTestsSize;
        int negInputSize = negativeInputStrings.size() + inputSize;

        if (g < genTestsSize) {
            /* Generalized test cases to try first like 0, null, "", etc*/

            g++;
            return generalTestCases.get(g-1);

        } else if (g < inputSize) {
            /* Source code was analyzed to find integer values and strings that might pose problems */

            g++;
            return inputStrings.get(g-genTestsSize-1);

        } else if (g < negInputSize) {
            /* The integers that we found when we parsed the code are negated and tested as well. (solves + vs - issues) */

            g++;
            return negativeInputStrings.get(g-inputSize-1);

        } else if(g < (negInputSize + conditions.size())) {
            /* Conditional statements were parsed out of the code and here we test to try and solve them */
            Expression see = conditions.get(g - negInputSize);
            if ((see.toString().contains("compareTo") &&
                    see.toString().contains("args[0]") &&
                    ((BinaryExpr)(see)).getRight().toString().equals("0"))
                    || (see.toString().contains("equals") &&
                    see.toString().contains("args[0]"))) {

                Node see1 = see.getChildNodes().get(0).getChildNodes().get(0);
                Node see2 = see.getChildNodes().get(0).getChildNodes().get(2);
                if(see1.toString().equals("args[0]"))
                {
                    g++;
                    if(see2 instanceof BinaryExpr && (!(see2.toString().contains("args[0]")))) // Doesn't have args[0]
                    {
                        String a = solveString((BinaryExpr) see2,"",1); // 1 is for concatenation
                        return a;
                    }
                    else if(see2 instanceof BinaryExpr && see2.toString().contains("args[0]"))// because unsolvable
                    {
                        return null;
                    }
                    String a = see2.toString();
                    return a.substring(1,a.length()-1);
                }
                else if(((see1 instanceof EnclosedExpr) || (see1 instanceof BinaryExpr)) && see1.toString().contains("args[0]"))
                {
                    g++;
                    String a = see2.toString();
                    if(see2 instanceof EnclosedExpr)
                    {
                        a = solveString(((EnclosedExpr) see2).getInner().asBinaryExpr(),"",1);
                    }
                    if(see1 instanceof EnclosedExpr)
                        return solveString(((EnclosedExpr)(see1)).getInner().asBinaryExpr(), a,0);// 0 is for deletion
                    else
                        return solveString((BinaryExpr)(see2), a,0);// 0 is for deletion
                }
                else if(see2.toString().equals("args[0]"))
                {
                    g++;
                    if(see1 instanceof BinaryExpr && (!(see1.toString().contains("args[0]")))) // Doesn't have args[0]
                    {
                        String a = solveString((BinaryExpr) see1,"",1); // 1 is for concatenation
                        return a;
                    }
                    else if(see1 instanceof BinaryExpr && see1.toString().contains("args[0]"))// because unsolvable
                    {
                        return null;
                    }
                    String a = see1.toString();
                    return a.substring(1,a.length()-1);
                }
                else if(((see2 instanceof EnclosedExpr) || (see2 instanceof BinaryExpr)) && (see2.toString().contains("args[0]")))
                {
                    g++;
                    String a = see1.toString();
                    if(see1 instanceof EnclosedExpr)
                    {
                        if(((EnclosedExpr) see1).getInner() instanceof BinaryExpr)
                        {
                            a = solveString(((EnclosedExpr) see1).getInner().asBinaryExpr(), "", 1);
                        }
                        else
                            a = ((EnclosedExpr) see1).getInner().toString();
                    }
                    if(see2 instanceof EnclosedExpr)
                        return solveString(((EnclosedExpr)(see2)).getInner().asBinaryExpr(), a,0);// 0 is for deletion
                    else
                        return solveString((BinaryExpr)(see2), a,0);// 0 is for deletion
                }
            }

            g++;
        }
        else {
            /* Our arrays of inputs should be fuzzed (randomly altered) and tested again */
            fuzzInputStrings();
            g = genTestsSize;
            g++;
            return inputStrings.get(g-genTestsSize-1);
        }


        return input;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Class Fields */
    private final ArrayList<String> generalTestCases = new ArrayList<>(Arrays.asList("0", null, "  ", "", "a", "-1", "a1", "1 a"));
    private int g;
    private int fuzzRange;
    private ArrayList<String> argTypes;
    private ArrayList<String> dependantArgNames;
    private ArrayList<Expression> conditions;
    private ArrayList<DependantArg> dependancies;

    private ArrayList<Integer> ints;
    private ArrayList<Double> doubles;
    private ArrayList<String> strings;

    private ArrayList<String> inputStrings;
    private ArrayList<String> negativeInputStrings;

    private ArrayList<Integer> fuzzedInts;
    private ArrayList<Double> fuzzedDoubles;
    private ArrayList<String> fuzzedStrings;

}