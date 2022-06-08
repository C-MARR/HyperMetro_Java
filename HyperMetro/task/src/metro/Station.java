package metro;

public class Station {

    final String name;
    String prevStation = null;
    String nextStation = null;
    Boolean isHead = false;
    int stationOrder = -1;
    String line = null;

    public Station(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s",
                prevStation == null ? "depot" : prevStation,
                name,
                nextStation == null ? "depot" : nextStation);
    }
}
