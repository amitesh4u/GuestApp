package com.amitesh.guestapp.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.amitesh.guestapp.constant.PROFILE_ID
import com.amitesh.guestapp.domainobject.GuestDetails
import com.amitesh.guestapp.enm.ApiStatus
import com.amitesh.guestapp.enm.ReservationStatus
import com.amitesh.guestapp.network.GuestAppApi
import com.amitesh.guestapp.util.convertLongToDateString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * The [ViewModel] that is attached to the [com.amitesh.guestapp.fragment.TitleFragment].
 */
class TitleViewModel : ViewModel() {

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<ApiStatus>()

    // The external immutable LiveData for the request status
    private val status: LiveData<ApiStatus>
        get() = _status

    // The internal MutableLiveData that stores the success status message of the most recent request
    private val _statusMessage = MutableLiveData<String>()

    // The external immutable LiveData for the request status
    val statusMessage: LiveData<String>
        get() = _statusMessage


    // The internal MutableLiveData that stores the error status message of the Active Rez call request
    private val _activeRezCallErrorStatusMessage = MutableLiveData<String>()

    // The external immutable LiveData for the request status
    val activeRezCallErrorStatusMessage: LiveData<String>
        get() = _activeRezCallErrorStatusMessage

    // The internal MutableLiveData that stores the error status message of Create Rez call request
    private val _createRezCallErrorStatusMessage = MutableLiveData<String>()

    // The external immutable LiveData for the request status
    val createRezCallErrorStatusMessage: LiveData<String>
        get() = _createRezCallErrorStatusMessage

    // The internal MutableLiveData that stores the error status message of Create Rez call request
    private val _checkInRezCallErrorStatusMessage = MutableLiveData<String>()

    // The external immutable LiveData for the request status
    val checkInRezCallErrorStatusMessage: LiveData<String>
        get() = _checkInRezCallErrorStatusMessage

    // The internal MutableLiveData that stores the error status message of Create Rez call request
    private val _checkOutRezCallErrorStatusMessage = MutableLiveData<String>()

    // The external immutable LiveData for the request status
    val checkOutRezCallErrorStatusMessage: LiveData<String>
        get() = _checkOutRezCallErrorStatusMessage

    // The internal MutableLiveData that stores the error status message of Create Rez call request
    private val _changeRoomRezCallErrorStatusMessage = MutableLiveData<String>()

    // The external immutable LiveData for the request status
    val changeRoomRezCallErrorStatusMessage: LiveData<String>
        get() = _changeRoomRezCallErrorStatusMessage


    // The internal MutableLiveData that stores the Current Reservation status of the Guest
    private val _currentRezStatus = MutableLiveData<ReservationStatus>()

    // The external immutable LiveData for the Reservation status
    private val currentRezStatus: LiveData<ReservationStatus>
        get() = _currentRezStatus

    // Internally, we use a MutableLiveData, because we will be updating the Guest Details
    // with new values
    private val _currentRez = MutableLiveData<GuestDetails>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val currentRez: LiveData<GuestDetails>
        get() = _currentRez

//    private val _activeRez = MutableLiveData<LinkedList<GuestDetails>>()
//
//    // The external LiveData interface to the property is immutable, so only this class can modify
//    val activeRez: LiveData<LinkedList<GuestDetails>>
//        get() = _activeRez

    val newRezStatus: LiveData<Boolean> = Transformations.map(currentRezStatus) {
        currentRezStatus.value == ReservationStatus.NONE
    }

    val arrivingStatus: LiveData<Boolean> = Transformations.map(currentRezStatus) {
        currentRezStatus.value == ReservationStatus.ARRIVING
    }

    val inHouseStatus: LiveData<Boolean> = Transformations.map(currentRezStatus) {
        currentRezStatus.value == ReservationStatus.INHOUSE
    }

    val activeStatus: LiveData<Boolean> = Transformations.map(currentRezStatus) {
        currentRezStatus.value == ReservationStatus.ARRIVING || currentRezStatus.value == ReservationStatus.INHOUSE
    }

