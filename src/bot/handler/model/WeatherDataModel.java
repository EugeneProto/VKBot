package bot.handler.model;

public class WeatherDataModel {
    /**
     * Parameters for weather forecast request.
     */
    private String city, countryCode,

    /**
     * Marker.
     */
    whatIsNext;

    public WeatherDataModel() {
        whatIsNext="city";
    }

    public void setCity(String city) {
        this.city = city;
        whatIsNext="countryCode";
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String whatIsNext(){
        return whatIsNext;
    }

    public String getCity() {
        return city;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
