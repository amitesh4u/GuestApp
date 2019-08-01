package com.amitesh.guestapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.amitesh.guestapp.databinding.ActivityMainBinding

// Contains all the views
private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Find the controller from the ID of our NavHostFragment using the KTX extension function.
        val navController = this.findNavController(R.id.myNavHostFragment)
        //Link the NavController to our ActionBar
        NavigationUI.setupActionBarWithNavController(this, navController)

    }

    /*
        Override the onSupportNavigateUp method from the activity (ctrl+o) and call navigateUp in nav controller.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return navController.navigateUp()
    }
}
