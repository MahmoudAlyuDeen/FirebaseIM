package afterapps.com.firebaseim.thread;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import afterapps.com.firebaseim.Constants;
import afterapps.com.firebaseim.R;
import afterapps.com.firebaseim.beans.User;
import afterapps.com.firebaseim.login.LoginActivity;
import afterapps.com.firebaseim.widgets.EmptyStateRecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;

public class ThreadActivity extends AppCompatActivity implements ValueEventListener {

    @BindView(R.id.activity_thread_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_thread_messages_recycler)
    EmptyStateRecyclerView messagesRecycler;
    @BindView(R.id.activity_thread_send_fab)
    FloatingActionButton sendFab;
    @BindView(R.id.activity_thread_input_edit_text)
    TextInputEditText inputEditText;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @State
    String ownerUid;
    @State
    String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        Icepick.restoreInstanceState(this, savedInstanceState);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (savedInstanceState == null) {
            userUid = getIntent().getStringExtra(Constants.USER_ID_EXTRA);
        }

        initializeUserListener();
        initializeAuthListener();
    }

    private void initializeUserListener() {
        DatabaseReference userReference = mDatabase
                .child("users")
                .child(userUid);

        userReference.addListenerForSingleValueEvent(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void initializeAuthListener() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    ownerUid = user.getUid();
                    initializeMessagesRecycler();

                    Log.d("@@@@", "thread:signed_in:" + user.getUid());
                } else {
                    Log.d("@@@@", "thread:signed_out");
                    Intent login = new Intent(ThreadActivity.this, LoginActivity.class);
                    startActivity(login);
                    finish();
                }
            }
        };
    }

    private void initializeMessagesRecycler() {
        MessagesAdapter adapter = new MessagesAdapter(this, ownerUid, mDatabase.child("messages"));
        //todo: figure out a proper structure to store and query messages
        //todo: messages must by sorted by timestamp
        //todo: keep in mind, you can perform operations server side!
    }

    @OnClick(R.id.activity_thread_send_fab)
    public void onClick() {
        //todo: send message
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        User user = dataSnapshot.getValue(User.class);
        displayUserDetails(user);
    }

    private void displayUserDetails(User user) {
        //todo: display name and profile picture in toolbar, WhatsApp style
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Toast.makeText(this, R.string.error_loading_user, Toast.LENGTH_SHORT).show();
        finish();
    }
}
