package com.amitesh.guestapp


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.amitesh.guestapp.databinding.FragmentSmartKeyUsageBinding
import com.amitesh.guestapp.model.SmartKeyUsageModel


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
        // Giving the binding access to the OverviewViewModel
        binding.smartKeyUsageModel = smartKeyUsageModel

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        Toast.makeText(
            context,
            "Reservation No: ${args.reservationNo}",
            Toast.LENGTH_LONG
        ).show()

        //https://support.google.com/chrome/thread/10975485?hl=en - Too Sensitive Issue
//        binding.pullToRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
//            smartKeyUsageModel.fetchSmartKeyUsage(args.reservationNo)
//            pullToRefresh.isRefreshing = false
//        })

        smartKeyUsageModel.fetchSmartKeyUsage(args.reservationNo)

        return binding.root
    }


}
