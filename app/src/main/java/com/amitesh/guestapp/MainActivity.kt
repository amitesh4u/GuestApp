package com.amitesh.guestapp

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.amitesh.guestapp.databinding.ActivityMainBinding

// Contains all the views
private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    var perms = arrayOf("android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE")
    var permsRequestCode = 200
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (shouldAskPermission(perms[0]) || shouldAskPermission(perms[1])) {
            if (hasPermission(perms[0]) && hasPermission(perms[1])) {

            } else {
                if (!hasPermission(perms[0]) && !hasPermission(perms[1])) {
                    requestPermissions(perms, permsRequestCode)
                }
            }
        }

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermission(permission: String): Boolean {
        //if (canMakeSmores()) {
        return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
        //}
        //return true
    }

    /**
     * Just a check to see if we have marshmallows (version 23)
     * @return
     */
    @Suppress("Unused")
    private fun canMakeSmores(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.M
    }

    override fun onRequestPermissionsResult(permsRequestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (permsRequestCode) {
            200 -> {
                val writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (writeAccepted) markAsAsked(perms[0])
                val readAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (readAccepted) markAsAsked(perms[1])
            }
        }
    }

    /**
     * method to determine whether we have asked
     * for this permission before.. if we have, we do not want to ask again.
     * They either rejected us or later removed the permission.
     * @param permission
     * @return
     */
    private fun shouldAskPermission(permission: String): Boolean {
        return sharedPreferences.getBoolean(permission, true)
    }

    /**
     * we will save that we have already asked the user
     * @param permission
     */
    private fun markAsAsked(permission: String) {
        sharedPreferences.edit().putBoolean(permission, false).apply()
    }

}
