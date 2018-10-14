package net.muniere.grapher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.apollographql.apollo.api.Response
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import net.muniere.grapher.entity.Language
import net.muniere.grapher.entity.Owner
import net.muniere.grapher.entity.Repository
import net.muniere.grapher.net.GraphClient
import java.net.URL

public final class MainViewModel(app: Application) : AndroidViewModel(app) {

  //
  // Props
  //
  public val repositories: LiveData<List<Repository>>
    get() = this._repositories

  private var _repositories = MutableLiveData<List<Repository>>()

  private val client by lazy {
    GraphClient(this.getApplication<Application>().getString(R.string.github_api_token))
  }

  //
  // Network
  //
  public fun search(query: String): Completable {
    return this.client
      .request(SearchRepositoryQuery(query))
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { response: Response<SearchRepositoryQuery.Data> ->
        this._repositories.value = response.data()?.let(this::transform) ?: emptyList()
      }
      .ignoreElement()
  }

  //
  // Helper
  //
  private fun transform(data: SearchRepositoryQuery.Data): List<Repository> {
    val edges = data.search().edges() ?: run {
      return emptyList()
    }

    return edges.asSequence()
      .mapNotNull { it.node() }
      .mapNotNull { it.asRepository() }
      .mapNotNull { this.transform(it) }
      .toList()
  }

  private fun transform(data: SearchRepositoryQuery.AsRepository): Repository {
    return Repository(
      id = data.id(),
      url = data.url().toString().let(::URL),
      name = data.name(),
      owner = data.owner().let(this::transform),
      languages = data.languages()?.let(this::transform) ?: emptyList(),
      starCount = data.stargazers().totalCount
    )
  }

  private fun transform(data: SearchRepositoryQuery.Owner): Owner {
    return Owner(
      id = data.id(),
      name = data.login(),
      avatar = data.avatarUrl().toString().let(::URL)
    )
  }

  private fun transform(data: SearchRepositoryQuery.Languages): List<Language> {
    val edges = data.edges() ?: run {
      return emptyList<Language>()
    }

    return edges.asSequence()
      .mapNotNull { it.node() }
      .mapNotNull { this.transform(it) }
      .toList()
  }

  private fun transform(data: SearchRepositoryQuery.Node1): Language {
    return Language(
      id = data.id(),
      name = data.name(),
      color = data.color()
    )
  }
}
