package com.droidhats.campuscompass.models

class Explore_Place(
    name: String,
    address: String,
    rating: String,
    placeID: String,
    image: String
) {
    var place_name: String? = name
    var place_address: String? = address
    var place_rating: String? = rating
    var  place_placeID: String? = placeID
    var place_image: String? = image

    override fun toString(): String {

        return "Name: $place_name" +
                "\nAddress: $place_address" +
                "\nRating: $place_rating" +
                "\nID: $place_placeID" +
                "\nImage: $place_image"
    }
}