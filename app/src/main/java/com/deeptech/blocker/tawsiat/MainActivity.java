    package com.deeptech.blocker.tawsiat;

    import android.Manifest;
    import android.accounts.Account;
    import android.accounts.AccountManager;
    import android.annotation.SuppressLint;
    import android.app.Activity;
    import android.content.Context;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.media.MediaPlayer;
    import android.net.wifi.WifiInfo;
    import android.net.wifi.WifiManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.os.Parcelable;
    import android.support.annotation.RequiresApi;
    import android.support.v4.app.ActivityCompat;
    import android.support.v4.content.ContextCompat;
    import android.support.v7.app.AppCompatActivity;
    import android.support.v7.widget.LinearLayoutManager;
    import android.support.v7.widget.RecyclerView;
    import android.telephony.TelephonyManager;
    import android.util.Log;
    import android.util.Patterns;
    import android.view.View;
    import android.widget.Button;
    import android.widget.Toast;

    import com.google.android.gms.auth.GoogleAuthUtil;
    import com.google.android.gms.common.AccountPicker;

    import org.eclipse.paho.android.service.MqttAndroidClient;
    import org.eclipse.paho.client.mqttv3.IMqttActionListener;
    import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
    import org.eclipse.paho.client.mqttv3.IMqttToken;
    import org.eclipse.paho.client.mqttv3.MqttCallback;
    import org.eclipse.paho.client.mqttv3.MqttClient;
    import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
    import org.eclipse.paho.client.mqttv3.MqttException;
    import org.eclipse.paho.client.mqttv3.MqttMessage;
    import org.json.JSONObject;

    import java.io.Serializable;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.regex.Pattern;


    public class MainActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener, Serializable {
        private static final int REQUEST_CODE = 10;
        private static final int PERMISSION_REQUEST_CODE = 1;
        private static String FlagPoint = "FlagPoint";
        boolean isArabic=true;
        private  RecyclerView recy;
        private  RecyAdabter recyAdabter;

        private Parcelable recyclerViewState;
        private  static MainActivity mainActivity;
        private ArrayList<SenderInfo> senderInfo = new ArrayList<>();
        private Activity activity = MainActivity.this;
        MediaPlayer mp;

        @Override
        protected void onPause() {
            super.onPause();
         //   recyclerViewState = recy.getLayoutManager().onSaveInstanceState();//save
        }

        //Tawsiat
        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
          //  outState.putSerializable("senderInfo", senderInfo);
           // outState.putSerializable("counter", counter);
            //Parcelable listState = recy.getLayoutManager().onSaveInstanceState();
            // putting recyclerview position
        }

        private Button btn;
    private LinearLayoutManager mlayout;
    private  int counter =0;
        public static final String TAG = "DOG";

        @SuppressWarnings("unchecked")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            String wantPermission = Manifest.permission.GET_ACCOUNTS;
            if (!checkPermission(wantPermission)) {
                requestPermission(wantPermission);

            } else {
                getEmails();
            }
            call();


            WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            String address = info.getMacAddress();
            Log.v("Address : ", address);
            Toast.makeText(this, address, Toast.LENGTH_LONG).show();
            Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE},
                    true, null, null, null, null);
            startActivityForResult(googlePicker, REQUEST_CODE);

            recy = findViewById(R.id.my_recycler_view);
            if ((savedInstanceState != null) && (savedInstanceState.getSerializable("counter") != null) && (savedInstanceState.getSerializable("senderInfo") != null)) {
                //          senderInfo.clear();
                counter = (int) savedInstanceState.getSerializable("counter");

                senderInfo = (ArrayList<SenderInfo>) savedInstanceState.getSerializable("senderInfo");

            }
            recyAdabter = new RecyAdabter(mainActivity, senderInfo);
            mlayout = new LinearLayoutManager(getApplicationContext());
            mlayout.setReverseLayout(true);
            mlayout.setStackFromEnd(true);
            mlayout.supportsPredictiveItemAnimations();

            recy.setLayoutManager(mlayout);
            recy.setAdapter(recyAdabter);

            btn = findViewById(R.id.connectId);
            checkConnection();
            mp = MediaPlayer.create(this, R.raw.bellring);
            btn.setOnClickListener(new View.OnClickListener() {

                //"tcp://broker.hivemq.com:1883"
                @Override
                public void onClick(final View view) {


                    Connect();
                }
            });


            boolean DISCCONECT_FLAG = false;
            if(!DISCCONECT_FLAG)            Connect();







        }


        public void call() {
            String[] permissions;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissions = new String[]{Manifest.permission.READ_PHONE_STATE};
                ActivityCompat.requestPermissions(this, permissions, 22);
                return;
            }
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.v("imei ", String.valueOf(telephonyManager.getImei()));
            }

        }

        private void getEmails() {
            Pattern emailPattern = Patterns.EMAIL_ADDRESS;

            // Getting all registered Google Accounts;
            // Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");

            // Getting all registered Accounts;
            Account[] accounts = AccountManager.get(this).getAccounts();

            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    Log.d(TAG, String.format("%s - %s", account.name, account.type));
                }
            }
        }

        private boolean checkPermission(String permission) {
            if (Build.VERSION.SDK_INT >= 23) {
                int result = ContextCompat.checkSelfPermission(activity, permission);
                return result == PackageManager.PERMISSION_GRANTED;
            } else {
                return true;
            }
        }

        private void requestPermission(String permission) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Toast.makeText(activity, "Get account permission allows us to get your email",
                        Toast.LENGTH_LONG).show();
            }

        }

        @SuppressLint("MissingPermission")
        @Override
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            switch (requestCode) {


                case PERMISSION_REQUEST_CODE:
                    Log.v("rereOne", String.valueOf(requestCode));
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getEmails();
                    } else {
                        Toast.makeText(activity, "Permission Denied.", Toast.LENGTH_LONG).show();
                        System.exit(0);
                    }

                    break;
                case 22:

                    if (requestCode == 22 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.v("Shit", String.valueOf(requestCode));
                        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.v("imei ", String.valueOf(telephonyManager.getImei()));
                        }
                    } else {
                        Log.v("shits", String.valueOf(requestCode));
                    }
                    break;
            }

        }


        protected void onActivityResult(final int requestCode, final int resultCode,
                                        final Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                Log.d(TAG, accountName);
            } else {
                System.exit(0);
            }
        }

























        void Connect(){

            try {
                String clientId = MqttClient.generateClientId();
                final MqttAndroidClient client =
                        new MqttAndroidClient(MainActivity.this.getApplicationContext(), "tcp://52.90.45.114:1500",
                                clientId);
                MqttConnectOptions options = new MqttConnectOptions();
                options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
                String topic = "users/last/will";
                byte[] payload = "some payload".getBytes();
                options.setWill(topic, payload ,1,false);
                options.setUserName("polto");
                options.setPassword("Winner#2018".toCharArray());
                IMqttToken token = client.connect(options);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // We are connected
                        Log.d(TAG, "onSuccess");
                        SetSub(client);
                        btn.setText("connected");
                        btn.setEnabled(false);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems


                        Toast.makeText(MainActivity.this, "Something Went Wrong Check Internet Connection !", Toast.LENGTH_SHORT).show();
                        btn.setEnabled(true);

                        Log.d(TAG, "onFailure");
                        Log.d(TAG, exception.getMessage());

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }



        }


        private void prepareMovieData(String name,String data,String date,boolean isArabic) {

            mp.seekTo(0);
            mp.start();

            SenderInfo sender = new SenderInfo(name,data,date,isArabic);
            senderInfo.add(sender);

            recyAdabter.notifyDataSetChanged();
            mlayout.scrollToPosition(counter);
    counter++;
        }


        void SetSub(final MqttAndroidClient client){
            String topic = "global/stream";
            int qos = 1;
            try {
                IMqttToken subToken = client.subscribe(topic, qos);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                     client.setCallback(new MqttCallback() {
                         @Override
                         public void connectionLost(Throwable cause) {
                             Log.d("hello","YEAAAAAAAAAh");
                             btn.setEnabled(true);
                             btn.setText("Reconnect!");
                             Toast.makeText(MainActivity.this
                                     ,"Connection Failed !",Toast.LENGTH_SHORT).show();
                         }

                         @Override
                         public void messageArrived(String topic, MqttMessage message) throws Exception {
                             final JSONObject obj = new JSONObject(new String(message.getPayload()));
                             String name = obj.getString("name");
                             String data=obj.getString("data");
                             String lang = obj.getString("lang");


                                 Calendar c = Calendar.getInstance();
                             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                             String strDate = sdf.format(c.getTime());

                             if(lang.contains("AR")){
                                 isArabic=false;
                                 prepareMovieData(name,data,strDate,isArabic);
                                 Log.d("testingMain","if");
                             }else if(lang.contains("EN")){
                                 isArabic=true;
                                 Log.d("testingMain","elif");

                                 prepareMovieData(name,data,strDate,isArabic);

                             }
                             //   Toast.makeText(MainActivity.this,new String(message.getPayload()),Toast.LENGTH_LONG).show();
                            // Toast.makeText(MainActivity.this,name,Toast.LENGTH_LONG).show();
                         }

                         @Override
                         public void deliveryComplete(IMqttDeliveryToken token) {

                         }
                     });   // The message was published
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        // The subscription could not be performed, maybe the user was not
                        // authorized to subscribe on the specified topic e.g. using wildcards
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();

            }

        }




        private void checkConnection() {
            boolean isConnected = ConnectivityReceiver.isConnected();
            showSnack(isConnected);
        }

        private void showSnack(boolean isConnected) {
            if (isConnected) {
            //    setContentView(R.layout.activity_main);
            } else {
    btn.setEnabled(true);
    btn.setText("Recconect!");

            }
        }


        @Override
        public void onNetworkConnectionChanged(boolean isConnected) {
            showSnack(isConnected);
        }

        @Override
        protected void onResume() {
            super.onResume();
            MyApplication.getInstance().setConnectivityListener(this);
            if(recyclerViewState!=null) {
                recy.getLayoutManager().onRestoreInstanceState(recyclerViewState);//restore
            }
        }



    }


