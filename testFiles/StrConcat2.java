/* program will crash with args[0] == "ab1" */

class StrConcat2 {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 1){
            return;
        }

        if ("ab1wow".compareTo(args[0] + "wow") == 0) {
            System.out.println("crash" + 10/0);
        }
    }
}