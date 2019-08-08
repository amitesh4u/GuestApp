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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amitesh.guestapp.databinding.FragmentSmartKeyUsageBinding
import com.amitesh.guestapp.model.SmartKeyUsageModel
import kotlinx.android.synthetic.main.fragment_title.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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

        binding.pullToRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            smartKeyUsageModel.fetchSmartKeyUsage(args.reservationNo)
            pullToRefresh.isRefreshing = false
        })

        smartKeyUsageModel.fetchSmartKeyUsage(args.reservationNo)

        return binding.root
    }


}
