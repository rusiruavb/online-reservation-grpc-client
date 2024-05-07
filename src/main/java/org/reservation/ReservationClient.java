package org.reservation;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.item.*;
import org.nameserver.NameServiceClient;
import org.nameserver.ServiceDetails;
import org.user.LoginUserRequest;
import org.user.LoginUserResponse;
import org.user.UserServiceGrpc;
import org.user.CreateUserRequest;
import org.user.CreateUserResponse;

import java.io.IOException;
import java.util.Scanner;

import static org.reservation.commons.Constants.*;

public class ReservationClient {
    public static final String NAME_SERVICE_ADDRESS = "http://localhost:2379";
    private ManagedChannel channel = null;
    ReservationServiceGrpc.ReservationServiceBlockingStub reservationServiceBlockingStub = null;

    UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub = null;

    ItemServiceGrpc.ItemServiceBlockingStub itemServiceBlockingStub = null;

    String host = null;
    int port = -1;
    Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws IOException, InterruptedException {
        ReservationClient client = new ReservationClient();

        client.initializeConnection();
        client.processUserRequests();
        client.closeConnection();
    }

    public ReservationClient() throws IOException, InterruptedException {
        fetchServerDetails();
    }

    private void fetchServerDetails() throws IOException, InterruptedException {
        NameServiceClient client = new NameServiceClient(NAME_SERVICE_ADDRESS);
        ServiceDetails serviceDetails = client.findService("reservation");
        host = serviceDetails.getIPAddress();
        port = serviceDetails.getPort();
    }

    public void initializeConnection() {
        this.channel = ManagedChannelBuilder.forAddress(this.host, this.port).usePlaintext().build();
        this.reservationServiceBlockingStub = ReservationServiceGrpc.newBlockingStub(this.channel);
        this.userServiceBlockingStub = UserServiceGrpc.newBlockingStub(this.channel);
        this.itemServiceBlockingStub = ItemServiceGrpc.newBlockingStub(this.channel);
        this.channel.getState(true);
    }

    public void processUserRequests() {
        String input;

        System.out.println(WELCOME_STR);
        input = sc.nextLine().trim();

        switch (input) {
            case ONE:

                while (true) {
                    boolean status = loginUser();
                    if (status) {
                        break;
                    }
                }
                handleSeller();
                break;

            case TWO:
                while (true) {
                    boolean status = loginUser();
                    if (status) {
                        break;
                    }
                }
                handleClerk();
                break;

            case THREE:
                while (true) {
                    boolean status = loginUser();
                    if (status) {
                        break;
                    }
                }
                handleCustomer();
                break;

            case STAR:
                registerUser();
                break;

        }

    }

    public void registerUser() {
        System.out.println(USER_USERNAME);
        String userName = sc.nextLine().trim();
        System.out.println(USER_USERROLE);
        String role = sc.nextLine().trim();
        System.out.println(USER_USERPASSWORD);
        String password = sc.nextLine().trim();

        CreateUserRequest createUserRequest = CreateUserRequest
                .newBuilder()
                .setUserName(userName)
                .setRole(role)
                .setPassword(password)
                .build();

        CreateUserResponse createUserResponse = userServiceBlockingStub.createUser(createUserRequest);
        System.out.println("User with username: " + createUserResponse.getUserName() + " was created");
    }

    public boolean loginUser() {

        System.out.println(USERNAME_STR);
        String userName = sc.nextLine().trim();
        System.out.println(PASSWORD_STR);
        String password = sc.nextLine().trim();

        LoginUserRequest loginUserRequest = LoginUserRequest
                .newBuilder()
                .setUserName(userName)
                .setPassword(password)
                .build();

        LoginUserResponse loginUserResponse = userServiceBlockingStub.loginUser(loginUserRequest);
        return true;
    }

