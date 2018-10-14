package net.muniere.grapher.entity

import java.net.URL

public final data class Repository(
    public val id: String,
    public val url: URL,
    public val name: String,
    public val owner: Owner,
    public val languages: List<Language>,
    public val starCount: Int
)
