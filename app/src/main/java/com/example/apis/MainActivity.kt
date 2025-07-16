package com.example.apis

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DogApp()
        }
    }
}

@Composable
fun DogApp() {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var breed by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun fetchDogImage() {
        val client = AsyncHttpClient()
        val url = "https://dog.ceo/api/breeds/image/random"

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                val image = response?.getString("message") ?: ""
                imageUrl = image
                status = response?.getString("status") ?: ""

                // Parse breed from URL (e.g., .../breeds/hound-afghan/...)
                breed = image.split("/").getOrNull(4)?.replace("-", " ")?.replaceFirstChar { it.uppercase() } ?: "Unknown"
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                throwable: Throwable?,
                errorResponse: JSONObject?
            ) {
                imageUrl = null
                status = "error"
                breed = ""
            }
        })
    }

    // Initial image load
    LaunchedEffect(Unit) {
        fetchDogImage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageUrl != null) {
            AndroidView(
                factory = {
                    ImageView(it).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(600, 600)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        Glide.with(it).load(imageUrl).into(this)
                    }
                },
                update = { imageView ->
                    Glide.with(context).load(imageUrl).into(imageView)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Breed: $breed", style = MaterialTheme.typography.bodyLarge)
            Text("Status: $status", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { fetchDogImage() }) {
            Text("Fetch New Dog")
        }
    }
}
