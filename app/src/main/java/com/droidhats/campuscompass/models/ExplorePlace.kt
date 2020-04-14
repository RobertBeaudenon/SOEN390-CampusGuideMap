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
    var placeAddress: String? = address
    var placeRating: String? = rating
    var  placePlaceId: String? = placeID
    var placeImage: String? = image
    var placeCoordinate: LatLng = coordinate
    var place : Place? = null

    override fun toString(): String {

        return "Name: $placeName" +
                "\nAddress: $placeAddress" +
                "\nRating: $placeRating" +
                "\nID: $placePlaceId" +
                "\nImage: $placeImage"+
                "\nCoordinate: $placeCoordinate"
    }
}