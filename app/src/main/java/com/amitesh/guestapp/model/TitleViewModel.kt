package com.amitesh.guestapp.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amitesh.guestapp.domainobject.GuestDetails
import com.amitesh.guestapp.network.GuestAppApi
import com.amitesh.guestapp.util.convertLongToDateString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class ApiStatus { LOADING, ERROR, DONE }
enum class ReservationStatus { NONE, ARRIVING, INHOUSE }

private const val PROFILE_ID = "sg0300747"

/**
 * The [ViewModel] that is attached to the [TitleFragment].
 */
class TitleViewModel : ViewModel() {

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<ApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<ApiStatus>
        get() = _status


    // The internal MutableLiveData that stores the Current Reservation status of the Guest
    private val _currentRezStatus = MutableLiveData<ReservationStatus>()

    // The external immutable LiveData for the Reservation status
    val currentRezStatus: LiveData<ReservationStatus>
        get() = _currentRezStatus

    // Internally, we use a MutableLiveData, because we will be updating the Guest Details
    // with new values
    private val _currentRez = MutableLiveData<GuestDetails>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val currentRez: LiveData<GuestDetails>
        get() = _currentRez

    private val _allRez = MutableLiveData<List<GuestDetails>>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val allRez: LiveData<List<GuestDetails>>
        get() = _allRez


    /**
     * viewModelJob allows us to cancel all co-routines started by this ViewModel.
     */
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * When the [ViewModel] is finished, we cancel our coroutine [viewModelJob], which tells the
     * Retrofit service to stop.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * Call getInhouseRezDetails() on init so we can display status immediately.
     */
    init {
        _currentRez.value = null
        getRezDetails(PROFILE_ID)
    }

    /**
     * Gets Inhouse Reservation Details from the API Retrofit service and
     * updates the [GuestDetails] and [ApiStatus] [LiveData]. The Retrofit service
     * returns a coroutine Deferred, which we await to get the result of the transaction.
     */
    private fun getRezDetails(profileId: String) {
        uiScope.launch {
            // Get the Deferred object for our Retrofit request
            val getAllRezDetailsDeferred = GuestAppApi.retrofitService.allReservations(profileId)
            try {
                _status.value = ApiStatus.LOADING
                // this will run on a thread managed by Retrofit
                val result = getAllRezDetailsDeferred
                _status.value = ApiStatus.DONE
                _allRez.value = result
                updateCurrentRez()
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _allRez.value = ArrayList()
                updateCurrentRez()
            }
        }
    }

    private fun updateCurrentRez() {
        val allRez = _allRez.value?.first()
        when (allRez?.rezStatus) {
            "ARRIVING" -> {
                _currentRez.value = allRez
                _currentRezStatus.value = ReservationStatus.ARRIVING
            }
            "IN-HOUSE" -> {
                _currentRez.value = allRez
                _currentRezStatus.value = ReservationStatus.INHOUSE
            }
            else -> {
                _currentRez.value = null
                _currentRezStatus.value = ReservationStatus.NONE
            }
        }
    }

    fun convertTimeToString(timeInMillisec: Long): String {
        return convertLongToDateString(timeInMillisec)
    }
}