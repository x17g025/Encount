package com.example.encount.user

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.encount.PostDataClassList
import com.example.encount.PostList
import com.example.encount.R
import com.example.encount.SQLiteHelper
import com.example.encount.post.PostAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_user_home.swipelayout
import kotlinx.android.synthetic.main.activity_user_post_list.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.lang.Exception

class UserPostList : Fragment() {

    var _helper : SQLiteHelper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.activity_user_post_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        _helper = SQLiteHelper(context)

        swipelayout.setColorSchemeResources(R.color.colorMain)

        UserPostGet().execute()

        /*
        PostDataList.setOnItemClickListener {parent, view, position, id ->

            val postId = view.findViewById<TextView>(R.id.PostId).text

            /* val intent = Intent(this, PostDetails::class.java)
             intent.putExtra("Post_Id", postId)
             startActivity(intent)*/
        }

        PostDataList.setOnItemLongClickListener { parent, view, position, id ->

            val userId = view.findViewById<TextView>(R.id.UserId).text

            var id     = ""
            val db     = _helper!!.writableDatabase
            val sql    = "select * from userInfo"
            val cursor = db.rawQuery(sql, null)

            while(cursor.moveToNext()){

                val idxId = cursor.getColumnIndex("user_id")
                id = cursor.getString(idxId)
            }

            if(userId == id){

                // startActivity(Intent(this, UserProfile::class.java))
            }
            else{

                /*val intent = Intent(this, FriendProfile::class.java)
                intent.putExtra("User_Id", userId)
                startActivity(intent)*/
            }

            return@setOnItemLongClickListener true
        }*/

        swipelayout.setOnRefreshListener {

            UserPostGet().execute()
        }
    }

    private inner class UserPostGet() : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {

            var id = ""
            val db = _helper!!.writableDatabase
            val sql = "select * from userInfo"
            val cursor = db.rawQuery(sql, null)

            while(cursor.moveToNext()){

                val idxId = cursor.getColumnIndex("user_id")
                id = cursor.getString(idxId)
            }

            val client = OkHttpClient()

            //アクセスするURL
            val url = "https://encount.cf/encount/UserPostGet.php"

            //Formを作成
            val formBuilder = FormBody.Builder()

            //formに要素を追加
            formBuilder.add("id",id)
            //リクエストの内容にformを追加
            val form = formBuilder.build()

            //リクエストを生成
            val request = Request.Builder().url(url).post(form).build()

            try {
                val response = client.newCall(request).execute()
                return response.body()!!.string()
            }
            catch (e: IOException) {
                e.printStackTrace()
                return "Error"
            }
        }

        override fun onPostExecute(result: String) {

            try {
                var postList = mutableListOf<PostList>()
                val listType = object : TypeToken<List<PostDataClassList>>() {}.type
                val postData = Gson().fromJson<List<PostDataClassList>>(result, listType)

                for (i in postData) {

                    if(i.likeId == null){

                        postList.add(
                            PostList(
                                "false",
                                i.postId,
                                i.userId,
                                i.userName,
                                i.postText,
                                i.postDate,
                                i.postImage
                            )
                        )
                    }
                    else{

                        postList.add(
                            PostList(
                                i.likeId,
                                i.postId,
                                i.userId,
                                i.userName,
                                i.postText,
                                i.postDate,
                                i.postImage
                            )
                        )
                    }
                }
                postList[1].postid

                UserProfilePost.adapter = PostAdapter(context, postList)
                swipelayout.isRefreshing = false
            }
            catch(e : Exception){

            }
        }
    }
}