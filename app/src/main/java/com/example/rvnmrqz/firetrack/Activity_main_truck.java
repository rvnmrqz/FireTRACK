package com.example.rvnmrqz.firetrack;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.maps.model.LatLng;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Activity_main_truck extends AppCompatActivity {

    static Context staticContext;
    static Activity staticActivtity;
    String account_id=null;
    static DBHelper dbHelper;
    public static AHBottomNavigation bottomNavigation;
    AHBottomNavigationItem item1,item2,item3;

    //tab 1
    FrameLayout tab1;
    FrameLayout frameContainer;
    static int shownfirenotifid_in_Map;
    public static boolean fullscreen=false;

    //tab 2
    RelativeLayout tab2;
    SwipeRefreshLayout tab2_swipeRefreshLayout;
    static LinearLayout tab2_listLayout, tab2_loadingLayout, tab2_errormsgLayout;
    static FrameLayout tab2_zoomlayout;
    static ListView tab2_listview;
    static TextView tab2_loadingTxt, tab2_errorTxt, tab2_zoomExitTxt;
    static Button tab2_errorButton;
    ProgressBar tab2_loadingProgressbar;
    static TouchImageView tab2_zoomTouchimage;
    static boolean imageIsZoomed=false;
    static ReportAdapter adapter;

    static ArrayList<String> report_firenotif_ids_list= new ArrayList<String>();
    static ArrayList<String> report_images_list= new ArrayList<String>();
    static ArrayList<String> report_coordinates_list = new ArrayList<String>();
    static ArrayList<String> report_datetime_list = new ArrayList<String>();
    static ArrayList<String> report_firestatus_list = new ArrayList<String>();
    static ArrayList<String> report_alarmlevel_list = new ArrayList<String>();
    static ArrayList<String> report_additionalInfo_list= new ArrayList<String>();

    //tab 3
    RelativeLayout tab3;


    //location
    public static LatLng myLocation;
    private static LocationManager locationManager;
    private static LocationListener locationListener;
    static int     LOCATION_PERMISSION=2;
    static int OPEN_GPS_SETTINGS_REQUEST=30;
    int OPEN_PERMISSION_REQUEST=40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_main_truck);

        staticContext = Activity_main_truck.this;
        staticActivtity = Activity_main_truck.this;
        dbHelper = new DBHelper(this);

        //get the current user's account_id
        Cursor c = dbHelper.getSqliteData("SELECT "+dbHelper.COL_ACC_ID+" FROM "+dbHelper.TABLE_USER +" WHERE "+dbHelper.COL_USER_LOC_ID+"=1");
        if(c!=null){
            if(c.getCount()>0){
                c.moveToFirst();
                account_id = c.getString(c.getColumnIndex(dbHelper.COL_ACC_ID));
            }
        }
        frameContainer = (FrameLayout) findViewById(R.id.truck_containter);
        tab1 = (FrameLayout) findViewById(R.id.truck_tab1);
        tab2 = (RelativeLayout) findViewById(R.id.truck_tab2);
        tab3 = (RelativeLayout) findViewById(R.id.truck_tab3);

        initializeBottomNav();

        //tab 1
        displayFragmentMap();

        //tab2
        tab2_listLayout = (LinearLayout) findViewById(R.id.tab2_listviewlayout);
        tab2_listview = (ListView) findViewById(R.id.tab2_listview_reports);
        tab2_swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.tab2_swipeRefreshLayout);
        tab2_swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { loadReportNotifications(); tab2_swipeRefreshLayout.setRefreshing(false);}});
        tab2_errormsgLayout = (LinearLayout) findViewById(R.id.tab2_errormessagelayout);
        tab2_errorTxt = (TextView) findViewById(R.id.tab2_errorTextView);
        tab2_errorButton = (Button) findViewById(R.id.tab2_errorButton);
        tab2_errorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadReportNotifications();
            }
        });
        tab2_loadingLayout = (LinearLayout) findViewById(R.id.tab2_loadinglayout);
        tab2_loadingProgressbar = (ProgressBar) findViewById(R.id.tab2_loading_progressbar);
        tab2_loadingTxt = (TextView) findViewById(R.id.tab2_loading_textview);
        tab2_zoomlayout = (FrameLayout) findViewById(R.id.tab2_zoomlayout);
        tab2_zoomExitTxt = (TextView) findViewById(R.id.truck_tab2_zoom_txtClose);
        tab2_zoomExitTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomImageExit();
            }
        });
        tab2_zoomTouchimage = (TouchImageView) findViewById(R.id.truck_tab2_touchimageview);
        loadReportNotifications();

    }

    @Override
    protected void onDestroy() {
        stopLocationListener();
        super.onDestroy();
    }

    protected void initializeBottomNav(){
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.truck_bottomnavigation);

        // Create items
        item1 = new AHBottomNavigationItem("Map", R.drawable.ic_map_black, R.color.colorBottomNavigationPrimary);
        item2 = new AHBottomNavigationItem("Reports", R.drawable.fire_bw,R.color.colorBottomNavigationPrimary);
        item3 = new AHBottomNavigationItem("Account",R.drawable.user,R.color.colorBottomNavigationPrimary);

        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);

        // Set background color
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

        // Change colors
        bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));


        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if(imageIsZoomed){
                    zoomImageExit();
                }

                switch (position){
                    case 0:
                        showTab1();
                        break;
                    case 1:
                        showTab2();
                        break;
                    case 2:
                        showTab3();
                        break;
                }
                return true;
            }
        });
    }

    //Tab transisitions
    protected void showTab1(){
        tab1.setVisibility(View.VISIBLE);
        tab2.setVisibility(View.GONE);
        tab3.setVisibility(View.GONE);
    }
    protected void showTab2(){
        tab1.setVisibility(View.GONE);
        tab2.setVisibility(View.VISIBLE);
        tab3.setVisibility(View.GONE);
    }
    protected void showTab3(){
        tab1.setVisibility(View.GONE);
        tab2.setVisibility(View.GONE);
        tab3.setVisibility(View.VISIBLE);
    }

    //TAB1
    protected void displayFragmentMap(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.truck_fragment_container,new Fragment_truck_map()).commit();
    }

    //************************************************************

    //TAB2
    protected void loadReportNotifications(){
        report_firenotif_ids_list.clear();
        report_images_list.clear();
        report_datetime_list.clear();
        report_coordinates_list.clear();
        report_firestatus_list.clear();
        report_alarmlevel_list.clear();
        report_additionalInfo_list.clear();

        showTab2LoadingLayout(true);

        final String query = "SELECT f.firenotif_id, coordinates, additional_info, fire_status, picture, alarm_level, report_datetime " +
                " FROM " +
                " tbl_reports r " +
                " INNER JOIN " +
                " tbl_firenotifs f " +
                " ON r.report_id = f.report_id " +
                " LEFT JOIN " +
                " tbl_firenotif_response fr " +
                " ON f.firenotif_id = fr.firenotif_id " +
                " WHERE response_id is null " +
                " AND r.fire_status='on going' " +
                " AND r.report_status = 'approved'"+
                " AND f.firenotif_receiver="+account_id+";";
        String url = ServerInfoClass.HOST_ADDRESS+"/get_data.php";

        RequestQueue requestQueue = new Volley().newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.wtf("loadReportNotifications()","Response Received: "+response);
                try{
                    JSONObject object = new JSONObject(response);
                    JSONArray Jarray  = object.getJSONArray("mydata");
                    if(Jarray.length()>0){

                        //do extraction

                        String notif_id,encoded_image,coordinates,datetime,firestatus,alarmlevel,additionalInfo;

                        for (int i = 0; i < Jarray.length(); i++) {
                            JSONObject Jasonobject = Jarray.getJSONObject(i);
                            notif_id = Jasonobject.getString(dbHelper.COL_FIRENOTIF_ID);
                            encoded_image = Jasonobject.getString(dbHelper.COL_REPORT_PICUTRE);
                            coordinates = Jasonobject.getString(dbHelper.COL_REPORT_COORDINATES);
                            datetime = Jasonobject.getString(dbHelper.COL_REPORT_DATETIME);
                            firestatus = Jasonobject.getString(dbHelper.COL_REPORT_FIRE_STATUS);
                            alarmlevel = Jasonobject.getString(dbHelper.COL_ALARM_LEVEL);
                            if(alarmlevel == null){
                                alarmlevel = "Unknown";
                            }else{
                                if(alarmlevel.trim().equalsIgnoreCase("null") || alarmlevel.trim().equalsIgnoreCase("")){
                                    alarmlevel = "Unknown";
                                }
                            }
                            additionalInfo = Jasonobject.getString(dbHelper.COL_REPORT_ADDITIONAL_INFO);
                            if(additionalInfo == null){
                                additionalInfo = "None";
                            }else{
                                if(additionalInfo.trim().equalsIgnoreCase("null") || additionalInfo.trim().equalsIgnoreCase("")){
                                    additionalInfo = "None";
                                }
                            }
                            report_firenotif_ids_list.add(notif_id);
                            report_images_list.add(encoded_image);
                            report_coordinates_list.add(coordinates);
                            report_datetime_list.add(datetime);
                            report_firestatus_list.add(firestatus);
                            report_alarmlevel_list.add(alarmlevel);
                            report_additionalInfo_list.add(additionalInfo);
                        }

                        //initialize the adapter
                        showTab2Listview(true);
                        tab2_listview.setAdapter(null);
                        adapter = new ReportAdapter(getApplicationContext(),
                                report_firenotif_ids_list,
                                report_images_list,
                                report_coordinates_list,
                                report_datetime_list,
                                report_firestatus_list,
                                report_alarmlevel_list,
                                report_additionalInfo_list);
                        tab2_listview.setAdapter(adapter);

                        updateDeliveredReportsNotif();

                    }else{
                        //show no reports yet UI
                        showTab2MessageLayout(true,"No Fire Reports",true,"Refresh");
                    }
                }catch (Exception e){
                    Log.wtf("LoadReportNotifications Exception","Error : "+e.getMessage());
                    showTab2MessageLayout(true,e.getMessage(),true,"Retry");
                }
                if(tab2_swipeRefreshLayout.isRefreshing()){
                    tab2_swipeRefreshLayout.setRefreshing(false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String message = "Error Received";
                Log.wtf("LoadFeed: onErrorResponse","Volley Error \n"+volleyError.getMessage());
                if (volleyError instanceof NetworkError) {
                    message = "Network Error Encountered";
                    Log.wtf("loadFeed (Volley Error)","NetworkError");
                    //showSnackbar("You're not connected to internet");
                } else if (volleyError instanceof ServerError) {
                    message = "Please check your internet connection";
                    Log.wtf("loadFeed (Volley Error)","ServerError");

                } else if (volleyError instanceof AuthFailureError) {
                    message = "Please check your internet connection";
                    Log.wtf("loadFeed (Volley Error)","AuthFailureError");

                } else if (volleyError instanceof ParseError) {
                    message = "An error encountered, Please try again";
                    Log.wtf("loadFeed (Volley Error)","ParseError");
                } else if (volleyError instanceof NoConnectionError) {
                    message = "No internet connection";
                    Log.wtf("loadFeed (Volley Error)","NoConnectionError");
                } else if (volleyError instanceof TimeoutError) {
                    message = "Connection Timeout";
                    Log.wtf("loadFeed (Volley Error)","TimeoutError");
                }
                Log.wtf("Volley Error Message","Error: "+volleyError.getMessage());
                if(tab2_swipeRefreshLayout.isRefreshing()){
                    tab2_swipeRefreshLayout.setRefreshing(false);
                }
                showTab2MessageLayout(true,message,true,"Retry");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("qry",query);
                Log.wtf("Map<String><String>","Query: "+query);
                return params;
            }
        };
        int socketTimeout = ServerInfoClass.TIME_OUT;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);

    }
    protected void showTab2Listview(boolean show){
        if(show){
            tab2_loadingLayout.setVisibility(View.GONE);
            tab2_errormsgLayout.setVisibility(View.GONE);
            tab2_listLayout.setVisibility(View.VISIBLE);
        }else{
            tab2_listLayout.setVisibility(View.GONE);
        }

    }
    protected void showTab2LoadingLayout(boolean show){
         if(show){
             tab2_listLayout.setVisibility(View.GONE);
             tab2_errormsgLayout.setVisibility(View.GONE);
             tab2_loadingLayout.setVisibility(View.VISIBLE);
         }else{
             tab2_loadingLayout.setVisibility(View.GONE);
         }
    }
    protected static void showTab2MessageLayout(boolean show,String msg, boolean showButton, String buttonText){
        if(show) {
            tab2_listLayout.setVisibility(View.GONE);
            tab2_loadingLayout.setVisibility(View.GONE);
            tab2_errormsgLayout.setVisibility(View.VISIBLE);
            tab2_errorTxt.setText(msg);
            if(showButton){
                tab2_errorButton.setVisibility(View.VISIBLE);
                tab2_errorButton.setText(buttonText);
            }else{
                tab2_errorButton.setVisibility(View.GONE);
            }
        }else{
            tab2_errormsgLayout.setVisibility(View.GONE);
        }
    }
    protected static void zoomImage(Bitmap bitmap){
        tab2_zoomlayout.setVisibility(View.VISIBLE);
        try{
            tab2_zoomTouchimage.setImageBitmap(bitmap);
            imageIsZoomed=true;
        }catch (Exception e){
            Toast.makeText(staticContext, "Cant load the image", Toast.LENGTH_SHORT).show();
            Log.wtf("zoomImage()","Exception Encountered: "+e.getMessage());
        }
    }
    protected void zoomImageExit(){
        tab2_zoomlayout.setVisibility(View.GONE);
        imageIsZoomed=false;
    }

    public static void setReportNotificationBadge(int notifcount){
        bottomNavigation.setNotification((notifcount+""),1);
    }
    // REPORT LISTVIEW ADAPTER
    static class ReportAdapter extends ArrayAdapter {
        ArrayList<String> report_firenotif_ids_list= new ArrayList<String>();
        ArrayList<String> report_images_list= new ArrayList<String>();
        ArrayList<String> report_coordinates_list = new ArrayList<String>();
        ArrayList<String> report_datetime_list = new ArrayList<String>();
        ArrayList<String> report_firestatus_list = new ArrayList<String>();
        ArrayList<String> report_alarmlevel_list = new ArrayList<String>();
        ArrayList<String> report_additionalInfo_list= new ArrayList<String>();

        public ReportAdapter(Context context,
                             ArrayList<String> report_firenotif_ids_list,
                             ArrayList<String> report_images_list,
                             ArrayList<String> report_coordinates_list,
                             ArrayList<String> report_datetime_list,
                             ArrayList<String> report_firestatus_list,
                             ArrayList<String> report_alarmlevel_list,
                             ArrayList<String> report_additionalInfo_list) {

            //Overriding Default Constructor off ArratAdapter
            super(context, R.layout.template_post,R.id.post_id,report_firenotif_ids_list);
            this.report_firenotif_ids_list = report_firenotif_ids_list;
            this.report_images_list = report_images_list;
            this.report_coordinates_list = report_coordinates_list;
            this.report_datetime_list = report_datetime_list;
            this.report_firestatus_list = report_firestatus_list;
            this.report_alarmlevel_list = report_alarmlevel_list;
            this.report_additionalInfo_list = report_additionalInfo_list;
        }
        @NonNull
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            //Inflating the layout
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.template_fire_report,parent,false);

            //Get the reference to the view objects
            TextView id  = (TextView) row.findViewById(R.id.report_template_firenotif_id);
            final ImageView imageView = (ImageView) row.findViewById(R.id.report_template_image);
            TextView datetime = (TextView) row.findViewById(R.id.report_template_datetime);
            TextView coordinates = (TextView) row.findViewById(R.id.report_template_coordinates);
            TextView moredetailsTXT = (TextView) row.findViewById(R.id.report_template_moredetailsTXT);
            ExpandableTextView moreDetails = (ExpandableTextView) row.findViewById(R.id.report_template_moreDetails);
            final ImageButton btnExpand = (ImageButton) row.findViewById(R.id.expand_collapse);
            ImageButton btnShowInMap = (ImageButton) row.findViewById(R.id.report_template_showInMapButton);

            btnShowInMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //extract the coordinate
                    Fragment_truck_map.resetMapView();
                    shownfirenotifid_in_Map = Integer.parseInt(report_firenotif_ids_list.get(position));
                    String parts[] = report_coordinates_list.get(position).trim().split(",");
                    Double latitude = Double.parseDouble(parts[0]);
                    Double longtitude = Double.parseDouble(parts[1]);
                    bottomNavigation.setCurrentItem(0);
                    Fragment_truck_map.showConfirmationOnMap(true);
                    Fragment_truck_map.addDestinationmarker(new LatLng(latitude,longtitude),"Fire Location",report_coordinates_list.get(position));
                }
            });
            Button btnAccept = (Button) row.findViewById(R.id.report_template_acceptButton);
            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   showConfirmationDialog(position,report_firenotif_ids_list.get(position),1);
                }
            });
            Button btnDecline = (Button) row.findViewById(R.id.report_template_declineButton);
            btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmationDialog(position,report_firenotif_ids_list.get(position),0);
                }
            });


            moredetailsTXT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnExpand.performClick();
                }
            });
            moreDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnExpand.performClick();
                }
            });

            //Providing the element of an array by specifying its position
            id.setText(report_firenotif_ids_list.get(position));
            datetime.setText(report_datetime_list.get(position));
            coordinates.setText(report_coordinates_list.get(position));

            String moredetails = "Fire Status: "+report_firestatus_list.get(position)+"\nAlarm Level: "+report_alarmlevel_list.get(position)+"\nAdditional Info: "+report_additionalInfo_list.get(position);

            moreDetails.setText(moredetails);

            String encoded_post_picture = report_images_list.get(position);
            if(encoded_post_picture!=null && encoded_post_picture.length()>10){
                try{
                    byte[] decodedString = Base64.decode(encoded_post_picture, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imageView.setImageBitmap(decodedByte);
                }catch (Exception ee){
                    Toast.makeText(getContext(), "Failed to set Image", Toast.LENGTH_SHORT).show();
                }
            }else{
                //get the image from drawables
                imageView.setBackgroundResource(R.drawable.no_image_found);
            }

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.wtf("adapter","Image is clicked");
                    Toast.makeText(staticContext, "image is clicked", Toast.LENGTH_SHORT).show();
                    Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                    zoomImage(bitmap);
                }
            });

            return row;
        }
    }
    protected static void showConfirmationDialog(final int position, final String notif_id, final int respond){
        String response = "accept";
        if(respond==0) response="decline";

        new AlertDialog.Builder(staticContext)
                .setTitle("Confirmation")
                .setMessage("You are about to "+response+" this respond request, continue?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResponse(position,notif_id,respond);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
    protected static void sendResponse(final int position, String notif_id, final int respond ){
        String response = "ACCEPTED";
        if(respond==0) response="DECLINED";

        final String query = "INSERT INTO "+dbHelper.TABLE_FIRENOTIF_RESPONSE+"("+dbHelper.COL_FIRENOTIF_ID+","+dbHelper.COL_RESPONSE+","+dbHelper.COL_RESPONSES_DATETIME+") VALUES("+notif_id+",'"+response+"',NOW());";

        String url = ServerInfoClass.HOST_ADDRESS+"/do_query.php";
        RequestQueue requestQueue  = Volley.newRequestQueue(staticContext);
        StringRequest request = new StringRequest(Request.Method.POST, url
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.wtf("updateDeliveredReportsNotif()","Response: "+response);
                //delete from the listview
                if(response.trim().equalsIgnoreCase("Process Successful")){
                    // plot the position in map
                    if(shownfirenotifid_in_Map == Integer.parseInt(report_firenotif_ids_list.get(position)) && respond==0){
                        //it is currently shown in map and it is declined
                        Fragment_truck_map.resetMapView();
                    }
                    deleteIndexFromList(position);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.wtf("updateDeliveredReportsNotif()","Error: "+error.getMessage());
                //show an error message

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                String entries ="";
                for(int x = 0;x<report_firenotif_ids_list.size();x++){
                    entries= entries+report_firenotif_ids_list.get(x);
                    if(x!=(report_firenotif_ids_list.size()-1)){
                        //not the last item in the list
                        entries = entries+", ";
                    }
                }
                 Log.wtf("updateDeliveredReportsNotif()","Map<String><String>, Query: "+query);
                params.put("query",query);
                return params;
            }
        };
        int socketTimeout = ServerInfoClass.TIME_OUT;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    protected void updateDeliveredReportsNotif(){
        Log.wtf("updateDeliveredReportsNotif()","CALLED");
        String url = ServerInfoClass.HOST_ADDRESS+"/do_query.php";
        RequestQueue requestQueue  = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, url
                , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.wtf("updateDeliveredReportsNotif()","Response: "+response);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.wtf("updateDeliveredReportsNotif()","Error: "+error.getMessage());
                    }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                String entries ="";
                for(int x = 0;x<report_firenotif_ids_list.size();x++){
                    entries= entries+report_firenotif_ids_list.get(x);
                    if(x!=(report_firenotif_ids_list.size()-1)){
                        //not the last item in the list
                        entries = entries+", ";
                    }
                }
                String query = "UPDATE "+dbHelper.TABLE_FIRENOTIFS+" SET "+dbHelper.COL_DELIVERED+" = 1 WHERE "+dbHelper.COL_FIRENOTIF_ID+" IN("+entries+");";
                Log.wtf("updateDeliveredReportsNotif()","Map<String><String>, Query: "+query);
                params.put("query",query);
                return params;
            }
        };
        int socketTimeout = ServerInfoClass.TIME_OUT;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    public static void deleteIndexFromList(int position){
        Log.wtf("deleteIndexFromList()","Deleting from index: "+position);
        report_firenotif_ids_list.remove(position);
        report_images_list.remove(position);
        report_coordinates_list.remove(position);
        report_datetime_list.remove(position);
        report_firestatus_list.remove(position);
        report_alarmlevel_list.remove(position);
        report_additionalInfo_list.remove(position);
        tab2_listview.setAdapter(null);
        adapter = new ReportAdapter(staticContext,
                report_firenotif_ids_list,
                report_images_list,
                report_coordinates_list,
                report_datetime_list,
                report_firestatus_list,
                report_alarmlevel_list,
                report_additionalInfo_list);
        tab2_listview.setAdapter(adapter);

        Log.wtf("deleteIndexFromList()","report_firenotif_ids_list size: "+report_firenotif_ids_list.size());
        if(report_firenotif_ids_list.size()==0){
            showTab2MessageLayout(true,"No Fire Reports",true,"Refresh");
        }
    }

    //************************************************************

    //LOCATION
    protected static void getCurrentLocation(){
        if(isLocationPermissionGranted()){
            Log.wtf("get current location","Permission is Granted");
            if(isLocationEnabled(staticContext)){
                requestLocationUpdate();
                Log.wtf("get current location","Location is enabled");
            }else{
                Log.wtf("get current location","Location is disabled");
                openGPSinSettings();
            }
        }else{
            //request permission
            Log.wtf("getCurrentLocation","Permission not granted");
            Toast.makeText(staticContext, "Grant Permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(staticActivtity,new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
    }
    protected static void locationManagerInitialize(){
        Log.wtf("locationInitialize","called");
        locationManager = (LocationManager) staticActivtity.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.wtf("onLocationChange","Location is changed "+location);
                myLocation = new LatLng(location.getLatitude(),location.getLongitude());
                Fragment_truck_map.showLoadingLayout(false,false,"");
                if(Fragment_truck_map.origin_marker!=null){
                    Fragment_truck_map.origin_marker.setPosition(myLocation);
                }
                Toast.makeText(staticContext,"Location Captured",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }
    protected static void requestLocationUpdate() {
        try {
            locationManagerInitialize();
            if (ActivityCompat.checkSelfPermission(staticActivtity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Log.wtf("request Location","Called");

            Fragment_truck_map.showLoadingLayout(true,true,"Getting your location");
            locationManager.requestLocationUpdates("gps", 10000, 0, locationListener);
        }catch (Exception e){
            Toast.makeText(staticContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.wtf("getLocation Error",e.getMessage());
        }
    }
    protected static boolean isLocationPermissionGranted(){
        int locationCheck = ActivityCompat.checkSelfPermission(staticContext,Manifest.permission.ACCESS_FINE_LOCATION);
        if(locationCheck == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
    protected static void openGPSinSettings(){
        new android.app.AlertDialog.Builder(staticActivtity)
                .setTitle("Turn On Location")
                .setMessage("This Application Requires Location")
                .setCancelable(false)
                .setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.wtf("TurnOnGPSTracking","Settings intent is called");
                        staticActivtity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),OPEN_GPS_SETTINGS_REQUEST);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.wtf("TurnOnGPSTracking","User clicked Cancel");
                        Toast.makeText(staticContext, "Locations is required", Toast.LENGTH_SHORT).show();
                        staticActivtity.finish();
                    }
                })
                .show();
    }
    protected void stopLocationListener(){
        if(locationManager!=null) {
            locationManager.removeUpdates(locationListener);
            locationManager=null;
        }
    }


    //PERMISSION
    public static void checkPermission() {
        Log.wtf("CHECKPERMISSION()", "CALLED");
        getCurrentLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try{

             if(requestCode == LOCATION_PERMISSION){
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.wtf("permissionResult","Fine Location is granted");
                    getCurrentLocation();
                }else{
                    Log.wtf("permissionResult","Fine Location is NOT granted");
                    //NOT GRANTED
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    if(!showRationale){
                        //USER SELECTED DO NOT SHOW AGAIN
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Allow Permission");
                        builder.setMessage("You cannot use this app without permission. Please allow the permission.");
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)),OPEN_PERMISSION_REQUEST);
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(Activity_main_truck.this, "Location permission is required", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                        builder.show();
                    }
                    else{

                    }
                }
            }
        }catch (Exception e){
            Toast.makeText(Activity_main_truck.this, "Exception Handled ", Toast.LENGTH_SHORT).show();
            Log.wtf("onPermissionResult:ERROR",e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

         if(requestCode ==  OPEN_GPS_SETTINGS_REQUEST){
            Log.wtf("activity result","Result for opening gps location");
            if(isLocationEnabled(this)){
                Log.wtf("activity_result","location is not opened");
                requestLocationUpdate();
            }else{
                openGPSinSettings();
                Log.wtf("activity_result","location is not opened");
            }
        }
        else if(requestCode == OPEN_PERMISSION_REQUEST){
            if(isLocationPermissionGranted()){
                getCurrentLocation();
            }else{
                finish();
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onBackPressed() {
        switch (bottomNavigation.getCurrentItem()){
            case 0:
                //map
                if(fullscreen){
                    //exitFullScreenMap();
                }else{
                    new AlertDialog.Builder(this)
                            .setTitle("Closing")
                            .setMessage("You're about to exit the app, continue?")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
                break;
            case 1:
                //reports
                if(imageIsZoomed){
                    zoomImageExit();
                }else{
                    bottomNavigation.setCurrentItem(0);
                }
                break;
            case 2:
                //my account
                super.onBackPressed();
                break;
            default:
                Log.wtf("onBackPressed","DEFAULT IN SWITCH");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.truck_main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_logout){
            logout();
        }
        else if(id == R.id.menu_settings){
            startActivity(new Intent(Activity_main_truck.this, Activity_User_Settings.class));
        }
        return true;
    }

    protected void logout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logging out");
        builder.setMessage("Continue to log-out?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getSharedPreferences(MySharedPref.SHAREDPREF_NAME,MODE_PRIVATE);
                sharedPreferences.edit().clear().commit();
                stopService(new Intent(Activity_main_truck.this,Service_Notification.class));
                dbHelper.removeAllData();
                startActivity(new Intent(Activity_main_truck.this,SplashScreen.class));
                finish();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });
        builder.show();
    }

}
