package metro;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class MetroSystem {

    String fileName;
    LinkedList<LinkedList<Station>> metroLines;

    public MetroSystem(String fileName) {
        this.fileName = fileName;
        this.metroLines = readStationsFile();
    }

    private LinkedList<LinkedList<Station>> readStationsFile() {
        try (
                FileReader fr = new FileReader(fileName);
                BufferedReader br = new BufferedReader(fr)
        ) {
            LinkedList<LinkedList<Station>> lines = new LinkedList<>();
            JsonElement jsonElement = JsonParser.parseReader(br);
            JsonObject linesFromJson = jsonElement.getAsJsonObject();
            linesFromJson.entrySet().forEach(line -> {
                String lineName = line.getKey();
                LinkedList<Station> stations = new LinkedList<>();
                JsonObject stationsFromJson = line.getValue().getAsJsonObject();
                stationsFromJson.entrySet().forEach(station -> {
                    Station newStation = new Station(station.getValue().getAsJsonObject().get("name").getAsString());
                    newStation.line = lineName;
                    newStation.stationOrder = Integer.parseInt(station.getKey());
                    if (newStation.stationOrder == 1) {
                        newStation.isHead = true;
                    }
                    JsonElement transfer = station.getValue().getAsJsonObject().get("transfer");
                    if (!transfer.getAsJsonArray().isEmpty()) {
                        transfer.getAsJsonArray().forEach(element -> {
                            newStation.transferLine = element.getAsJsonObject().get("line").getAsString();
                            newStation.transferStation = element.getAsJsonObject().get("station").getAsString();
                        });
                    }
                    stations.add(newStation);
                });
                stations.sort(Comparator.comparingInt(station -> station.stationOrder));
                stations.forEach(station -> {
                    if (!station.isHead) {
                        station.prevStation = stations.get(station.stationOrder - 2).name;
                    }
                    if (station.stationOrder != stations.size()) {
                        station.nextStation = stations.get(station.stationOrder).name;
                    }
                });
                lines.add(stations);
            });
            return lines;
        } catch (Exception e) {
            System.out.println("Error! Such a file doesn't exist!");
            return new LinkedList<>();
        }
    }

    public boolean menu() {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if (input.equals(MenuCommands.EXIT.getCommand())) {
            System.out.println("Bye!");
            return true;
        } else {
            String command = input.replaceFirst(" \\S.+", "");
            if (Arrays.stream(MenuCommands.values()).noneMatch(c -> command.equals(c.getCommand()))) {
                MenuCommands.invalidCommandError();
                return false;
            }
            String lineName = input.split("\"")[1];
            if (command.equals(MenuCommands.OUTPUT.getCommand())) {
                printStationList(lineName);
            } else if (command.equals(MenuCommands.CONNECT.getCommand())) {
                if (!input.matches("^\\S+ \"[\\S ]+\" \"[\\S ]+\" \"[\\S ]+\" \"[\\S ]+\"*")) {
                    MenuCommands.invalidCommandError();
                } else {
                    var transferStations = input.replaceFirst("^\\S+ \"", "").split("\" \"");
                    String lineToConnect = transferStations[0];
                    String stationToConnect = transferStations[1];
                    String transferLine = transferStations[2];
                    String transferStation = transferStations[3].replace("\"", "");
                    if (!connectStations(lineToConnect, stationToConnect, transferLine, transferStation)) {
                        MenuCommands.invalidCommandError();
                    }
                }
            } else {
                String stationName = input.replaceFirst("^\\S+ \"[\\S ]+\" ", "").replaceAll("\"", "");
                if (command.equals(MenuCommands.ADD_HEAD.getCommand())) {
                    addHeadStation(lineName, stationName);
                } else if (command.equals(MenuCommands.APPEND.getCommand())) {
                    appendStation(lineName, stationName);
                } else if (command.equals(MenuCommands.REMOVE.getCommand())) {
                    removeStation(lineName, stationName);
                }
            }
        }
        return false;
    }

    private void addHeadStation(String lineName, String stationName) {
        metroLines.forEach(line -> {
            if (line.getFirst().line.equals(lineName)) {
                Station previousHeadStation = line.getFirst();
                previousHeadStation.isHead = false;
                Station newHeadStation = new Station(stationName);
                newHeadStation.nextStation = previousHeadStation.name;
                newHeadStation.line = lineName;
                previousHeadStation.prevStation = newHeadStation.name;
                newHeadStation.stationOrder = 0;
                newHeadStation.isHead = true;
                line.addFirst(newHeadStation);
                line.forEach(station -> station.stationOrder++);
            }
        });
    }

    private void appendStation(String lineName, String stationName) {
        metroLines.forEach(line -> {
            if (line.getFirst().line.equals(lineName)) {
                Station lastStation = line.getLast();
                Station newStation = new Station(stationName);
                lastStation.nextStation = stationName;
                newStation.prevStation = lastStation.name;
                newStation.line = lineName;
                newStation.stationOrder = lastStation.stationOrder + 1;
                line.add(newStation);
            }
        });
    }

    private boolean connectStations(String lineToConnect, String stationToConnect
            , String transferLine, String transferStation) {
        Station station1 = null;
        Station station2 = null;
        for (LinkedList<Station> line : metroLines) {
            if (line.getFirst().line.equals(lineToConnect) || line.getFirst().line.equals(transferLine)) {
                for (Station station : line) {
                    if (station.name.equals(stationToConnect) && station.line.equals(lineToConnect)) {
                        station1 = station;
                    }
                    if (station.name.equals(transferStation) && station.line.equals(transferLine)) {
                        station2 = station;
                    }
                }
            }
        }
        if (station1 != null && station2 != null) {
            station1.transferLine = station2.line;
            station1.transferStation = station2.name;
            station2.transferLine = station1.line;
            station2.transferStation = station1.name;
            return true;
        }
        return false;
    }

    private void printStationList(String line) {
        for (LinkedList<Station> metroLine : metroLines) {
            if (metroLine.getFirst().line.equals(line)) {
                metroLine.forEach(System.out::println);
            }
        }
    }

    private void removeStation(String lineName, String stationName) {
        metroLines.forEach(line -> {
            boolean stationRemoved = false;
            if (line.getFirst().line.equals(lineName)) {
                for (int i = 0; i < line.size(); i++) {
                    Station station = line.get(i);
                    if (station.name.equals(stationName)) {
                        Station previousStation = null;
                        Station nextStation = null;
                        if (i + 1 < line.size()) {
                            nextStation = line.get(i + 1);
                        }
                        if (!station.isHead) {
                            previousStation = line.get(i - 1);
                            if (nextStation != null) {
                                previousStation.nextStation = nextStation.name;
                            } else {
                                previousStation.nextStation = null;
                            }
                        } else {
                            if (nextStation != null) {
                                nextStation.isHead = true;
                                nextStation.prevStation = null;
                            }
                        }
                        if (nextStation != null) {
                            if (i + 1 == line.size() - 1) {
                                nextStation.nextStation = null;
                            }
                            if (previousStation != null) {
                                nextStation.prevStation = previousStation.name;
                            }
                        }
                        line.remove(station);
                        i--;
                        stationRemoved = true;
                    } else if (stationRemoved) {
                        station.stationOrder--;
                    }

                }
            }
        });
    }
}

