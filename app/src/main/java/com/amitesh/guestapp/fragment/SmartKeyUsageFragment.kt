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
import androidx.navigation.fragment.navArgs
import com.amitesh.guestapp.R
import com.amitesh.guestapp.databinding.FragmentSmartKeyUsageBinding
import com.amitesh.guestapp.model.SmartKeyUsageModel
import com.google.android.material.snackbar.Snackbar


/**
 * A simple [Fragment] subclass.
 *
 */
class SmartKeyUsageFragment : Fragment() {
    private val args: SmartKeyUsageFragmentArgs by navArgs()

    /**
     * Lazily initialize our [SmartKeyUsageModel].
     */
    private val smartKeyUsageModel: SmartKeyUsageModel by lazy {
        ViewModelProviders.of(this).get(SmartKeyUsageModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate using Data binding
        val binding = DataBindingUtil.inflate<FragmentSmartKeyUsageBinding>(
            inflater, R.layout.fragment_smart_key_usage, container, false
        )
        // Giving the binding access to the ViewModel
        binding.smartKeyUsageModel = smartKeyUsageModel

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this
//
//        Toast.makeText(
//            context,
//            "Reservation No: ${args.reservationNo}",
//            Toast.LENGTH_LONG
//        ).show()

        //https://support.google.com/chrome/thread/10975485?hl=en - Too Sensitive Issue
//        binding.pullToRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
//            smartKeyUsageModel.fetchSmartKeyUsage(args.reservationNo)
//            pullToRefresh.isRefreshing = false
//        })

        // Add an Observer on the state variable for showing a Snackbar message
        addStatusMessageObserver()

        // Add an Observer on the state variable for showing a Snackbar message for Error while fetching Rez details
        addSmartKeyUsageApiCallErrorMessageObserver()

        smartKeyUsageModel.fetchSmartKeyUsage(args.reservationNo)

        return binding.root
    }

    private fun addStatusMessageObserver() {
        smartKeyUsageModel.statusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_LONG
                ).show()

                // Reset state to make sure the snackbar is only shown once, even if the device
                // has a configuration change.
                smartKeyUsageModel.actionComplete()
            }
        })
    }

    private fun addSmartKeyUsageApiCallErrorMessageObserver() {
        smartKeyUsageModel.smartKeyUsageCallErrorStatusMessage.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(TRY_AGAIN) {
                    // Call action functions here
                    smartKeyUsageModel.fetchSmartKeyUsage(args.reservationNo)
                }.show()

                //smartKeyUsageModel.actionComplete()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        smartKeyUsageModel.actionComplete()
    }

}
