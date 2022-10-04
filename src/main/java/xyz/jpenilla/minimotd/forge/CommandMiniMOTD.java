package xyz.jpenilla.minimotd.forge;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import xyz.jpenilla.minimotd.common.CommandHandler;

public class CommandMiniMOTD extends CommandBase {
    final CommandHandler<ICommandSender> handler;

    public CommandMiniMOTD(CommandHandler<ICommandSender> handler) {
        this.handler = handler;
    }

    @Override
    public String getName() {
        return "minimotd";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length == 1) {
            if (args[0].equals("reload")) {
                reload(server, sender);
            } else if(args[0].equals("about")) {
                about(server, sender);
            }
            return;
        }
        help(server, sender);
    }

    void reload(MinecraftServer server, ICommandSender sender) {
        handler.reload(sender);
    }

    void about(MinecraftServer server, ICommandSender sender) {
        handler.about(sender);
    }

    void help(MinecraftServer server, ICommandSender sender) {
        handler.help(sender);
    }
}
