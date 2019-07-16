/* program will crash with args[0] == "98_" */

class StrConcat1 {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 1){
            return;
        }

        if ((args[0]+"hi").compareTo("98_hi") == 0) {
            System.out.println("crash" + 10/0);
        }
    }
}