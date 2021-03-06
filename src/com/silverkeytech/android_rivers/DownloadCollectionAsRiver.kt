package com.silverkeytech.android_rivers

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.silverkeytech.android_rivers.riverjs.RiverItemMeta
import com.silverkeytech.android_rivers.riverjs.accumulateList
import com.silverkeytech.android_rivers.riverjs.sortRiverItemMeta
import com.silverkeytech.android_rivers.syndications.SyndicationFilter
import com.silverkeytech.android_rivers.syndications.downloadSingleFeed
import java.util.Calendar
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

public class DownloadCollectionAsRiver(it: Context?, private val collectionId: Int): AsyncTask<String, Int, Result<List<RiverItemMeta>>>(){
    class object {
        public val TAG: String = javaClass<DownloadCollectionAsRiver>().getSimpleName()
    }

    var context: Activity = it!! as Activity
    var dialog: InfinityProgressDialog = InfinityProgressDialog(context, context.getString(R.string.please_wait_while_loading)!!)

    protected override fun onPreExecute() {
        dialog.onCancel {
            dlg ->
            dlg.dismiss()
            this@DownloadCollectionAsRiver.cancel(true)
        }

        dialog.show()
    }

    protected override fun doInBackground(vararg p0: String?): Result<List<RiverItemMeta>>? {
        val latestDate = daysBeforeNow(PreferenceDefaults.CONTENT_BOOKMARK_COLLECTION_LATEST_DATE_FILTER_IN_DAYS)
        val before = System.currentTimeMillis()
        var list = arrayListOf<RiverItemMeta>()

        var executor = Executors.newFixedThreadPool(4)

        val maximumItemFilter = when(p0.size){
            in 0..5 -> 20
            in 6..12 -> 15
            in 12..20 -> 10
            else -> 6
        }

        for(val url in p0){
            executor.execute(
                runnable {
                    val res = downloadSingleFeed(url!!, SyndicationFilter(maximumItemFilter, latestDate))

                    if (res.isFalse())
                        Log.d(TAG, "Value at ${res.exception?.getMessage()}")
                    else
                    {
                        Log.d(TAG, "Download for $url is successful")
                        accumulateList(list, res.value!!)
                    }
                }
            )
        }

        executor.shutdown()

        try{
            if (!executor.awaitTermination(20.toLong(), TimeUnit.SECONDS))
                executor.shutdownNow()

            val after = System.currentTimeMillis()
            val diff = after - before
            Log.d(TAG, "It takes $diff mili seconds to complete ${p0.size} rss downloads")
            return Result.right(list)
        }
        catch(ex : InterruptedException){
            executor.shutdownNow()
            return Result.wrong<List<RiverItemMeta>>(ex)
        }
    }

    var callback: ((String, Result<List<RiverItemMeta>>) -> Unit)? = null

    //Set up function to call when download is done
    public fun executeOnCompletion(action: ((String, Result<List<RiverItemMeta>>) -> Unit)?): DownloadCollectionAsRiver {
        callback = action
        return this
    }

    protected override fun onPostExecute(result: Result<List<RiverItemMeta>>?) {
        dialog.dismiss()
        val url = makeLocalUrl(collectionId)
        if (result != null){
            if (result.isTrue()){
                try{
                    val sortedNewsItems = sortRiverItemMeta(result.value!!)
                    context.getMain().setRiverCache(url, sortedNewsItems, 3.toHoursInMinutes())
                    if (callback != null)
                        callback!!(url, Result.right(sortedNewsItems))
                }
                catch (e: Exception){
                    if (callback != null)
                        callback!!(url, Result.wrong<List<RiverItemMeta>>(result.exception))
                }
            }
            else
            {
                if (callback != null)
                    callback!!(url, Result.wrong<List<RiverItemMeta>>(result.exception))
            }
        }
    }
}