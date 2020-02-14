package com.example.songstagram_f16.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.songstagram_f16.R
import com.example.songstagram_f16.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage?= null
    var photoUri : Uri? = null // image uri
    var auth : FirebaseAuth? = null // 유저의 정보를 가져오기 위함
    var firestore : FirebaseFirestore? = null // DB 를 사용하기 위해 선언
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //Initiate storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        //Open the album
        //이 액티비티가 실행되자마자 화면이 열리도
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        //add image upload event
        addphoto_btn_upload.setOnClickListener{
            println("00000000000000000000000번")
            contentUpload()}
    }
    //선택한 이미지를 받는 기능
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            //결과값이 사진을 선택했을 때
            if(resultCode == Activity.RESULT_OK) {
                //this is path to the selected image
                // 이미지의 경로가 넘어간다.
                println("333333333333333333333번")
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)

            }else{ // 취소했을 때 작동하는 부분
                //Exit the addphotoActivity if you leave the album without selecting it
                finish() // 액티비티 닫음
            }
        }
    }
    fun contentUpload(){
        //Make filename
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        println("111111111111111111111번"+imageFileName)
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)
        println("2222222222222222222222번"+storageRef)
        //FileUpload ( 업로드에는 Callback 과 Promise 방식이 있다. 둘 중 편한걸 선택해서 사용하면 된다.)
        //Promise method (구글 권장 방식)
        storageRef?.putFile(photoUri!!)?.continueWithTask{ task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
            // 이미지 업로드가 완료되면, 이미지 주소를 받아오는 작업
            // 이미지 주소를 받아오자마자 데이터 모델을 만든다.
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO() // 클래스의 인스턴스 생성

            //Insert downloadUrl of image
            contentDTO.imageUrl = uri.toString()

            //Insert uid of user
            contentDTO.uid = auth?.currentUser?.uid

            //Insert userId
            contentDTO.userId = auth?.currentUser?.email

            //Insert explain of content
            contentDTO.explain = addphoto_edit_explain.text.toString()

            //Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document("feeds")?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()
        }
        //Callback method
        /*//DB입력
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            //Toast.makeText(this, getString(R.string.upload_success),Toast.LENGTH_LONG).show() }
            // 이미지 업로드가 완료되면, 이미지 주소를 받아오는 작업
            // 이미지 주소를 받아오자마자 데이터 모델을 만든다.
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()

                //Insert downloadUrl of image
                contentDTO.imageUrl = uri.toString()

                //Insert uid of user
                contentDTO.uid = auth?.currentUser?.uid

                //Insert userId
                contentDTO.userId = auth?.currentUser?.email

                //Insert explain of content
                contentDTO.explain = addphoto_edit_explain.text.toString()

                //Insert timestamp
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)

                finish()
            }
        }*/
    }
}
