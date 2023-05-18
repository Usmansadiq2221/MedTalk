package com.devtwist.medtalk.Models;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendNotification {

    //used to store the value passed from activity class...
    private String title, notifyBody, token, senderUid, senderType;
    private Activity mActivity;

    private String toGo;

    private RequestQueue requestQueue;


    //empty constructor...
    public SendNotification() {
    }

    //initialize the values in constructor passed by the activity class...


    public SendNotification(String title, String notifyBody, String token, String senderUid, String senderType, Activity mActivity, String toGo) {
        this.title = title;
        this.notifyBody = notifyBody;
        this.token = token;
        this.senderUid = senderUid;
        this.senderType = senderType;
        this.mActivity = mActivity;
        this.toGo = toGo;
    }

    public void sendNotification(){
        try {
            requestQueue = Volley.newRequestQueue(mActivity);
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject notifyData = new JSONObject();
            JSONObject extraData = new JSONObject();

            notifyData.put("title", title);
            notifyData.put("body", notifyBody);

            extraData.put("userType", senderType);
            extraData.put("userID", senderUid);
            extraData.put("toGo", toGo);


            JSONObject mainObj = new JSONObject();
            mainObj.put("notification", notifyData);
            mainObj.put("data",extraData);
            mainObj.put("to", token);





            JsonObjectRequest request = new JsonObjectRequest(url, mainObj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("NtfyError", error.toString());
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<>();
                    String serverKey = "Key=AAAA38XGEIY:APA91bFsU4DOvo084WOnGWUxaRMpVvXKw6SL9bJr2JJJqBYvYuI0f5_f33e-yDzOPNNfq3-_8E0z10wCslnWS2v1nEbQgsC36eJq3G-JITXeOwt48wRUnfyOyacPSIqjsYbH_F770KMq";
                    map.put("Authorization", serverKey);
                    map.put("Content-Type", "application/json");
                    return map;
                }
            };
            requestQueue.add(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
