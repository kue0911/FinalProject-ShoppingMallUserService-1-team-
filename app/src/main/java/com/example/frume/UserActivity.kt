package com.example.frume

import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.example.frume.databinding.ActivityUserBinding
import com.example.frume.fragment.user_fragment.UserLoginFragment
import com.example.frume.fragment.user_fragment.UserSignUpFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import kotlin.concurrent.thread

class UserActivity : AppCompatActivity() {
    lateinit var activityUserBinding: ActivityUserBinding

    // 현재 Fragment와 다음 Fragment를 담을 변수(애니메이션 이동 때문에...)
    var newFragment: Fragment? = null
    var oldFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityUserBinding = DataBindingUtil.setContentView(this@UserActivity, R.layout.activity_user)
        setContentView(R.layout.activity_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 자동 로그인 처리 메서드 호출해야 함
        // 첫번째 Fragment를 설정한다.
        replaceFragment(UserFragmentName.USER_LOGIN_FRAGMENT, false, false, null)
    }


    // 프래그먼트를 교체하는 함수
    fun replaceFragment(fragmentName: UserFragmentName, isAddToBackStack:Boolean, animate:Boolean, dataBundle: Bundle?){
        // newFragment가 null이 아니라면 oldFragment 변수에 담아준다.
        if(newFragment != null){
            oldFragment = newFragment
        }
        // 프래그먼트 객체
        newFragment = when(fragmentName){
            // 로그인 화면
            UserFragmentName.USER_LOGIN_FRAGMENT -> UserLoginFragment()
            UserFragmentName.USER_SIGN_UP_FRAGMENT -> UserSignUpFragment()
        }

        // bundle 객체가 null이 아니라면
        if(dataBundle != null){
            newFragment?.arguments = dataBundle
        }

        // 프래그먼트 교체
        supportFragmentManager.commit {

            if(animate) {
                // 만약 이전 프래그먼트가 있다면
                if(oldFragment != null){
                    oldFragment?.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
                    oldFragment?.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
                }

                newFragment?.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
                newFragment?.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
                newFragment?.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
                newFragment?.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
            }

            replace(R.id.containerUser, newFragment!!)
            if(isAddToBackStack){
                addToBackStack(fragmentName.str)
            }
        }
    }


    // 프래그먼트를 BackStack에서 제거하는 메서드
    fun removeFragment(fragmentName: UserFragmentName){
        supportFragmentManager.popBackStack(fragmentName.str, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    // 다이얼로그를 통해 메시지를 보여주는 함수
    fun showMessageDialog(title:String, message:String, posTitle:String, callback:()-> Unit){
        val builder = MaterialAlertDialogBuilder(this@UserActivity)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(posTitle){ dialogInterface: DialogInterface, i: Int ->
            callback()
        }
        builder.show()
    }


    // 키보드 올리는 메서드
    fun showSoftInput(view: View){
        // 입력을 관리하는 매니저
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        // 포커스를 준다.
        view.requestFocus()

        thread {
            SystemClock.sleep(500)
            // 키보드를 올린다.
            inputManager.showSoftInput(view, 0)
        }
    }
    // 키보드를 내리는 메서드
    fun hideSoftInput(){
        // 포커스가 있는 뷰가 있다면
        if(currentFocus != null){
            // 입력을 관리하는 매니저
            val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            // 키보드를 내린다.
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            // 포커스를 해제한다.
            currentFocus?.clearFocus()
        }
    }

    // Activity에서 터치가 발생하면 호출되는 메서드
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // 만약 포커스가 주어진 View가 있다면
        if(currentFocus != null){
            // 현재 포커스가 주어진 View의 화면상의 영역 정보를 가져온다.
            val rect = Rect()
            currentFocus?.getGlobalVisibleRect(rect)
            // 현재 터치 지점이 포커스를 가지고 있는 View의 영역 내부가 아니라면
            if(rect.contains(ev?.x?.toInt()!!, ev?.y?.toInt()!!) == false){
                // 키보드를 내리고 포커스를 제거한다.
                hideSoftInput()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    // 프래그먼트들을 나타내는 값들
    enum class UserFragmentName(var number:Int, var str:String){
        // 로그인 화면
        USER_LOGIN_FRAGMENT(1, "UserLoginFragment"),
        // 회원 가입 화면
        USER_SIGN_UP_FRAGMENT(2, "UserSignUpFragment"),

    }
}