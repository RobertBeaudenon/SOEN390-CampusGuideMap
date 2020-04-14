package com.droidhats.campuscompass.models

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ExplorePlaceTest {

    private val testExplorePlace: Explore_Place = Explore_Place(
        "Home",
        "3450 Drummond",
        "4.5",
        "1234",
        "image",
        LatLng(1234.0,1234.0))

    // Testing the constructor
    @Test
    fun testConstructor() {
        Assert.assertEquals(testExplorePlace.place_name, "Home")
        Assert.assertEquals(testExplorePlace.place_address, "3450 Drummond")
        Assert.assertEquals(testExplorePlace.place_rating, "4.5")
        Assert.assertEquals(testExplorePlace.place_placeID, "1234")
        Assert.assertEquals(testExplorePlace.place_image, "image")
        Assert.assertEquals(testExplorePlace.place_coordinate,  LatLng(1234.0,1234.0))
    }

    // testing the toString method
    @Test
    fun testToString() {
        Assert.assertEquals(
            testExplorePlace.toString(),
            "Name: Home" +
                    "\nAddress: 3450 Drummond" +
                    "\nRating: 4.5" +
                    "\nID: 1234" +
                    "\nImage: image"
        )
    }

}