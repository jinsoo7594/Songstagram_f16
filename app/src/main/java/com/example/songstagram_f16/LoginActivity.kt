package com.example.songstagram_f16

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
//import sun.jvm.hotspot.utilities.IntArray
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001 // google login 에 사용할 request code
    var callbackManager : CallbackManager ? = null // facebook login 결과값을 가져온다.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // FirebaseAuth : 메모리 절약을 위한 Singleton 패턴의 클래스, 로그인 정보를 관리할 때 사용하는 메인 클래스
        //  ㄴ createUserWithEmailAndPassword : 회원가입(비밀번호 기반)
        //  ㄴ signInWithEmailAndPassword : 로그인
        //  ㄴ sendEmailVerification : 회원가입한 이메일 유효 확인
        //  ㄴ updateEmail : 회원가입한 아이디 이메일 변경
        //  ㄴ sendPasswordResetEmail : 회원 가입한 비밀번호 재설정
        //  ㄴ reauthenticate : 아이디 재인증
        //  ㄴ delete : 회원가입한 아이디 삭제
        auth = FirebaseAuth.getInstance() // FirebaseAuth 선언
        email_login_button.setOnClickListener{
            signinAndSignup()
        }
        google_sign_in_button.setOnClickListener{
            //first step
            googleLogin()
        }
        facebook_login_button.setOnClickListener{
            //first step
            facebookLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // input: google api key
            .requestEmail() // E-mail ID를 받아온다.
            .build() //코드 마침
        googleSignInClient = GoogleSignIn.getClient(this,gso) // 위에서 받아온 옵션값을 세팅
        //printHashKey()
        callbackManager = CallbackManager.Factory.create() // onActivityResult()로 넘어감
    }
    //printHashKey()로 해쉬키 도출
    //HH9Sn/t0KDpS0Q2eYVSCNSV2K94=  이 값을 facebook 에 등록해야 로그인이 가능해진다.
    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }


    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
    }

    fun facebookLogin(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile","email"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{ //Facebook Login이 성공했을 때 넘어오는 부분
                override fun onSuccess(result: LoginResult?) {
                    //second step
                    // Login에 성공하면 Facebook Data --> Firebase 로 전달
                    handleFacebookAccessToken(result?.accessToken)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }

            })
    }
    fun handleFacebookAccessToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential) //token 내용을 Firebase 에 넘겨줌
            ?.addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    //third step
                    // Login
                    moveMainPage(task.result?.user)
                }else{
                    //Show the error message
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode,resultCode,data) // callbackManager로 전달받은 값을 넘겨준다.
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data) // google에서 넘어온 login 결과값을 저장
            if(result.isSuccess){
                var account = result.signInAccount
                //second step
                //firebase에  사용자 인증정보를 넘겨준다.
                firebaseAuthWithGoogle(account)
            }
        }
    }
    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    // Login
                    moveMainPage(task.result?.user)
                }else{
                    //Show the error message
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }
    fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // ID 생성이 성공적으로 끝나면 수행할 것들
                    //Creating a user account
                    moveMainPage(task.result?.user)
                } else if (!(task.exception?.message.isNullOrEmpty())){
                // 로그인 에러시
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }else{
                    //login if you have account
                    // 아이디 호출도 아니고 에러메시지도 아닐때
                    signinEmail()
                }
            }
    }
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    // Login
                    moveMainPage(task.result?.user)
                }else{
                    //Show the error message
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onStart() {
        //LoginActivity가 시작되면 자동적으로 auth?.currentUser가 로그인 정보가 있는지 체크하는 코드다.
        super.onStart()
        //자동 로그인 설정
        moveMainPage(auth?.currentUser)
    }
    //firebaseuser 상태를 넘겨주고
   fun moveMainPage(user: FirebaseUser?){

        if(user != null){
            // 있을 경우 매인액티비티를 호출
            // var intent = Intent(this, MainActivity::class.java)
            // startActivity(intent)
            Toast.makeText(this, getString(R.string.signin_complete),Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,MainActivity::class.java))
        }
    }

}
