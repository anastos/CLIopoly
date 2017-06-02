import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Mark on 11/7/15.
 */

class MainCLI {

    private static boolean running;

    private static Map<String, Runnable> commands =
            new HashMap<String, Runnable>() {{
                put("help", () -> {
                    String help =
                            "Commands:\n" +
                            "monopoly -- play the game.\n" +
                            "exit     -- quit the program.";
                    System.out.println(help);
                });
                put("monopoly", Monopoly::new);
                put("exit", () -> running = false);
            }};

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        running = true;
        //Keep interpreting commands until the program is exited.
        while (running) {
            Scanner line = new Scanner(s.nextLine());
            //If there is a command to interpret...
            if (line.hasNext()) {
                String cmd = line.next();
                //If this command has been defined in "commands"...
                if (commands.containsKey(cmd))
                    commands.get(cmd).run();
                else
                    System.out.println("Command not recognized.");
            }
        }
    }

}