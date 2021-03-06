/*
Android Rivers is an app to read and discover news using RiverJs, RSS and OPML format.
Copyright (C) 2012 Dody Gunawinata (dodyg@silverkeytech.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package com.silverkeytech.android_rivers

import android.os.Bundle
import com.actionbarsherlock.app.SherlockListActivity
import com.actionbarsherlock.view.Menu
import com.actionbarsherlock.view.MenuItem
import com.silverkeytech.android_rivers.db.getBookmarksFromDbByCollection
import java.text.MessageFormat

public open class BookmarkCollectionActivity(): SherlockListActivity() {
    class object {
        public val TAG: String = javaClass<BookmarkCollectionActivity>().getSimpleName()
    }

    var collectionTitle: String = ""
    var collectionId: Int = 0

    public override fun onCreate(savedInstanceState: Bundle?): Unit {
        setTheme(this.getVisualPref().getTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.collection)

        var i = getIntent()!!
        collectionId = i.getIntExtra(Params.COLLECTION_ID, 0)
        collectionTitle = i.getStringExtra(Params.COLLECTION_TITLE)!!

        var actionBar = getSupportActionBar()!!
        actionBar.setDisplayShowHomeEnabled(false) //hide the app icon.
        actionBar.setDisplayShowTitleEnabled(true)
        actionBar.setTitle(getString(R.string.edit_sources_of_collection, collectionTitle))

        displayCollection()
    }


    public override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater = getSupportMenuInflater()!!
        inflater.inflate(R.menu.collection_menu, menu)
        return true
    }

    public override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.getItemId()){
            R.id.collection_menu_help ->{
                downloadOpml(this, PreferenceDefaults.CONTENT_OUTLINE_HELP_SOURCE, getString(R.string.help)!!)
                return true
            }
            else -> {
                return false
            }
        }
    }

    public fun refreshCollection() {
        displayCollection()
    }

    fun displayCollection() {
        val bookmarks = getBookmarksFromDbByCollection(collectionId)
        BookmarkCollectionRenderer(this).handleListing(bookmarks)
    }
}