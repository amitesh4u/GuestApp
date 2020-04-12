package com.amitesh.guestapp

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.amitesh.guestapp.databinding.ActivityMainBinding

// Contains all the views
private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Check and Request for External Storgae permission */
        checkAndRequestPermission()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        /* Initialize the drawerLayout from the binding variable */
        drawerLayout = binding.drawerLayout

        // Find the controller from the ID of our NavHostFragment using the KTX extension function.
        val navController = this.findNavController(R.id.myNavHostFragment)
        //Link the NavController to our ActionBar
//        NavigationUI.setupActionBarWithNavController(this, navController)

        // prevent nav gesture if not on start destination
        preventNavGestureFromNonStartDestination(navController)

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        NavigationUI.setupWithNavController(binding.navView, navController)

    }

    private fun preventNavGestureFromNonStartDestination(navController: NavController) {
        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, _: Bundle? ->
            if (nd.id == nc.graph.startDestination) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
    }

    private fun checkAndRequestPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        }
    }

    //    /* Adding logic for double Back press before exit */
//    private val backPressDelay = 2000 // # milliseconds, desired time passed between two back presses.
//    private var mBackPressed: Long = 0
//    override fun onBackPressed() {
//        Log.i("backstackentrycount",supportFragmentManager.backStackEntryCount.toString())
//        val toast = Toast.makeText(this, "Press back button again to exit", Toast.LENGTH_SHORT)
//        if (mBackPressed + backPressDelay > System.currentTimeMillis() || supportFragmentManager.backStackEntryCount > 0) {
//            toast.cancel()
//            super.onBackPressed()
//            return
//        } else {
//            toast.show()
//        }
//        mBackPressed = System.currentTimeMillis()
//    }
    /*
        Override the onSupportNavigateUp method from the activity (ctrl+o) and call navigateUp in nav controller.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return NavigationUI.navigateUp(navController, appBarConfiguration)
        //return navController.navigateUp()
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
}
