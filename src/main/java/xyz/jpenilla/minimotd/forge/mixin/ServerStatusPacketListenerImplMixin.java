/*
 * This file is part of MiniMOTD, licensed under the MIT License.
 *
 * Copyright (c) 2021 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.minimotd.forge.mixin;

import com.mojang.authlib.GameProfile;
import net.kyori.adventure.text.Component;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.jpenilla.minimotd.common.ComponentColorDownsampler;
import xyz.jpenilla.minimotd.common.Constants;
import xyz.jpenilla.minimotd.common.MOTDIconPair;
import xyz.jpenilla.minimotd.common.config.MiniMOTDConfig;
import xyz.jpenilla.minimotd.common.config.MiniMOTDConfig.PlayerCount;
import xyz.jpenilla.minimotd.forge.MiniMOTDForge;
import xyz.jpenilla.minimotd.forge.TextUtils;
import xyz.jpenilla.minimotd.forge.access.ConnectionAccess;

@Unique
@Mixin(net.minecraft.server.network.NetHandlerStatusServer .class)
abstract class ServerStatusPacketListenerImplMixin {
  private static final Logger LOGGER = LogManager.getLogger();
  @Shadow
  @Final
  private NetworkManager networkManager;

  @Redirect(method = "processServerQuery", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getServerStatusResponse()Lnet/minecraft/network/ServerStatusResponse;"))
  public ServerStatusResponse injectHandleStatusRequest(final MinecraftServer minecraftServer) {
    final ServerStatusResponse rsp = minecraftServer.getServerStatusResponse();
    final MiniMOTDForge miniMOTDForge = MiniMOTDForge.get();
    final xyz.jpenilla.minimotd.common.MiniMOTD<String> miniMOTD = miniMOTDForge.miniMOTD();
    final MiniMOTDConfig config = miniMOTD.configManager().mainConfig();
    final PlayerCount count = config.modifyPlayerCount(minecraftServer.getCurrentPlayerCount(), rsp.getPlayers().getMaxPlayers());
    final int onlinePlayers = count.onlinePlayers();
    final int maxPlayers = count.maxPlayers();
    final MOTDIconPair<String> pair = miniMOTD.createMOTD(config, onlinePlayers, maxPlayers);

    final Component motdComponent = pair.motd();
    if (motdComponent != null) {
      if (((ConnectionAccess) this.networkManager).protocolVersion() >= Constants.MINECRAFT_1_16_PROTOCOL_VERSION) {
        ITextComponent component = TextUtils.toNative(motdComponent);
        rsp.setServerDescription(component);
      } else {
        ITextComponent component = TextUtils.toNative(
                ComponentColorDownsampler.downsampler().downsample(motdComponent)
        );
        rsp.setServerDescription(component);
      }
    }

    final String favicon = pair.icon();
    if (favicon != null) {
      rsp.setFavicon(favicon);
    }

    if (!config.hidePlayerCount()) {
      final GameProfile[] oldSample = rsp.getPlayers().getPlayers();
      final ServerStatusResponse.Players newPlayers = new ServerStatusResponse.Players(maxPlayers, onlinePlayers);
      if (config.disablePlayerListHover()) {
        newPlayers.setPlayers(new GameProfile[]{});
      } else {
        newPlayers.setPlayers(oldSample);
      }
      rsp.setPlayers(newPlayers);
    }

    return rsp;
  }
}
