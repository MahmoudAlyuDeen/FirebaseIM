package afterapps.com.firebaseim;

/*
 * Created by Mahmoud on 3/13/2017.
 */

import com.google.firebase.database.FirebaseDatabase;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}
