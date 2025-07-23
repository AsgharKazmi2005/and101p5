package com.example.apis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    data class DogItem(val imageUrl: String, val breed: String, val status: String)

    class DogAdapter(private val dogs: List<DogItem>) :
        RecyclerView.Adapter<DogAdapter.DogViewHolder>() {

        class DogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image: ImageView = view.findViewById(R.id.dog_image)
            val breed: TextView = view.findViewById(R.id.dog_breed)
            val status: TextView = view.findViewById(R.id.dog_status)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.dog_item, parent, false)
            return DogViewHolder(view)
        }

        override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
            val dog = dogs[position]
            Glide.with(holder.image.context).load(dog.imageUrl).into(holder.image)
            holder.breed.text = "Breed: ${dog.breed}"
            holder.status.text = "Status: ${dog.status}"
        }

        override fun getItemCount(): Int = dogs.size
    }

    private val dogs = mutableListOf<DogItem>()
    private lateinit var adapter: DogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        adapter = DogAdapter(dogs)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchDogs(10)


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem >= totalItemCount - 3) {
                    fetchDogs(5) // Load more
                }
            }
        })
    }

    private fun fetchDogs(count: Int) {
        val client = AsyncHttpClient()

        repeat(count) {
            client.get("https://dog.ceo/api/breeds/image/random", object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    val imageUrl = response?.getString("message") ?: return
                    val status = response.optString("status", "unknown")
                    val breed = imageUrl.split("/").getOrNull(4)
                        ?.replace("-", " ")
                        ?.replaceFirstChar { it.uppercase() } ?: "Unknown"

                    dogs.add(DogItem(imageUrl, breed, status))
                    adapter.notifyItemInserted(dogs.size - 1)
                }
            })
        }
    }
}
