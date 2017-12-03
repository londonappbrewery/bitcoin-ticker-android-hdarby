package com.londonappbrewery.bitcointicker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hdarby on 12/3/2017.
 */

public class BitcoinPrice {

    public static String fromJSON(JSONObject response) {

        try {
            return String.valueOf(response.get("last"));
        } catch (JSONException e) {
           return "";
        }
    }
}
