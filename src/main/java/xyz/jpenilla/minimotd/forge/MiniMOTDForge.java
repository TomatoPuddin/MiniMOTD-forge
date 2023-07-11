package xyz.jpenilla.minimotd.forge;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.jpenilla.minimotd.common.CommandHandler;
import xyz.jpenilla.minimotd.common.Constants;
import xyz.jpenilla.minimotd.common.MiniMOTD;
import xyz.jpenilla.minimotd.common.MiniMOTDPlatform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Mod(
        modid = Constants.PluginMetadata.ID,
        name = Constants.PluginMetadata.NAME,
        version = Constants.PluginMetadata.VERSION,
        serverSideOnly = true,
        acceptableRemoteVersions = "*"
)
@Mod.EventBusSubscriber
public class MiniMOTDForge implements MiniMOTDPlatform<String> {

    @Mod.Instance(Constants.PluginMetadata.ID)
    public static MiniMOTDForge instance;

    private final Path dataDirectory = Paths.get("config", Constants.PluginMetadata.ID);
    private final MiniMOTD<String> miniMOTD = new MiniMOTD<>(this);

    private static final Logger LOGGER = LogManager.getLogger();

    @Mod.EventHandler
    public static void onRegisterCommand(FMLServerStartingEvent event) {
        final CommandHandler<ICommandSender> handler = new CommandHandler<>(instance.miniMOTD,
                (source, component) -> source.sendMessage(TextUtils.toNative(component)));
        event.registerServerCommand(new CommandMiniMOTD(handler));
    }

    public xyz.jpenilla.minimotd.common.MiniMOTD<String> miniMOTD() {
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
        return LOGGER;
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
