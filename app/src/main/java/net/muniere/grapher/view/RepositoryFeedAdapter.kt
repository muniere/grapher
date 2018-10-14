package net.muniere.grapher.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.muniere.grapher.R
import net.muniere.grapher.entity.Repository

public final class RepositoryFeedAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  //
  // Listener
  //
  public interface OnItemClickListener {
    public fun onItemClick(view: View, repository: Repository)
  }

  public var onItemClickListener: OnItemClickListener? = null

  //
  // Data
  //
  private var repositories: List<Repository> = emptyList()

  //
  // Mutation
  //
  public fun setData(data: List<Repository>) {
    this.repositories = data
    this.notifyDataSetChanged()
  }

  //
  // Adapter
  //
  override fun getItemCount(): Int {
    return this.repositories.size
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return RepositoryViewHolder(
      LayoutInflater.from(parent.context).inflate(R.layout.item_repository, parent, false)
    )
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is RepositoryViewHolder -> {
        val repo = this.repositories.getOrNull(position) ?: run {
          throw IllegalStateException("this.repositories.getOrNull($position) == null")
        }

        holder.bind(repo)
        holder.setOnClickListener {
          this.onItemClickListener?.onItemClick(it, repo)
        }
      }
      else -> {
        // do nothing
      }
    }
  }
}
