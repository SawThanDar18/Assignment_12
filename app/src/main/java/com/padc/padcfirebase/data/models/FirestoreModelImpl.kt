package com.padc.padcfirebase.data.models

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.padc.padcfirebase.data.vos.ArticleVO
import com.padc.padcfirebase.data.vos.CommentVO
import com.padc.padcfirebase.data.vos.UserVO
import com.padc.padcfirebase.utils.REF_KEY_CLAP_COUNT
import com.padc.padcfirebase.utils.REF_KEY_COMMENTS
import com.padc.padcfirebase.utils.REF_PATH_ARTICLES
import com.padc.padcfirebase.utils.STORAGE_FOLDER_PATH

object FirestoreModelImpl: FirebaseModel {

    const val TAG = "FirestoreModel"

    private val databaseRef = FirebaseFirestore.getInstance()

    override fun getAllArticles(cleared: LiveData<Unit>): LiveData<List<ArticleVO>> {
        val liveData = MutableLiveData<List<ArticleVO>>()

        val articlesRef = databaseRef.collection(REF_PATH_ARTICLES)

        // Read from the database
        val realTimeListener = object : EventListener<QuerySnapshot> {
            override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {

                if (e != null) {
                    Log.w(TAG, "error", e)
                    return
                }

                val articles = ArrayList<ArticleVO>()
                for (snapshot in documentSnapshots?.documents!!) {
                    val article = snapshot.toObject(ArticleVO::class.java)
                    article?.let {
                        articles.add(article)
                    }
                }

                Log.d(TAG, "Value: $articles")

                liveData.value = articles
            }
        }

        // Start real-time data observing
        val listenerRegister = articlesRef.addSnapshotListener(realTimeListener)

        // Stop real-time data observing
        cleared.observeForever(object : Observer<Unit> {
            override fun onChanged(unit: Unit?) {
                unit?.let {
                    listenerRegister.remove()
                    cleared.removeObserver(this)
                }
            }
        })

        return liveData
    }

    override fun getArticleById(id: String, cleared: LiveData<Unit>): LiveData<ArticleVO> {
        val liveData = MutableLiveData<ArticleVO>()

        val articleRef = databaseRef.collection(REF_PATH_ARTICLES).document(id)

        // Start real-time data observing
        val listenerRegister = articleRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d(TAG, "fail", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "data: ${snapshot.data}")
                val article = snapshot.toObject(ArticleVO::class.java)
                article?.also {
                    liveData.value = it
                }

            } else {
                Log.d(TAG, "data=null")
            }
        }

        // Stop real-time data observing
        cleared.observeForever(object : Observer<Unit> {
            override fun onChanged(unit: Unit?) {
                unit?.let {
                    listenerRegister.remove()
                    cleared.removeObserver(this)
                }
            }
        })

        return liveData
    }

    override fun updateClapCount(count: Int, article: ArticleVO) {
        val articleRef = databaseRef.collection(REF_PATH_ARTICLES).document(article.id)
        val data = hashMapOf(REF_KEY_CLAP_COUNT to count + article.claps)
        articleRef.set(data, SetOptions.merge())
    }

    override fun addComment(comment: String, pickedImage: Uri?, article: ArticleVO) {
        if (pickedImage != null) {
            uploadImageAndAddComment(comment, pickedImage, article)
        }
        else {
            val currentUser = UserAuthenticationModelImpl.currentUser!!
            val newComment = CommentVO(
                System.currentTimeMillis().toString(), "", comment, UserVO(
                    currentUser.providerId,
                    currentUser.displayName ?: "",
                    currentUser.photoUrl.toString()
                )
            )
            addComment(newComment, article)
        }
    }

    private fun uploadImageAndAddComment(comment: String, pickedImage: Uri, article: ArticleVO) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesFolderRef = storageRef.child(STORAGE_FOLDER_PATH)

        val imageRef = imagesFolderRef.child(pickedImage.lastPathSegment ?: System.currentTimeMillis().toString())

        val uploadTask = imageRef.putFile(pickedImage)

        uploadTask.addOnFailureListener {
            Log.e(TAG, it.localizedMessage)
        }
            .addOnSuccessListener {
                // get comment image's url

                imageRef.downloadUrl.addOnCompleteListener {
                    Log.d(TAG, "Image Uploaded ${it.result.toString()}")

                    val currentUser = UserAuthenticationModelImpl.currentUser!!
                    val newComment = CommentVO(
                        System.currentTimeMillis().toString(), it.result.toString(), comment,
                        UserVO(
                            currentUser.providerId,
                            currentUser.displayName ?: "",
                            currentUser.photoUrl.toString()
                        )
                    )

                    addComment(newComment, article)
                }

            }
    }

    private fun addComment(comment: CommentVO, article: ArticleVO) {
        val commentsRef = databaseRef.collection(REF_PATH_ARTICLES).document(article.id)

        val key = comment.id

        val comments = article.comments.toMutableMap()
        comments[key] = comment

        val dataWrap = mapOf(REF_KEY_COMMENTS to comments)

        commentsRef.update(dataWrap)
            .addOnSuccessListener {
                Log.d(TAG, "Add Comment")
            }
            .addOnFailureListener {  Log.e(TAG, "Add Comment error ${it.localizedMessage}")
            }
    }
}

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

