package com.londonappbrewery.bitcointicker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    // Constants:
    // TODO: Create the base URL
    //private final String BASE_URL = "https://apiv2.bitcoinaverage.com/indices/global/ticker/all";
    private final String BASE_URL = "https://apiv2.bitcoinaverage.com/indices/global/ticker/BTC";
    private final String PUBLIC_KEY = "ZTMyMDZkYjBiMWRkNGI0YmFlYjZiN2FiNGRlNDBjMWY";
    private final String SECRET_KEY = "N2NkZTQxMjdlN2Y0NDI2YzhmY2UzYmZkYjAyNGEzZjZhOTYxMmYxZmMxNGQ0M2E0YTFmZTVlZDQxZDRmNmM3Yg";

    // Member Variables:
    TextView mPriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPriceTextView = (TextView) findViewById(R.id.priceLabel);
        Spinner spinner = (Spinner) findViewById(R.id.currency_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Bitcoin", " and " + parent.getItemAtPosition(position));

                String url = BASE_URL + parent.getItemAtPosition(position);

                letsDoSomeNetworking(BASE_URL, parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Noop, I assume
                Log.d("Bitcoin", "nothing selected");
            }
        });

        // Create an ArrayAdapter using the String array and a spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, R.layout.spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // TODO: Set an OnItemSelected listener on the spinner

    }

    // TODO: complete the letsDoSomeNetworking() method
    private void letsDoSomeNetworking(String url, final String currency) {

        long timestamp = new Date().getTime() / 1000;

        String xSignature = getSignature(SECRET_KEY, PUBLIC_KEY);

        RequestParams params = new RequestParams();

//        params.add("crypto", "BTC");
//        params.add("fiat", currency);
//        params.add("X-signature", xSignature);

        String currencyUrl = url + currency;


        AsyncHttpClient client = new AsyncHttpClient();
        client.get(currencyUrl, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.d("Bitcoin", "onStart");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                // called when response HTTP status is "200 OK"
                Log.d("Bitcoin", "JSON: " + response.toString());

                String price = BitcoinPrice.fromJSON(response);
                updateUI(price);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Bitcoin", "Request fail! Status code: " + statusCode);
                Log.d("Bitcoin", "Fail response: " + response);
                Log.e("ERROR", e.toString());

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String s, Throwable e) {
                Log.d("Bitcoin", "Request fail! Status code: " + statusCode);
                Log.d("Bitcoin", "Fail response: " + s);
                Log.e("ERROR", e.toString());
            }

        });
    }

    public void updateUI(String price) {
        TextView priceLabel = (TextView) findViewById(R.id.priceLabel);

        priceLabel.setText(price);
    }

    private String getSignature(String secretKey, String publicKey) {

        long timestamp = System.currentTimeMillis() / 1000L;
        String payload = timestamp + "." + publicKey;
        String signature = new String();

        try {
            Mac sha256_Mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_Mac.init(secretKeySpec);
            String hashHex = android.util.Base64.encodeToString(secretKey.getBytes(), Base64.NO_WRAP);
            signature = payload + "." + hashHex;
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            Log.e("MainActivity", "getSignature failed" + ex.toString());
        }
        return signature;
    }

}