    public void handleSeller() {

        System.out.println(HANDLE_SELLER);
        String input = sc.nextLine().trim();
        switch (input) {
            case ONE:
                System.out.println(ENTER_ITEM_ID);
                int id = Integer.parseInt(sc.nextLine().trim());
                System.out.println(ENTER_PRICE);
                double price = Double.parseDouble(sc.nextLine().trim());
                System.out.println(ENTER_QUANTITY);
                int quantity = Integer.parseInt(sc.nextLine().trim());
                System.out.println(ENTER_NAME);
                String name = sc.nextLine().trim();
                System.out.println(ENTER_DESCRIPTION);
                String description = sc.nextLine().trim();

                AddItemRequest addItemRequest = AddItemRequest
                        .newBuilder()
                        .setItemId(id)
                        .setPrice(price)
                        .setQuantity(quantity)
                        .setName(name)
                        .setDescription(description)
                        .build();

                AddItemResponse addItemResponse = itemServiceBlockingStub.addItem(addItemRequest);
                System.out.println("Item " + addItemResponse.getItemId() + " was added successfully");
                break;

            case TWO:
                System.out.println(ENTER_UPDATED_ITEM_ID);
                int updatedId = Integer.parseInt(sc.nextLine().trim());
                System.out.println(ENTER_UPDATED_PRICE);
                double updatedPrice = Double.parseDouble(sc.nextLine().trim());
                System.out.println(ENTER_UPDATED_QUANTITY);
                int updatedQuantity = Integer.parseInt(sc.nextLine().trim());
                System.out.println(ENTER_UPDATED_NAME);
                String updatedName = sc.nextLine().trim();
                System.out.println(ENTER_UPDATED_DESCRIPTION);
                String updatedDescription = sc.nextLine().trim();

                UpdateItemRequest updateItemRequest = UpdateItemRequest
                        .newBuilder()
                        .setItemId(updatedId)
                        .setPrice(updatedPrice)
                        .setQuantity(updatedQuantity)
                        .setName(updatedName)
                        .setDescription(updatedDescription)
                        .build();

                UpdateItemResponse updateItemResponse = itemServiceBlockingStub.updateItem(updateItemRequest);
                System.out.println("Item " + updateItemResponse.getItemId() + " was updated successfully");
                break;

            case THREE:
                System.out.println(ITEM_LIST);
                com.google.protobuf.Empty request = com.google.protobuf.Empty.newBuilder().build();
                ListItemsResponse listItemsResponse = itemServiceBlockingStub.listItems(request);
                for (ListItemResponse item : listItemsResponse.getItemsList()) {
                    System.out.println("Item ID: " + item.getItemId());
                    System.out.println("Item Name: " + item.getName());
                    System.out.println("Item Description: " + item.getDescription());
                    System.out.println("Item Price: " + item.getPrice());
                    System.out.println("Item Quantity: " + item.getQuantity());
                    System.out.println("-------------------------------------------------");
                }

                break;

            case FOUR:
                System.out.println(REMOVE_ITEM);
                int itemId = Integer.parseInt(sc.nextLine());
                RemoveItemRequest removeItemRequest = RemoveItemRequest
                        .newBuilder()
                        .setItemId(itemId)
                        .build();

                RemoveItemResponse removeItemResponse = itemServiceBlockingStub.removeItem(removeItemRequest);
                System.out.println(removeItemResponse.getMessage());
                break;

            case FIVE:
                System.out.println(ENTER_ITEM_ID);
                int updateitemId = Integer.parseInt(sc.nextLine());
                System.out.println(ENTER_QUANTITY);
                int updateItemQuantity = Integer.parseInt(sc.nextLine().trim());

                UpdateQuantityRequest updateQuantityRequest = UpdateQuantityRequest
                        .newBuilder()
                        .setItemId(updateitemId)
                        .setQuantity(updateItemQuantity)
                        .build();
                UpdateItemResponse updateQuantityItemResponse = itemServiceBlockingStub
                        .updateQuantity(updateQuantityRequest);
                System.out.println(
                        "Item " + updateQuantityItemResponse.getItemId() + " quantity was updated successfully");
                break;
        }

    }

