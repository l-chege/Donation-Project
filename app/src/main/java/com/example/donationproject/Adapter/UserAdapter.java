package com.example.donationproject.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.example.donationproject.Email.JavaMailApi;
import com.example.donationproject.Model.User;
import com.example.donationproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.Viewholder>{

    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.user_displayed_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        final User user = userList.get(position);

        holder.type.setText(user.getType());

        //filter donors out
        if (user.getType().equals("donor")){
            holder.emailNow.setVisibility(View.VISIBLE);
        }

        holder.userEmail.setText(user.getEmail());
        holder.phoneNumber.setText(user.getPhonenumber());
        holder.userName.setText(user.getName());
        holder.donationType.setText(user.getDonationtype());

        //fetch image with glide
        @GlideModule
        class MyAppGlideModule extends AppGlideModule {
            @Override
            public boolean isManifestParsingEnabled() {
                return false;
            }
        }

        final String nameOfTheReceiver = user.getName();
        final String idOfTheReceiver = user.getId();

        //sending the email by setting on click

        holder.emailNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("SEND EMAIL")
                        .setMessage("Send email to" + user.getName()+ "?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        reference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                String nameOfSender = snapshot.child("name").getValue().toString();
                                                String email = snapshot.child("email").getValue().toString();
                                                String phone = snapshot.child("phoneNumber").getValue().toString();
                                                String donation = snapshot.child("donationType").getValue().toString();

                                                String  mEmail = user.getEmail();
                                                String  mSubject = "DONATION REQUEST";
                                                String mMessage = "Hello "+ nameOfTheReceiver+", "+nameOfSender+
                                                        "would like donation from you. Here's his/her details:\n"
                                                        +"Name: "+nameOfSender+ "\n"+
                                                        "Phone Number: "+phone+ "\n"+
                                                        "Email: "+email+ "\n"+
                                                        "Kindly Reach out to him/her. Thank you!\n"
                                                        +"DONATION APP - DONATE, SAVE LIVES!";

                                                JavaMailApi javaMailApi = new JavaMailApi(context, mEmail, mSubject, mMessage);
                                                javaMailApi.execute();


                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("emails")
                                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                ref.child(idOfTheReceiver).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference("emails")
                                                                    .child(idOfTheReceiver);
                                                            receiverRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);

                                                            addNotifications(idOfTheReceiver, FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                        }

                                                    }
                                                });


                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        })
                .setNegativeButton("No", null)
                        .show();

            }
        });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder{

        private CircleImageView userProfileImage;
        private TextView type, userName, userEmail, phoneNumber, donationType;
        private Button emailNow;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            type = itemView.findViewById(R.id.type);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            phoneNumber = itemView.findViewById(R.id.phoneNumber);
            donationType = itemView.findViewById(R.id.donationType);
            emailNow = itemView.findViewById(R.id.emailNow);

        }
    }

    private void addNotifications(String receiverId, String senderId){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference().child("notifications").child(receiverId);
        String date = DateFormat.getDateInstance().format(new Date());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("receiverId", receiverId);
        hashMap.put("senderId", senderId);
        hashMap.put("text", "Sent you an email, kindly check it out!");
        hashMap.put("date", date);

        reference.push().setValue(hashMap);
    }

}
