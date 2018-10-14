package net.muniere.grapher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.*
import net.muniere.grapher.entity.Repository
import net.muniere.grapher.view.RepositoryFeedAdapter

public final class MainActivity : AppCompatActivity(), LifecycleOwner {

  //
  // Props
  //
  private lateinit var viewModel: MainViewModel

  private val feedAdapter by lazy {
    RepositoryFeedAdapter().also {
      it.onItemClickListener = OnRepositoryClickTranslator(this)
    }
  }

  private val compositeDisposable = CompositeDisposable()

  //
  // Event
  //
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    this.bootstrapViews()
    this.bootstrapModels()
  }

  override fun onDestroy() {
    super.onDestroy()

    this.compositeDisposable.clear()
  }

  //
  // Bootstrap
  //
  private fun bootstrapViews() {
    this.setContentView(R.layout.activity_main)

    this.recyclerView.also {
      it.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
      it.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
      it.adapter = this.feedAdapter
    }
  }

  private fun bootstrapModels() {
    this.viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

    this.observeData()
    this.fetchData()
  }

  //
  // Reactive
  //
  private fun observeData() {
    this.viewModel.repositories.observe(this, Observer {
      this.feedAdapter.setData(it)
    })
  }

  //
  // View
  //
  private fun startProgress() {
    this.progressBar?.visibility = View.VISIBLE
  }

  private fun stopProgress() {
    this.progressBar?.visibility = View.GONE
  }

  private fun alertError(error: Throwable) {
    Log.e("graphql", error.localizedMessage)
  }

  //
  // Network
  //
  private fun fetchData() {
    this.startProgress()

    this.viewModel.search(query = "language")
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeBy(
        onComplete = this::stopProgress,
        onError = this::alertError
      )
      .addTo(this.compositeDisposable)
  }

  //
  // Translator
  //
  private class OnRepositoryClickTranslator(
    private val parent: MainActivity
  ) : RepositoryFeedAdapter.OnItemClickListener {
    override fun onItemClick(view: View, repository: Repository) {
      this.parent.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(repository.url.toString()))
      )
    }
  }
}
