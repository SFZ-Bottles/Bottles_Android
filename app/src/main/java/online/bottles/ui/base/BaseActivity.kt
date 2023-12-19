package online.bottles.ui.base

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import online.bottles.R

abstract class BaseActivity : AppCompatActivity() {

}/*    //앱 바의 뒤로가기 버튼
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }*/