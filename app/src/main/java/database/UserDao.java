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

    // Get user ID by username
    public int getUserIdByUsername(String username) {
        int userId = -1; // Default to -1 if user not found
        String query = "SELECT " + DatabaseHelper.COLUMN_ID +
                " FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.COLUMN_USERNAME + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{username});
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        }
        cursor.close();
        return userId;
    }

    // Get username by user ID
    public String getUserName(int userId) {
        String username = null;
        String query = "SELECT " + DatabaseHelper.COLUMN_USERNAME +
                " FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.COLUMN_ID + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
        }
        cursor.close();
        return username;
    }

    // Update user password
    public boolean updateUserPassword(int userId, String newPassword) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PASSWORD, newPassword);

        int rowsUpdated = database.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        return rowsUpdated > 0; // Return true if update was successful
    }

    // Optional: Update username
    public boolean updateUserName(int userId, String newUsername) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, newUsername);

        int rowsUpdated = database.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        return rowsUpdated > 0; // Return true if update was successful
    }
}
