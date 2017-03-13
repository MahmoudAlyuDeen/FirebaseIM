package afterapps.com.firebaseim.home;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import org.greenrobot.eventbus.EventBus;

import afterapps.com.firebaseim.R;
import afterapps.com.firebaseim.beans.User;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/*
 * Created by Mahmoud on 3/13/2017.
 */

class UsersAdapter extends FirebaseRecyclerAdapter<User, UsersAdapter.UserViewHolder> {

    private final Context context;

    UsersAdapter(Context context, Query ref) {
        super(User.class, R.layout.item_user, UserViewHolder.class, ref);
        this.context = context;
    }

    @Override
    protected void populateViewHolder(UserViewHolder holder, User user, int position) {
        holder.setUser(user);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);

        return new UserViewHolder(itemView);
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.item_user_image_view)
        ImageView itemUserImageView;
        @BindView(R.id.item_friend_name_text_view)
        TextView itemFriendNameTextView;
        @BindView(R.id.item_friend_email_text_view)
        TextView itemFriendEmailTextView;
        @BindView(R.id.item_user_parent)
        CardView itemUserParent;

        UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemUserParent.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_user_parent:
                    EventBus.getDefault().post(getRef(getLayoutPosition()));
                    break;
            }
        }

        void setUser(User user) {
            itemFriendNameTextView.setText(user.getDisplayName());
            itemFriendEmailTextView.setText(user.getEmail());
            Glide.with(context)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.placeholder_user)
                    .centerCrop()
                    .dontAnimate()
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(itemUserImageView);
        }
    }
}
