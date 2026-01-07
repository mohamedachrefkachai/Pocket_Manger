package com.example.recyclersleam.Location;

import com.google.gson.annotations.SerializedName;

public class GeocodingResponse {

    @SerializedName("display_name")
    public String displayName;

    // Ajout de la latitude et longitude renvoyées par l'API Nominatim (format
    // String par défaut chez eux)
    @SerializedName("lat")
    public String lat;

    @SerializedName("lon")
    public String lon;
}
