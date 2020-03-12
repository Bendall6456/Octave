package xyz.gnarbot.gnar.commands.music

import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.music.MusicManager

@Command(
        aliases = ["shuffle"],
        description = "Shuffle the music queue."
)
@BotInfo(
        id = 72,
        category = Category.MUSIC,
        scope = Scope.VOICE,
        djLock = true
)
class ShuffleCommand : MusicCommandExecutor(true, false, true) {
    override fun execute(context: Context, label: String, args: Array<String>, manager: MusicManager) {
        if (manager.scheduler.queue.isEmpty()) {
            context.send().issue("The queue is empty.\n$PLAY_MESSAGE").queue()
            return
        }

        manager.scheduler.shuffle()

        context.send().info("Player has been shuffled").queue()
    }
}
