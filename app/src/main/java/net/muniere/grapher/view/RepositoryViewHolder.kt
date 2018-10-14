package net.muniere.grapher.view

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_repository.view.*
import net.muniere.grapher.entity.Language
import net.muniere.grapher.entity.Repository
import java.text.NumberFormat

public final class RepositoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  //
  // Constants
  //
  private object Const {
    internal object Limit {
      internal const val LANGUAGE: Int = 5
    }
    internal object Symbol {
      internal const val SPACE = " "
      internal const val LEGEND_EMPTY = "□"
      internal const val LEGEND_FILLED = "■"
    }
  }

  //
  // Bind
  //
  public fun bind(repository: Repository) {
    this.itemView.nameLabel.text = this.makeNameText(repository)
    this.itemView.languageLabel.text = this.makeLanguageText(repository)
    this.itemView.stargazerLabel.text = this.makeStargazerText(repository)
  }

  public fun setOnClickListener(listener: ((View) -> Unit)?) {
    this.itemView.setOnClickListener(listener)
  }

  //
  // Helper
  //
  private fun makeNameText(repository: Repository): CharSequence {
    return "%s / %s".format(repository.owner.name, repository.name)
  }

  private fun makeLanguageText(repository: Repository): CharSequence {
    val languages = repository.languages.take(Const.Limit.LANGUAGE)

    return languages.joinTo(SpannableStringBuilder(), separator = ", ") {
      this.makeLanguageText(it)
    }
  }

  private fun makeLanguageText(language: Language): CharSequence {
    val legend = this.makeLegendText(language)
    val name = this.makeLangNameText(language)
    val space = Const.Symbol.SPACE

    return SpannableStringBuilder()
        .append(legend)
        .append(space)
        .append(name)
  }

  private fun makeLegendText(language: Language): CharSequence {
    when (language.color) {
      null -> {
        return SpannableString(Const.Symbol.LEGEND_EMPTY)
      }
      else -> {
        return SpannableString(Const.Symbol.LEGEND_FILLED).also {
          val color = Color.parseColor(language.color)
          val span = ForegroundColorSpan(color)
          val flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
          it.setSpan(span, 0, it.length, flag)
        }
      }
    }
  }

  private fun makeLangNameText(language: Language): CharSequence {
    return SpannableString(language.name)
  }

  private fun makeStargazerText(repository: Repository): CharSequence {
    return "%s Stars".format(NumberFormat.getIntegerInstance().format(repository.starCount))
  }
}
