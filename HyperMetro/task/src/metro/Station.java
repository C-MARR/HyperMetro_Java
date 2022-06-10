package metro;

public class Station {

    final String name;
    String line = null;
    Boolean isHead = false;
    String prevStation = null;
    String nextStation = null;
    int stationOrder = -1;
    String transferStation = null;
    String transferLine = null;

    public Station(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return (prevStation == null ? "depot\n" : "")
                + name + (transferStation != null && transferLine != null
                    ? String.format(" - %s (%s)", transferStation, transferLine) : "")
                + (nextStation == null ? "\ndepot" : "");
    }
}
