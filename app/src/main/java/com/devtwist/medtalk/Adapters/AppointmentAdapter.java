package com.devtwist.medtalk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.medtalk.Activities.ViewAppointmentActivity;
import com.devtwist.medtalk.Models.AppointmentData;
import com.devtwist.medtalk.Models.AppointmentListData;
import com.devtwist.medtalk.Models.DoctorData;
import com.devtwist.medtalk.Models.PatientData;
import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>{

    //for storing context of the activity passed from Call Fragment...
    private Context context;

    //for storing the list of calls data...
    private ArrayList<AppointmentListData> appointmentList;


    private String myId, accType = "";

    //initializing the list and variables and list in constructor passed from the NotificationActivity...
    public AppointmentAdapter(Context context, ArrayList<AppointmentListData> appointmentList, String myId) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.myId = myId;
    }


    // We are inflating the notification_items
    // inside on Create View Holder method...
    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_item, parent,false);
        return new AppointmentViewHolder(view);
    }

    // Inside on bind view holder we will
    // set data to item of Recycler View...
    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        AppointmentListData model = appointmentList.get(position);

        FirebaseDatabase.getInstance().getReference().child("Appointments").child(model.getAppointId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                AppointmentData appointmentData = snapshot.getValue(AppointmentData.class);
                                holder.appointDateItem.setText(appointmentData.getAppointDate());
                                holder.appointTimeItem.setText(appointmentData.getAppointTime());

                                SharedPreferences preferences = context.getSharedPreferences("MedTalkData", Context.MODE_PRIVATE);
                                accType = preferences.getString("accType", "");
                                if (accType.equals("Doctor")) {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(model.getClientType())
                                            .child(model.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (model.getClientType().equals("Patient")){
                                                        PatientData userdata = snapshot.getValue(PatientData.class);
                                                        Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(holder.appointProfileItem);
                                                        holder.appointName.setText(userdata.getUsername());
                                                        holder.clientTypeItem.setText("Appointment with Patient");
                                                        holder.clientTypeItem.setVisibility(View.VISIBLE);
                                                    }
                                                    else if (model.getClientType().equals("Student")){
                                                        StudentData userdata = snapshot.getValue(StudentData.class);
                                                        Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(holder.appointProfileItem);
                                                        holder.appointName.setText(userdata.getUsername());
                                                        holder.docAPCategoryItem.setText(userdata.getCategory());
                                                        holder.docAPCategoryItem.setVisibility(View.VISIBLE);
                                                        holder.clientTypeItem.setText("Appointment with Student");
                                                        holder.clientTypeItem.setVisibility(View.VISIBLE);
                                                        holder.docHospital.setText(userdata.getHospital());
                                                        holder.docHospital.setVisibility(View.VISIBLE);
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                }
                                else if (accType.equals("Student") || accType.equals("Patient")){
                                    FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor")
                                            .child(model.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    DoctorData userdata = snapshot.getValue(DoctorData.class);
                                                    Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(holder.appointProfileItem);
                                                    holder.appointName.setText(userdata.getUsername());
                                                    holder.docAPCategoryItem.setText(userdata.getCategory());
                                                    holder.docAPCategoryItem.setVisibility(View.VISIBLE);
                                                    holder.clientTypeItem.setText("Appointment with Doctor");
                                                    holder.clientTypeItem.setVisibility(View.VISIBLE);
                                                    holder.docHospital.setText(userdata.getHospital());
                                                    holder.docHospital.setVisibility(View.VISIBLE);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        holder.viewAppointItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewAppointmentActivity.class);
                intent.putExtra("appointId",model.getAppointId());
                context.startActivity(intent);
            }
        });

    }

    // this method will return the count of our list...
    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public class AppointmentViewHolder extends RecyclerView.ViewHolder{

        // Adapter class for initializing
        // the views of our notification_items view...
        private ImageView appointProfileItem;
        private TextView appointName, docAPCategoryItem, appointDateItem, appointTimeItem, docHospital, clientTypeItem;
        private Button viewAppointItem;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            appointProfileItem = (ImageView) itemView.findViewById(R.id.appointProfileItem);
            appointName = (TextView) itemView.findViewById(R.id.appointName);
            docAPCategoryItem = (TextView) itemView.findViewById(R.id.docAPCategoryItem);
            appointDateItem = (TextView) itemView.findViewById(R.id.appointDateItem);
            appointTimeItem = (TextView) itemView.findViewById(R.id.appointTimeItem);
            docHospital = (TextView) itemView.findViewById(R.id.docHospital);
            clientTypeItem = (TextView) itemView.findViewById(R.id.clientTypeItem);
            viewAppointItem = (Button) itemView.findViewById(R.id.viewAppointItem);
        }
    }
}
