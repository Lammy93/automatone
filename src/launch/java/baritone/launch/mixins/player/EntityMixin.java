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

package baritone.launch.mixins.player;

import baritone.api.fakeplayer.AutomatoneFakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @SuppressWarnings("unused") // makes the field mutable for use by IEntityAccessor
    @Shadow @Mutable @Final private EntityType<?> type;

    @Shadow
    private java.util.stream.Stream<Entity> streamIntoPassengers() {
        throw new AssertionError();
    }

    // 1.20.3+: hasPlayerRider() is getPlayerPassengers() == 1, and the player filter is a lambda
    // inside getPlayerPassengers(). Instead of targeting that lambda by name, subtract fake players
    // from the result so they never count as player passengers.
    @Inject(method = "getPlayerPassengers", at = @At("RETURN"), cancellable = true)
    private void removeFakePlayers(CallbackInfoReturnable<Integer> cir) {
        long fakePlayers = this.streamIntoPassengers()
                .filter(e -> e instanceof AutomatoneFakePlayer)
                .count();
        if (fakePlayers > 0) {
            cir.setReturnValue(cir.getReturnValueI() - (int) fakePlayers);
        }
    }
}
