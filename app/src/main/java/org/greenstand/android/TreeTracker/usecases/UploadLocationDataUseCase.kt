package org.greenstand.android.TreeTracker.usecases

import com.amazonaws.AmazonClientException
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.greenstand.android.TreeTracker.api.ObjectStorageClient
import org.greenstand.android.TreeTracker.api.models.requests.LocationRequest
import org.greenstand.android.TreeTracker.api.models.requests.TracksRequest
import org.greenstand.android.TreeTracker.api.models.requests.UploadBundle
import org.greenstand.android.TreeTracker.database.TreeTrackerDAO
import org.greenstand.android.TreeTracker.models.LocationData
import org.greenstand.android.TreeTracker.utilities.md5
import timber.log.Timber

class UploadLocationDataUseCase(
    private val dao: TreeTrackerDAO,
    private val gson: Gson
) : UseCase<Unit, Boolean>() {

    private val storageClient = ObjectStorageClient.instance()

    override suspend fun execute(params: Unit): Boolean {
        try {
            Timber.d("Processing tree location data")
            withContext(Dispatchers.IO) {
                // V2
                val locationEntities = dao.getLocationData()
                val sessionIdToLocations = locationEntities.groupBy { it.sessionId }
                val sessionIdToLocationRequests = sessionIdToLocations
                    .map { (sessionId, entities) ->
                        val locationRequests = entities.map { gson.fromJson(it.locationDataJson, LocationData::class.java) }
                        .map {
                            LocationRequest(
                                accuracy = it.accuracy,
                                latitude = it.latitude,
                                longitude = it.longitude,
                                capturedAt = it.capturedAt,
                            )
                        }
                        return@map sessionId to locationRequests
                    }

                val sessionEntities = sessionIdToLocations.map { dao.getSessionById(it.key) }
                val trackRequests = sessionIdToLocationRequests.map { (sessionId, locationList) ->
                    TracksRequest(
                        sessionId = sessionEntities.find { it.id == sessionId }!!.uuid,
                        locations = locationList,
                    )
                }

                val dataBundle = gson.toJson(UploadBundle.createV2(
                    tracks = trackRequests,
                ))
                storageClient.uploadBundle(
                    dataBundle,
                    "${dataBundle.md5()}_tracks"
                )

                dao.updateLocationDataUploadStatus(locationEntities.map { it.id }, true)
                dao.purgeUploadedLocations()

                Timber.d("Completed uploading ${locationEntities.size} V2 GPS locations")
            }
        } catch (ace: AmazonClientException) {
            Timber.e(
                "Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network."
            )
            Timber.e("Error Message: ${ace.message}")
            return false
        } catch (e: Exception) {
            Timber.e("Location upload error: ${e.message}")
            return false
        }
        return true
    }
}
