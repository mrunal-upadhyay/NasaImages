package com.mrunal.nasaimages

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), ImageRequester.ImageRequesterResponse {

  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var gridLayoutManager: GridLayoutManager
  private var photosList: ArrayList<Photo> = ArrayList()
  private lateinit var imageRequester: ImageRequester
  private lateinit var adapter:RecyclerAdapter
  private val lastVisibleItemPosition:Int
    get() = if(recyclerView.layoutManager == linearLayoutManager) {
      linearLayoutManager.findLastVisibleItemPosition()
    } else {
     gridLayoutManager.findLastVisibleItemPosition()
    }

  // For loading/requesting new photo when the user is viewing the last item in the list
  private fun setRecyclerViewScrollListener() {
    recyclerView.addOnScrollListener(object:RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        val totalItemCount = recyclerView.layoutManager!!.itemCount
        if(!imageRequester.isLoadingData && totalItemCount == lastVisibleItemPosition + 1) {
          requestPhoto()
        }
      }
    })
  }

  // For swipe gestures - left/right to remove the item from the list and notify the adapter
  private fun setRecyclerViewItemTouchListener() {
    val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
      override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
        return false
      }

      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
        val position = viewHolder.adapterPosition
        photosList.removeAt(position)
        recyclerView.adapter!!.notifyItemRemoved(position)
      }
    }

    val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
    itemTouchHelper.attachToRecyclerView(recyclerView)
  }

  // For changing the layoutManager
  private fun changeLayoutManager() {
    if(recyclerView.layoutManager == linearLayoutManager){
      recyclerView.layoutManager = gridLayoutManager
      if(photosList.size == 1){
        requestPhoto()
      }
    } else {
      recyclerView.layoutManager = linearLayoutManager
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    linearLayoutManager = LinearLayoutManager(this)
    gridLayoutManager = GridLayoutManager(this,2)
    recyclerView.layoutManager = linearLayoutManager
    adapter= RecyclerAdapter(photosList)
    recyclerView.adapter  = adapter
    setRecyclerViewScrollListener()
    setRecyclerViewItemTouchListener()

    imageRequester = ImageRequester(this)
  }

  override fun onStart() {
    super.onStart()
    if(photosList.size == 0){
      requestPhoto()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if(item.itemId == R.id.action_change_recycler_manager){
      changeLayoutManager()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  private fun requestPhoto() {
    try {
      imageRequester.getPhoto()
    } catch (e: IOException) {
      e.printStackTrace()
    }

  }

  override fun receivedNewPhoto(newPhoto: Photo) {
    runOnUiThread {
      photosList.add(newPhoto)
      adapter.notifyItemInserted(photosList.size-1)
    }
  }
}
