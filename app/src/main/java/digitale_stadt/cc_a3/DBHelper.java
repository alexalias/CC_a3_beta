package digitale_stadt.cc_a3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Anne Lorenz on 27.04.2016.
 *
 * Hier wird eine SQLite Datenbank erzeugt, die eine Tabelle (positions)
 * mit den einzelnen GPS-POsitionen enthält.
 *
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "CycleCity.db";

    private SQLiteDatabase db;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        db = getWritableDatabase();
    }

    public interface DatabaseHandler<T> {
        void onComplete(boolean success, T result);
    }

    private static abstract class DatabaseAsyncTask<T> extends AsyncTask<Void, Void, T> {

        private DatabaseHandler<T> handler;
        private RuntimeException error;

        public DatabaseAsyncTask(DatabaseHandler<T> handler) {
            this.handler = handler;
        }

        @Override
        protected T doInBackground(Void... params) {
            try {
                return executeMethod();
            } catch (RuntimeException error) {
                this.error = error;
                return null;
            }
        }

        protected abstract T executeMethod();

        @Override
        protected void onPostExecute(T result) {
            handler.onComplete(error == null, result);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE positions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "trackId INTEGER," +
                "deviceId TEXT," +
                "timestamp INTEGER," +
                "latitude REAL," +
                "longitude REAL," +
                "altitude REAL," +
                "sent INTEGER)");

        // add dummy gps positions
        db.execSQL("INSERT INTO positions (trackId, deviceId, timestamp, latitude, longitude, altitude, sent)" +
                "VALUES (1, 100, 1451649018000, 53.551085, 9.993682, 0.0, 0), " +
                "(1, 100, 1451649019000, 53.551090, 9.993692, 2.0, 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS positions;");
        onCreate(db);
        Log.d("DBHelper", "DB Upgrade, old version: " + oldVersion +
                ", new version: " + newVersion);
    }

    public void insertPosition(Position position2) {

        ContentValues values = new ContentValues();
        values.put("trackId", position2.getTourID());
        values.put("timestamp", position2.getTime().getTime());
        values.put("latitude", position2.getLatitude());
        values.put("longitude", position2.getLongitude());
        values.put("altitude", position2.getAltitude());
        values.put("sent", position2.getSent());

        db.insertOrThrow("positions", null, values);
    }

    public void insertPositionAsync(final Position position2, DatabaseHandler<Void> handler) {
        new DatabaseAsyncTask<Void>(handler) {
            @Override
            protected Void executeMethod() {
                insertPosition(position2);
                return null;
            }
        }.execute();
    }

    // Gibt die erste Position2 in der Tabelle zurück.
    // TODO: BRAUCHEN WIR SO WAS ÜBERHAUPT???

    public Position selectPosition() {
        Position position2 = new Position();
        Cursor cursor = db.rawQuery("SELECT * FROM positions ORDER BY id DESC LIMIT 1", null);

        try {
            if (cursor.getCount() > 0) {

                cursor.moveToFirst();
                position2.setId(cursor.getLong(cursor.getColumnIndex("id")));
                position2.setTourID(cursor.getString(cursor.getColumnIndex("trackId")));
                position2.setTime(new Date(cursor.getLong(cursor.getColumnIndex("timestamp"))));
                position2.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
                position2.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
                position2.setAltitude(cursor.getDouble(cursor.getColumnIndex("altitude")));

            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
        return position2;
    }

    public void selectPositionAsync(DatabaseHandler<Position> handler) {
        new DatabaseAsyncTask<Position>(handler) {
            @Override
            protected Position executeMethod() {
                return selectPosition();
            }
        }.execute();
    }

    // Gibt eine Liste aller Positionsobjekte zurück,
    // die noch nicht an den Server verschickt worden sind.
    public ArrayList<Position> selectAllPositionsNotSent() {
        ArrayList<Position> positions = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM positions WHERE sent = 0 ORDER BY id", null);

        return EvaluateCursor(cursor);
    }

    public void selectAllPositionsNotSentAsync(DatabaseHandler<Void> handler) {
        new DatabaseAsyncTask<Void>(handler) {
            @Override
            protected Void executeMethod() {
                selectAllPositionsNotSent();
                return null;
            }
        }.execute();
    }

    // Gibt eine Liste aller Positionsobjekte zurück,
    public ArrayList<Position> selectAllPositions() {
        ArrayList<Position> positions = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM positions ORDER BY id", null);

        return EvaluateCursor(cursor);
    }

    public void selectAllPositionsAsync(DatabaseHandler<Void> handler) {
        new DatabaseAsyncTask<Void>(handler) {
            @Override
            protected Void executeMethod() {
                selectAllPositions();
                return null;
            }
        }.execute();
    }

    // Gibt eine Liste aller Positionsobjekte der gegebenen Tour zurück,
    public ArrayList<Position> selectAllPositionsFromTour(int tourID) {

        Cursor cursor = db.rawQuery("SELECT * FROM positions WHERE trackID = " + tourID + " ORDER BY id", null);

        return EvaluateCursor(cursor);
    }

    public void selectAllPositionsFromTourAsync(final int tourID, DatabaseHandler<ArrayList<Position>> handler) {

        new DatabaseAsyncTask<ArrayList<Position>>(handler) {
            @Override
            protected ArrayList<Position> executeMethod() {
                return selectAllPositionsFromTour(tourID);
                //return null;
            }
        }.execute();
    }

    public void deletePosition(long id) {
        if (db.delete("positions", "id = ?", new String[] { String.valueOf(id) }) != 1) {
            throw new SQLException();
        }
    }

    public void deletePositionAsync(final long id, DatabaseHandler<Void> handler) {
        new DatabaseAsyncTask<Void>(handler) {
            @Override
            protected Void executeMethod() {
                deletePosition(id);
                return null;
            }
        }.execute();
    }

    private ArrayList<Position> EvaluateCursor(Cursor cursor)
    {
        ArrayList<Position> positions = new ArrayList<>();
        Position pos;

        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                for (int i = 0; i < cursor.getCount(); i++) {
                    pos = new Position();
                    pos.setId(cursor.getLong(cursor.getColumnIndex("id")));
                    pos.setTourID(cursor.getString(cursor.getColumnIndex("trackId")));
                    pos.setTime(new Date(cursor.getLong(cursor.getColumnIndex("timestamp"))));
                    pos.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
                    pos.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
                    pos.setAltitude(cursor.getDouble(cursor.getColumnIndex("altitude")));

                    positions.add(pos);
                    cursor.moveToNext();
                }
            }
        } finally {
            cursor.close();
        }
        return positions;
    }
}
