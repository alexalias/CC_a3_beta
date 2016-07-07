package digitale_stadt.cc_a3;

/**
 * Created by lorcan on 06.07.2016.
 */
public class TourData {
    public double distance_km;
    public long duration_ms;
    public double currentSpeed_kmh;
    public double averageSpeed_kmh;

    public TourData() {
        distance_km = 0;
        duration_ms = 0;
        currentSpeed_kmh = 0;
        averageSpeed_kmh = 0;
    }

    public TourData(double distance_km, long duration_ms, double currentSpeed_kmh, double averageSpeed_kmh) {
        this.distance_km = distance_km;
        this.duration_ms = duration_ms;
        this.currentSpeed_kmh = currentSpeed_kmh;
        this.averageSpeed_kmh = averageSpeed_kmh;
    }
}
