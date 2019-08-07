package com.amitesh.guestapp.model

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class SmartKeyCodeModel : ViewModel() {
    companion object {
        // These represent different important times
        // This is when the game is over
        const val DONE = 0L
        // This is the number of milliseconds in a second
        const val ONE_SECOND = 1000L
        // This is the total time of the game
        const val COUNTDOWN_TIME = 20000L
        // This is the time when the phone will start buzzing each second
        private const val COUNTDOWN_PANIC_SECONDS = 15L
    }

    private val timer: CountDownTimer

    private val _currentTime = MutableLiveData<Long>()
    private val currentTime: LiveData<Long>
        get() = _currentTime

    /* Map the Transformed LiveData for output in View */
    /* Use Transformation.map to take currentTime to a String output from currentTimeString */
    val currentTimeString: LiveData<String> = Transformations.map(currentTime) { time ->
        DateUtils.formatElapsedTime(time)
    }

    // Event which triggers the Code Expiry event
    private val _eventCodeExpired = MutableLiveData<Boolean>()
    val eventCodeExpired: LiveData<Boolean>
        get() = _eventCodeExpired

    // Event which triggers the Code Expiry event
    private val _eventCodeActive = MutableLiveData<Boolean>()
    val eventCodeActive: LiveData<Boolean>
        get() = _eventCodeActive

    init {
        // Creates a timer which triggers the end of the game when it finishes
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                // Implement what should happen each tick of the timer
                _currentTime.value = (millisUntilFinished / ONE_SECOND)
                //                if (millisUntilFinished / ONE_SECOND <= COUNTDOWN_PANIC_SECONDS) {
                //                    _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
                //                }
            }

            override fun onFinish() {
                // Implement what should happen when the timer finishes
                _currentTime.value = DONE
                _eventCodeExpired.value = true
                Log.i("expired", _eventCodeExpired.value.toString())
                Log.i("expired", eventCodeExpired.value.toString())
                _eventCodeActive.value = false
                //                _eventBuzz.value = BuzzType.GAME_OVER
            }
        }
        //_eventCodeExpired.value = false

        initialize()
    }

    private fun initialize() {
        _currentTime.value = COUNTDOWN_TIME
        timer.start()
    }

    override fun onCleared() {
        super.onCleared()
        /* To avoid memory leaks, you should always cancel a CountDownTimer if you no longer need it. */
        timer.cancel()
    }

    fun onCodeRegenerateComplete() {
        initialize()
        _eventCodeExpired.value = false
        _eventCodeActive.value = true
    }

//    fun onBuzzComplete() {
//        _eventBuzz.value = BuzzType.NO_BUZZ
//    }
}