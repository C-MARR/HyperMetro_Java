package metro;

public class Main {
    public static void main(String[] args) {
        String fileName = args[0];
        MetroSystem metroSystem = new MetroSystem(fileName);
        boolean programClosed = false;
        while (!programClosed) {
            programClosed = metroSystem.menu();
        }
    }
}
