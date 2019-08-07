package com.amitesh.guestapp


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.amitesh.guestapp.databinding.FragmentSmartKeyUsageBinding


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate using Data binding
        val binding = DataBindingUtil.inflate<FragmentSmartKeyUsageBinding>(
            inflater, R.layout.fragment_smart_key_usage, container, false
        )
        Toast.makeText(
            context,
            "Reservation No: ${args.reservationNo}",
            Toast.LENGTH_LONG
        ).show()
        return binding.root
    }


}
