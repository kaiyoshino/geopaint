package edu.uw.kaiyosh.geopaint;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.ShareActionProvider;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class geopaint_map extends ActionBarActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private boolean pen = true;
    private static GoogleMap mMap;
    private static double lastLat = 0.0;
    private static double lastLng = 0.0;
    private static final String TAG = "** MAP **";
    private int mSelectedColor = Color.RED;
    private List<Polyline> lines = new ArrayList<Polyline>();
    GoogleApiClient mGoogleApiClient;
    private File file;
    private ShareActionProvider share = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geopaint_map);



        if(mGoogleApiClient == null) {
            mGoogleApiClient =
                    new GoogleApiClient.Builder(this)
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build(); //build me the client already dammit!
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);
    }

    /** Helper method for getting location **/
    public void getLocation(View v){
        if(mGoogleApiClient != null) {
            Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(loc != null) {
                geopaint_map.lastLat = loc.getLatitude();
                geopaint_map.lastLng = loc.getLongitude();
            }else
                Log.v(TAG, "Last location is null");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();


    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //when API has connected!x`
        getLocation(null);

        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);

        share= (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
//        share.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
//            @Override
//            public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
//                handleSaveFile(saveDrawing());
//                Intent intentSend = new Intent(Intent.ACTION_SEND);
//                intentSend.setType("text/plain");
//                intentSend.putExtra(Intent.EXTRA_EMAIL, "emailaddress@emailaddress.com");
//                intentSend.putExtra(Intent.EXTRA_SUBJECT, "Subject");
//                intentSend.putExtra(Intent.EXTRA_TEXT, "Here is a geojson file of my Geopaint painting!");
//                intentSend.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//                share.setShareIntent(intentSend);
//                return true;
//            }
//        });
        Intent intentSend = new Intent(Intent.ACTION_SEND);
        intentSend.setType("text/plain");
        intentSend.putExtra(Intent.EXTRA_EMAIL, "emailaddress@emailaddress.com");
        intentSend.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        intentSend.putExtra(Intent.EXTRA_TEXT, "Here is a geojson file of my Geopaint painting!");
        share.setShareIntent(intentSend);
        Intent chooser = Intent.createChooser(intentSend, "Share File");
        if (intentSend.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_pen:
                if (pen) {
                    Toast.makeText(getApplicationContext(), "Painting Turned Off", Toast.LENGTH_SHORT).show();
                    pen = false;
                } else {
                    Toast.makeText(getApplicationContext(), "Painting Turned On", Toast.LENGTH_SHORT).show();
                    pen = true;
                }
                return true;
            case R.id.menu_color:
                Log.v(TAG, "Map menu color item");
                int[] mColors = getResources().getIntArray(R.array.default_rainbow);
                ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                        mColors,
                        mSelectedColor,
                        5, // Number of columns
                        ColorPickerDialog.SIZE_SMALL);

                dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        mSelectedColor = color;
                    }
                });
                dialog.show(getFragmentManager(), "color_dialog_test");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng seattle = new LatLng(47.6550, -122.3080);
        mMap.addMarker(new MarkerOptions().position(seattle).title("Marker in UW"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seattle));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.v(TAG, "location change");
        if (geopaint_map.mMap != null) {
            if (pen) {
                Log.v(TAG, "line!");
                Polyline line = geopaint_map.mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(geopaint_map.lastLat, geopaint_map.lastLng), new LatLng(location.getLatitude(), location.getLongitude()))
                        .width(5)
                        .color(mSelectedColor));
                lines.add(line);
                handleSaveFile(saveDrawing());
                Intent intentSend = new Intent(Intent.ACTION_SEND);
                intentSend.setType("text/plain");
                intentSend.putExtra(Intent.EXTRA_EMAIL, "emailaddress@emailaddress.com");
                intentSend.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                intentSend.putExtra(Intent.EXTRA_TEXT, "Here is a geojson file of my Geopaint painting!");
                intentSend.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                share.setShareIntent(intentSend);
            }
            geopaint_map.lastLat = location.getLatitude();
            geopaint_map.lastLng = location.getLongitude();

        } else {
            Log.v(TAG, "null map");
        }
    }

    public String saveDrawing() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ \"type\": \"FeatureCollection\", " +
                "\"features\": [");
        for (Polyline line : lines) {
            builder.append("{ \"type\": \"Feature\", " +
                    "        \"geometry\": { \"type\": \"LineString\", \"coordinates\": ");
            List<LatLng> points = line.getPoints();
            LatLng l1 = points.get(0);
            LatLng l2 =points.get(1);
            builder.append("[[" + l1.longitude + ", " + l1.latitude + "], [ " + l2.longitude + ", " + l2.latitude + "]]");
            builder.append("}, \"properties\": { \"color\" : \"" + line.getColor() + "\"}");
            builder.append("},");
        }
        // removes the last , at the very end
        builder = builder.deleteCharAt(builder.length() - 1);
        builder.append("]}");
        return builder.toString().replace("lat/lng:", "").replace("(", "[").replace(")", "]");
    }

    public void handleSaveFile(String drawing){
        Log.v(TAG, "Save button clicked");

        if(isExternalStorageWritable()){

            try {
                file = new File(this.getExternalFilesDir(null), "drawing.geojson");
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(drawing.getBytes()); //write the string to the file
                outputStream.close(); //close the stream

//                File dir; //where to save stuff
//                //public external
//                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                //dir = Environment.getExternalStorageDirectory() //private external
//                //dir = getFilesDir(); //internal file storage
//                //dir = getCacheDir(); //internal cache
//                //dir = getExternalCacheDir(); //external cache
//
//                File file = new File(dir, "myFile.txt");
//
//                FileOutputStream outputStream = new FileOutputStream(file);
//
//                //outputStream = openFileOutput("myFile.txt", MODE_PRIVATE); //internal storage
//
//
//                String message = "Hello file!";
//
//                outputStream.write(message.getBytes());
//
//                outputStream.close();
//                Log.v(TAG, "File written");
            }
            catch(IOException ioe){
                ioe.printStackTrace();
            }



        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}
