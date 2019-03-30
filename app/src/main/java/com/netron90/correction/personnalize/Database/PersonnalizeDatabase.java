package com.netron90.correction.personnalize.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by CHRISTIAN on 17/02/2019.
 */

@Database(entities = {DocumentUser.class, DiapositiveFormat.class, DiapoImagePath.class,
        DocumentAvailable.class, UserMessageDb.class}, version = 1)
public abstract class PersonnalizeDatabase extends RoomDatabase {

    public abstract UserDao userDao();
}
