package com.guitarnotes.a48k.fyp;

import android.Manifest;
//import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
//import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.text.Layout;
import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
//import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.maps.android.SphericalUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapActivity extends FragmentActivity implements PlaceSelectionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener, View.OnClickListener, OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private static final String TAG = "MapActivity";
    private static final String FineLocation = Manifest.permission.ACCESS_FINE_LOCATION;//fine location permissions
    private static final String CoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;//coarse location permissions
    private static final int LocationPermissionReqCode = 1111;//error code for debug purpose
    private static final float DefaultZoom = 16f;//camera default zoom value
    //private  static final LatLngBounds latlong_bounds= new LatLngBounds(new LatLng(-40,-168), new LatLng(71, 136));//encompass the entire world
    private double lati, longi;//from latlong
    private double tolati, tolongi;//end latlong

    //drawer list view declaration
    private DrawerLayout drawerLayout;
    private ListView drawerlistview;
    private ArrayAdapter<String> drawerAdapter;

    //spinner dropdown section declaration
    private Spinner dropdown;
    private static final String[] maptypes = {"Normal", "Satellite", "Hybrid"};

    //plotting marker in map section
    ArrayList markerPoints = new ArrayList();
    LatLng start,end;

    //variables
    private boolean permissioncheck = false;
    private GoogleMap gmap;
    private FusedLocationProviderClient mLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    private ImageView currentgps;

    @Override//google connection failure
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //ready the google maps fragment
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is being deployed", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onMapReady: Map Ready");//log
        gmap = googleMap;

        gmap.setOnMarkerDragListener(this);//map marker drag listener
        gmap.setOnMapLongClickListener(this);//map long tap listener
