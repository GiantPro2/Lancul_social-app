package com.arabian.lancul.UI.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.arabian.lancul.R;
import com.arabian.lancul.UI.Object.Guider;
import com.arabian.lancul.UI.Object.Invite;
import com.arabian.lancul.UI.Util.Global;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InviteGuiderActivity extends AppCompatActivity {

    private ImageView guider_photo;
    private TextView guider_bio;
    private Guider guider;
    private Button btn_send;
    private EditText invite_message;
    private String TAG = "Invite";
    private ProgressDialog wait;
    private TextView guider_name, guider_rating, send_date;
    private Integer position;
    private Spinner require_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_guider);
        Intent intent = getIntent();
        position = intent.getIntExtra("Index",0);
        guider = Global.array_guider.get(position);
        init_view();
    }

    private void init_view() {
        guider_photo = findViewById(R.id.invite_guider_photo);
        guider_bio = findViewById(R.id.invite_guider_bio);
        if(!guider.getImageURL().equals(""))
        {
            Glide.with(InviteGuiderActivity.this).load(guider.getImageURL()).into(guider_photo);
        }
        guider_bio.setText(guider.getBio().toString());
        btn_send = findViewById(R.id.btn_send_invite);
        invite_message = findViewById(R.id.edt_invoice);
        guider_name = findViewById(R.id.guider_name);
        guider_rating =  findViewById(R.id.guider_rating);
        send_date = findViewById(R.id.send_date_time);
        require_time = findViewById(R.id.needed_time);
        guider_name.setText(guider.getName());
        guider_rating.setText(String.valueOf(guider.getRate()));
        send_date.setText(Global.getToday());
        wait = new ProgressDialog(this);
        wait.setTitle(getString(R.string.progress_send_invite));
        wait.setCancelable(false);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_invite();
            }
        });
    }

    private void send_invite() {
        wait.show();
        String message = invite_message.getText().toString();
        String date = Global.getToday();
        FirebaseApp.initializeApp(LoginActivity.getInstance());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Invite invite = new Invite();
        invite.setInvite_content(message);
        invite.setInvite_date(date);
        invite.setInvite_sender_name(Global.my_name);
        invite.setInvite_sender_email(Global.my_email);
        invite.setInvite_status("New");
        invite.setInvite_require_time(require_time.getSelectedItem().toString());
        db.collection("guiders").document(guider.getEmail()).collection("invite").document(Global.my_email)
                .set(invite)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "upload user data:success");
                        upgrade_my_data(guider.getEmail());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"Failed");
                        wait.dismiss();
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, "Failed to send invite.", Snackbar.LENGTH_SHORT).show();

                    }
                });

    }

    private void upgrade_my_data(String guider_email) {
        FirebaseApp.initializeApp(LoginActivity.getInstance());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> Invite = new HashMap<>();
        List<String> linked_guiders;
        if(Global.my_user_data.getLinked_guiders()!=null) {
            linked_guiders = Global.my_user_data.getLinked_guiders();
        }
        else{
            linked_guiders = new ArrayList<String>();
        }
        linked_guiders.add(guider_email);
        Invite.put("user_linked_guiders",linked_guiders);
        db.collection("users").document(Global.my_email)
                .update(Invite)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        wait.dismiss();
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, "Sending invite success.", Snackbar.LENGTH_SHORT).show();
                        Log.d(TAG, "send invite:success");
                        Intent intent = new Intent(InviteGuiderActivity.this, ChatActivity.class);
                        intent.putExtra("pending", true);
                        intent.putExtra("partner_index",position);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        wait.dismiss();
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, "Sending invite Failed.", Snackbar.LENGTH_SHORT).show();
                        Log.e(TAG,"Failed");
                        finish();
                    }
                });

    }
}
