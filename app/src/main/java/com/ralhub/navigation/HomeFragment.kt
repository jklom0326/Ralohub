package com.ralhub.navigation

import android.annotation.SuppressLint
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
    lateinit var uid : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_home,container,false)
        firestore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser!!
        uid = FirebaseAuth.getInstance().currentUser?.uid.toString()


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
                if(querySnapshot== null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents){
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

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHoler = (holder as CustomViewHoler).itemView

            // UserId
            viewHoler.detailviewitem_profile_textview.text = contentDTOs[position].userId

            //Image
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewHoler.detailviewitem_imageview_content)

            //Explain of content
            viewHoler.detailviewitem_explain_textview.text = contentDTOs[position].explain

            //likes
            viewHoler.detailviewitem_favoritecount_textview.text = "Likes" + contentDTOs[position].faviriteCount

            //ProfileImage
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewHoler.detailviewitem_profile_image)

            // This code is when the button is clicked
            viewHoler.detailviewitem_favorite_imageview.setOnClickListener{
                favoriteEvent(position)
            }

            // This code is when the page is loaded
                if (contentDTOs[position].favorites.containsKey(uid)){
                // This is like status
                viewHoler.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
            }else{
                // This is unlike status
                viewHoler.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }
            viewHoler.detailviewitem_profile_image.setOnClickListener{
                val fragment = UserFragment()
                val bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[position].uid)
                bundle.putString("userId",contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }
        }
        override fun getItemCount(): Int {
            return contentDTOs.size
        }
        fun favoriteEvent(position : Int){
            val tsDoc = firestore.collection("image").document(contentUidList[position])
            firestore.runTransaction {

                val contentDTO = it.get(tsDoc).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)){
                    // when the button is clicked
                    contentDTO.faviriteCount = contentDTO.faviriteCount - 1
                    contentDTO.favorites.remove(uid)
                }else{
                    // when the button is not clicked
                    contentDTO.faviriteCount = contentDTO.faviriteCount + 1
                    contentDTO.favorites[uid!!] = true
                }
                it.set(tsDoc,contentDTO)
            }

        }


    }
}