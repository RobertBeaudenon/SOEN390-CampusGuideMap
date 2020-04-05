package com.droidhats.campuscompass.repositories

import androidx.lifecycle.MutableLiveData
import com.droidhats.campuscompass.models.IndoorLocation

class IndoorNavigationRepository private constructor() {

    companion object {
        // Singleton instantiation
        private var instance: IndoorNavigationRepository? = null

        fun getInstance() =
                instance
                        ?: synchronized(this) {
                            instance
                                    ?: IndoorNavigationRepository().also { instance = it }
                        }
    }

    private var startAndEnd: MutableLiveData<Pair<IndoorLocation, IndoorLocation>?> = MutableLiveData(null)

    fun setStartAndEnd(startAndEnd: Pair<IndoorLocation, IndoorLocation>) {
        this.startAndEnd.value = startAndEnd
    }

    fun getStartAndEnd(): Pair<IndoorLocation, IndoorLocation>? {
        val startAndEnd = this.startAndEnd.value
        this.startAndEnd.value = null
        return startAndEnd
    }

}