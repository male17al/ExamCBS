package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.cbsexam.UserEndpoints;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;
  private static UserEndpoints userEndpoints;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        null,
                        rs.getString("email"));


        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        null,
                        rs.getString("email"));


        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it. : FIX
    //Hashing the password
    user.setPassword(Hashing.md5(user.getPassword()));
    int userID = dbCon.insert(
            "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
                    + user.getFirstname()
                    + "', '"
                    + user.getLastname()
                    + "', '"
                    + user.getPassword()
                    + "', '"
                    + user.getEmail()
                    + "', "
                    + user.getCreatedTime()
                    + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else {
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }

  public static void deleteUser(int userID) {

    // Write in log that we've reached this step
    Log.writeLog(UserController.class.getName(), userID, "Actually deleting a user in the DB", 0);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Delete user in DB
    dbCon.delete(
            "DELETE FROM user where id=" + userID);


  }

  public static User updateUser(int idUser, User newUserData) {

    // Write in log that we've reached this step
    Log.writeLog(UserController.class.getName(), newUserData, "Actually updating a user in the DB", 0);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //User password is being hashed before updating
    newUserData.setPassword(Hashing.md5(newUserData.getPassword()));
    //Update user data in DB
    dbCon.update(
            "UPDATE user SET first_name='" + newUserData.getFirstname() + "', last_name='" + newUserData.getLastname() + "', password='" + newUserData.getPassword() + "', email='" + newUserData.getEmail() + "' WHERE id='" + idUser + "'");

    return newUserData;
  }

  public static User authorizeUser(String email, String password) {
    //check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //Hashing the user password:

    //Build the query for DB
    String sql = "SELECT * FROM user where email='" + email + "' AND password='" + password + "'";

    //Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));


        //Return the created object
        return user;
      } else {
        System.out.println("User not found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    //Return null
    return user;
  }

}
