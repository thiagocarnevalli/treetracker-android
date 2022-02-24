package org.greenstand.android.TreeTracker.orgpicker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.greenstand.android.TreeTracker.models.SessionTracker
import org.greenstand.android.TreeTracker.models.StepCounter
import org.greenstand.android.TreeTracker.models.Users
import org.greenstand.android.TreeTracker.models.location.LocationDataCapturer
import org.koin.core.KoinComponent
import org.koin.core.get

data class AddOrgState(
    val orgName: String = "",
    val userImagePath: String = "",
)

class AddOrgViewModel(
    private val userId: Long,
    private val destinationWallet: String,
    private val users: Users,
    private val stepCounter: StepCounter,
    private val sessionTracker: SessionTracker,
    private val locationDataCapturer: LocationDataCapturer,
) : ViewModel() {

    private val _state = MutableLiveData<AddOrgState>()
    val state: LiveData<AddOrgState> = _state

    init {
        viewModelScope.launch {
            _state.value = AddOrgState(
                userImagePath = users.getUser(userId)!!.photoPath
            )
        }
    }

    fun updateOrgName(orgName: String) {
        _state.value = _state.value!!.copy(
            orgName = orgName
        )
    }

    suspend fun startSession() {
        stepCounter.enable()
        sessionTracker.startSession(
            userId = userId,
            destinationWallet = destinationWallet,
            organization = _state.value!!.orgName
        )
        locationDataCapturer.startGpsUpdates()
    }
}

class AddOrgViewModelFactory(
    private val userId: Long,
    private val destinationWallet: String)
    : ViewModelProvider.Factory, KoinComponent {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddOrgViewModel(userId, destinationWallet, get(), get(), get(), get()) as T
    }
}