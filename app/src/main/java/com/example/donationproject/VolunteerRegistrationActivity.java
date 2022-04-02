package com.example.donationproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class VolunteerRegistrationActivity extends AppCompatActivity {

    private TextView backButton;

    private CircleImageView profile_image;

    private TextInputEditText registername, registerIdNumber,
            registerEmailID, registerPhoneNumber, registerEmail, registerPassword;

    private Button registerButton;

    private Uri resultUri;

    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_volunteer_registration);

            backButton = findViewById(R.id.backButton);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(VolunteerRegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });

            profile_image = findViewById(R.id.profile_image);
            registername = findViewById(R.id.registername);
            registerIdNumber = findViewById(R.id.registerIdNumber);
            registerEmailID = findViewById(R.id.registerEmailID);
            registerPhoneNumber = findViewById(R.id.registerPhoneNumber);
            registerEmail = findViewById(R.id.registerEmail);
            registerPassword = findViewById(R.id.registerPassword);
            registerButton = findViewById(R.id.registerButton);
            loader = new ProgressDialog(this);

            mAuth = FirebaseAuth.getInstance();

            profile_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);
                }
            });

            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String email = registerEmail.getText().toString().trim();
                    final String password = registerPassword.getText().toString().trim();
                    final String name = registername.getText().toString().trim();
                    final String IdNumber = registerIdNumber.getText().toString().trim();
                    final String phoneNumber = registerPhoneNumber.getText().toString().trim();
                    final String emailID = registerEmailID.getText().toString().trim();

                    if (TextUtils.isEmpty(email)){
                        registerEmail.setError("Email is required");
                        return;
                    }
                    if (TextUtils.isEmpty(password)){
                        registerPassword.setError("Password is required");
                        return;
                    }
                    if (TextUtils.isEmpty(name)){
                        registername.setError("Name is required");
                        return;
                    }
                    if (TextUtils.isEmpty(IdNumber)){
                        registerIdNumber.setError("IdNumber is required");
                        return;
                    }
                    if (TextUtils.isEmpty(phoneNumber)){
                        registerPhoneNumber.setError("PhoneNumber is required");
                        return;
                    }
                    if (TextUtils.isEmpty(emailID)){
                        registerEmailID.setError("EmailID is required");
                        return;
                    }

                    else {
                        loader.setMessage("Registering you...");
                        loader.setCanceledOnTouchOutside(false);
                        loader.show();

                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){
                                    String error = task.getException().toString();
                                    Toast.makeText(VolunteerRegistrationActivity.this, "Error" + error, Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    String currentUserId = mAuth.getCurrentUser().getUid();
                                    userDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(currentUserId);
                                    HashMap userinfo = new HashMap();
                                    userinfo.put("id", currentUserId);
                                    userinfo.put("name", name);
                                    userinfo.put("email", emailID);
                                    userinfo.put("IdNumber", IdNumber);
                                    userinfo.put("phonenumber", phoneNumber);

                                    userDatabaseRef.updateChildren(userinfo).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(VolunteerRegistrationActivity.this, "Data set Successful", Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(VolunteerRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }

                                            finish();
                                            //loader.dismiss();
                                        }
                                    });

                                    if (resultUri !=null){
                                        final StorageReference filePath = FirebaseStorage.getInstance().getReference()
                                                .child("profile images").child(currentUserId);
                                        Bitmap bitmap = null;

                                        try {
                                            bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                                        }catch (IOException e){
                                            e.printStackTrace();
                                        }
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG,20, byteArrayOutputStream);
                                        byte[] data = byteArrayOutputStream.toByteArray();
                                        UploadTask uploadTask = filePath.putBytes(data);

                                        uploadTask.addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(VolunteerRegistrationActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                if(taskSnapshot.getMetadata() !=null && taskSnapshot.getMetadata().getReference() !=null){
                                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            String imageUrl = uri.toString();
                                                            Map newImageMap = new HashMap();
                                                            newImageMap.put("profilepictureurl", imageUrl);

                                                            userDatabaseRef.updateChildren(newImageMap).addOnCompleteListener(new OnCompleteListener() {
                                                                @Override
                                                                public void onComplete(@NonNull Task task) {
                                                                    if (task.isSuccessful()){
                                                                        Toast.makeText(VolunteerRegistrationActivity.this, "Image url added to database successfully", Toast.LENGTH_SHORT).show();
                                                                    }else {
                                                                        Toast.makeText(VolunteerRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });

                                                            finish();

                                                        }
                                                    });
                                                }
                                            }
                                        });

                                        Intent intent = new Intent(VolunteerRegistrationActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                        loader.dismiss();

                                    }
                                }

                            }
                        });

                    }

                }
            });
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==1 && resultCode == RESULT_OK && data !=null){
            resultUri = data.getData();
            profile_image.setImageURI(resultUri);
        }
    }
    }
