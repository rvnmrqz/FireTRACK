package com.example.rvnmrqz.firetrack;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.Language;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class Fragment_truck_map extends Fragment implements OnMapReadyCallback, DirectionCallback{

    static Context context;
    static DirectionCallback directionCallback;

    static GoogleMap mGooglemap;
    MapView mMapView;
    View myview;

    static Animation anim_down;
    static LinearLayout confirmationLayout;
    Button btnAccept,btnDecline, btnCancel;

    static LinearLayout progress_layout;
    static ProgressBar progressBar;
    static TextView progressTxt;

    //adding of routes preview
    static boolean accepted=false;
    static ImageButton btnFullscreen;
    static ImageButton btnShowRoutesDetails;
    Animation anim_slideLeft, anim_slideRight;
    static LinearLayout routesDetailsLayout,  button_extra_Layout_showDetails;
    static boolean routesDetailsIsShown = true;

    //directions
    private static String serverKey = "AIzaSyAz5QHXjEaFaTG2MNTsJsJmseT8oAp1CBE";
    static LatLng destinationLatlng;
    static Marker destination_marker;
    public static boolean waitingForLocation=false;

    public static boolean ON_SESSION=false;
    public static Marker origin_marker;
    public static String markerCurrentPosition;


    String colors[] = new String[]{"#3d40ed","#2abc25", "#ef15ec"};
    static Polyline[] polylines = new Polyline[3];
    double distance[] = new double[3];
    String duration[] = new String[3];
    LatLng middleLatlng[] = new LatLng[3];
    static Marker[] markers = new Marker[3];
    MarkerOptions markerOptions[] = new MarkerOptions[3];
    LinearLayout  route1, route2, route3;
    LinearLayout routes_linearLayouts[];

    TextView r1_txt,r2_txt,r3_txt;
    ImageButton r1_imgbtn,r2_imgbtn,r3_imgbtn;


    //container of all latlng points
    static int activeRoute=-1;
    ArrayList<LatLng> directionPositionList;
    static ArrayList<LatLng>[] directionPositionLists = new ArrayList[3];


    public Fragment_truck_map() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myview = inflater.inflate(R.layout.fragment_truck_map, container, false);
        return myview;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progress_layout = (LinearLayout) getActivity().findViewById(R.id.truck_map_progresslayout);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.truck_map_progressBar);
        progressTxt = (TextView) getActivity().findViewById(R.id.truck_map_progresText);

        confirmationLayout = (LinearLayout) getActivity().findViewById(R.id.truck_map_confirmationLayout);
        btnAccept = (Button) getActivity().findViewById(R.id.confirmation_acceptButton);
        btnDecline = (Button) getActivity().findViewById(R.id.confirmation_declineButton);
        btnCancel = (Button) getActivity().findViewById(R.id.confirmation_cancelButton);
        mMapView = (MapView) myview.findViewById(R.id.map);
        if(mMapView !=null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
        confirmationButtonListener();

        //addition of route details
        btnFullscreen = (ImageButton) getActivity().findViewById(R.id.truck_imgbtnFullScreen);
        routesDetailsLayout = (LinearLayout) getActivity().findViewById(R.id.truck_routesDetailsLayout);
        btnShowRoutesDetails = (ImageButton) getActivity().findViewById(R.id.truck_imgbtnShowRouteDetails);
        btnShowRoutesDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation counterclockwise = AnimationUtils.loadAnimation(context,R.anim.rotate_counterclock);
                Animation clockwise = AnimationUtils.loadAnimation(context,R.anim.rotate_clockwise);

                if(routesDetailsIsShown){
                    //hide it
                    btnShowRoutesDetails.startAnimation(clockwise);
                    anim_slideLeft = AnimationUtils.loadAnimation(context,R.anim.slide_left);
                    routesDetailsLayout.startAnimation(anim_slideLeft);
                    anim_slideLeft.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            Log.wtf("anim_slideLeft","onAnimationStart");
                            routesDetailsIsShown=false;
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Log.wtf("anim_slideLeft","onAnimationEnd");
                            routesDetailsLayout.setVisibility(View.INVISIBLE);
                            routesDetailsIsShown=false;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }else{
                    //show it
                    btnShowRoutesDetails.startAnimation(counterclockwise);
                    anim_slideRight = AnimationUtils.loadAnimation(context,R.anim.slide_right);
                    routesDetailsLayout.startAnimation(anim_slideRight);
                    anim_slideRight.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            Log.wtf("anim_slideRight","onAnimationStart");
                            routesDetailsIsShown=true;
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Log.wtf("anim_slideRight","onAnimationEnd");
                            routesDetailsLayout.setVisibility(View.VISIBLE);
                            routesDetailsIsShown=true;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });
        button_extra_Layout_showDetails = (LinearLayout) getActivity().findViewById(R.id.truck_button_extra_Layout_showDetails);
        if(routesDetailsLayout.isShown()) routesDetailsIsShown=true;
        else  routesDetailsIsShown=false;

        btnFullScreenListener();

        route1 = (LinearLayout) getActivity().findViewById(R.id.route1_layout);
        route2 = (LinearLayout) getActivity().findViewById(R.id.route2_layout);
        route3 = (LinearLayout) getActivity().findViewById(R.id.route3_layout);
        routes_linearLayouts = new LinearLayout[]{route1,route2,route3};

        r1_txt = (TextView) getActivity().findViewById(R.id.route1_textView);
        r2_txt = (TextView) getActivity().findViewById(R.id.route2_textView);
        r3_txt = (TextView) getActivity().findViewById(R.id.route3_textView);

        r1_imgbtn = (ImageButton) getActivity().findViewById(R.id.route1_ImgBtn);
        r2_imgbtn = (ImageButton) getActivity().findViewById(R.id.route2_ImgBtn);
        r3_imgbtn = (ImageButton) getActivity().findViewById(R.id.route3_ImgBtn);
        routeClickListener();

        Activity_main_truck.checkPermission();


    }

    public static void showLoadingLayout(boolean show,boolean showProgress, String progressText){
        if (show){
            progress_layout.setVisibility(View.VISIBLE);
            if(showProgress){
                progressBar.setVisibility(View.VISIBLE);
            }else{
                progressBar.setVisibility(View.GONE);
            }
            progressTxt.setText(progressText);
        }else{
            progress_layout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(context);
        mGooglemap = googleMap;
        mGooglemap.getUiSettings().setMapToolbarEnabled(false);
        mGooglemap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGooglemap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Toast.makeText(context, "Polyline clicked: "+polyline.getId(), Toast.LENGTH_SHORT).show();
            }
        });
       showValenzuelaInMap();

        //remove this after testing stage
        mGooglemap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                origin_marker.setPosition(marker.getPosition());
                markerCurrentPosition = origin_marker.getPosition().latitude+","+origin_marker.getPosition().longitude;
                Log.wtf("onMarkerDragEnd","\nMarker: "+marker.getPosition()+"\nOrigin:"+origin_marker.getPosition());
                NearestPointFinder nearestPointFinder = new NearestPointFinder();
                nearestPointFinder.execute(markerCurrentPosition);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        Log.wtf("onAttach","Called");
        super.onAttach(context);
        this.context = context;
       directionCallback = this;
    }

    public static void resetMapView(){
        accepted=false;
        activeRoute=-1;
        routesDetailsIsShown=false;
        waitingForLocation=false;
       // progress_layout.setVisibility(View.GONE);
        confirmationLayout.setVisibility(View.GONE);
        mGooglemap.clear();
        origin_marker=null;
        markerCurrentPosition=null;
        showRoutesDetails(false);
        showValenzuelaInMap();

    }

    public static void showValenzuelaInMap(){
        LatLng valenzuela_center = new LatLng(14.699006, 120.983371);
        mGooglemap.animateCamera(CameraUpdateFactory.newLatLngZoom(valenzuela_center, 13.5f));
    }

    //PREVIEW AND CONFIRMATION
    public static void showConfirmationOnMap(boolean show){
        if(show){
           confirmationLayout.setVisibility(View.VISIBLE);
        }else{
            confirmationLayout.setVisibility(View.GONE);
        }
    }
    protected void confirmationButtonListener(){
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accepted=true;
                Toast.makeText(getContext(),"Accept is clicked",Toast.LENGTH_SHORT).show();
                Activity_main_truck.showConfirmationDialog(Activity_main_truck.report_firenotif_ids_list.get(Activity_main_truck.SELECTED_FIRE_REPORT_INDEX),1);
            }
        });
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accepted=false;
                Activity_main_truck.showConfirmationDialog(Activity_main_truck.report_firenotif_ids_list.get(Activity_main_truck.SELECTED_FIRE_REPORT_INDEX),0);
                Log.wtf("confirmationListener","Button Decline is clicked");
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accepted=false;
                Log.wtf("confirmationListener","Button Cancel is clicked");
                resetMapView();
                hideConfirmationLayout();
            }
        });

    }
    protected static void hideConfirmationLayout(){
        anim_down = AnimationUtils.loadAnimation(context,R.anim.slide_down);
        confirmationLayout.startAnimation(anim_down);
        anim_down.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                confirmationLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    public static void addDestinationmarker(LatLng coor,String title, String snippetmsg){
        accepted=false;
        destinationLatlng = coor;
        animateInitialCameraView(false,destinationLatlng);
        destination_marker =  mGooglemap.addMarker(new MarkerOptions().position(coor).title(title).snippet(snippetmsg));
        destination_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.fire_marker));
    }
    public static void animateInitialCameraView(boolean tilted, LatLng location){
        destinationLatlng = location;
        if(Activity_main_truck.myLocation!=null){
            Log.wtf("animateInitialCameraView()","Current locataion is not null");
            waitingForLocation=false;
            requestDirection(Activity_main_truck.myLocation,destinationLatlng);
        }else{
            Log.wtf("animateInitialCameraView()","Current locataion is null");
            waitingForLocation=true;
            CameraPosition position;
            if(tilted){
                position = new CameraPosition.Builder()
                        .target(location)
                        .tilt(65.5f).zoom(18f).build();
            }else{
                position = new CameraPosition.Builder()
                        .target(location)
                        .zoom(18f).build();
            }
            mGooglemap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        }
    }

    //route details
    protected void btnFullScreenListener(){
        btnFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Activity_main_truck.fullscreen){
                    fullScreenMap();
                }else{
                    //exit from fullscreen
                    exitFullScreenMap();
                }
            }
        });
    }
    protected void fullScreenMap(){
        //make it fullscreen
        Activity_main_truck.fullscreen=true;
        btnFullscreen.setImageResource(R.drawable.ic_fullscreen_exit_black);
        Activity_main_truck.bottomNavigation.setVisibility(View.GONE);
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }
    protected static void exitFullScreenMap(){
        //exit from fullscreen
        btnFullscreen.setImageResource(R.drawable.ic_fulllscreen_black);
        Activity_main_truck.fullscreen=false;
        Activity_main_truck.bottomNavigation.setVisibility(View.VISIBLE);
        Activity_main_truck.bottomNavigation.restoreBottomNavigation();
        ((AppCompatActivity)context).getSupportActionBar().show();
    }
    protected static void showRoutesDetails(boolean show){
        if(show){
            Log.wtf("showRoutesDetails()","TRUE");
            routesDetailsIsShown=false;
            button_extra_Layout_showDetails.setVisibility(View.VISIBLE);
            btnShowRoutesDetails.performClick();

        }else{
            Log.wtf("showRoutesDetails()","FALSE");
            routesDetailsIsShown=true;
            button_extra_Layout_showDetails.setVisibility(View.INVISIBLE);
            routesDetailsLayout.setVisibility(View.INVISIBLE);
        }
    }

    //directions
 public static void requestDirection(LatLng origin, LatLng destination) {
     destinationLatlng = destination;
     Log.wtf("requestDirection()","Request started, origin: "+origin+ "\t destination"+destination);
        showLoadingLayout(true,true,"Requesting Directions");
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .language(Language.ENGLISH)
                .alternativeRoute(true)
                .transportMode(TransportMode.DRIVING)
                .execute(directionCallback);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            showLoadingLayout(false,false,"");
            waitingForLocation=false;
            //to fit the markers in screen
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(Activity_main_truck.myLocation);
            builder.include(destinationLatlng);
            LatLngBounds bounds = builder.build();
            mGooglemap.setBuildingsEnabled(true);
            mGooglemap.animateCamera(CameraUpdateFactory
                    .newLatLngBounds(bounds, 40)
            );

            //**********************************************
            //to make origin marker draggable
            origin_marker = mGooglemap.addMarker(new MarkerOptions().position(Activity_main_truck.myLocation).draggable(true));

            mGooglemap.addMarker(new MarkerOptions().position(destinationLatlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.fire_marker)));
            int numberOfOptions = direction.getRouteList().size();
            if(numberOfOptions>3){
                //limit the size
                numberOfOptions=3;
            }

            for(int x=(numberOfOptions-1);x>-1;x--){
                double value = (Double.parseDouble(direction.getRouteList().get(x).getLegList().get(0).getDistance().getValue().toString())) * 0.001;
                distance[x] = (double)Math.round(value * 10d) / 10d;
                duration[x] = convertTime(Double.parseDouble(direction.getRouteList().get(x).getLegList().get(0).getDuration().getValue()));

                directionPositionList = direction.getRouteList().get(x).getLegList().get(0).getDirectionPoint();
                directionPositionLists[x] = directionPositionList;

                //getting the middle
                int mid = (directionPositionList.size())/2;
                middleLatlng[x] = directionPositionList.get(mid);

                //moving the origin marker to the road
                origin_marker.setPosition(directionPositionList.get(0));
                routes_linearLayouts[x].setVisibility(View.VISIBLE);

                polylines[x] = mGooglemap.addPolyline(DirectionConverter.createPolyline(context, directionPositionList, 5, Color.parseColor(colors[x])));

                //use a transparent 1px & 1px box as your marker
                BitmapDescriptor transparent = BitmapDescriptorFactory.fromResource(R.drawable.transparent);
                markerOptions[x] = new MarkerOptions()
                        .position(middleLatlng[x])
                        .title("Route "+(x+1))
                        .snippet(distance[x]+"km, "+duration[x])
                        .icon(transparent)
                        .anchor((float) 0.5, (float) 0.5); //puts the info window on the polyline

                markers[x] = mGooglemap.addMarker(markerOptions[x]);
                //open the marker's info window
                markers[x].showInfoWindow();
                  }
            showRoutesDetails(true);
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Toast.makeText(context, "Cannot Request Directions", Toast.LENGTH_SHORT).show();
    }

    //route click functions
    public static void hideRoutesExcept(int routeNo){
        Log.wtf("hideRouteExcpet()","routeNo: "+routeNo);
        if(routeNo==-1){
            routeNo=0;
        }
            //new route is selected
            Log.wtf("Route No",routeNo+"");
            polylines[routeNo].setVisible(true);
            markers[routeNo].showInfoWindow();
            activeRoute=routeNo;
            for (int x=0;x<polylines.length;x++){
                Log.wtf("x",x+"");
                if(x!=routeNo){
                    if(polylines[x]!=null){
                        polylines[x].setVisible(false);
                        markers[x].hideInfoWindow();
                        Log.wtf("for loop","Route hidden "+x);
                    }
                }else{
                    Log.wtf("for loop","SAME ROUTE SELECTED");
                }
            }
            if(accepted) {
                rotateMap();
            }
    }
    private void routeClickListener(){
        //route1
        route1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.wtf("route1_distance","Clicked");
                hideRoutesExcept(0);
                activeRoute=0;
            }
        });

        r1_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRoutesExcept(0);
                activeRoute=0;
            }
        });
        r1_imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRoutesExcept(0);
                activeRoute=0;
            }
        });

        //route 2
        route2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.wtf("route2","Clicked");
                hideRoutesExcept(1);
                activeRoute=1;
            }
        });

        r2_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRoutesExcept(1);
                activeRoute=1;
            }
        });
        r2_imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRoutesExcept(1);
                activeRoute=1;
            }
        });


        //route 3
        route3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.wtf("route3","Clicked");
                hideRoutesExcept(2);
                activeRoute=2;
            }
        });

        r3_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRoutesExcept(2);
                activeRoute=2;
            }
        });
        r3_imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRoutesExcept(2);
                activeRoute=2;
            }
        });

    }
    private String convertTime(double seconds){
        String finalhr ="", finalmin="", finalsec="",testVar, parts[];

        double hrs = seconds * 0.000277778;
        testVar = hrs+"";
        parts = testVar.split("\\.");
        if(hrs>=1){
            finalhr = parts[0]="hr ";
        }
        hrs = Double.parseDouble(("0."+parts[1]));

        double min = hrs * 60;
        testVar = min+"";
        parts = testVar.split("\\.");
        if(min>=1){
            finalmin = parts[0]+"min ";
        }

        long sec  = Long.parseLong(parts[1]);
        if(sec>=1){
            testVar = sec+"";
            if(testVar.length()>2){
                finalsec = testVar.substring(0,1)+"sec";
            }
        }

        return (finalhr+finalmin+finalsec);
    }

    //camera and map animations
    private static void rotateMap(){
        if(origin_marker!=null){
            Log.wtf("rotateMap","origin marker is not null");
            if(activeRoute==-1){
                Log.wtf("rotateMap","There is no active route selected");
                activeRoute=0; // to make the active route the first one in the results
                animateDirectionCameraView(2);
            }else{
                Log.wtf("rotateMap","There is already selected active route");
                if(markerCurrentPosition!=null){
                    Log.wtf("rotateMap","markerCurrent position is not null, already moved from original");
                    //marker is not yet been moved
                    NearestPointFinder nearestPointFinder = new NearestPointFinder();
                    nearestPointFinder.execute(markerCurrentPosition);
                }else{
                    Log.wtf("rotateMap","markerCurrent is null, not been moved");
                    animateDirectionCameraView(2);
                }
            }
        }else{
            Toast.makeText(context, "Request Location First", Toast.LENGTH_SHORT).show();
        }
    }
    private static void animateDirectionCameraView(int INDEX){
        try{
            if(INDEX>=directionPositionLists[activeRoute].size()){
                INDEX = directionPositionLists[activeRoute].size()-1;
            }
            LatLng markerLoc = origin_marker.getPosition();
            LatLng directionPoint = directionPositionLists[activeRoute].get(INDEX);

            double dLon = (directionPoint.longitude-markerLoc.longitude);
            double y = Math.sin(dLon) * Math.cos(directionPoint.latitude);
            double x = Math.cos(markerLoc.latitude)*Math.sin(directionPoint.latitude) - Math.sin(markerLoc.latitude)*Math.cos(directionPoint.latitude)*Math.cos(dLon);
            float brng = Float.parseFloat((Math.toDegrees((Math.atan2(y, x))))+"");
            brng = (360 - ((brng + 360) % 360));

            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(markerLoc)
                    .bearing(brng).tilt(65.5f).zoom(20f).build();
            mGooglemap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(currentPlace));

        }catch (Exception e){
            Log.wtf("animateCameraView",e.getMessage());
            Toast.makeText(context, "Exception while animating camera", Toast.LENGTH_SHORT).show();
        }
    }
    static class NearestPointFinder extends AsyncTask<String,Void,Integer> {
        LatLng origin_marker_position;

        @Override
        protected Integer doInBackground(String... params) {
            try{
                Log.wtf("doinBackground","CALLED");
                String[] latlong =  params[0].split(",");
                Log.wtf("latlong","value: "+params[0]);

                double latitude = Double.parseDouble(latlong[0]);
                double longitude = Double.parseDouble(latlong[1]);
                origin_marker_position = new LatLng(latitude,longitude);
                int index = findNearestPoint();
                return index;
            }catch (Exception e){
                Log.wtf("doInbackground ERROR",e.getMessage());
                return -1;
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.wtf("onPreExecute","called");
        }

        private int findNearestPoint(){
            try {
                float nearest=0;
                float temp1=0;
                int index=0;
                LatLng currentPosition = origin_marker_position;

                nearest  =   getDistance(currentPosition, directionPositionLists[activeRoute].get(0));
                for (int x=0;x<directionPositionLists[activeRoute].size();x++){

                    temp1 = getDistance(currentPosition, directionPositionLists[activeRoute].get(x));
                    Log.wtf("findnearestPointLoop["+x+"]","temp = "+temp1+"\nnearest = "+nearest);
                    if(temp1<=nearest){
                        nearest = temp1;
                        index=x;
                    }else{
                        //temp is higher than the nearest
                        // break;
                    }
                }
            Log.wtf("(findNearestPoint)","Nearest point is index: "+index);
            return index;

            }catch (Exception e){
               Log.wtf("Exception (findNearestPoint)",e.getMessage());
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer!=-1){
                //animate camera
                animateDirectionCameraView(integer+1);
            }
            Toast.makeText(context, "Animate Direction ["+integer+"]", Toast.LENGTH_SHORT).show();
        }

        private float getDistance(LatLng position, LatLng assumed){
            float results[] = new float[1];
            Location.distanceBetween(
                    position.latitude,
                    position.longitude,
                    assumed.latitude,
                    assumed.longitude,
                    results);
            Log.wtf("showDistance","Result is "+results[0]+" meter");

            float value = results[0];
            return value;
        }
    }
}
