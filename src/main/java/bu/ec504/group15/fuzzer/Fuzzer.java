package bu.ec504.group15.fuzzer;

import java.lang.Process;

public class Fuzzer {

    Fuzzer() {
        program = null;
    }

    Fuzzer(String prog) {
        program = prog;
        fileName = prog;
    }

    Fuzzer(String prog, String regExpression) {
        program = prog;
        fileName = prog;
        regExp = regExpression;
    }

    public void setRegExp(String regExpression) {
        regExp = regExpression;
    }

    public void setProgram(String prog) {
        program = prog;
        fileName = prog;
    }

    public String getRegExp() {
        return regExp;
    }

    public String getProgram() {
        return program;
    }


    public String runFuzzer() {
        String fuzzResults = "";
        String input = "";
        String pOut = "";
        int exitCode = 0;
        boolean copyFile = false;

        /* Make sure a program file was passed. */
        if (program == null) {
            return "Fuzzer can not run without a valid file input.\n Please specify an input file!";
        }

        /* Make sure a java program was passed */
        if (!program.substring(program.length() - 5).equals(".java")) {
            return "Source file is not a .java file!";
        }

        /* Move the file to be analyzed into this directory */
        try {
            for (int i=0; i<program.length(); i++) {
                if (program.charAt(i) == '/') {
                    copyFile = true;
                    break;
                }
            }
            if (copyFile) {
                String moveArgs[] = {"cp", program, "."};
                Process copyToWorkingDirectory = Runtime.getRuntime().exec(moveArgs);
                copyToWorkingDirectory.waitFor();
                if (copyToWorkingDirectory.exitValue() != 0) {
                    return "Couldnt put file into working directory";
                } else {
                    /* trim /path/to/file.java down to file.java */
                    int index = 0;
                    for (int i = program.length() - 1; i >= 0; i--) {
                        if (program.charAt(i) == '/') {
                            index = i;
                            break;
                        }
                    }
                    if (index != 0) {
                        program = program.substring(index+1);
                    }
                }
            }
        } catch (Exception e) {

        }

        /* Compile the test program into a .class so we can run it */
        try {
            String compileArgs[] = {"javac", program};
            Process compile = Runtime.getRuntime().exec(compileArgs);
            compile.waitFor();

            /* If the file did not compile we can not continue.  */
            if (compile.exitValue() != 0) {
                return "Error compiling the java program";
            } else {
                /* Trim .java off the program name for exec'ing it */
                /* to run testFile.java: 'java testFile argument' */
                program = program.substring(0, program.length() - 5);
            }
        } catch (Exception e) {
            return "Exception thrown during compilation of the program:\n" + e.toString();
        }

        /* Create the Input Generator */
        InputGenerator inputGenerator = new InputGenerator(fileName);
        //inputGenerator.displayParsedFileInfo();

        /* Repeatedly run the program with a fuzzed input */
        String arg[] = {"java", program, null};
        String argNull[] = {"java", program};
        Process p;
        final long NANOSECONDS_IN_SECOND = 1_000_000_000;
        final long startTime = System.nanoTime();
        do { /* repeatedly run the program until 5 minutes have passed or we have found an error */
            try {
                input = inputGenerator.createInput();
                if (input == null){
                    p = Runtime.getRuntime().exec(argNull);
                } else {
                    arg[2] = input;
                    p = Runtime.getRuntime().exec(arg);
                }
                p.waitFor();
                exitCode = p.exitValue();
                /*
                BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((pOut = is.readLine()) != null) {
                    System.out.println(pOut);
                }
                */
            } catch (Exception e) {
                /*Handle exception*/

                /* we will be returning so lets clean up the files in the working directory */
                try {
                    String cleanArgs[] = {"rm", program + ".class"};
                    Process clean = Runtime.getRuntime().exec(cleanArgs);
                    clean.waitFor();

                    /* Remove copied .java file if we found a problem */
                    if (copyFile) {
                        String cleanCopyArgs[] = {"rm", program + ".java"};
                        Process cleanCopy = Runtime.getRuntime().exec(cleanCopyArgs);
                        cleanCopy.waitFor();
                    }
                } catch (Exception e2) { }

                return "Exception thrown while running the program with input: " + input + " \n" + e.toString();
            }

            /* Run for 5 minutes or until an error is found in the source program */
        } while (exitCode == 0 && ((System.nanoTime()-startTime) < 5*60*NANOSECONDS_IN_SECOND));

        /* Did we find an error, or did we fuzz without any errors being found? */
        if (exitCode != 0)
            fuzzResults = "Bug found with input: '" + input + "'";
        else
            fuzzResults = "Program fuzzed successfully. No bugs found.";

        /* remove the .class function created from compilation
         *  and possibly the .java file if it was coppied into the working directory*/
        try {
            String cleanArgs[] = {"rm", program + ".class"};
            Process clean = Runtime.getRuntime().exec(cleanArgs);
            clean.waitFor();

            /* Remove copied .java file if we found a problem */
            if (copyFile) {
                String cleanCopyArgs[] = {"rm", program + ".java"};
                Process cleanCopy = Runtime.getRuntime().exec(cleanCopyArgs);
                cleanCopy.waitFor();
            }
        } catch (Exception e) { }
        return fuzzResults;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Fields */
    private String program = null;
    private String fileName;
    private String regExp = null;
}