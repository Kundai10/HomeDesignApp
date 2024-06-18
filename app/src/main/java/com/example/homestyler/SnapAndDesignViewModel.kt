package com.example.homestyler

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homestyler.model.GetResultResponse
import com.example.homestyler.model.PredictionInputData
import com.example.homestyler.model.PredictionRequest
import com.example.homestyler.model.PredictionResponse
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.Response


class SnapAndDesignViewModel : ViewModel() {

    private val api: ReplicateApi = RetrofitInstance.replicateApi
    private val _predictionResponse: MutableLiveData<PredictionResponse> = MutableLiveData()
    val predictionResponse: LiveData<PredictionResponse> = _predictionResponse

    private val _getResultResponse: MutableLiveData<GetResultResponse> = MutableLiveData()
    val getResultResponse: LiveData<GetResultResponse> = _getResultResponse

    var getUrlJob: Job? = null

    fun makePrediction(imageUrl: String, promptText: String) {
        viewModelScope.launch {
            val requestBody = PredictionRequest(
                input = PredictionInputData(
                    image = imageUrl,
                    prompt = promptText
                )
            )
            val request = api.makePrediction(requestBody)
            if (request.isSuccessful) {
                _predictionResponse.value = request.body()
            } else {
                // You can do some error handling here
                // Hint: Explore what sealed classes can do for handling state ;)

            }
        }
    }

    // Function to retrieve result using URL
    fun getResult(url: String) {
        val coroutinePoller = CoroutinePoller()
        getUrlJob = viewModelScope.launch {
            try {
                // Call the API using Retrofit
                Log.d("GET_RESULT", "result for $url")
                // Polls every 3 seconds.
                val response = coroutinePoller.poll(3000, url)
                response.collect {
                    if (it.isSuccessful) {
                        Log.d("GET_RESULT", "success")
                        if (it.body()?.status == "succeeded") {
                            _getResultResponse.value = it.body()
                            coroutinePoller.close()
                        }
                    } else {
                        Log.d("GET_RESULT", "Fail with ${it}")
                        // Handle API call failure (e.g., show error message)
                        coroutinePoller.close()
                    }
                }
                // Poll the API with exponential backoff.

                // Check for successful response

            } catch (e: Exception) {
                // Exception is still thrown when job is killed
                Log.d("GET_RESULT", "Error with $e")
                // Handle network errors (e.g., logging, retry logic)
                coroutinePoller.close()
            }
        }
    }

    // Polling example I found here but its broken because cancellation doesn't happen from dispatcher
    // I used Coroutine Job workaround whilst I think about a better way.
    // but see article here --> https://proandroiddev.com/polling-with-kotlin-channels-flows-1a69e94fdfe9
    private inner class CoroutinePoller : Poller {
        val dispatcher = Dispatchers.IO

        @OptIn(DelicateCoroutinesApi::class)
        override fun poll(delay: Long, url: String): Flow<Response<GetResultResponse>> {
            return channelFlow {
                while (!isClosedForSend) {
                    delay(delay)
                    Log.d("POLL", "attempting to poll")
                    val data = api.getResult(url)
                    trySend(data)
                }
            }.flowOn(dispatcher)
        }

        override fun close() {
            Log.d("POLL", "Closing the Poll Completed")
            dispatcher.cancel()
            // needs to be removed and only should work with dispatcher cancel.
            getUrlJob?.cancel()
            getUrlJob = null
        }

    }

}

interface Poller {
    fun poll(delay: Long, url: String): Flow<Response<GetResultResponse>>
    fun close()
}