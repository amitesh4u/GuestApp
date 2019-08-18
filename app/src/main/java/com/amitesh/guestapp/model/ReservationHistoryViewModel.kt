package com.amitesh.guestapp.model

import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat
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
import java.util.*

class ReservationHistoryViewModel : ViewModel() {

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


    // The internal MutableLiveData that stores the error status message of the All Rez call request
    private val _allRezCallErrorStatusMessage = MutableLiveData<String>()

    // The external immutable LiveData for the request status
    val allRezCallErrorStatusMessage: LiveData<String>
        get() = _allRezCallErrorStatusMessage

    private val _rezHistory = MutableLiveData<List<GuestDetails>>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    private val rezHistory: LiveData<List<GuestDetails>>
        get() = _rezHistory

    val rezHistoryNotPresentVisibility: LiveData<Boolean> = Transformations.map(rezHistory) {
        rezHistory.value.isNullOrEmpty()
    }

    val rezHistoryVisibility: LiveData<Boolean> = Transformations.map(rezHistory) {
        !rezHistory.value.isNullOrEmpty()
    }

    /**
     * Converted Reservation History to Spanned for displaying.
     */
    val rezHistoryHtml = Transformations.map(rezHistory) { rezHistory ->
        formatReservationHistory(rezHistory)
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

//    /**
//     * Call getInhouseRezDetails() on init so we can display status immediately.
//     */
//    init {
//        fetchRezDetails()
//    }

    fun fetchRezDetails() {
        getRezDetails(PROFILE_ID)
    }

    /**
     * Gets All Reservation Details from the API Retrofit service and
     * updates the [GuestDetails] and [ApiStatus] [LiveData]. The Retrofit service
     * returns a coroutine Deferred, which we await to get the result of the transaction.
     */
    private fun getRezDetails(profileId: String) {
        _allRezCallErrorStatusMessage.value = null
        uiScope.launch {
            try {
                _status.value = ApiStatus.LOADING
                // Get the Deferred object for our Retrofit request
                val getAllRezDetailsDeferred = GuestAppApi.retrofitService.allReservations(profileId)
                // this will run on a thread managed by Retrofit
                _status.value = ApiStatus.DONE
                _rezHistory.value = LinkedList(getAllRezDetailsDeferred)
                _statusMessage.value = "Reservation History fetched successfully!!"
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _allRezCallErrorStatusMessage.value = "Error fetching Reservation History."
            }
        }
    }

    private fun formatReservationHistory(reservationHistory: List<GuestDetails>): Spanned {
        val sb = StringBuilder()
        if (!reservationHistory.isNullOrEmpty()) {
            val fontColorCheckedOut = "#AC0000"
            val fontColorRegular = "#007F39"
            sb.apply {
                append("<h1><font color='#E50000'>Here is your Reservation History (${reservationHistory.size}) </font></h1>")
                append("<br>")
                reservationHistory.forEach {
                    var fontColorElement = "<font color='${fontColorRegular}'>"
                    if (it.rezStatus == ReservationStatus.CHECKEDOUT.code) {
                        fontColorElement = "<font color='${fontColorCheckedOut}'>"
                    }

                    append("<div style=\"border-style: solid;border-color:red\">")
                    append("<b>Reservation No: </b>")
                    append(fontColorElement)
                    append("<i>\t${it.reservationNo}</i></font><br>")
                    if (it.rezStatus != ReservationStatus.CHECKEDOUT.code) {
                        append("<b>Reservation Status: </b>")
                        append(fontColorElement)
                        append("<i>\t${it.rezStatus}</i></font><br>")
                    }
                    if (it.rezStatus != ReservationStatus.ARRIVING.code) {
                        append("<b>Room No: </b>")
                        append(fontColorElement)
                        append("<i>\t${it.roomNo}</i></font><br>")
                        append("<b>Checked-In at: </b>")
                        append(fontColorElement)
                        append("<i>\t${convertLongToDateString(it.checkedInAt ?: 0)}</i></font><br>")
                        if (it.rezStatus == ReservationStatus.CHECKEDOUT.code) {
                            append("<b>Checked-Out at: </b>")
                            append(fontColorElement)
                            append("<i>\t${convertLongToDateString(it.checkedOutAt ?: 0)}</i></font><br>")
                        }
                    }
                    append("<br><br>")
                    append("</div>")
                }
            }
        }
        val returnHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(sb.toString(), Html.FROM_HTML_OPTION_USE_CSS_COLORS)
        } else {
            HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS)
        }
        //Log.i("RezHistHtml", returnHtml.toString())
        return returnHtml
    }


    fun actionComplete() {
        _statusMessage.value = null
        _allRezCallErrorStatusMessage.value = null
    }
}
