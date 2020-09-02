package com.ralhub.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.api.Billing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.ralhub.LoginMainActivity
import com.ralhub.MainActivity
import kotlinx.android.synthetic.main.fragment_user.view.*
import com.ralhub.R
import com.ralhub.model.AlarmDTO
import com.ralhub.model.ContentDTO
import com.ralhub.model.FollowDTO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class UserFragment : Fragment(){
    lateinit var fragmentView : View
    lateinit var firestore : FirebaseFirestore
    lateinit var uid : String
    lateinit var auth : FirebaseAuth
    lateinit var currentUserUid: String
    lateinit var imageprofileListenerRegistration : ListenerRegistration
    lateinit var followListenerRegistration: ListenerRegistration
    lateinit var followingListenerRegistration: ListenerRegistration
    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        uid = arguments?.getString("destinationUid")!!
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = auth.currentUser?.uid.toString()

        if (uid == currentUserUid){
            //my page
            fragmentView.account_btn_follow_signout.text = getString(R.string.signout)
            fragmentView.account_btn_follow_signout.setOnClickListener{
                activity?.finish()
                startActivity(Intent(activity,LoginMainActivity::class.java))
                auth.signOut()
            }
        }else{
            // otherUserPage
            fragmentView.account_btn_follow_signout.text = getString(R.string.follow)
            val mainactivity = (activity as MainActivity)
            mainactivity.toolbar_username.text = arguments!!.getString("userId")
            mainactivity.toolbar_btn_back.setOnClickListener {
                mainactivity.bottom_navigation.selectedItemId = R.id.navigation_home
            }
            mainactivity.toolbar_title_image.visibility = View.GONE
            mainactivity.toolbar_username.visibility = View.VISIBLE
            mainactivity.toolbar_btn_back.visibility = View.VISIBLE
            mainactivity.message_send_button.visibility = View.GONE
            fragmentView.account_btn_follow_signout.setOnClickListener {
                requestFollow()
            }

        }

        fragmentView.account_recyclerview.adapter = UserFragmentRecyclerViewAdpater()
        fragmentView.account_recyclerview.layoutManager = GridLayoutManager(activity!!,3)

        fragmentView.account_iv_profile.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }
//        getFollowerAndFolloewing()
        getFollowing()
        getFollower()
        getProfileImage()
        return fragmentView
    }
    fun getFollowing(){
        followingListenerRegistration = firestore.collection("users").document(uid).addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->
            val followDTO = documentSnapshot?.toObject(FollowDTO::class.java)
            if (followDTO == null) return@addSnapshotListener
            fragmentView.account_tv_following_count.text = followDTO.followingCount.toString()
        }
    }
    fun getFollower() {

        followListenerRegistration = firestore.collection("users").document(uid).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val followDTO = documentSnapshot?.toObject(FollowDTO::class.java)
            if (followDTO == null) return@addSnapshotListener
            fragmentView.account_tv_follower_count?.text = followDTO.followerCount.toString()
            if (followDTO.followers.containsKey(currentUserUid)) {

                fragmentView.account_btn_follow_signout?.text = getString(R.string.follow_cancel)
                fragmentView.account_btn_follow_signout
                    ?.background
                    ?.setColorFilter(ContextCompat.getColor(activity!!, R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
            } else {

                if (uid != currentUserUid) {

                    fragmentView.account_btn_follow_signout?.text = getString(R.string.follow)
                    fragmentView.account_btn_follow_signout?.background?.colorFilter = null
                }
            }

        }
    }

//    fun getFollowerAndFolloewing(){
//        firestore.collection("users").document(uid).addSnapshotListener{ documentSnapshot , firebaseFirestoreException ->
//            if(documentSnapshot == null ) return@addSnapshotListener
//            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
//            if (followDTO?.followingCount != null){
//                fragmentView.account_btn_follow_signout.text = getString(R.string.follow_cancel)
//                fragmentView.account_btn_follow_signout.background.setColorFilter(ContextCompat.getColor(activity!!,R.color.colorLightGray),PorterDuff.Mode.MULTIPLY)
//            }else{
//                if (uid != currentUserUid){
//                    fragmentView.account_btn_follow_signout.text = getString(R.string.follow)
//                    fragmentView.account_btn_follow_signout.background.colorFilter =null
//                }
//            }
//        }
//    }

    fun requestFollow(){
        // Save data to my account
        val tsDocFollowing = firestore.collection("users").document(currentUserUid)
        firestore.runTransaction {
            var followDTO = it.get(tsDocFollowing).toObject(FollowDTO::class.java)

            if (followDTO == null ){
                followDTO = FollowDTO()
                followDTO.followingCount = 1
                followDTO.followings[uid] = true
                it.set(tsDocFollowing,followDTO)
                return@runTransaction
            }
            if (followDTO.followings.containsKey(uid)){
                // It remove follwing third person when a third person folloew me
                followDTO.followingCount = followDTO.followingCount - 1
                followDTO.followings.remove(uid)
            }else{
                // It add follwing third person when a third person do not folloew me
                followDTO.followingCount = followDTO.followingCount + 1
                followDTO.followings[uid] = true
                folloewrAlarm(uid)
            }
            it.set(tsDocFollowing,followDTO)
            return@runTransaction
        }
        // save data to third person
        val tsDocFollower = firestore.collection("users").document(uid)
        firestore.runTransaction{
            var followDTO = it.get(tsDocFollower).toObject(FollowDTO::class.java)
            if (followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid] = true

                it.set(tsDocFollower,followDTO!!)
                return@runTransaction
            }
            if (followDTO!!.followers.containsKey(currentUserUid)){
                // It cancel my follower when i folloew a third person
                followDTO!!.followerCount =followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid)
            }else{
                followDTO!!.followerCount = followDTO!!.followerCount +1
                followDTO!!.followers[currentUserUid] = true
                folloewrAlarm(uid)
            }
            it.set(tsDocFollower,followDTO!!)
            return@runTransaction
        }
    }
    fun folloewrAlarm(destinationUid : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth.currentUser?.email
        alarmDTO.uid = auth.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp =System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }

    fun getProfileImage(){
        imageprofileListenerRegistration  = firestore.collection("profileImages").document(uid)
            .addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->

            if (documentSnapshot?.data != null) {
                val url= documentSnapshot.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView.account_iv_profile!!)
            }
        }

    }

    inner class UserFragmentRecyclerViewAdpater : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore.collection("images").whereEqualTo("uid",uid).addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                //sometimes, This code return null of querySnapshot when it signout
                if(querySnapshot == null) return@addSnapshotListener
                // Get data
                for(snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                fragmentView.account_tv_post_count.text =contentDTOs.size.toString()
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val width = resources.displayMetrics.widthPixels / 3

            val imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHolder(imageview)
        }
        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview){
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}