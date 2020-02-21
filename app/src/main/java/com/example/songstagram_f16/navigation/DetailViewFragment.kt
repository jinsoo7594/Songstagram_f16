package com.example.songstagram_f16.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.songstagram_f16.R
import com.example.songstagram_f16.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment(){
    var firestore : FirebaseFirestore? = null
    //onCreateView : fragment가 자신의 UI를 호출한다. UI를 그리기 위해 메서드에서 View를 return 해야하는데 그렇지 않으면 null을 반환한다.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //기존 코드는 아래와 같이 나와있지만
        //var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container,false)
        //인자로 inflater가 전달되기 때문에 아래와 같이 수정이 가능하다.
        //fragment_detail의 RecyclerView형태의 레이아웃을 메모리에 객체화(inflation)하여 뷰그릅 타입의 객체로 만든 다음 메인 레이아웃에 추가한다.
        var view = inflater.inflate(R.layout.fragment_detail, container,false)

        firestore = FirebaseFirestore.getInstance()

        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()
        //생성자
        init{
            //DB에 접근을 해서 데이터를 가져올 수 있는 쿼리  (시간순으로)
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear() // 초기화
                contentUidList.clear()
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java) // 받아온 데이터를 ContentDTO 클래스로 캐스팅
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged() // 값이 새로고침 되도록
            }
       }

        //recyclerview 를 사용할때 메모리를 적게 사용하기 위해 작성한다.
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }
        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)
        //recyclerview 개수를 넘겨준다.
        override fun getItemCount(): Int {
            return contentDTOs.size
        }
        // Server에서 넘어온 데이터들을 매핑시켜준다.
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // holder를 CustomViewHolder 로 캐스팅
            var viewholder = (holder as CustomViewHolder).itemView
            //UserId
            viewholder.detailviewitem_profile_textview.text = contentDTOs[position].userId
            //Images
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewholder.detailviewitem_imageview_content)
            //Explain of Content
            viewholder.detailviewitem_explain_textview.text = contentDTOs[position].explain
            //likes
            viewholder.detailviewitem_favoritecounter_textview.text = "Likes " + contentDTOs[position].favoriteCount
            //ProfileImage
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewholder.detailviewitem_profile_image)

        }

    }




}