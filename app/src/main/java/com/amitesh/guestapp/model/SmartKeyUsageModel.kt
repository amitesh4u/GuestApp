package com.amitesh.guestapp.model

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.amitesh.guestapp.domainobject.SmartKeyUnlockDoor
import com.amitesh.guestapp.enm.ApiStatus
import com.amitesh.guestapp.network.GuestAppApi
import com.amitesh.guestapp.util.convertLongToDateString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SmartKeyUsageModel : ViewModel() {

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
    private val _smartKeyUsageCallErrorStatusMessage = MutableLiveData<String>()

    // The external immutable LiveData for the request status
    val smartKeyUsageCallErrorStatusMessage: LiveData<String>
        get() = _smartKeyUsageCallErrorStatusMessage


    private val _smartKeyUsages = MutableLiveData<List<SmartKeyUnlockDoor>>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    private val smartKeyUsages: LiveData<List<SmartKeyUnlockDoor>>
        get() = _smartKeyUsages

    /**
     * Converted nights to Spanned for displaying.
     */
    val smartKeyUsageHtml = Transformations.map(smartKeyUsages) { smartKeyUsages ->
        formatSmartKeyUsages(smartKeyUsages)
    }

    val progressBarStatus: LiveData<Boolean> = Transformations.map(status) {
        status.value == ApiStatus.LOADING
    }

    val smartKeyUsageNotPresentVisibility: LiveData<Boolean> = Transformations.map(smartKeyUsages) {
        smartKeyUsages.value.isNullOrEmpty()
    }

    val smartKeyUsageVisibility: LiveData<Boolean> = Transformations.map(smartKeyUsages) {
        !smartKeyUsages.value.isNullOrEmpty()
    }

    fun fetchSmartKeyUsage(rezNo: String) {
        getSmartKeyUsage(rezNo)
    }

    /**
     * viewModelJob allows us to cancel all co-routines started by this ViewModel.
     */
    private var viewModelJob = Job()

    /* the Co-routine runs using the Main (UI) dispatcher */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * When the [ViewModel] is finished, we cancel our co-routine [viewModelJob], which tells the
     * Retrofit service to stop.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private fun getSmartKeyUsage(rezNo: String) {
        _smartKeyUsageCallErrorStatusMessage.value = null
        uiScope.launch {
            try {
                _status.value = ApiStatus.LOADING
                // Get the Deferred object for our Retrofit request
                val getSmartKeyUsageDeferred =
                    GuestAppApi.retrofitService.getSmartKeyUsage(rezNo) //"REZ21118163", "REZ66696640"
                // this will run on a thread managed by Retrofit
                _status.value = ApiStatus.DONE
                Log.i("SmartKeyUsage", getSmartKeyUsageDeferred.toString())
                _smartKeyUsages.value = getSmartKeyUsageDeferred
                _statusMessage.value = "Smart Key usage details fetched successfully!!"
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _smartKeyUsageCallErrorStatusMessage.value = "Error Fetching Smart Key usage details."
            }
        }
    }

    fun actionComplete() {
        _statusMessage.value = null
        _smartKeyUsageCallErrorStatusMessage.value = null
    }

    private fun formatSmartKeyUsages(smartKeyUsages: List<SmartKeyUnlockDoor>): Spanned {
        val sb = StringBuilder()
        if (!smartKeyUsages.isNullOrEmpty()) {
            sb.apply {
                append("<h1><font color='#E50000'>Smart Key Usage Details for ${smartKeyUsages[0].reservationNo}</font></h1>")
                append("<br>")
                var fontColorElement = "<font color='#AC0000'>"
                smartKeyUsages.forEach {
                    append("<div>")
                    append("<b>Room No: </b>")
                    append(fontColorElement)
                    append("<i>\t${it.roomNo}</i></font><br>")
                    append("<b>Request Id: </b>")
                    append(fontColorElement)
                    append("<i>\t${it.reqId}</i></font><br>")
                    append("<b>at </b>")
                    append(fontColorElement)
                    append("<i>\t${convertLongToDateString(it.reqTime)}</i></font><br>")
                    append("<br><br>")
                    append("</div>")
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
        } else {
            return HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }
}