package com.example.songstagram_f16
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.songstagram_f16.navigation.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when(p0.itemId){
            R.id.action_home ->{
                // Fragment 객체 생성
                var detailViewFragment = DetailViewFragment()
                // FragmentManager-fragmentTransaction 의 replace 기능을 통해 앞에서 생성한 Fragment 객체로 FrameLayout 뷰를 변경해준다.
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()
                return true
            }
            R.id.action_search ->{
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()
                return true
            }
            R.id.action_add_photo ->{
                //외부 스토리지 경로를 가져올 수 있는 권한이 있는지 여부 체크
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    startActivity(Intent(this, AddPhotoActivity::class.java))
                }

                return true
            }
            R.id.action_favorite_alarm ->{
                var alarmFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,alarmFragment).commit()
                return true
            }
            R.id.action_account ->{
                var userFragment = UserFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,userFragment).commit()
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(this)

        //사진을 가져올 수 있는 권한 요청
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        //메인 화면이 뜨면 DetailViewFragment가 메인화면에 보일수 있도 세팅
        //set default screen
        bottom_navigation.selectedItemId = R.id.action_home
    }
}
