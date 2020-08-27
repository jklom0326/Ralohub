package com.ralhub.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.ralhub.R
import com.ralhub.model.ContentDTO
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class HomeFragment : Fragment(){
   lateinit var user: FirebaseUser
   lateinit var firestore : FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_home,container,false)
        firestore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser!!

        view.detailviewfragment_recyclerview.adapter = DetailRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class DetailRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        val contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        val contentUidList : ArrayList<String> = arrayListOf()
 
        init {
            firestore.collection("image").orderBy("timestamp").addSnapshotListener{ querySnapshot , firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                for (snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged() //값이 새로고침됨
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent,false)
            return CustomViewHoler(view)
        }

        inner class CustomViewHoler(view: View) : RecyclerView.ViewHolder(view) {
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHoler = (holder as CustomViewHoler).itemView

            // UserId
            viewHoler.detailviewitem_explain_textview.text = contentDTOs[position].userId

            //Image
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewHoler.detailviewitem_profile_imageview_content)

            //Explain of content
            viewHoler.detailviewitem_explain_textview.text = "Likes" + contentDTOs[position].faviriteCount

            //ProfileImage
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewHoler.detailviewitem_profile_image)

        }


        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}