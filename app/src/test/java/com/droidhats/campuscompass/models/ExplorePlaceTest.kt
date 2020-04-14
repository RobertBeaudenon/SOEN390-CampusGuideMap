package com.droidhats.campuscompass.models

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ExplorePlaceTest {

    private val testExplorePlace: ExplorePlace = ExplorePlace(
        "Home",
        "3450 Drummond",
        "4.5",
        "1234",
        "image",
        LatLng(1234.0,1234.0))

    // Testing the constructor
    @Test
    fun testConstructor() {
        Assert.assertEquals(testExplorePlace.placeName, "Home")
        Assert.assertEquals(testExplorePlace.placeAddress, "3450 Drummond")
        Assert.assertEquals(testExplorePlace.placeRating, "4.5")
        Assert.assertEquals(testExplorePlace.placePlaceId, "1234")
        Assert.assertEquals(testExplorePlace.placeImage, "image")
        Assert.assertEquals(testExplorePlace.placeCoordinate,  LatLng(1234.0,1234.0))
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
                    "\nImage: image" +
                    "\nCoordinate: lat/lng: (90.0,154.0)"
        )
    }

}