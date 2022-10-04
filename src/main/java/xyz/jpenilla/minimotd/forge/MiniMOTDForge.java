package xyz.jpenilla.minimotd.forge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.function.Function;


@Mod(MiniMOTDForge.ModId)
@Mod.EventBusSubscriber
public class MiniMOTDForge implements MiniMOTDPlatform<String> {
    public static final String ModId = "minimotd";
    private static MiniMOTDForge instance = null;

    private final Path dataDirectory = FMLPaths.CONFIGDIR.get().resolve(ModId);
    private final MiniMOTD<String> miniMOTD = new MiniMOTD<>(this);

    private static final Logger LOGGER = LogManager.getLogger();
    public MiniMOTDForge() {
        instance = this;
    }

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        final CommandHandler<CommandSourceStack> handlerFactory = new CommandHandler(instance.miniMOTD,
                (CommandHandler.ICommandResponsor<CommandSourceStack>) (source, component) -> source.sendSuccess(TextUtils.toNative(component), true));
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


    public MiniMOTD<String> miniMOTD() {
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
    public String loadIcon(final BufferedImage bufferedImage) throws Exception {
        final ByteBuf byteBuf = Unpooled.buffer();
        final String icon;
        try {
            ImageIO.write(bufferedImage, "PNG", new ByteBufOutputStream(byteBuf));
            final ByteBuffer base64 = Base64.getEncoder().encode(byteBuf.nioBuffer());
            icon = "data:image/png;base64," + StandardCharsets.UTF_8.decode(base64);
        } finally {
            byteBuf.release();
        }

        return icon;
    }
}
