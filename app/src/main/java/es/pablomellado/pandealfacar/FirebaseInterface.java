package es.pablomellado.pandealfacar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import es.pablomellado.pandealfacar.model.Address;
import es.pablomellado.pandealfacar.model.Client;
import es.pablomellado.pandealfacar.model.Order;
import es.pablomellado.pandealfacar.model.Product;

/**
 * Created by Pablo Mellado on 19/4/17.
 */


public class FirebaseInterface {

    private Activity context;
    private FirebaseAuth mAuth;
    private static final String EMAIL_USER_FIREBASE = "";
    private static final String PASSWORD_USER_FIREBASE = "";

    boolean mConnected;

    private static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    public FirebaseInterface(Activity ctx) {
        this.context = ctx;
        // Login
        getConnection();
        signIn();
    }

    private void getConnection(){
        DatabaseReference ref = mDatabase.getReference(".info/connected");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mConnected = snapshot.getValue(Boolean.class);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled at .info/connected");
            }
        });
    }

    private void signIn(){
        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(EMAIL_USER_FIREBASE, PASSWORD_USER_FIREBASE)
                .addOnCompleteListener(this.context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("INF", "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("ERR", "signInWithEmail:failed", task.getException());
                        }

                        // ...
                    }
                });
    }

    public void addClient(Client client){

        mDatabase.getReference().child("clients").child(client.getPhone()).setValue(client);

    }

    public void confirmClient(String phoneNo){
        mDatabase.getReference().child("clients").child(phoneNo).child("verified").setValue(true);
    }

    public String addClientAddress(Client client, Address address){
        return addClientAddress(client.getPhone(), address);
    }

    public String addClientAddress(String phone, Address address){
        String key = mDatabase.getReference().child("clients").child(phone).child("addresses")
                .push().getKey();
        mDatabase.getReference().child("clients").child(phone).child("addresses").child(key)
                .setValue(address);

        return key;
    }

    public void editClientAddress(String phone, Address address){
        mDatabase.getReference().child("clients").child(phone).child("addresses").child(address.getKey()).setValue(address);
    }

    public void deleteClientAddress(String phone, Address address){
        mDatabase.getReference().child("clients").child(phone).child("addresses").child(address.getKey()).removeValue();
    }

    public interface AddOrderCallBackListener {
        void orderAdded(boolean successfully);
    }

    public void addOrder(Order order, boolean isLastOrder, final AddOrderCallBackListener callBackListener){
        DatabaseReference orderRef = mDatabase.getReference().child("orders").push();
        orderRef.setValue(order, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError==null){
                    callBackListener.orderAdded(true);
                }
                else {
                    callBackListener.orderAdded(false);
                }
            }
        });
        if (isLastOrder){
            mDatabase.getReference().child("clients").child(order.getClientId()).child("lastOrder").setValue(order);
        }
    }

    public void addOrder(Order order, AddOrderCallBackListener callBackListener){
        addOrder(order, true, callBackListener);
    }


    public static Map<String,String> getServerTimestamp(){
        return ServerValue.TIMESTAMP;
    }



    public interface ServerTimeCallBackListener {
        void gotServerTime(long epoch);
        void errorGettingServerTime();
    }

    public void getServerEpoch(final ServerTimeCallBackListener context) {
        DatabaseReference ref = mDatabase.getReference();
        String key = ref.push().getKey(); // this will create a new unique key
        Map<String, Object> value = new HashMap<>();
        value.put(key, ServerValue.TIMESTAMP);
        ref.child("timestamps").setValue(value);

        ref = mDatabase.getReference("timestamps/"+key);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long epoch = (long)dataSnapshot.getValue();
                    context.gotServerTime(epoch);
                    dataSnapshot.getRef().setValue(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                context.errorGettingServerTime();
            }
        });
    }

    public interface ClientAddressesCallBackListener {
        void gotClientAddresses(Address[] addresses);
        void errorGettingAddresses();
    }

    public void getClientAddresses(Client client, ClientAddressesCallBackListener context){
        getClientAddresses(client.getPhone(), context);
    }

    public void getClientAddresses(String phone, final ClientAddressesCallBackListener context){
        DatabaseReference ref = mDatabase.getReference("clients/"+ phone + "/addresses");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Address[] addresses;
                if (dataSnapshot.exists()) {
                    addresses = new Address[(int)dataSnapshot.getChildrenCount()];
                    int i =0;
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        addresses[i] = ds.getValue(Address.class);
                        addresses[i].setKey(ds.getKey());
                        i++;
                    }
                }
                else{
                    //There were no addresses for this client
                    addresses = new Address[0];
                }
                context.gotClientAddresses(addresses);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("FRB", "Failed to read value.", databaseError.toException());
                context.errorGettingAddresses();
            }
        });
    }

    public interface LastOrderCallBackListener {
        void gotLastOrder(Order lastOrder);
        void errorGettingLastOrder();
    }

    public void getLastOrder(Client client, final LastOrderCallBackListener context){
        DatabaseReference ref = mDatabase.getReference("clients/"+ client.getPhone() + "/lastOrder");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Order lastOrder = dataSnapshot.getValue(Order.class);
                    context.gotLastOrder(lastOrder);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("FRB", "Failed to read value.", databaseError.toException());
                context.errorGettingLastOrder();
            }
        });
    }

    public interface ProductListCallBackListener {
        void gotProductList(Product[] products);
        void errorGettingProductList();
    }

    public void getProductList(final ProductListCallBackListener context){
        DatabaseReference ref = mDatabase.getReference("products");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Product[] products;

                if (dataSnapshot.exists()) {
                    products = new Product[(int)dataSnapshot.getChildrenCount()];
                    int i =0;
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        products[i] = ds.getValue(Product.class);
                        products[i].setId(ds.getKey());
                        i++;
                    }
                    context.gotProductList(products);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                context.errorGettingProductList();
            }
        });
    }

    public interface OrderListCallBackListener {
        void gotPendingOrderList(Order[] orders);
        void errorGettingPendingOrderList();
    }

    public void getOrderList(final String clientId, final OrderListCallBackListener context) {
        DatabaseReference ordersRef = mDatabase.getReference("orders");

        ordersRef.orderByChild("clientId").equalTo(clientId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Order> orders = new ArrayList<Order>();

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                Order currentOrder = ds.getValue(Order.class);
                                if (currentOrder.getStatus() == Order.Status.ORDERED){
                                    //currentOrder.setClientId(clientId);
                                    orders.add(currentOrder);
                                }
                            }

                            context.gotPendingOrderList((Order[])orders.toArray(new Order[0]));

                        }
                        else{
                            context.gotPendingOrderList(null);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        context.errorGettingPendingOrderList();
                    }
                }
        );
    }

}
