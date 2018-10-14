package net.muniere.grapher.net

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import io.reactivex.Single
import io.reactivex.exceptions.Exceptions
import io.reactivex.schedulers.Schedulers
import okhttp3.Authenticator
import okhttp3.OkHttpClient

public final class GraphClient(
  private val token: String
) {

  //
  // Props
  //
  private val delegate by lazy {
    this.makeApolloClient()
  }

  //
  // Request
  //
  public fun <T> request(query: Query<*, T, *>): Single<Response<T>> {
    val watcher = this.delegate.query(query).watcher()

    val single = Single.create<Response<T>> { emitter ->
      emitter.setCancellable {
        watcher.cancel()
      }

      watcher.enqueueAndWatch(object : ApolloCall.Callback<T>() {
        override fun onResponse(response: Response<T>) {
          if (!emitter.isDisposed) {
            emitter.onSuccess(response);
          }
        }

        override fun onFailure(e: ApolloException) {
          Exceptions.throwIfFatal(e)

          if (!emitter.isDisposed) {
            emitter.onError(e);
          }
        }
      })
    }

    return single.subscribeOn(Schedulers.io())
  }

  //
  // Helper
  //
  private fun makeApolloClient(): ApolloClient {
    val httpClient = this.makeHttpClient()

    return ApolloClient.builder()
      .okHttpClient(httpClient)
      .serverUrl("https://api.github.com/graphql")
      .build()
  }

  private fun makeHttpClient(): OkHttpClient {
    val authenticator = this.makeAuthenticator()

    return OkHttpClient.Builder()
      .authenticator(authenticator)
      .build()
  }

  private fun makeAuthenticator(): Authenticator {
    return Authenticator { _, response ->
      response.request().newBuilder()
        .addHeader("Authorization", "Bearer ${this.token}")
        .build()
    }
  }
}
