package net.rutger.home.domain;

public enum WeatherDataType {

    DR(0.1,"Precipitation duration (in 0.1 hour"),
    RH(0.1,"Daily precipitation amount (in 0.1 mm) (-1 for <0.05 mm)"),
    EV24(0.1,"Potential evapotranspiration (Makkink) (in 0.1 mm)"),
    TG(0.1,"Daily mean temperature in (0.1 degrees Celsius)"),
    TN(0.1,"Minimum temperature (in 0.1 degrees Celsius)"),
    TX(0.1,"Maximum temperature (in 0.1 degrees Celsius)"),
    SQ(0.1,"Sunshine duration (in 0.1 hour) calculated from global radiation (-1 for <0.05 hour)"),
    FG(0.1,"Daily mean windspeed (in 0.1 m/s)"),
    SP(1,"Percentage of maximum potential sunshine duration"),
    UG(1,"Percentage of daily mean relative atmospheric humidity");

    private final String description;
    private final double factor;

    WeatherDataType(final double factor, final String desc) {
        this.description = desc;
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }

    public String getDescription() {
        return description;
    }
}
