package com.cbsexam;

import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

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
      }
      else {
        //Returning status code 404 but showing "User ID not found" to the user.
        return Response.status(404).entity("User ID not found").build();
      }
      } catch (Exception e) {
      return Response.status(404).build();
    }
  }


  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = UserController.getUsers();

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

  // TODO: Make the system able to login users and assign them a token to use throughout the system.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String x) {

    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }

  // TODO: Make the system able to delete users : FIX

    @POST
    @Path("/deleteuser/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(String userID) {

      // Read the json from body and transfer it to a user class
      User chosenUserID = new Gson().fromJson(userID, User.class);

      if (doesUserIDExist(chosenUserID.getId())) {

      // Use the controller to delete the user with the chosen user ID
      UserController.deleteUser((chosenUserID.getId()));

        // Return if the user could be deleted or not
        // Return a response with status 200 and JSON as type
        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User with the userID " + chosenUserID.getId() + " has been deleted").build();
      } else {
        return Response.status(400).entity("User ID not found").build();
      }
    }

  // TODO: Make the system able to update users : FIX
  @PUT
  @Path("/updateUser/{idUser}")
  @Consumes(MediaType.APPLICATION_JSON)

  public Response updateUser(@PathParam("idUser") int idUser, String body) {

    try {
      // Use the ID to get the user from the controller.
      User chosenUser = UserController.getUser(idUser);

      // Read the json from body and transfer it to a user class
      User newUserData = new Gson().fromJson(body, User.class);

      //Update the user with the chosen id with the new data
      UserController.updateUser(idUser, newUserData);

      //Creating new user object in order for the program to print out the new user data with the user id
      User updatedUser = UserController.getUser(idUser);

      // Convert the user object to json in order to return the object
      String json = new Gson().toJson(updatedUser);

      if (chosenUser != null) {
        // Return a response with status 200 and JSON as type
        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
      } else {
        return Response.status(404).entity("User id not found").build();
      }
    } catch (Exception e) {
      return Response.status(404).build();
    }
  }

  private Boolean doesUserIDExist (int userID) {
    return UserController.getUser(userID) != null;
  }
}