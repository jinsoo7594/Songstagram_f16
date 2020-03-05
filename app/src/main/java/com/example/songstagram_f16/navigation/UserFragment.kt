package com.example.songstagram_f16.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.songstagram_f16.LoginActivity
import com.example.songstagram_f16.MainActivity
import com.example.songstagram_f16.R
import com.example.songstagram_f16.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment(){

    //내 계정에 대한 정보와 상대방 계정에 대한 정보 두가지를 염두해야 한다.

    var fragmentView : View? = null
    var firestore : FirebaseFirestore?= null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUid : String? = null
    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if(uid == currentUserUid){
            //Mypage
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity,LoginActivity::class.java))
                auth?.signOut() // Firebase logout
            }
        }else{
            //OtherUserPage
            // text뷰 변경과 뒤로 버튼 활성
            // 버튼 텍스트 변경
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
            //메인액티비티를 가져와서
            var mainactivity = (activity as MainActivity)
            // 뒤로 버튼 옆 텍스트 변경, Fragment 끼리 arguments 로 데이터 주고 받음 like Activity - intent
            mainactivity?.toolbar_username?.text = arguments?.getString("userId")
            mainactivity?.toolbar_btn_back?.setOnClickListener {
                mainactivity.bottom_navigation.selectedItemId = R.id.action_home
            }
            // 타이틀 숨김
            mainactivity?.toolbar_title_image?.visibility = View.GONE
            mainactivity?.toolbar_username?.visibility = View.VISIBLE
            mainactivity?.toolbar_btn_back.visibility = View.VISIBLE
        }
        fragmentView?.account_recyclerview?.adapter = UserFragmentRecycleViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!, 3)

        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage()
        return fragmentView
    }
    //DB에 올린 이미지를 다운받는 기능
    fun getProfileImage(){
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null){
                var url = documentSnapshot?.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_iv_profile!!)
            }
        }
    }

    inner class UserFragmentRecycleViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init{
            //Query로 검색할 경우 Parameter로 querySnapshot과 firebaseFirestoreException이 넘어온다.
            //Query 검색을 했을 경우 여러 개의 Document가 검색되기 때문에 QuerySnapshot을 통해서 받는 것이다.
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                //Sometimes, This code return null of querySnapshot when it sign out
                if(querySnapshot == null) return@addSnapshotListener

                //Get data
                for(snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                notifyDataSetChanged() // 리사이클러뷰 새로고침
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHolder(imageView)
        }
        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView){

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
           var imageview = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }


    }
}