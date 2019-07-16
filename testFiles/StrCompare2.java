/* will crash when args[0] == "div" */

class StrCompare2 {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 1) {
            return;
        }

        if ("div".compareTo(args[0]) == 0) {
            System.out.println(" 10 / 0 = " + 10/0);
        }
    }
}
