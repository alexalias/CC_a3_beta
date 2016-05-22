package digitale_stadt.cc_a3;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class AnzeigeTafelFragment extends Fragment {

//    private OnFragmentInteractionListener mListener;

    TourManager _tourManager;
    private long dauer;
    private String startZeitpunkt;
    private double geschwindigkeit;
    private double zurückgelegteStrecke;

    // Required empty public constructor
    //Variabeln für die AnzeigeTafel, die Daten aus dem TourManager auslesen
    public AnzeigeTafelFragment() {

        _tourManager = new TourManager(getActivity(), "001");
        dauer = _tourManager.GetDuration_ms();
        startZeitpunkt = _tourManager.GetStartTime();
        geschwindigkeit =_tourManager.GetAvgSpeed_kmh();
        zurückgelegteStrecke = _tourManager.GetDistance_km();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //ToDo: Die Daten sollten sich regelmäßig aktualisieren;
        //ToDo: Die Daten aus dem TourManager sind falsch! Warum?
        String [] trackInfoArray = {
                "Startzeitpunkt: " + startZeitpunkt + " Uhr",
                "Bisherige Dauer: "+ dauer + " Std",
                "Geschwindigkeit: " + geschwindigkeit + " km/h",
                "Zurückgelegte Strecke: " + zurückgelegteStrecke + " km"
        };
        List<String> trackInfoList = new ArrayList<>(Arrays.asList(trackInfoArray));

        //Unser Adapter für das ListView
        ArrayAdapter<String> trackInfoAdapter = new ArrayAdapter<String>(
                getActivity(), //Liefert den Context (diese Activity)
                R.layout.list_item_anzeige, //Die Layout-Datei, wo die Liste sich befindet
                R.id.list_item_anzeige_textview, // Unser Layout für jede Zeile der Liste
                trackInfoList  //Die Daten, die später in die Liste angezeigt werden
        );

        //Die View-Hierarchie aus unserem Fragment. Erlaubt uns nach dem ListView zu suchen.
        View rootView = inflater.inflate(R.layout.fragment_anzeige_tafel, container, false);

        ListView trackInfoListView = (ListView) rootView.findViewById(R.id.listView_anzeigeTafel);
        trackInfoListView.setAdapter(trackInfoAdapter);  //Binden des Adapters an das ListView

        return rootView;
    }
//
//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p/>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        public void onFragmentInteraction(Uri uri);
//    }

}
