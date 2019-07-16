/* Will crash for args[0] == '7' */
/* Based on our implementation there is no guarantee this error
*  will be found but it is probable that it will */

class IntegerRangeCondition2 {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 1) {
            return;
        }

        if (!args[0].matches("[+-]?[0-9]+")) {
            return;
        }
        int i = Integer.parseInt(args[0]);
        System.out.println("div error 10/0 = " + 10/ ((i*2) - 14));

    }
}