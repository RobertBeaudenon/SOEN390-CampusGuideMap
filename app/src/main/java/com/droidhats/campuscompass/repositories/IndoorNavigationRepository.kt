package com.droidhats.campuscompass.repositories

import androidx.lifecycle.MutableLiveData

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

    private var startAndEnd: MutableLiveData<Pair<Int, Int>?> = MutableLiveData(null)

    fun setStartAndEnd(startAndEnd: Pair<Int, Int>) {
        this.startAndEnd.value = startAndEnd
    }

    fun getStartAndEnd(): Pair<Int, Int>? {
        val startAndEnd = this.startAndEnd.value
        this.startAndEnd.value = null
        return startAndEnd
    }

}