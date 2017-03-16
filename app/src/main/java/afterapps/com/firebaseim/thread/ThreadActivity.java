package afterapps.com.firebaseim.thread;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

import afterapps.com.firebaseim.BaseActivity;
import afterapps.com.firebaseim.Constants;
import afterapps.com.firebaseim.R;
import afterapps.com.firebaseim.beans.Message;
import afterapps.com.firebaseim.beans.User;
import afterapps.com.firebaseim.login.LoginActivity;
import afterapps.com.firebaseim.widgets.EmptyStateRecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ThreadActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.activity_thread_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_thread_messages_recycler)
    EmptyStateRecyclerView messagesRecycler;
    @BindView(R.id.activity_thread_send_fab)
    FloatingActionButton sendFab;
    @BindView(R.id.activity_thread_input_edit_text)
    TextInputEditText inputEditText;
    @BindView(R.id.activity_thread_empty_view)
    TextView emptyView;
    @BindView(R.id.activity_thread_editor_parent)
    RelativeLayout editorParent;
    @BindView(R.id.activity_thread_progress)
    ProgressBar progress;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @State
    String userUid;
    @State
    boolean emptyInput;

    private User user;
    private FirebaseUser owner;

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
        sendFab.requestFocus();

        loadUserDetails();
        initializeAuthListener();
        initializeInteractionListeners();
    }

    private void initializeInteractionListeners() {
        inputEditText.addTextChangedListener(this);
    }

    private void loadUserDetails() {
        DatabaseReference userReference = mDatabase
                .child("users")
                .child(userUid);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                initializeMessagesRecycler();
                displayUserDetails();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ThreadActivity.this, R.string.error_loading_user, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void initializeAuthListener() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                owner = firebaseAuth.getCurrentUser();
                if (owner != null) {
                    initializeMessagesRecycler();

                    Log.d("@@@@", "thread:signed_in:" + owner.getUid());
                } else {
                    Log.d("@@@@", "thread:signed_out");
                    Intent login = new Intent(ThreadActivity.this, LoginActivity.class);
                    startActivity(login);
                    finish();
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void initializeMessagesRecycler() {
        if (user == null || owner == null) {
            Log.d("@@@@", "initializeMessagesRecycler: User:" + user + " Owner:" + owner);
            return;
        }
        Query messagesQuery = mDatabase
                .child("messages")
                .child(owner.getUid())
                .child(user.getUid())
                .orderByChild("negatedTimestamp");
        MessagesAdapter adapter = new MessagesAdapter(this, owner.getUid(), messagesQuery);
        messagesRecycler.setAdapter(null);
        messagesRecycler.setAdapter(adapter);
        messagesRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        messagesRecycler.setEmptyView(emptyView);
        messagesRecycler.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                messagesRecycler.smoothScrollToPosition(0);
            }
        });
    }

    @OnClick(R.id.activity_thread_send_fab)
    public void onClick() {
        if (user == null || owner == null) {
            Log.d("@@@@", "onSendClick: User:" + user + " Owner:" + owner);
            return;
        }
        long timestamp = new Date().getTime();
        long dayTimestamp = getDayTimestamp(timestamp);
        String body = inputEditText.getText().toString().trim();
        String ownerUid = owner.getUid();
        String userUid = user.getUid();
        Message message =
                new Message(timestamp, -timestamp, dayTimestamp, body, ownerUid, userUid);
        mDatabase
                .child("notifications")
                .child("messages")
                .push()
                .setValue(message);
        mDatabase
                .child("messages")
                .child(userUid)
                .child(ownerUid)
                .push()
                .setValue(message);
        if (!userUid.equals(ownerUid)) {
            mDatabase
                    .child("messages")
                    .child(ownerUid)
                    .child(userUid)
                    .push()
                    .setValue(message);
        }
        inputEditText.setText("");
    }

    @Override
    protected void displayLoadingState() {
        //was considering a progress bar but firebase offline database makes it unnecessary

        //TransitionManager.beginDelayedTransition(editorParent);
        progress.setVisibility(isLoading ? VISIBLE : INVISIBLE);
        //displayInputState();
    }

    private void displayInputState() {
        //inputEditText.setEnabled(!isLoading);
        sendFab.setEnabled(!emptyInput && !isLoading);
        //sendFab.setImageResource(isLoading ? R.color.colorTransparent : R.drawable.ic_send);
    }

    private long getDayTimestamp(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTimeInMillis();
    }

    private void displayUserDetails() {
        //todo[improvement]: maybe display the picture in the toolbar.. WhatsApp style
        toolbar.setTitle(user.getDisplayName());
        //toolbar.setSubtitle(user.getEmail());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        emptyInput = s.toString().trim().isEmpty();
        displayInputState();
    }
}