    val progressBarStatus: LiveData<Boolean> = Transformations.map(status) {
        status.value == ApiStatus.LOADING
    }

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
        //fetchRezDetails()
    }

    fun fetchRezDetails() {
        Log.i("TitleViewModel", "fetchRezDetails Called")
        getRezDetails(PROFILE_ID)
    }

    /**
     * Gets All Reservation Details from the API Retrofit service and
     * updates the [GuestDetails] and [ApiStatus] [LiveData]. The Retrofit service
     * returns a coroutine Deferred, which we await to get the result of the transaction.
     */
    private fun getRezDetails(profileId: String) {
        _activeRezCallErrorStatusMessage.value = null
        uiScope.launch {
            try {
                _status.value = ApiStatus.LOADING
                // Get the Deferred object for our Retrofit request
                val getRezDeferred = GuestAppApi.retrofitService.activeReservation(profileId)
                // this will run on a thread managed by Retrofit
                _status.value = ApiStatus.DONE
                _statusMessage.value = "Reservation details fetched successfully!!"
                if (getRezDeferred.isNullOrEmpty()) {
                    _currentRez.value = null
                    _currentRezStatus.value = ReservationStatus.NONE
                } else {
                    updateCurrentRez(getRezDeferred.first())
                }
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                if (_currentRez.value == null) {
                    _activeRezCallErrorStatusMessage.value = "Error Fetching Reservation details."
                } else {
                    _activeRezCallErrorStatusMessage.value = "Error Fetching Reservation details. Showing Cached Data."
                }
            }
        }
    }

    fun createNewReservation() {
        createReservation(PROFILE_ID)
    }

    /**
     * Create a New Reservation and updates the [GuestDetails] and [ApiStatus] [LiveData].
     * The Retrofit service returns a coroutine Deferred, which we await to get the result of the transaction.
     */
    private fun createReservation(profileId: String) {
        _createRezCallErrorStatusMessage.value = null
        uiScope.launch {
            try {
                _status.value = ApiStatus.LOADING
                // Get the Deferred object for our Retrofit request
                val getRezDeferred = GuestAppApi.retrofitService.createReservation(profileId)
                // this will run on a thread managed by Retrofit
                _status.value = ApiStatus.DONE
                _statusMessage.value = "Reservation created successfully!!"
                updateCurrentRez(getRezDeferred)
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _createRezCallErrorStatusMessage.value = "Error Creating Reservation."
            }
        }
    }

    fun checkInReservation() {
        checkInReservation(_currentRez.value!!.reservationNo)
    }

    /**
     * Check In New Reservation and updates the [GuestDetails] and [ApiStatus] [LiveData].
     * The Retrofit service returns a coroutine Deferred, which we await to get the result of the transaction.
     */
    private fun checkInReservation(reservationNo: String) {
        _checkInRezCallErrorStatusMessage.value = null
        uiScope.launch {
            try {
                _status.value = ApiStatus.LOADING
                // Get the Deferred object for our Retrofit request
                val getRezDeferred = GuestAppApi.retrofitService.checkInReservation(reservationNo)
                // this will run on a thread managed by Retrofit
                _status.value = ApiStatus.DONE
                _statusMessage.value = "Reservation Checked-In successfully!!"
                updateCurrentRez(getRezDeferred)
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _checkInRezCallErrorStatusMessage.value = "Error Checking In Reservation."
            }
        }
    }

    fun checkOutReservation() {
        checkOutReservation(_currentRez.value!!.reservationNo)
    }

    /**
     * Check Out New Reservation and updates the [GuestDetails] and [ApiStatus] [LiveData].
     * The Retrofit service returns a coroutine Deferred, which we await to get the result of the transaction.
     */
    private fun checkOutReservation(reservationNo: String) {
        _checkOutRezCallErrorStatusMessage.value = null
        uiScope.launch {
            try {
                _status.value = ApiStatus.LOADING
                // Get the Deferred object for our Retrofit request
                val getRezDeferred = GuestAppApi.retrofitService.checkOutReservation(reservationNo)
                // this will run on a thread managed by Retrofit
                _status.value = ApiStatus.DONE
                _statusMessage.value = "Reservation Checked-Out successfully!!"
                updateCurrentRez(getRezDeferred)
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _checkOutRezCallErrorStatusMessage.value = "Error Checking Out Reservation."
            }
        }
    }

    fun changeRoomOfReservation() {
        changeRoomOfReservation(_currentRez.value!!.reservationNo)
    }

    /**
     * Change Room of Reservation and updates the [GuestDetails] and [ApiStatus] [LiveData].
     * The Retrofit service returns a coroutine Deferred, which we await to get the result of the transaction.
     */
    private fun changeRoomOfReservation(reservationNo: String) {
        _changeRoomRezCallErrorStatusMessage.value = null
        uiScope.launch {
            try {
                _status.value = ApiStatus.LOADING
                // Get the Deferred object for our Retrofit request
                val getRezDeferred = GuestAppApi.retrofitService.changeRoomOfReservation(reservationNo)
                // this will run on a thread managed by Retrofit
                _status.value = ApiStatus.DONE
                _statusMessage.value =
                    "Room no changed successfully from " + _currentRez.value!!.roomNo + " to " + getRezDeferred.roomNo
                updateCurrentRez(getRezDeferred)
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _changeRoomRezCallErrorStatusMessage.value = "Error Changing Room of Reservation."
            }
        }
    }

    fun updateCurrentRez(rezResponse: GuestDetails) {
        when (rezResponse.rezStatus) {
            ReservationStatus.ARRIVING.code -> {
                _currentRez.value = rezResponse
                _currentRezStatus.value = ReservationStatus.ARRIVING
            }
            ReservationStatus.INHOUSE.code -> {
                _currentRez.value = rezResponse
                _currentRezStatus.value = ReservationStatus.INHOUSE
            }
            else -> {
                _currentRez.value = null
                _currentRezStatus.value = ReservationStatus.NONE
            }
        }
        Log.i("Rez status", _currentRezStatus.value.toString())
        Log.i("Current Rez", _currentRez.value.toString())

    }

    fun convertTimeToString(timeInMillisec: Long): String {
        return convertLongToDateString(timeInMillisec)
    }

    fun actionComplete() {
        _statusMessage.value = null
        _activeRezCallErrorStatusMessage.value = null
        _createRezCallErrorStatusMessage.value = null
        _checkInRezCallErrorStatusMessage.value = null
        _checkOutRezCallErrorStatusMessage.value = null
        _changeRoomRezCallErrorStatusMessage.value = null
    }

//    fun initializeCurrentRez(currentRez: GuestDetails) {
//        _currentRez.value = currentRez
//    }
}