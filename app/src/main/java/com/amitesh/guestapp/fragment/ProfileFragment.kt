package com.amitesh.guestapp.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.amitesh.guestapp.R
import com.amitesh.guestapp.databinding.FragmentProfileBinding

/**
 * A simple [Fragment] subclass.
 *
 */
class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate using Data binding
        val binding = DataBindingUtil.inflate<FragmentProfileBinding>(
            inflater, R.layout.fragment_profile, container, false
        )
        return binding.root
    }


}
