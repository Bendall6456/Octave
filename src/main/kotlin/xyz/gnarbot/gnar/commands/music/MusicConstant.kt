package xyz.gnarbot.gnar.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import xyz.gnarbot.gnar.Bot
import java.util.regex.Pattern

private val prefix = Bot.getInstance().configuration.prefix
internal val PLAY_MESSAGE = "\uD83C\uDFB6 `${prefix}play (song/url)` in a voice channel to start playing some music!"

private val markdownCharacters = "[*_`~]".toRegex()
val AudioTrackInfo.embedTitle: String get() = markdownCharacters.replace(title) { "\\${it.value}" }