package edu.cccdci.opal.activities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import edu.cccdci.opal.R

class HomeUi : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_ui)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {

            when(it.itemId){
                R.id.nav_profile -> Toast.makeText(applicationContext, "Cliked Home", Toast.LENGTH_SHORT).show()
                R.id.nav_address -> Toast.makeText(applicationContext, "Cliked pengpengpeng", Toast.LENGTH_SHORT).show()
                R.id.nav_near_me -> Toast.makeText(applicationContext, "Cliked bruuthhhh", Toast.LENGTH_SHORT).show()
                R.id.nav_history -> Toast.makeText(applicationContext, "Cliked hihihihi", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> Toast.makeText(applicationContext, "Cliked ahahah", Toast.LENGTH_SHORT).show()
                R.id.logout -> Toast.makeText(applicationContext, "Cliked Lagout", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))

        return super.onOptionsItemSelected(item)

            return true
        }
    }
