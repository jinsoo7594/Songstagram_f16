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
        //인자로 LayoutInflater 객체가 전달되기 때문에 아래와 같이 inflate()메서드를 바로 사용할 수 있다! //정정
        //fragment_detail의 RecyclerView형태의 레이아웃을 메모리에 객체화(inflation)하여 View 타입의 객체로 만든 다음 메인 레이아웃에 추가한다.
        //Fragment의 유저 인터페이스가 화면에 그려지는 시점에 호출된다.
        //XML 레이아웃을 inflate하여 Fragment를 위한 View를 생성하고 Fragment 레이아웃의 root에 해당되는 View를 Activity에게 리턴해야 한다.
        //inflate란 XML 레이아웃에 정의된 뷰나 레이아웃을 읽어서 메모리상의 view 객체를 생성해주는 것
        var view = inflater.inflate(R.layout.fragment_detail, container,false)

        firestore = FirebaseFirestore.getInstance()


        //보여줄 데이터를 recyclerview가 아닌 adapter에 추가하고, recyclerview는 adapter를 통해 데이터를 얻고 View를 생성한다.
        // data  --> adapter --> recyclerview --> view
        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        //layoutManager :  View를 어떤 배열로 보여줄지 결정한다.(항목의 배치)
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

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
        // ViewHolder가 새로 만들어지는 시점에 자동호출된다.
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            // 각 뷰에 배치될 아이템을 위해 정의한 item_detail.xml 레이아웃을 View 객체로 만든다.
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            // ViewHolder (CustomViewHolder) 객체를 생성 및 반환하면서 View(view) 객체를 전달한다.
            // Adapter 내부에서 반환된 ViewHolder 를 내부적으로 메모리에 유지했다가 onBindViewHolder() 호출 시 매개변수로 전달된다.
            return CustomViewHolder(view)
        }
        //RecyclerView.ViewHolder를 만들어주는 부분으로 RecyclerView의 메모리 누수를 방지한다.
        //ViewHolder 객체를 Adapter 내부에서 메모리에 유지해줌으로써 ViewHolder에 의해 최초에 한번만 findViewById 하여
        // 잦은 findViewById 수행에 의한 성능 이슈를 해결하기 위함!
        // <<뷰홀더 내부에 각 뷰를 위한 프로퍼티를 추가하는 방법>>
        // 뷰홀더 클래스 내부에 각 뷰의 인스턴스를 저장할 수 있는 프로퍼티를 추가하고, 생성자에서 각 뷰의 인스턴스를 일괄로 할당하도록 하면
        // findViewById() 함수를 한 번만 호출하게 된다. 해당 소스를 수정해보자

        //inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view){
            var profile_textview = view.detailviewitem_profile_textview
            var imageview_content = view.detailviewitem_imageview_content
            var explain_textview = view.detailviewitem_explain_textview
            var favoritecounter_textview = view.detailviewitem_favoritecounter_textview
            var profile_image = view.detailviewitem_profile_image
        }



        //recyclerview 개수를 넘겨준다.
        override fun getItemCount(): Int {
            return contentDTOs.size
        }
        // Server에서 넘어온 데이터들을 매핑시켜준다.
        //
        // ViewHolder 객체가 재사용될 때 자동호출된다.
        // View 객체는 기존 것을 그대로 사용하고 데이터만 바꿔준다.
        // But, 아래 소스는 onBindViewHolder() 메서드가 호출될 때마다 매번 findViewById() 메서드를 호출하게 되므로 성능이 떨어지며,
        // 데이터의 수가 증가할수록 그 영향은 더욱 커진다.
        // <<뷰홀더 내부에 각 뷰를 위한 프로퍼티를 추가하는 방법>>
        // 뷰홀더 클래스 내부에 각 뷰의 인스턴스를 저장할 수 있는 프로퍼티를 추가하고, 생성자에서 각 뷰의 인스턴스를 일괄로 할당하도록 하면
        // findViewById() 함수를 한 번만 호출하게 된다. 해당 소스를 수정해보자
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // holder를 CustomViewHolder 로 캐스팅
            //var viewHolder = (holder as CustomViewHolder).itemView
            var holder = (holder as CustomViewHolder)
            //UserId
            //viewholder.detailviewitem_profile_textview.text = contentDTOs[position].userId
            holder.profile_textview.text = contentDTOs[position].userId
            //Images
            //Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewholder.detailviewitem_imageview_content)
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(holder.imageview_content)

            //Explain of Content
            //viewholder.detailviewitem_explain_textview.text = contentDTOs[position].explain
            holder.explain_textview.text = contentDTOs[position].explain
            //likes
            //viewholder.detailviewitem_favoritecounter_textview.text = "Likes " + contentDTOs[position].favoriteCount
            holder.favoritecounter_textview.text = "Likes " + contentDTOs[position].favoriteCount

            //ProfileImage
            //Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewholder.detailviewitem_profile_image)
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(holder.profile_image)

        }

    }




}