package Day3.Holidaytask;
import java.util.*;
public class SubwaySurfers {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        boolean active = true;
        int score = 0;

        while (active) {
            int obstacle = random.nextInt(3);
            int time_limit = random.nextInt(4) + 1;
            String type = "";

            switch (obstacle) {
                case 0:
                    type = "Train";
                    break;
                case 1:
                    type = "Barrier";
                    break;
                case 2:
                    type = "Tunnel";
                    break;
            }

            System.out.println("Obstacle: " + type);
            System.out.println("Time: " + time_limit + "s");
            System.out.print("Action: ");

            long start = System.currentTimeMillis();
            String input = scanner.next().toLowerCase();
            long end = System.currentTimeMillis();

            double duration = (end - start) / 1000.0;

            if (duration > time_limit) {
                System.out.println("Too slow!");
                active = false;
            } else {
                switch (obstacle) {
                    case 0:
                        if (!input.equals("jump")) {
                            active = false;
                        }
                        break;
                    case 1:
                        if (!input.equals("jump")) {
                            active = false;
                        }
                        break;
                    case 2:
                        if (!input.equals("duck")) {
                            active = false;
                        }
                        break;
                }
            }

            if (active) {
                score++;
                System.out.println("Success! Current Score: " + score + "\n");
            } else {
                System.out.println("Game Over!");
                System.out.println("Final Score: " + score);
            }
        }
        scanner.close();
    }
}
