package com.amitesh.guestapp.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.amitesh.guestapp.R
import com.amitesh.guestapp.databinding.FragmentSmartKeyBinding

/**
 * A simple [Fragment] subclass.
 *
 */
class SmartKeyFragment : Fragment() {

    private val args: SmartKeyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate using Data binding
        val binding = DataBindingUtil.inflate<FragmentSmartKeyBinding>(
            inflater, R.layout.fragment_smart_key, container, false
        )

        /* The complete onClickListener with Navigation using createNavigateOnClickListener.
        Navigate through NavDirections i.e. SafeARgs
        */
        binding.smartKeyUsageButton.setOnClickListener { view: View ->
            Navigation.findNavController(view).navigate(
                SmartKeyFragmentDirections.actionSmartKeyFragmentToSmartKeyUsageFragment(
                    args.reservationNo
                )
            )
        }

        binding.smartKeyCodeButton.setOnClickListener { view: View ->
            Navigation.findNavController(view).navigate(
                SmartKeyFragmentDirections.actionSmartKeyFragmentToSmartKeyCodeFragment(
                    args.reservationNo,
                    args.roomNo
                )
            )
        }

        return binding.root
    }


}
