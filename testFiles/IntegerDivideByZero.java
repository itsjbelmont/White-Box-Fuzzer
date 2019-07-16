/* TEST FILE FOR FUZZER */
/*
 *  Divide By Zero Exception when args[0] = "-5"
*/

class IntegerDivideByZero {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 1) {
            return;
        }

        if (!args[0].matches("[+-]?[0-9]+")) {
            return;
        }

        int num = Integer.parseInt(args[0]);
        int div = 10 / (num + 9);

        System.out.println("div = "  + div);
    }
}