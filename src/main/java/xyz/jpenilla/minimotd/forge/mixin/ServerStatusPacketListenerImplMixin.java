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
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
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
import xyz.jpenilla.minimotd.common.MiniMOTD;
import xyz.jpenilla.minimotd.common.config.MiniMOTDConfig;
import xyz.jpenilla.minimotd.common.config.MiniMOTDConfig.PlayerCount;
import xyz.jpenilla.minimotd.forge.MiniMOTDForge;
import xyz.jpenilla.minimotd.forge.TextUtils;
import xyz.jpenilla.minimotd.forge.access.ConnectionAccess;

@Unique
@Mixin(net.minecraft.server.network.ServerStatusPacketListenerImpl .class)
abstract class ServerStatusPacketListenerImplMixin {
  private final Logger LOGGER = LogManager.getLogger();
  @Shadow
  @Final
  private Connection connection;

  @Redirect(method = "handleStatusRequest", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getStatus()Lnet/minecraft/network/protocol/status/ServerStatus;"))
  public ServerStatus injectHandleStatusRequest(final MinecraftServer minecraftServer) {
    final ServerStatus rsp = minecraftServer.getStatus();

    final MiniMOTDForge miniMOTDForge = MiniMOTDForge.get();
    final MiniMOTD<String> miniMOTD = miniMOTDForge.miniMOTD();
    final MiniMOTDConfig config = miniMOTD.configManager().mainConfig();
    final PlayerCount count = config.modifyPlayerCount(minecraftServer.getPlayerCount(), rsp.getPlayers().getMaxPlayers());
    final int onlinePlayers = count.onlinePlayers();
    final int maxPlayers = count.maxPlayers();
    final MOTDIconPair<String> pair = miniMOTD.createMOTD(config, onlinePlayers, maxPlayers);

    try {
      final Component motdComponent = pair.motd();
      if (motdComponent != null) {
        if (((ConnectionAccess) this.connection).protocolVersion() >= Constants.MINECRAFT_1_16_PROTOCOL_VERSION) {
          net.minecraft.network.chat.Component component = TextUtils.toNative(motdComponent);
          rsp.setDescription(component);
        } else {
          net.minecraft.network.chat.Component component = TextUtils.toNative(
                  ComponentColorDownsampler.downsampler().downsample(motdComponent)
          );
          rsp.setDescription(component);
        }
      }
    } catch (Throwable t) {
      LOGGER.error("error generating server desc", t);
    }

    final String favicon = pair.icon();
    if (favicon != null) {
      rsp.setFavicon(favicon);
    }

    if (!config.hidePlayerCount()) {
      final GameProfile[] oldSample = rsp.getPlayers().getSample();
      final ServerStatus.Players newPlayers = new ServerStatus.Players(maxPlayers, onlinePlayers);
      if (config.disablePlayerListHover()) {
        newPlayers.setSample(new GameProfile[]{});
      } else {
        newPlayers.setSample(oldSample);
      }
      rsp.setPlayers(newPlayers);
    }

    return rsp;
  }
}
