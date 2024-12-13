package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserDao {
    private final SQLiteDatabase database;

    public UserDao(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    // Insert new user
    public boolean registerUser(String username, String password) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);

        long result = database.insert(DatabaseHelper.TABLE_USERS, null, values);
        return result != -1; // Success if result != -1
    }

    // Check user credentials
    public boolean checkUser(String username, String password) {
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.COLUMN_USERNAME + " = ? AND " +
                DatabaseHelper.COLUMN_PASSWORD + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{username, password});
        boolean exists = cursor.moveToFirst(); // Check if query returned any rows
        cursor.close();
        return exists;
    }
}
