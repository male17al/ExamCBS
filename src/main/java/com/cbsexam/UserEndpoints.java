package com.cbsexam;

import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import model.User;
import utils.Config;
import utils.Encryption;
import utils.Hashing;
import utils.Log;
import java.util.Date;

@Path("user")
public class UserEndpoints {

  //Creating a new instance of userCache
  UserCache userCache = new UserCache();

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    try {

      // Use the ID to get the user from the controller.
      User user = UserController.getUser(idUser);

      // TODO: Add Encryption to JSON : FIX
      // Convert the user object to json in order to return the object
      String json = new Gson().toJson(user);

      //Encryption added through the encryption method in utils
      json = Encryption.encryptDecryptXOR(json);

      // TODO: What should happen if something breaks down? : FIX
      //Failure-handling
      //Try-catch is cathing exceptions if something goes wrong

      /*Furthermore if-else construction is made to return either status code 200 (OK) if the user id exist
      or status code 404 (NOT FOUND) if user id doesn't exist.*/
      if (user != null) {
        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
      } else {
        //Returning status code 404 but showing "User ID not found" to the user.
        return Response.status(404).entity("User ID not found").build();
      }
    } catch (Exception e) {
      return Response.status(404).build();
    }
  }


  /**
   * @return Responses
   */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Making an arraylist and using caching layer
    //forceUpdate = false since arraylist is empty and we dont want to force update everytime we load users.
    ArrayList<User> users = userCache.getUsers(false);


    // TODO: Add Encryption to JSON : FIX
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);

    //Encryption added through the encryption method in utils
    json = Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);
    //force update cache when a new user is created
    userCache.getUsers(true);


    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system. FIX
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

    // Read the json from body and transfer it to a user class
    User userData = new Gson().fromJson(body, User.class);

    User userToLogin = UserController.authorizeUser(userData.getEmail(), Hashing.md5(userData.getPassword()));

    try {
      //Checking if user exists and if the user has a token
      if (userToLogin != null) {

        String token = createToken(userToLogin);
        //Setting token
        userToLogin.setToken(token);

        //Get token to json in order for us to print it later
        String json = new Gson().toJson(userToLogin.getToken());

        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("You have been logged in. This is your token: \n" + json).build();

      }
      else {
        return Response.status(400).entity("User not found. Email or password are wrong.").build();
      }
    } catch (Exception e) {
      return Response.status(404).build();
    }
  }


  // TODO: Make the system able to delete users : FIX

    @POST
    @Path("/deleteuser/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(String body) {

      try {
        // Read the json from body and transfer it to a user class
        User chosenUser = new Gson().fromJson(body, User.class);

        if (verifyToken(chosenUser.getToken(), chosenUser)) {

          // Use the controller to delete the user with the chosen user ID
          UserController.deleteUser((chosenUser.getId()));

          //Update the usercache if a user is deleted
          userCache.getUsers(true);


          // Return if the user could be deleted or not
          // Return a response with status 200 and JSON as type
          return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User with the userID " + chosenUser.getId() + " has been deleted").build();
        } else {
          return Response.status(400).entity("The token and user id do not correspond").build();
        }
      } catch (Exception e) {
        return Response.status(400).build();
      }
    }

  // TODO: Make the system able to update users : FIX
  @PUT
  @Path("/updateuser/")
  @Consumes(MediaType.APPLICATION_JSON)

  public Response updateUser(String body) {

    try {
      // Read the json from body and transfer it to a user class
      User newUserData = new Gson().fromJson(body, User.class);

      //Verifies if any of the fields are empty
      if (!newUserData.getFirstname().isEmpty() && !newUserData.getLastname().isEmpty() && !newUserData.getPassword().isEmpty() && !newUserData.getEmail().isEmpty()) {
      //Verifies the token if none of the above are empty
        if (verifyToken(newUserData.getToken(), newUserData)) {

          //Update the user with the chosen id with the new data
          User updatedUserData = UserController.updateUser(newUserData.getId(), newUserData);

          //Update the usercache if a user is updated
          userCache.getUsers(true);

          // Convert the user object to json in order to return the object
          String json = new Gson().toJson(updatedUserData);

          // Return a response with status 200 and JSON as type
          return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("The following data has been updated\n" + json).build();
        }
        else {
        return Response.status(404).entity("Could not verify token").build();
      }
      } else {
        return Response.status(400).entity("You can't leave any fields blank. \nIf there are any data you don't want to update simply just input your old data at that field.").build();
        }
    } catch (Exception e) {
      return Response.status(400).build();
    }
  }

  //Inspiration from source: https://github.com/auth0/java-jwt
private String createToken (User user) {
  try {
    Algorithm algorithm = Algorithm.HMAC256(Config.getTokenKey());
    String token = JWT.create()
            .withIssuer("auth0")
            .withIssuedAt(new Date(System.currentTimeMillis()))
            .withExpiresAt(new Date(System.currentTimeMillis() + 900000))
            .withSubject(Integer.toString(user.getId()))
            .sign(algorithm);
    return token;
  } catch (JWTCreationException exception) {
    return null;
  }
}

//Inspiration from source: https://github.com/auth0/java-jwt
private boolean verifyToken (String token, User user) {
  try {
    Algorithm algorithm = Algorithm.HMAC256(Config.getTokenKey());
    JWTVerifier verifier = JWT.require(algorithm)
            .withIssuer("auth0")
            .withSubject(Integer.toString(user.getId()))
            .build();
    verifier.verify(token);
    return true;
  } catch (JWTVerificationException exception) {
    return false;
  }
}
}