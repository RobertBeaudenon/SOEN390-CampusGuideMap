package com.droidhats.campuscompass.repositories

import android.os.Build
import com.droidhats.campuscompass.R
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ExplorePlaceRepositoryTest {

    private val exploreRepository: ExplorePlaceRepository = ExplorePlaceRepository(
        RuntimeEnvironment.application)

     @Test
     fun testConstructRequestURL(){
         var urlLoyola: String ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                 "&location=" + "45.458488,-73.639862" +
                 "&radius=" + "2000" +
                 "&type=" + "Restaurant" +
                 "&key=" + RuntimeEnvironment.application.applicationContext.getString(R.string.ApiKey)

         var urlSGW: String ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                 "&location=" + "45.497406,-73.577102" +
                 "&radius=" + "1000" +
                 "&type=" + "Restaurant" +
                 "&key=" + RuntimeEnvironment.application.applicationContext.getString(R.string.ApiKey)

         var urlLoyola2: String =exploreRepository.constructRequestURL("Loyola", "Restaurant")
         var urlSGW2: String =exploreRepository.constructRequestURL("SGW", "Restaurant")

         Assert.assertEquals(urlLoyola, urlLoyola2)
         Assert.assertEquals(urlSGW, urlSGW2)
     }

    @Test
    fun testConstructImageURL(){
        var image: String ="https://maps.googleapis.com/maps/api/place/photo?" +
                "&photoreference=" + "abcd"+
                "&sensor=" + "false" +
                "&maxheight=" + "3024" +
                "&maxwidth=" + "3024" +
                "&key=" + RuntimeEnvironment.application.applicationContext.getString(R.string.ApiKey)
        
        var image2: String =exploreRepository.constructImageURL("abcd")

        Assert.assertEquals(image, image2)

    }
}