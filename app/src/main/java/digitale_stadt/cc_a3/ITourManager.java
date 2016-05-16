package digitale_stadt.cc_a3;

import android.location.Location;

/**
 * Created by Ralf Engelken on 16.05.2016.
 * Interface for the TourManager
 */
 interface ITourManager {
    //creates an empty tour with a new tour-id
     void StartNewTour();
     void StopTour();
     void ClearWayPoints();

    //gets/sets the number of waypoints to be collected before sending the tour
     void SetQueueLength(int queueLength);
     int GetQueueLength();

    //gets/sets flag to send data immediately or when connected to WLAN
     void SetSendDirect(boolean sendDirect);
     boolean GetSendDirect();

    //adds the given location to the tour
     void AddWayPoint(Location location);

    //returns the actual tour data
     Tour GetTour();
     Location GetStartLocation();
     Location GetLastLocation();

     long GetDuration_ms();
     double GetDistance_km();
     double GetAvgSpeed_kmh();

    //loads/saves the actual tour data to the database/ sends tour data to theserver
     boolean LoadTourDataFromDB(String tourID);
     boolean SaveTourToDB();
     boolean SendTourToServer();
}