/*
        //onclicklistener for login
        findViewById(R.id.hybridmap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gmap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

        //onclicklistener for satellite map
        findViewById(R.id.satellite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gmap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
        });

        //onclicklistener for reset map type
        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });
*/
        //setting onclick listener for maps
        gmap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                findViewById(R.id.placedetails).setVisibility(View.INVISIBLE);//disable show path button

                //clear  markers
                gmap.clear();

                //Adding marker to the current long pressed position, green marker
                gmap.addMarker(new MarkerOptions().position(latLng).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                tolati = latLng.latitude;//latitude
                tolongi = latLng.longitude;//longitude

                LatLng start = new LatLng(lati,longi);//get start coordinates
                LatLng end = latLng;//get end coordinates

                Double dis = SphericalUtil.computeDistanceBetween(start, end);//calculate distance between 2 points
                Toast.makeText(MapActivity.this,"Distance: " + dis + " meters.",Toast.LENGTH_SHORT).show();

                findViewById(R.id.btn_path).setVisibility(View.VISIBLE);//enabling show path button
                findViewById(R.id.btn_path).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LatLng end = new LatLng(tolati,tolongi);//get end coordinates
                        plottpath(end);
                    }
                });
            }
        });

        //get users' current location at the beginning of the application
        if (permissioncheck) {
            DeviceCurrentLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            //permissions required for setmylocationenabled(true)
            gmap.setMyLocationEnabled(true);
            gmap.getUiSettings().setMyLocationButtonEnabled(false);

            //setting up autocomplete fragment onselectlistener method
            PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    // TODO: Get info about the selected place.
                    Log.i(TAG, "Place: " + place.getName());
                    Toast.makeText(MapActivity.this, "Locating...", Toast.LENGTH_SHORT).show();//popup toast

                    gmap.clear();
                    gmap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
                    //gmap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                    gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16f));

                    //details for selected place
                    try {
                        Toast.makeText(MapActivity.this, "Location: " + place.getName(), Toast.LENGTH_SHORT).show();

                        //calculate distance to the place
                        tolati=place.getLatLng().latitude;
                        tolongi=place.getLatLng().longitude;
                        LatLng start = new LatLng(lati,longi);//get start coordinates
                        LatLng end = new LatLng(tolati,tolongi);//get end coordinates

                        Double dis = SphericalUtil.computeDistanceBetween(start, end);//calculate distance between 2 points

                        findViewById(R.id.btn_path).setVisibility(View.INVISIBLE);//disabling show path button

                        //placing place details to the text view box
                        findViewById(R.id.placedetails).setVisibility(View.VISIBLE);
                        Button textbox = (Button) findViewById(R.id.placedetails);
                        textbox.setText(place.getName()
                                + "\nPlace Rating: " + place.getRating()
                                + "\nContact Number: " + place.getPhoneNumber()
                                + "\nPlace Site:" + place.getWebsiteUri()
                                + "\nDistance: " + dis + "m.");

                        Context context = getApplicationContext();
                        writehistory(place, context);//updating search history file

                        findViewById(R.id.placedetails).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LatLng end = new LatLng(tolati,tolongi);//get end coordinates
                                plottpath(end);
                            }
                        });

                    } catch (NullPointerException e) {
                        Log.d(TAG, "onResult: Null Pointer Exception: " + e.getMessage());
                    }
                }
                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    Log.i(TAG, "An error occurred: " + status);
                }
            });

            initialize();//calling the 'Enter' key override method for the searching
        }
    }


    //draw path from the destination to the point
    private void plottpath(LatLng latLng) {
        //allow just one marker in the map
        if (markerPoints.size() > 0) {
            markerPoints.clear();
            gmap.clear();
        }

        //adding new item to the map position ArrayList
        markerPoints.add(latLng);

        //creating marker
        MarkerOptions markerOptions = new MarkerOptions();

        //setting the position of the marker, green marker
        markerOptions.position(latLng).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        //add new marker to the Google Map Android API V2
        gmap.addMarker(markerOptions);

        //check for start and end locations
        if (markerPoints.size() >= 1) {
            start = new LatLng(lati,longi);
            end = new LatLng(tolati,tolongi);

            //getting URL to the Google Directions API
            String url = getDirectionsUrl(start, end);

            DownloadTask downloadTask = new DownloadTask();

            //start downloading json data from Google Directions API
            downloadTask.execute(url);

            //Double distance = SphericalUtil.computeDistanceBetween(start, end);
            //Toast.makeText(MapActivity.this, String.valueOf(distance + " Meters"), Toast.LENGTH_LONG).show();
        }
    }


    @Override//check location permissions
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //initialize side drawer
        drawerlistview = (ListView)findViewById(R.id.navList);
        addDrawerItems();

        // obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //initializing googleapi client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        currentgps = (ImageView)findViewById(R.id.ic_current);//current location redirection button

        //spinner drop down spinner initialization
        dropdown = (Spinner)findViewById(R.id.maps_type);
        ArrayAdapter<String>adapter = new ArrayAdapter<String>(MapActivity.this,
                android.R.layout.simple_spinner_item,maptypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);

        getlocationpermission();//check for location permissions
    }


    //initialize drawer section
    private void addDrawerItems() {
        String[] navlistitem = { "Share Location", "Share Search Location", "Receive Location", "Search History", "Login" };
        drawerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, navlistitem);
        drawerlistview.setAdapter(drawerAdapter);

        drawerlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                DrawerLayout drawerLayout = findViewById(R.id.maplayout);

                if (id == 0){
                    drawerlistview.setItemChecked(position,true);
                    drawerLayout.closeDrawers();//closing drawer

                    Toast.makeText(MapActivity.this, lati + ", " + longi + ".\nDevice location copied to clipboard", Toast.LENGTH_SHORT).show();

                    //copying lati longi to clipboard for sharing
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);//clipboard initialize
                    ClipData clip = ClipData.newPlainText("Device location latitude longitude ", lati + "," + longi);
                    clipboard.setPrimaryClip(clip);//set the clipboard's primary clip.
                }
                if (id == 1){
                    drawerlistview.setItemChecked(position,true);
                    drawerLayout.closeDrawers();//closing drawer

                    Toast.makeText(MapActivity.this, tolati + ", " + tolongi + ". \nCopied to clipboard." , Toast.LENGTH_SHORT).show();

                    //copying lati longi to clipboard for sharing
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);//clipboard initialize
                    ClipData clip = ClipData.newPlainText("Searched location latitude longitude ", tolati + "," + tolongi);
                    clipboard.setPrimaryClip(clip);//set the clipboard's primary clip.
                }
                if (id == 2){
                    drawerlistview.setItemChecked(position,true);
                    drawerLayout.closeDrawers();//closing drawer

                    Toast.makeText(MapActivity.this, "Paste location coordinates you recieved to the input dailog box...", Toast.LENGTH_SHORT).show();

                    findViewById(R.id.dailoginput).setVisibility(View.VISIBLE);//enabling input dailog
                    AutoCompleteTextView autoCompleteTextView = findViewById(R.id.input_latlng);
                    autoCompleteTextView.setText("");//clearing input field
                }
                if (id == 3){
                    drawerlistview.setItemChecked(position,true);
                    drawerLayout.closeDrawers();//closing drawer

                    //starting mapactivity
                    Intent intent = new Intent(MapActivity.this, SearchHistory.class);
                    startActivity(intent);//running the map class from the button click
                }
                if (id == 4){
                    drawerLayout.closeDrawers();//closing drawer

                    Toast.makeText(MapActivity.this,"Google Sign in feature coming soon in the near future.\nStay tuned!", Toast.LENGTH_SHORT).show();
                    /*
                    Toast.makeText(MapActivity.this, "Loading login page...", Toast.LENGTH_SHORT).show();
                    //starting login activity
                    Intent intent = new Intent(MapActivity.this, MenuActivity.class);
                    startActivity(intent);//running the menu class from the list item click
                    */
                }

            }
        });

        //initialize input dailog section
        findViewById(R.id.btn_input).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView input = findViewById(R.id.input_latlng);
                String[] points = input.getText().toString().split(",");
                try{
                    Toast.makeText(MapActivity.this,"Coordinates received:\n" + points[0] + ", " + points[1],Toast.LENGTH_SHORT).show();

                    LatLng recpoints = new LatLng(Float.parseFloat(points[0]),Float.parseFloat(points[1]));//creating latlng from text data
                    CameraPlacement(recpoints,DefaultZoom,"Retrieved Location");//passing latlng to camera placement method

                    findViewById(R.id.dailoginput).setVisibility(View.INVISIBLE);//disabling the dailog box
                }
                catch (Exception e){
                    Toast.makeText(MapActivity.this,"Invalid coordinates entry. Please make sure the points are valid!",Toast.LENGTH_SHORT).show();
                }

            }
        });
        findViewById(R.id.btn_inputcancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.dailoginput).setVisibility(View.INVISIBLE);
                AutoCompleteTextView autoCompleteTextView = findViewById(R.id.input_latlng);
                autoCompleteTextView.setText("");//clearing input field
            }
        });
    }


    //initialize search section
    private void initialize() {
        hidescreenkeyboard();//hide onscreen keyboard

        Log.d(TAG, "initialize: Initializing..");

        //creating google api client
        /*GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this ,
                        this )
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this,mGeoDataClient,latlong_bounds,null);
        SearchTxt.setAdapter(placeAutocompleteAdapter);//setting the place adapter in the search box

        SearchTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView txtView, int c, KeyEvent keyEvent) {
                if (c == EditorInfo.IME_ACTION_SEARCH || c == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    //run search method
                    LocatePlace();
                }
                return false;
            }
        });*/
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        currentgps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: re-directing to current location");
                DeviceCurrentLocation();//redirect to device current location
            }
        });
    }


    //spinner dropdown section item selection
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                //reset map type to normal
                gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                /*findViewById(R.id.ic_current).setBackgroundColor(Color.TRANSPARENT);
                findViewById(R.id.maps_type).setBackgroundColor(Color.TRANSPARENT);*/
                break;
            case 1:
                //set map type to terrain map type
                gmap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                /*findViewById(R.id.ic_current).setBackgroundColor(Color.GRAY);
                findViewById(R.id.maps_type).setBackgroundColor(Color.GRAY);*/
                break;
            case 2:
                //set map type to hybrid map type
                gmap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                /*findViewById(R.id.ic_current).setBackgroundColor(Color.GRAY);
                findViewById(R.id.maps_type).setBackgroundColor(Color.GRAY);*/
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //class to parse the Google Places in JSON format
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        //parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                JSONPathParser parser = new JSONPathParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }
            //drawing polyline in the Google Map
            gmap.addPolyline(lineOptions);
        }
    }

    //initiate task to create path
    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }

    //get URL for the path to the location
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        //start point of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        //end point of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        //sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        //building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        //output format
        String output = "json";

        //building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    //downloading json data from the obtained url
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    //getting current device location
    private void DeviceCurrentLocation(){
        hidescreenkeyboard();
        Log.d(TAG, "DeviceLocation: Getting your current device location");//log

            mLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

        try{
            if(permissioncheck){
                hidescreenkeyboard();//hide onscreen keyboard
                final com.google.android.gms.tasks.Task<Location> location = mLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Location> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Location Found!");
                            Location CurrentLocation = (Location) task.getResult();

                            CameraPlacement(new LatLng(CurrentLocation.getLatitude(), CurrentLocation.getLongitude()),DefaultZoom, "Current Location");//moving the camera to the current location
                            findViewById(R.id.btn_path).setVisibility(View.INVISIBLE);
                        }
                        else{
                            Log.d(TAG, "onComplete: Current location not found");
                            Toast.makeText(MapActivity.this, "Unable to get current device location",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
        catch (SecurityException e){
            Log.d(TAG, "DeviceCurrentLocation: SecurityException: " + e.getMessage());;
        }

        //clearing place details text box content
        Log.d(TAG, "DeviceCurrentLocation: place details text box content cleared");
        TextView textbox = (TextView) findViewById(R.id.placedetails);
        textbox.setText("");
    }

    //moving camera to the current device location
    private void CameraPlacement(LatLng latlong, float zoom, String title){
        hidescreenkeyboard();//hiding the onscreen keyboard after the camera is placed at a location
        Log.d(TAG, "CameraPlacement: Panning camera to: "+ latlong.latitude + " " + latlong.longitude );
        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong, 16f));
        if(title.equals("Current Location")){
            gmap.clear();//clear markers
            //marker placement
            MarkerOptions markeroptn =new MarkerOptions().draggable(false).position(latlong).title(title);
            gmap.addMarker(markeroptn);//adding marker to the map

            lati = markeroptn.getPosition().latitude;//getting start latitude
            longi = markeroptn.getPosition().longitude;//getting start longitude

            Log.d(TAG, "CameraPlacement: Current device location");
        }
        else{
            //clear  markers
            gmap.clear();

            //Adding marker to the current long pressed position, green marker
            gmap.addMarker(new MarkerOptions().position(latlong).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            tolati = latlong.latitude;//latitude
            tolongi = latlong.longitude;//longitude

            LatLng start = new LatLng(lati,longi);//get start coordinates
            LatLng end = latlong;//get end coordinates

            Double dis = SphericalUtil.computeDistanceBetween(start, end);//calculate distance between 2 points
            Toast.makeText(MapActivity.this,"Distance: " + dis + " meters.",Toast.LENGTH_SHORT).show();

            findViewById(R.id.btn_path).setVisibility(View.VISIBLE);//enabling show path button
            findViewById(R.id.btn_path).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LatLng end = new LatLng(tolati,tolongi);//get end coordinates
                    plottpath(end);
                }
            });
        }
    }

    //initiate map
    private  void loadMap(){//loading the map
        Log.d(TAG, "loadMap: Initializing the Map");//log

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) MapActivity.this);

    }

    //location permissions
    private  void getlocationpermission(){
        Log.d(TAG, "getlocationpermission: Getting location permissions");//log

        String[] permissions ={Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),FineLocation)== PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),CoarseLocation)==PackageManager.PERMISSION_GRANTED){
                //true the permissioncheck boolean
                permissioncheck=true;
                loadMap();
            }
            else{
                ActivityCompat.requestPermissions(this,permissions,LocationPermissionReqCode);
            }
        }
        else{
            ActivityCompat.requestPermissions(this,permissions,LocationPermissionReqCode);
        }
    }

    @Override//requesting for permission to access the location from the device (screen pop-up in device screen)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissioncheck=false;
        switch (requestCode){
            case LocationPermissionReqCode:{
                if (grantResults.length>0){
                    for (int c = 0; c<grantResults.length; c++){
                        if (grantResults[c]!=PackageManager.PERMISSION_GRANTED){
                            permissioncheck=false;
                            Log.d(TAG, "onRequestPermissionsResult: Permission Denied!");
                            return;
                        }
                    }
                    permissioncheck=true;
                    Log.d(TAG, "onRequestPermissionsResult: Permission Granted!");

                    loadMap();//load the map here
                }
            }
        }
    }

    //hiding the onscreen keyboard after the search is complete
    public void hidescreenkeyboard(){
        DrawerLayout relativeLayout = findViewById(R.id.maplayout);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(relativeLayout.getWindowToken(), 0);
        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    //writing to history file
    private void writehistory(Place place, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("history.txt", Context.MODE_APPEND));
            outputStreamWriter.write(place.getName().toString() + "\n");
            outputStreamWriter.close();
            Log.d(TAG, "writehistory: search history updated");
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    //map long tap
    @Override
    public void onMapLongClick(LatLng latLng) {
        gmap.clear();//clear markers
        findViewById(R.id.btn_path).setVisibility(View.INVISIBLE);//disable show path button
        plottpath(latLng);
    }

    //start marker drag
    @Override
    public void onMarkerDragStart(Marker marker) {
        findViewById(R.id.placedetails).setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_path).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    //stop dragging marker
    @Override
    public void onMarkerDragEnd(Marker marker) {

        //Getting the coordinates for the drag release position
        tolati=marker.getPosition().latitude;
        tolongi=marker.getPosition().longitude;

        LatLng latLng = marker.getPosition();
        //Moving the map
        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));

        //calcdistance();//call calculate distance method
        plottpath(new LatLng(tolati,tolongi));//call path drawing method
    }

    //calculate distance between current location and drag stop location
    public void calcdistance(){
        LatLng start = new LatLng(lati,longi);//get start coordinates
        LatLng end = new LatLng(tolati,tolongi);//get end coordinates

        Double dis = SphericalUtil.computeDistanceBetween(start, end);//calculate distance between 2 points
        //show distance
        Toast.makeText(this,String.valueOf(dis + "m. approximately"),Toast.LENGTH_SHORT);
        TextView textbox = (TextView) findViewById(R.id.placedetails);
        textbox.setText("Distance= " + dis + "m.");
        Log.d(TAG, "calcdistance: "+ dis);

        //getDirection();//show the route for the location
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //connect to GoogleAPI client
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    //disconnect from GoogleAPI client
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onPlaceSelected(Place place) {

    }

    @Override
    public void onError(Status status) {

    }
}
