package com.amitesh.guestapp.fragment


import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.amitesh.guestapp.constant.PROFILE_ID
import com.amitesh.guestapp.databinding.FragmentTitleBinding
import com.amitesh.guestapp.domainobject.GuestDetails
import com.amitesh.guestapp.enm.ReservationStatus
import com.amitesh.guestapp.model.TitleViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_title.*


const val TRY_AGAIN = "Try Again!!"

/**
 * A simple [Fragment] subclass.
 *
 */
class TitleFragment : Fragment() {

    /**
     * Lazily initialize our [TitleViewModel].
     */
    private val titleViewModel: TitleViewModel by lazy {
        ViewModelProviders.of(this).get(TitleViewModel::class.java)
    }

    private lateinit var mHandler: Handler
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate using Data binding
        val binding = DataBindingUtil.inflate<FragmentTitleBinding>(
            inflater, com.amitesh.guestapp.R.layout.fragment_title, container, false
        )

        // Giving the binding access to the ViewModel
        binding.titleViewModel = titleViewModel

        // Fetch and update current rez from local storage if present
        sharedPreferences = with(activity!!) { getPreferences(Context.MODE_PRIVATE) }

        fetchAndUpdateCurrentRezDetails()

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        //Tell Android that our Fragment has a menu
        setHasOptionsMenu(true)

        binding.pullToRefresh.setOnRefreshListener {
            titleViewModel.fetchRezDetails()
            pullToRefresh.isRefreshing = false
        }

        /* The complete onClickListener with Navigation using createNavigateOnClickListener.
        Navigate through NavDirections i.e. SafeARgs
        */
        binding.smartKeyButton.setOnClickListener { view: View ->
            /* Using SafeArgs Navigation option */
            val guestDetails = titleViewModel.currentRez.value!!
            Navigation.findNavController(view).navigate(
                TitleFragmentDirections.actionTitleFragmentToSmartKeyFragment(
                    guestDetails.reservationNo,
                    guestDetails.roomNo!!
                )
            )
        }

        binding.checkOutButton.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Check-out")
                .setMessage("Do you really want to Check-out?")
                .setPositiveButton("Yes") { _, _ ->
                    Toast.makeText(context, "Ok, Checking you Out now.", Toast.LENGTH_SHORT).show()
                    titleViewModel.checkOutReservation()
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Add an Observer on the state variable for updating local storage
        addCurrentRezObserver()

        // Add an Observer on the state variable for showing a Snackbar message
        addStatusMessageObserver()

        // Add an Observer on the state variable for showing a Snackbar message for Error while fetching Rez details
        addActiveRezApiCallErrorMessageObserver()

        // Add an Observer on the state variable for showing a Snackbar message for Error while creating new Rez
        addCreateRezApiCallErrorMessageObserver()

        // Add an Observer on the state variable for showing a Snackbar message for Error while checking in new Rez
        addCheckInRezApiCallErrorMessageObserver()

        // Add an Observer on the state variable for showing a Snackbar message for Error while checking out new Rez
        addCheckOutRezApiCallErrorMessageObserver()

        // Add an Observer on the state variable for showing a Snackbar message for Error while changing room of Rez
        addChangeRoomOfRezApiCallErrorMessageObserver()

        return binding.root
    }

    private fun fetchAndUpdateCurrentRezDetails() {
        val reservationNo = with(sharedPreferences) { getString("reservationNo", null) }
        if (reservationNo != null) {
            val rezStatus = with(sharedPreferences) { getString("rezStatus", ReservationStatus.ARRIVING.code) }
            val roomNo = with(sharedPreferences) { getString("roomNo", null) }
            val checkedInAt = with(sharedPreferences) { getLong("checkedInAt", 0) }
            val checkedOutAt = with(sharedPreferences) { getLong("checkedOutAt", 0) }

            val currentRez =
                GuestDetails(PROFILE_ID, reservationNo, rezStatus!!, roomNo, checkedInAt, checkedOutAt)
            titleViewModel.updateCurrentRez(currentRez)
        }
        titleViewModel.fetchRezDetails()
    }