    public void handleClerk() {

        System.out.println(HANDLE_CLERK);
        String input = sc.nextLine().trim();
        switch (input) {
            case ONE:
                System.out.println(ENTER_UPDATED_ITEM_ID);
                int updatedId = Integer.parseInt(sc.nextLine().trim());
                System.out.println(ENTER_UPDATED_PRICE);
                double updatedPrice = Double.parseDouble(sc.nextLine().trim());
                System.out.println(ENTER_UPDATED_QUANTITY);
                int updatedQuantity = Integer.parseInt(sc.nextLine().trim());
                System.out.println(ENTER_UPDATED_NAME);
                String updatedName = sc.nextLine().trim();
                System.out.println(ENTER_UPDATED_DESCRIPTION);
                String updatedDescription = sc.nextLine().trim();

                UpdateItemRequest updateItemRequest = UpdateItemRequest
                        .newBuilder()
                        .setItemId(updatedId)
                        .setPrice(updatedPrice)
                        .setQuantity(updatedQuantity)
                        .setName(updatedName)
                        .setDescription(updatedDescription)
                        .build();

                UpdateItemResponse updateItemResponse = itemServiceBlockingStub.updateItem(updateItemRequest);
                System.out.println("Item " + updateItemResponse.getItemId() + " was updated successfully");
                break;
            case TWO:
                System.out.println(ENTER_ITEM_ID);
                int updateitemId = Integer.parseInt(sc.nextLine());
                System.out.println(ENTER_QUANTITY);
                int updateItemQuantity = Integer.parseInt(sc.nextLine().trim());

                UpdateQuantityRequest updateQuantityRequest = UpdateQuantityRequest
                        .newBuilder()
                        .setItemId(updateitemId)
                        .setQuantity(updateItemQuantity)
                        .build();
                UpdateItemResponse updateQuantityItemResponse = itemServiceBlockingStub
                        .updateQuantity(updateQuantityRequest);
                System.out.println(
                        "Item " + updateQuantityItemResponse.getItemId() + " quantity was updated successfully");
                break;
        }

    }

    public void handleCustomer() {

        System.out.println(HANDLE_CUSTOMER);
        String input = sc.nextLine().trim();
        switch (input) {
            case ONE:
                System.out.println(ITEM_LIST);
                com.google.protobuf.Empty request = com.google.protobuf.Empty.newBuilder().build();
                ListItemsResponse listItemsResponse = itemServiceBlockingStub.listItems(request);
                for (ListItemResponse item : listItemsResponse.getItemsList()) {
                    System.out.println("Item ID: " + item.getItemId());
                    System.out.println("Item Name: " + item.getName());
                    System.out.println("Item Description: " + item.getDescription());
                    System.out.println("Item Price: " + item.getPrice());
                    System.out.println("Item Quantity: " + item.getQuantity());
                    System.out.println("-------------------------------------------------");
                }
                break;
            case TWO:
                System.out.println(ENTER_ITEM_ID);
                int itemId = Integer.parseInt(sc.nextLine());
                System.out.println(ENTER_RESERVATION_DATE);
                String reservationDate = sc.nextLine();
                System.out.println(ENTER_PAYMENT_TYPE);
                String type = sc.nextLine();
                System.out.println(ENTER_PAYMENT_AMOUNT);
                double amount = Double.parseDouble(sc.nextLine());

                PaymentDetails paymentDetails = PaymentDetails
                        .newBuilder()
                        .setType(type)
                        .setAmount(amount)
                        .build();

                CreateReservationRequest reservationRequest = CreateReservationRequest
                        .newBuilder()
                        .setItemId(itemId)
                        .setReservationDate(reservationDate)
                        .setPaymentDetails(paymentDetails)
                        .build();

                CreateReservationResponse createReservationResponse = reservationServiceBlockingStub
                        .createReservation(reservationRequest);
                System.out.println(createReservationResponse.getMessage());
                break;
        }
    }

    public void closeConnection() {
        this.channel.shutdown();
    }
}
