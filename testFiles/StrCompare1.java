/* will crash when args[0] == "hi" */

class StrCompare1 {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 1) {
            return;
        }

        if (args[0].compareTo("hi") == 0) {
            System.out.println(" 10 / 0 = " + 10/0);
        }
    }
}