    private fun addCurrentRezObserver() {
        titleViewModel.currentRez.observe(this, Observer {
            //            if (it != null) {
//                Snackbar.make(
//                    activity!!.findViewById(android.R.id.content),
//                    it.rezStatus,
//                    Snackbar.LENGTH_SHORT
//                ).show()
//            } else {
//                Snackbar.make(
//                    activity!!.findViewById(android.R.id.content),
//                    "No content",
//                    Snackbar.LENGTH_SHORT
//                ).show()
//            }
            updateCurrentRezDetailsInLocalStorage(it)
        })
    }

    private fun updateCurrentRezDetailsInLocalStorage(guestDetails: GuestDetails?) {
        val editor = sharedPreferences.edit()

        if (guestDetails == null) {
            editor.clear()
        } else {
            editor.putString("reservationNo", guestDetails.reservationNo)
            editor.putString("rezStatus", guestDetails.rezStatus)
            editor.putString("roomNo", guestDetails.roomNo)
            editor.putLong("checkedInAt", guestDetails.checkedInAt ?: 0)
            editor.putLong("checkedOutAt", guestDetails.checkedOutAt ?: 0)
        }
        editor.apply()
    }

    private fun addStatusMessageObserver() {
        titleViewModel.statusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_SHORT
                ).show()

                // Reset state to make sure the snackbar is only shown once, even if the device
                // has a configuration change.
                titleViewModel.actionComplete()
            }
        })
    }

    private fun addActiveRezApiCallErrorMessageObserver() {
        titleViewModel.activeRezCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_LONG
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    titleViewModel.fetchRezDetails()
                }.show()

                //titleViewModel.actionComplete()
            }
        })
    }

    private fun addCreateRezApiCallErrorMessageObserver() {
        titleViewModel.createRezCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_LONG
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    titleViewModel.createNewReservation()
                }.show()

                //titleViewModel.actionComplete()
            }
        })
    }

    private fun addCheckInRezApiCallErrorMessageObserver() {
        titleViewModel.checkInRezCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_LONG
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    titleViewModel.checkInReservation()
                }.show()

                //titleViewModel.actionComplete()
            }
        })
    }

    private fun addCheckOutRezApiCallErrorMessageObserver() {
        titleViewModel.checkOutRezCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_LONG
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    titleViewModel.checkOutReservation()
                }.show()

                //titleViewModel.actionComplete()
            }
        })
    }

    private fun addChangeRoomOfRezApiCallErrorMessageObserver() {
        titleViewModel.changeRoomRezCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_LONG
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    titleViewModel.changeRoomOfReservation()
                }.show()

                // titleViewModel.actionComplete()
            }
        })
    }

    private val mRunnable: Runnable = Runnable {
        titleViewModel.fetchRezDetails()
        doTheAutoRefresh()
    }

    private fun doTheAutoRefresh() {
        mHandler = Handler()
        mHandler.postDelayed(mRunnable, 20000)
    }

    /*
        Override onCreateOptionsMenu and inflate our new menu resource
        using the provided menu inflater and menu structure.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(com.amitesh.guestapp.R.menu.overflow_menu, menu)
    }

    /*
        Override onOptionsItemSelected to connect it to our NavigationUI.
        Add a backup option to get it handled by Android in case custom handler fails
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //!! asserts that an expression is non-null
        return NavigationUI.onNavDestinationSelected(item, view!!.findNavController())
                || super.onOptionsItemSelected(item)
    }

    /** Lifecycle Methods **/
    override fun onStart() {
        super.onStart()
        Log.i("TitleFragment", "onStart Called")
    }

    override fun onResume() {
        super.onResume()
//        titleViewModel.fetchRezDetails()
        //       doTheAutoRefresh()
        fetchAndUpdateCurrentRezDetails()
        Log.i("TitleFragment", "onResume Called")
    }

    override fun onPause() {
        super.onPause()
//        titleViewModel.actionComplete()
//        mHandler.removeCallbacks(mRunnable)
        Log.i("TitleFragment", "onPause Called")
    }

    override fun onStop() {
        super.onStop()
        Log.i("TitleFragment", "onStop Called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("TitleFragment", "onDestroy Called")
    }

}
