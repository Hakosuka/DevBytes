/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.devbyteviewer.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.devbyteviewer.database.VideosDatabase
import com.example.android.devbyteviewer.database.asDomainModel
import com.example.android.devbyteviewer.domain.Video
import com.example.android.devbyteviewer.network.Network
import com.example.android.devbyteviewer.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for fetching videos from the network and caching them in the device's storage.
 *
 * These kind of modules handle data operations. They act as intermediaries between different data
 * sources.
 *
 * @param database: A reference to local storage. Passing in the Application Context will result in
 * memory leaks.
 */
class VideosRepository(private val database: VideosDatabase) {

    // The playlist to be shown on screen. The LiveData is to facilitate the observer pattern.
    val videos: LiveData<List<Video>> = Transformations.map(database.videoDao.getVideos()){
        it.asDomainModel()
    }
    // Refreshes the offline cache, and is called from a coroutine.
    suspend fun refreshVideos() {
        // Forces the Kotlin coroutine to switch to the IO dispatcher, even from the main thread
        withContext(Dispatchers.IO) {
            val playlist = Network.devbytes.getPlaylist().await()
            // The asterisk is a "spread operator", allowing arrays to be passed into functions which expect varargs
            database.videoDao.insertAll(*playlist.asDatabaseModel())
        }
    }
}