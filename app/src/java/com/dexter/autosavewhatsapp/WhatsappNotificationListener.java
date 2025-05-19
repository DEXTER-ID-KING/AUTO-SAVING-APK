package com.dexter.autosavewhatsapp;

import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.provider.ContactsContract;
import android.content.ContentProviderOperation;
import android.net.Uri;
import android.database.Cursor;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class WhatsappNotificationListener extends NotificationListenerService {

    SharedPreferences prefs;
    String baseName;
    int contactCount;
    boolean isEnabled;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        baseName = prefs.getString("custom_name", "Dexter");
        contactCount = prefs.getInt("contact_count", 1);
        isEnabled = prefs.getBoolean("enabled", true);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!isEnabled) return;

        if (!sbn.getPackageName().equals("com.whatsapp")) return;

        Notification notif = sbn.getNotification();
        Bundle extras = notif.extras;
        if (extras == null) return;

        String title = extras.getString("android.title");

        if (title != null && isPhoneNumber(title) && !isNumberSaved(title)) {
            saveContact(title);
        }
    }

    private boolean isPhoneNumber(String number) {
        return number.matches("^\+?[0-9]{8,15}$");
    }

    private boolean isNumberSaved(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    private void saveContact(String number) {
        String name = baseName + " " + contactCount;

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
            .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            .build());

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            contactCount++;
            prefs.edit().putInt("contact_count", contactCount).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}