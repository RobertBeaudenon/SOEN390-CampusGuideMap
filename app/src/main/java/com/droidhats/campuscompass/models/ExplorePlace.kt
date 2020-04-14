package com.droidhats.campuscompass.models

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

class ExplorePlace(
    name: String,
    address: String,
    rating: String,
    placeID: String,
    image: String,
    coordinate: LatLng
) {
    var placeName: String? = name
    var place_address: String? = address
    var place_rating: String? = rating
    var  place_placeID: String? = placeID
    var place_image: String? = image
    var place_coordinate: LatLng = coordinate
    var place : Place? = null

    override fun toString(): String {

        return "Name: $placeName" +
                "\nAddress: $place_address" +
                "\nRating: $place_rating" +
                "\nID: $place_placeID" +
                "\nImage: $place_image"+
                "\nCoordinate: $place_coordinate"
    }
}