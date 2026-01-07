package com.example.recyclersleam.Location;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface GeocodingApi {

        // Reverse Geocoding : Coordonnées -> Adresse
        // Note : Nominatim utilise "lon" et non "longitude"
        @GET("reverse")
        Call<GeocodingResponse> reverseGeocode(
                        @Query("lat") double latitude,
                        @Query("lon") double longitude,
                        @Query("format") String format,
                        @Header("User-Agent") String userAgent,
                        @Query("accept-language") String lang);

        // Forward Geocoding : Adresse -> Coordonnées
        @GET("search")
        Call<List<GeocodingResponse>> searchAddress(
                        @Query("q") String query,
                        @Query("format") String format,
                        @Header("User-Agent") String userAgent,
                        @Query("accept-language") String lang);
}
