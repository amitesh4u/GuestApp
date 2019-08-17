package com.amitesh.guestapp.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amitesh.guestapp.R
import com.amitesh.guestapp.databinding.FragmentReservationHistoryBinding
import com.amitesh.guestapp.model.ReservationHistoryViewModel
import com.google.android.material.snackbar.Snackbar

class ReservationHistoryFragment : Fragment() {

    /**
     * Lazily initialize our [ReservationHistoryViewModel].
     */
    private val reservationHistoryViewModel: ReservationHistoryViewModel by lazy {
        ViewModelProviders.of(this).get(ReservationHistoryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate using Data binding
        val binding = DataBindingUtil.inflate<FragmentReservationHistoryBinding>(
            inflater, R.layout.fragment_reservation_history, container, false
        )
        // Giving the binding access to the ViewModel
        binding.reservationHistoryViewModel = reservationHistoryViewModel

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Add an Observer on the state variable for showing a Snackbar message
        addStatusMessageObserver()

        // Add an Observer on the state variable for showing a Snackbar message for Error while fetching Rez details
        addAllRezApiCallErrorMessageObserver()

        reservationHistoryViewModel.fetchRezDetails()

        return binding.root
    }

    private fun addStatusMessageObserver() {
        reservationHistoryViewModel.statusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_LONG
                ).show()

                // Reset state to make sure the snackbar is only shown once, even if the device
                // has a configuration change.
                reservationHistoryViewModel.actionComplete()
            }
        })
    }

    private fun addAllRezApiCallErrorMessageObserver() {
        reservationHistoryViewModel.allRezCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    reservationHistoryViewModel.fetchRezDetails()
                }.show()

                // reservationHistoryViewModel.actionComplete()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        reservationHistoryViewModel.actionComplete()
    }
}
