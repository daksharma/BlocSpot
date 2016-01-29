package com.daksharma.android.blocspot.model.database;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObject;
import io.realm.RealmSchema;

/**
 * Created by Daksh on 1/28/16.
 */
public class RealmDataBaseHelper implements RealmMigration {


    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema scheme = realm.getSchema();

        // TODO: figure out data migration

    }
}
