/* Will crash if args[0] == '9' */

class IntegerRangeCondition {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 1) {
            return;
        }

        if (!args[0].matches("[+-]?[0-9]+")) {
            return;
        }
        int i = Integer.parseInt(args[0]);
        if (i < 10  && i > 5) {
            System.out.println("div error 10/0 = " + 10/(i-8-1));
        }
    }
}
