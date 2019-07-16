/* crash if args[0] == "999" */

class Find999 {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 1) {
            return;
        }
        if (!args[0].matches("[+-]?[0-9]+")) {
            return;
        }

        System.out.println(args[0]);

        int i = Integer.parseInt(args[0]);
        if (i > 998 && i < 1000) {
            System.out.println("crash"+ 10/0);
        }
    }
}