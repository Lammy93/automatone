/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.api.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Typed server-to-client payloads (Minecraft 1.20.5+ networking). The wire fields are the same as
 * the raw packets Automatone sent before 1.20.5.
 */
public final class AutomatonePayloads {

    private AutomatonePayloads() {
    }

    /** Opens the click-to-path screen for the given Automatone entity. */
    public record OpenClickScreen(UUID entityUuid) implements CustomPayload {
        public static final Id<OpenClickScreen> ID = new Id<>(new Identifier("automatone", "open_click_screen"));
        public static final PacketCodec<RegistryByteBuf, OpenClickScreen> CODEC = PacketCodec.of(
                (value, buf) -> buf.writeUuid(value.entityUuid()),
                buf -> new OpenClickScreen(buf.readUuid()));

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** Updates the display profile (skin owner) of a fake player. */
    public record ProfileUpdate(int entityId, @Nullable GameProfile profile) implements CustomPayload {
        public static final Id<ProfileUpdate> ID = new Id<>(FakePlayers.PROFILE_UPDATE_PACKET_ID);
        public static final PacketCodec<RegistryByteBuf, ProfileUpdate> CODEC = PacketCodec.of(
                (value, buf) -> {
                    buf.writeVarInt(value.entityId());
                    writeProfile(buf, value.profile());
                },
                buf -> new ProfileUpdate(buf.readVarInt(), readProfile(buf)));

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void writeProfile(PacketByteBuf buf, @Nullable GameProfile profile) {
        buf.writeBoolean(profile != null);
        if (profile != null) {
            buf.writeUuid(profile.getId());
            buf.writeString(profile.getName());
        }
    }

    @Nullable
    public static GameProfile readProfile(PacketByteBuf buf) {
        return buf.readBoolean() ? new GameProfile(buf.readUuid(), buf.readString()) : null;
    }

    /** Call once from the common mod initializer. */
    public static void register() {
        PayloadTypeRegistry.playS2C().register(OpenClickScreen.ID, OpenClickScreen.CODEC);
        PayloadTypeRegistry.playS2C().register(ProfileUpdate.ID, ProfileUpdate.CODEC);
    }
}
