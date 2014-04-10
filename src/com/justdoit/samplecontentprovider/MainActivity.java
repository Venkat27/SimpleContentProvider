package com.justdoit.samplecontentprovider;



import java.util.ArrayList;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

public class MainActivity extends Activity implements 
NewItemFragment.OnNewItemAddedListener, 
LoaderManager.LoaderCallbacks<Cursor> {

	 private ArrayList<ToDoItem> todoItems;
	  private ToDoItemAdapter aa;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		   FragmentManager fm = getFragmentManager();
		    ToDoListFragment todoListFragment = 
		      (ToDoListFragment)fm.findFragmentById(R.id.TodoListFragment);
		     
		    // Create the array list of to do items
		    todoItems = new ArrayList<ToDoItem>();
		     
		    // Create the array adapter to bind the array to the ListView
		    int resID = R.layout.todolist_item;
		    aa = new ToDoItemAdapter(this, resID, todoItems);
		     
		    // Bind the array adapter to the ListView.
		    todoListFragment.setListAdapter(aa);
		    getLoaderManager().initLoader(0, null, this);
	}

	 @Override
	  protected void onResume() {
	    super.onResume();
	    getLoaderManager().restartLoader(0, null, this);
	  }
	 
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		  CursorLoader loader = new CursorLoader(this, 
			      TodoContentProvider.CONTENT_URI, null, null, null, null);
			    
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		 int keyTaskIndex = cursor.getColumnIndexOrThrow(TodoContentProvider.KEY_TASK);
		    
		    todoItems.clear();
		    while (cursor.moveToNext()) {
		      ToDoItem newItem = new ToDoItem(cursor.getString(keyTaskIndex));
		      todoItems.add(newItem);
		    }
		    aa.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewItemAdded(String newItem) {
		  ContentValues values = new ContentValues();
		    values.put(TodoContentProvider.KEY_TASK, newItem);
		    
		    getContentResolver().insert(TodoContentProvider.CONTENT_URI, values);
		    getLoaderManager().restartLoader(0, null, this);
		
	}

}
