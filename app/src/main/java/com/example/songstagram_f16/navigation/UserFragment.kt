package com.example.songstagram_f16.navigation

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
import com.example.songstagram_f16.R
import com.example.songstagram_f16.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment(){
    var fragmentView : View? = null
    var firestore : FirebaseFirestore?= null
    var uid : String? = null
    var auth : FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecycleViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!, 3)
        return fragmentView
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