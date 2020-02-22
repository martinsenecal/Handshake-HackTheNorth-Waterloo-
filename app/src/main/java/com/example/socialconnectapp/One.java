package com.example.socialconnectapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class One extends Fragment {
    static String x;
    static String t;
    EditText editText, editText2;
    DatabaseReference firebase;
    FirebaseUser auth;
    SubClass subClass = new SubClass();
    TextView textView;
    String email;
    View view;
    EditText editText3;
    ImageView imageView;
    String photoURL;
    Uri uri;
    String downloadUrl;
    FirebaseAuth auth2;
    ProgressBar progressBar;
    private static final int img = 0;
    NfcAdapter nfcAdapter;
    // Flag to indicate that Android Beam is available
    boolean androidBeamAvailable  = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (View) inflater.inflate(R.layout.fragment_one, container, false);
        editText = view.findViewById(R.id.editText);
        editText2 = view.findViewById(R.id.editText2);
        textView = view.findViewById(R.id.textView);
        Button button  = view.findViewById(R.id.button);
       // editText3 = view.findViewById(R.id.editText4);
        imageView = view.findViewById(R.id.imageView);
        auth2 = FirebaseAuth.getInstance();
        loadUserInformation();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });
        view.findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInfo();
            }
        });
        // NFC isn't available on the device
        /***********************
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            Toast.makeText(getContext(), "Not Available", Toast.LENGTH_SHORT).show();
            /*
             * Disable NFC features here.
             * For example, disable menu items or buttons that activate
             * NFC-related features

            // Android Beam file transfer isn't supported
        } else {
            androidBeamAvailable = true;
            nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());
            Toast.makeText(getContext(), "Available", Toast.LENGTH_SHORT).show();
        }
        ***********************************/
        auth = FirebaseAuth.getInstance().getCurrentUser();
        firebase = FirebaseDatabase.getInstance().getReference().child("FIREBASE DATA");
        email = auth.getEmail().split("\\.")[0]+","+auth.getEmail().split("\\.")[1];
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDb = mDatabase.getReference();
        mDb.child("FIREBASE DATA").child(email.toLowerCase()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        /*
                        SubClass user = dataSnapshot.getValue(SubClass.class);
                        if(user!=null) {
                            x = user.getFirstName();
                            t = user.getLastName();
                            */
                            textView.setText("Hello " + firebase.child(email).child("firstName").toString() + "\t " + firebase.child(email).child("lastName").toString());
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = auth.getEmail().split("\\.")[0]+","+auth.getEmail().split("\\.")[1];
                String n = editText.getText().toString();
                String x = editText2.getText().toString();
                //  System.out.println(n);
                subClass.setFirstName(x);
                subClass.setLastName(n);
                firebase.child(email).setValue(subClass);
                Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(auth2.getCurrentUser() == null){
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        }
    }
    private void loadUserInformation() {
        FirebaseUser user = auth2.getCurrentUser();
        // System.out.println("USER"+user);
        System.out.println(user.toString());


        if(user.getPhotoUrl()!=null) {
            downloadUrl = user.getPhotoUrl().toString();
            if (user.getPhotoUrl().toString() != null) {
                photoURL = user.getPhotoUrl().toString();
                System.out.println("url"+photoURL);
                Glide.with(this).load(user.getPhotoUrl().toString()).into(imageView);

            }
            /*
            if (user.getDisplayName() != null) {
                String dp = user.getDisplayName();
                editText3.setText(user.getDisplayName());
            }
            */
        }


    }

    private void saveUserInfo() {
        /*
        String displayName = editText3.getText().toString();
        if(displayName.isEmpty()){
            editText3.setError("Name Required");
            editText3.requestFocus();
            return;
        }*/
        FirebaseUser user = auth2.getCurrentUser();
        System.out.println("USER"+user);
        System.out.println("url"+downloadUrl);

        if(user!=null&&downloadUrl!=null){
            System.out.println("EMAIL: "+user.getEmail());
            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(user.getEmail())
                    .setPhotoUri(uri.parse(downloadUrl))
                    .build();
            user.updateProfile(userProfileChangeRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == img && resultCode == RESULT_OK && data != null && data.getData()  !=null){
            uri = data.getData();
            uploadImageToFirebase();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void uploadImageToFirebase() {
       // progressBar.setVisibility(View.VISIBLE);
        final StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");
        if (uri != null) {

            profileImageRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                           // progressBar.setVisibility(View.GONE);
                            // profileImageUrl taskSnapshot.getDownloadUrl().toString(); //this is depreciated
                            //     downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                            //this is the new way to do it
                            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl = uri.toString();
                                    System.out.println("DOWNLOAD URL IS "+downloadUrl);
                                    Toast.makeText(getContext(), "Image Upload Successful", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "aaa "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showImageChooser () {
        //progressBar.setVisibility(View.VISIBLE);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), img);
    }
}
