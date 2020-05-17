package gg.octave.bot.commands.general

import com.sun.management.OperatingSystemMXBean
import gg.octave.bot.Launcher
import gg.octave.bot.utils.Capacity
import gg.octave.bot.utils.OctaveBot
import gg.octave.bot.utils.extensions.config
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.JDAInfo
import org.json.JSONObject
import java.lang.management.ManagementFactory
import java.text.DecimalFormat

class BotInfo : Cog {
    private val dpFormatter = DecimalFormat("0.00")

    @Command(aliases = ["about", "info", "stats"], description = "Show information about the bot.")
    fun botinfo(ctx: Context) {
        val commandSize = ctx.commandClient.commands.size

        // Uptime
        val s = ManagementFactory.getRuntimeMXBean().uptime / 1000
        val m = s / 60
        val h = m / 60
        val d = h / 24

        val osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
        val procCpuUsage = dpFormatter.format(osBean.processCpuLoad * 100)
        val sysCpuUsage = dpFormatter.format(osBean.systemCpuLoad * 100)
        val ramUsedBytes = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        val ramUsedCalculated = Capacity.calculate(ramUsedBytes)
        val ramUsedFormatted = dpFormatter.format(ramUsedCalculated.amount)
        val ramUsedPercent = dpFormatter.format(ramUsedBytes.toDouble() / Runtime.getRuntime().totalMemory() * 100)
        var guilds = 0L
        var users = 0L
        var musicPlayers = 0L
        var totalNodes = 0L

        Launcher.database.jedisPool.resource.use {
            val stats = it.hgetAll("stats")
            for (shard in stats) {
                val jsonStats = JSONObject(shard.value);
                guilds += jsonStats.getLong("guild_count")
                users += jsonStats.getLong("cached_users")
            }

            val nodeStats = it.hgetAll("node-stats")
            for (node in nodeStats) {
                val jsonStats = JSONObject(node.value);
                musicPlayers += jsonStats.getLong("music_players")
                totalNodes++
            }
        }

        ctx.send {
            setColor(0x9570D3)
            setTitle("Octave (Revision ${OctaveBot.GIT_REVISION})")
            setThumbnail(ctx.jda.selfUser.avatarUrl)
            setDescription("Never miss a beat with Octave, " +
                "a simple and easy to use Discord music bot delivering high quality audio to hundreds of thousands of servers." +
                " We support Youtube, Soundcloud, and more!")

            addField("CPU Usage", "${procCpuUsage}% JVM\n${sysCpuUsage}% SYS", true)
            addField("RAM Usage", "$ramUsedFormatted${ramUsedCalculated.unit} (${ramUsedPercent}%)", true)

            addField("Guilds", guilds.toString(), true)
            addField("Voice Connections", musicPlayers.toString(), true)

            addField("Cached Users", users.toString(), true)
            addField("Uptime", "${d}d ${h % 24}h ${m % 60}m ${s % 60}s", true)

            val general = buildString {
                append("Premium: **[Patreon](https://www.patreon.com/octavebot)**\n")
                append("Commands: **$commandSize**\n")
                append("Library: **[JDA ${JDAInfo.VERSION}](${JDAInfo.GITHUB})**\n")
            }
            addField("General", general, true)
            setFooter("${Thread.activeCount()} threads | Current Shard: ${ctx.jda.shardInfo.shardId} | Current Node: ${ctx.config.nodeNumber} / $totalNodes")
        }
    }
}
