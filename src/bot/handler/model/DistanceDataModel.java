package bot.handler.model;

public class DistanceDataModel {
    /**
     * Parameters for distance count request.
     */
    private String city, origin, destination,

    /**
     * Marker.
     */
    whatIsNext;

    public DistanceDataModel() {
        whatIsNext="city";
    }

    public void setCity(String city) {
        this.city = city;
        whatIsNext="origin";
    }

    public void setOrigin(String origin) {
        this.origin = origin;
        whatIsNext="destination";
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCity() {
        return city;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String whatIsNext() {
        return whatIsNext;
    }
}
