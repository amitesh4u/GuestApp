package com.amitesh.guestapp


import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amitesh.guestapp.databinding.FragmentTitleBinding
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate using Data binding
        val binding = DataBindingUtil.inflate<FragmentTitleBinding>(
            inflater, R.layout.fragment_title, container, false
        )

        // Giving the binding access to the OverviewViewModel
        binding.titleViewModel = titleViewModel

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        //Tell Android that our Fragment has a menu
        setHasOptionsMenu(true)

        binding.pullToRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            titleViewModel.fetchRezDetails()
            pullToRefresh.isRefreshing = false
        })
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

        // Add an Observer on the state variable for showing a Snackbar message
        addStatusMessageObserver()

        // Add an Observer on the state variable for showing a Snackbar message for Error while fetching Rez details
        addAllRezApiCallErrorMessageObserver()

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

    private fun addStatusMessageObserver() {
        titleViewModel.statusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_LONG
                ).show()

                // Reset state to make sure the snackbar is only shown once, even if the device
                // has a configuration change.
                titleViewModel.doneShowingSnackbar()
            }
        })
    }

    private fun addAllRezApiCallErrorMessageObserver() {
        titleViewModel.allRezCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    titleViewModel.fetchRezDetails()
                }.show()
            }
        })
    }

    private fun addCreateRezApiCallErrorMessageObserver() {
        titleViewModel.createRezCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    titleViewModel.createNewReservation()
                }.show()
            }
        })
    }

    private fun addCheckInRezApiCallErrorMessageObserver() {
        titleViewModel.checkInRezCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    titleViewModel.checkInReservation()
                }.show()
            }
        })
    }

    private fun addCheckOutRezApiCallErrorMessageObserver() {
        titleViewModel.checkOutRezCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    titleViewModel.checkOutReservation()
                }.show()
            }
        })
    }

    private fun addChangeRoomOfRezApiCallErrorMessageObserver() {
        titleViewModel.changeRoomRezCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    titleViewModel.changeRoomOfReservation()
                }.show()
            }
        })
    }


    /*
        Override onCreateOptionsMenu and inflate our new menu resource
        using the provided menu inflater and menu structure.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu, menu)
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
}
