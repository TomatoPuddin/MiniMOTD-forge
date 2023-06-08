package xyz.jpenilla.minimotd.forge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.jpenilla.minimotd.common.CommandHandler;
import xyz.jpenilla.minimotd.common.MiniMOTD;
import xyz.jpenilla.minimotd.common.MiniMOTDPlatform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;


@Mod(MiniMOTDForge.ModId)
@Mod.EventBusSubscriber
public class MiniMOTDForge implements MiniMOTDPlatform<byte[]> {
    public static final String ModId = "minimotd";
    private static MiniMOTDForge instance = null;

    private final Path dataDirectory = FMLPaths.CONFIGDIR.get().resolve(ModId);
    private final MiniMOTD<byte[]> miniMOTD = new MiniMOTD<>(this);

    private static final Logger LOGGER = LogManager.getLogger();
    public MiniMOTDForge() {
        instance = this;
    }

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        final CommandHandler<CommandSourceStack> handlerFactory = new CommandHandler(instance.miniMOTD,
                (CommandHandler.ICommandResponsor<CommandSourceStack>) (source, component) -> source.sendSuccess(() -> TextUtils.toNative(component), true));
        Function<Consumer<CommandSourceStack>, Command<CommandSourceStack>> wrap = r -> ctx -> {
            r.accept(ctx.getSource());
            return 1;
        };

        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(ModId)
            .requires((CommandSourceStack) -> CommandSourceStack.hasPermission(4))
            .then(Commands.literal("reload").executes(wrap.apply(s -> handlerFactory.reload(s))))
            .then(Commands.literal("about").executes(wrap.apply(s -> handlerFactory.about(s))))
            .then(Commands.literal("help").executes(wrap.apply(s -> handlerFactory.help(s))));

        event.getDispatcher().register(builder);
    }


    public MiniMOTD<byte[]> miniMOTD() {
        return this.miniMOTD;
    }

    public static MiniMOTDForge get() {
        return instance;
    }

    @Override
    public Path dataDirectory() {
        return this.dataDirectory;
    }

    @Override
    public Logger logger() {
        return this.LOGGER;
    }

    @Override
    public byte[] loadIcon(final BufferedImage bufferedImage) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] array;
        try {
            ImageIO.write(bufferedImage, "PNG", stream);
            array = stream.toByteArray();
        } finally {
            stream.close();
        }

        return array;
    }
}
