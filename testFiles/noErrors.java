/* will not crash */

public class noErrors {

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 1) {
            return;
        }
        System.out.println("Argument -> " + args[0]);
        String s = "I love EC504";
        return;
    }
}
