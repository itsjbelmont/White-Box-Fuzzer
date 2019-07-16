package bu.ec504.group15.fuzzer;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.filechooser.*;

class Main extends JFrame implements ActionListener {
    private static JLabel label;
    private static String inputFile;

    private Main() {
    }

    /*Main Method which creates the main GUI window
     ***Legacy*** Checks if a file path was passed to main in args[]
     */
    public static void main(String[] args) {

        /*GUI creation
        Consists of a JFrame containing 2 buttons and a label
        One button allows the user to select a file from their computer
        The second button runs the file that the user selected
         */
        JFrame FuzzWin = new JFrame("White Box Fuzzer File Chooser");
        FuzzWin.setSize(450, 150);
        FuzzWin.setVisible(true);
        FuzzWin.setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        Main FileFuzz = new Main();

        //Select File Button to let user select a file
        JButton openButton = new JButton("Select File");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.gridx = 0;
        gbc.gridy = 0;
        pane.add(openButton, gbc);
        openButton.addActionListener(FileFuzz);

        //Label to display path of chosen file
        label = new JLabel("select file...");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 40;
        gbc.weightx = 0.5;
        gbc.gridx = 1;
        gbc.gridy = 0;
        pane.add(label, gbc);

        //Run Fuzzer button to let user run fuzzer on selected file
        JButton runButton = new JButton("Run Fuzzer");
        gbc.weightx = 0.5;
        gbc.gridx = 1;
        gbc.gridy = 1;
        pane.add(runButton, gbc);
        runButton.addActionListener(FileFuzz);

        //Adds panel to frame and displays GUI window
        FuzzWin.add(pane);
        FuzzWin.show();
    }

    /*Defines what occurs on different button presses
    Clicking the "Select File" button will open a file chooser and send the path of the selected file as a string to the JLabel component and to the inputFile variable
    Clicking the "Run Fuzzer" button will pass the inputFile to a new Fuzzer object and call runFuzzer() on the Fuzzer, a new window will then appear with the output of the Fuzzer
     */
    public void actionPerformed(ActionEvent actEvnt) {
        String result;
        String chosenButton = actEvnt.getActionCommand();
        if (chosenButton.equals("Select File")) {
            //Creates a new JFileChooser that allows the user to select a .java file to fuzz
            JFileChooser fileChs = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            fileChs.setDialogTitle("Select a .java file");
            int openVal = fileChs.showOpenDialog(null);
            if (openVal == JFileChooser.APPROVE_OPTION) {
                label.setText(fileChs.getSelectedFile().getAbsolutePath());
                inputFile = fileChs.getSelectedFile().toString();
            } else
                label.setText("file selection was cancelled");
        } else {
            //Create new Fuzzer object and call runFuzzer() on the chosen file
            //checks if a file was chosen and that the file is a .java file before running the Fuzzzer
            if ((!((label.getText().equals("select file...")) || (label.getText().equals("file selection was cancelled")))) && label.getText().substring(label.getText().length() - 5).equals(".java")) {
                Fuzzer fuz = new Fuzzer(inputFile);
                result = fuz.runFuzzer();
                //Create a new window to show the output of the Fuzzer with a GridBagLayout
                JFrame outWin = new JFrame("Output of Fuzzer");
                outWin.setSize(250,350);
                outWin.setVisible(true);
                JPanel outPane = new JPanel(new GridBagLayout());
                GridBagConstraints outGBC = new GridBagConstraints();

                //Text area where the output of the fuzzer will be displayed
                JTextArea outputResult = new JTextArea(result);
                outGBC.fill = GridBagConstraints.HORIZONTAL;
                outGBC.gridwidth = 1;
                outGBC.gridheight = 1;
                outGBC.gridx = 0;
                outGBC.gridy = 0;
                outGBC.ipadx = 250;
                outGBC.ipady = 350;
                outPane.add(outputResult, outGBC);

                //Adds panel to frame and displays new window
                outWin.add(outPane);
                outWin.show();
                label.setText("running Fuzzer on file" + inputFile);
            }
        }
    }
}

//
//
//public class Main {
//
//    public static void main(String[] args) {
//        if (args.length == 0) {
//            System.out.println("Please pass the path to the file to be fuzzed.");
//            return;
//        }
//
//
//        Fuzzer fuz = new Fuzzer(args[0]);
//
//
//
//        System.out.println("\nprogram passed to fuzzer: "+fuz.getProgram());
//        System.out.println(fuz.runFuzzer());
//
//
//    }
//}