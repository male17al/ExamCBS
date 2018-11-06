package controllers;

import java.security.Key;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.cbsexam.UserEndpoints;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("token"));


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
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("token"));


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

  public static User autorizeUser(String email, String password) {
    //check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

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
                        rs.getString("email"),
                        rs.getString("token"));

        //Return the created object
        return user;
      } else {
        System.out.println("User not found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    return user;
  }

  public static String updateToken(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually updating a token in DB", 0);

    //Creating token:
    //Source: https://github.com/auth0/java-jwt
    //https://github.com/jwtk/jjwt
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    long time = System.currentTimeMillis();
    String jwt = Jwts.builder()
            .signWith(key)
            .setSubject(Integer.toString(user.getId()))
            .setIssuedAt(new Date(time))
            .setExpiration(new Date(time + 30000))
            .compact();

    //Setting token
    user.setToken(jwt);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the token in the DB
    dbCon.update("UPDATE user SET token='" + jwt + "' WHERE id='" + user.getId() + "'");

    // Return user
    return jwt;

  }

  /*public static String getToken(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually getting a token in DB", 0);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Select token from DB
    dbCon.query("SELECT token FROM user WHERE id='" + user.getId() + "'");

    // Return token
    return user.getToken();
  }*/
}
