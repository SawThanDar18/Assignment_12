package com.padc.padcfirebase.data.models

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.padc.padcfirebase.data.vos.ArticleVO
import com.padc.padcfirebase.utils.REF_PATH_ARTICLES

object FirestoreModelImpl {

}

//object FirestoreModelImpl: FirebaseModel {
//    const val TAG = "FirebaseModel"
//
//    private val databaseRef = FirebaseDatabase.getInstance().reference
//
//    private val firestore = FirebaseFirestore.getInstance()
//
//    override fun getAllArticles(cleared: LiveData<Unit>): LiveData<List<ArticleVO>> {
//        val liveData = MutableLiveData<List<ArticleVO>>()
//
//        val articlesRef = databaseRef.child(REF_PATH_ARTICLES)
//
//        // Read from the database
//        val realTimeListener = object : ValueEventListener {
//
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                Log.d(TAG, "Key is: ${dataSnapshot.key}")
//
//                val articles = ArrayList<ArticleVO>()
//
//                for (articleData in dataSnapshot.children){
//                    val article = articleData.getValue(ArticleVO::class.java)
//                    article?.let{
//                        articles.add(article)
//                    }
//                }
//
//                Log.d(TAG, "Value is: $articles")
//
//                //add to firestore
//                for (article in articles){
//                    val id = article.id
//
//                    firestore.collection("articles")
//                        .document(id)
//                        .set(article)
//                        .addOnSuccessListener {
//                            Log.d(TAG, "DocumentSnapshot added with ID: ${it}")
//                        }
//                        .addOnFailureListener {
//                            Log.d(TAG, "Error adding document", it)
//                        }
//                }
//
//                liveData.value = articles
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException())
//            }
//        }
//
//        // Start real-time data observing
//        articlesRef.addValueEventListener(realTimeListener)
//
//        // Stop real-time data observing when Presenter's onCleared() was called
//        cleared.observeForever(object : Observer<Unit> {
//            override fun onChanged(unit: Unit?) {
//                unit?.let {
//                    articlesRef.removeEventListener(realTimeListener)
//                    cleared.removeObserver(this)
//                }
//            }
//        })
//
//        return liveData
//    }
//
//    override fun getArticleById(id: String, cleared: LiveData<Unit>): LiveData<ArticleVO> {
//    }
//
//    override fun updateClapCount(count: Int, article: ArticleVO) {
//    }
//
//    override fun addComment(comment: String, pickedImage: Uri?, article: ArticleVO) {
//    }
//}