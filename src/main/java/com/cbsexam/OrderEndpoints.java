package com.cbsexam;

import cache.OrderCache;
import com.google.gson.Gson;
import controllers.OrderController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Order;
import utils.Encryption;

@Path("order")
public class OrderEndpoints {
  //Creating a new instance of orderCache
  OrderCache orderCache = new OrderCache();


  /**
   * @param idOrder
   * @return Responses
   */
  @GET
  @Path("/{idOrder}")
  public Response getOrder(@PathParam("idOrder") int idOrder) {

    // Call our controller-layer in order to get the order from the DB
    Order order = OrderController.getOrder(idOrder);

    // TODO: Add Encryption to JSON : FIX
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(order);

    //Encryption added through the encryption method in utils
    json = Encryption.encryptDecryptXOR(json);


    // Return a response with status 200 and JSON as type
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getOrders() {

    // Making an arraylist and using caching layer
    //forceUpdate = false since arraylist is empty and we dont want to force update everytime we load users.
    ArrayList<Order> orders = orderCache.getOrders(false);


    // TODO: Add Encryption to JSON : FIX
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(orders);

    //Encryption added through the encryption method in utils
    json = Encryption.encryptDecryptXOR(json);

    // Return a response with status 200 and JSON as type
    // Changed the type from TEXT_PLAIN_TYPE to APPLICATION_JSON
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createOrder(String body) {

    // Read the json from body and transfer it to a order class
    Order newOrder = new Gson().fromJson(body, Order.class);

    // Use the controller to add the order
    Order createdOrder = OrderController.createOrder(newOrder);
    //force update orders when a new order is created
    orderCache.getOrders(true);

    // Get the order back with the added ID and return it to the user
    String json = new Gson().toJson(createdOrder);

    // Return the data to the user
    if (createdOrder != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {

      // Return a response with status 400 and a message in text
      return Response.status(400).entity("Could not create order").build();
    }
  }
}