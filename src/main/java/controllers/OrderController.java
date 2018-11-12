package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.*;
import utils.Log;

public class OrderController {

  private static DatabaseController dbCon;

  public OrderController() {
    dbCon = new DatabaseController();
  }

  public static Order getOrder(int id) {

    // check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL string to query
      String sql = "SELECT \n" +
              "\tuser.id as user_id, user.first_name, user.last_name, user.email, \n" +
              "    orders.id as order_id, orders.billing_address_id, orders.shipping_address_id,\n" +
              "    product.id as product_id, line_item.id as line_item_id, product.product_name, product.price,\n" +
              "    line_item.quantity,\n" +
              "    orders.order_total,\n" +
              "    b.street_address as billing_address,\n" +
              "    b.city as billing_address_city,\n" +
              "    b.zipcode as billing_address_zipcode,\n" +
              "    s.street_address as shipping_address,\n" +
              "    s.city as shipping_address_city,\n" +
              "    s.zipcode as shipping_address_zipcode\n" +
              "    FROM user\n" +
              "    INNER JOIN orders ON user.id = orders.user_id\n" +
              "    INNER JOIN line_item on line_item.order_id = orders.id\n" +
              "    INNER JOIN product on product.id = line_item.product_id\n" +
              "    INNER JOIN address b on orders.billing_address_id = b.id\n" +
              "    INNER JOIN address s on orders.shipping_address_id = s.id where order_id =" + id;

    // Do the query in the database and create an empty object for the results
    ResultSet rs = dbCon.query(sql);
    Order order = null;

    try {
      if (rs.next()) {

        //Perhaps we could optimize things a bit here and get rid of nested queries.
              User user = new User(
                      rs.getInt("user_id"),
                      rs.getString("first_name"),
                      rs.getString("last_name"),
                      null,
                      rs.getString("email"));

              Product product = new Product(
                      rs.getInt("product_id"),
                      rs.getString("product_name"),
                      null,
                      rs.getFloat("price"),
                      null,
                      0);

              // Initialize an instance of the line item object
              ArrayList<LineItem> items = new ArrayList<>();


                  LineItem lineItem =
                          new LineItem(
                                  rs.getInt("line_item_id"),
                                  product,
                                  rs.getInt("quantity"),
                                  0);

                      items.add(lineItem);


          Address billing_address = new Address(
                      rs.getInt("billing_address_id"),
                      null,
                      rs.getString("billing_address"),
                      rs.getString("billing_address_city"),
                      rs.getString("billing_address_zipcode")
              );

              Address shipping_address = new Address(
                      rs.getInt("shipping_address_id"),
                      null,
                      rs.getString("shipping_address"),
                      rs.getString("shipping_address_city"),
                      rs.getString("shipping_address_zipcode")
              );

              order = new Order(
                      rs.getInt("order_id"),
                      user,
                      items,
                      billing_address,
                      shipping_address,
                      rs.getFloat("order_total"),
                      0,
                      0);

              // Returns the build order
              return order;

      } else {
        System.out.println("No order found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Returns null
    return order;
  }

  /**
   * Get all orders in database
   *
   * @return
   */
  public static ArrayList<Order> getOrders() {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //String sql = "SELECT * FROM orders INNER JOIN first_name, last_name, email FROM user ON";

    String sql = "SELECT \n" +
            "\tuser.id as user_id, user.first_name, user.last_name, user.email, \n" +
            "    orders.id as order_id, orders.billing_address_id, orders.shipping_address_id,\n" +
            "    product.id as product_id, line_item.id as line_item_id, product.product_name, product.price,\n" +
            "    line_item.quantity,\n" +
            "    orders.order_total,\n" +
            "    b.street_address as billing_address,\n" +
            "    b.city as billing_address_city,\n" +
            "    b.zipcode as billing_address_zipcode,\n" +
            "    s.street_address as shipping_address,\n" +
            "    s.city as shipping_address_city,\n" +
            "    s.zipcode as shipping_address_zipcode\n" +
            "    FROM user\n" +
            "    INNER JOIN orders ON user.id = orders.user_id\n" +
            "    INNER JOIN line_item on line_item.order_id = orders.id\n" +
            "    INNER JOIN product on product.id = line_item.product_id\n" +
            "    INNER JOIN address b on orders.billing_address_id = b.id\n" +
            "    INNER JOIN address s on orders.shipping_address_id = s.id";

    ResultSet rs = dbCon.query(sql);

      Map<Integer, Order> orders = new HashMap<>();

    try {
      while(rs.next()) {

          //Perhaps we could optimize things a bit here and get rid of nested queries.
          int orderId = rs.getInt("order_id");

          Order order;
          if (orders.containsKey(orderId)) {


              Product product = new Product(
                      rs.getInt("product_id"),
                      rs.getString("product_name"),
                      null,
                      rs.getFloat("price"),
                      null,
                      0);


              LineItem lineItem =
                      new LineItem(
                              rs.getInt("line_item_id"),
                              product,
                              rs.getInt("quantity"),
                              0);

              orders.get(orderId).getLineItems().add(lineItem);



          }
          else {
              User user = new User(
                      rs.getInt("user_id"),
                      rs.getString("first_name"),
                      rs.getString("last_name"),
                      null,
                      rs.getString("email"));

              Product product = new Product(
                              rs.getInt("product_id"),
                              rs.getString("product_name"),
                              null,
                              rs.getFloat("price"),
                              null,
                              0);

              // Initialize an instance of the line item object
              ArrayList<LineItem> items = new ArrayList<>();

                  LineItem lineItem =
                          new LineItem(
                                  rs.getInt("line_item_id"),
                                  product,
                                  rs.getInt("quantity"),
                                  0);
                  items.add(lineItem);

                  Address billing_address = new Address(
                          rs.getInt("billing_address_id"),
                          null,
                          rs.getString("billing_address"),
                          rs.getString("billing_address_city"),
                          rs.getString("billing_address_zipcode")
                  );

                  Address shipping_address = new Address(
                          rs.getInt("shipping_address_id"),
                          null,
                          rs.getString("shipping_address"),
                          rs.getString("shipping_address_city"),
                          rs.getString("shipping_address_zipcode")
                  );

              order = new Order(
                      rs.getInt("order_id"),
                      user,
                      items,
                      billing_address,
                      shipping_address,
                      rs.getFloat("order_total"),
                      0,
                      0);

              orders.put(orderId, order);
          }
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // return the orders
    return new ArrayList<Order> (orders.values());
  }

  public static Order createOrder(Order order) {

      // Write in log that we've reach this step
      Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

      // Set creation and updated time for order.
      order.setCreatedAt(System.currentTimeMillis() / 1000L);
      order.setUpdatedAt(System.currentTimeMillis() / 1000L);

      // Check for DB Connection
      if (dbCon == null) {
        dbCon = new DatabaseController();
      }

      try {
          //Set autoocmmit = false. This is by default set as true
          //The reason its set to false is cause we dont want it to execute each statement but execute them all as "one" statement
      DatabaseController.getConnection().setAutoCommit(false);

      // Save addresses to database and save them back to initial order instance
      order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
      order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

      // Save the user to the database and save them back to initial order instance
      order.setCustomer(UserController.createUser(order.getCustomer()));

      // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts. : FIX
        //Source used: https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html

      // Insert the product in the DB
      int orderID = dbCon.insert(
              "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, created_at, updated_at) VALUES("
                      + order.getCustomer().getId()
                      + ", "
                      + order.getBillingAddress().getId()
                      + ", "
                      + order.getShippingAddress().getId()
                      + ", "
                      + order.calculateOrderTotal()
                      + ", "
                      + order.getCreatedAt()
                      + ", "
                      + order.getUpdatedAt()
                      + ")");

      if (orderID != 0) {
        //Update the productid of the product before returning
        order.setId(orderID);
      }

      // Create an empty list in order to go trough items and then save them back with ID
      ArrayList<LineItem> items = new ArrayList<LineItem>();

      // Save line items to database
      for (LineItem item : order.getLineItems()) {
        item = LineItemController.createLineItem(item, order.getId());
        items.add(item);
      }

      order.setLineItems(items);
      DatabaseController.getConnection().commit();

          //Return order
          return order;
      }
    catch (SQLException e) {
        System.out.println(e.getMessage());
        //Checks if database connection is closed if not it will rollback
        if (dbCon != null) {
            try {
                System.out.println("Failed. Doing a rollback");
                DatabaseController.getConnection().rollback();
            } catch (SQLException e1) {
                System.out.println(e1.getMessage());
            }
        }

    }
    finally {
          try {
              //Set AutoCommit back to true again
              DatabaseController.getConnection().setAutoCommit(true);
          } catch (SQLException e2) {
              System.out.println(e2.getMessage());
          }
      }
      //Return null
    return order;
  }
}