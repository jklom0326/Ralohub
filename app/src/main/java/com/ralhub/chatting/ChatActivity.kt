package com.ralhub.chatting

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class ChatActivity {
    class ChatAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return 0
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            return null
        }
    }
}