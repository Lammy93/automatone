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

package baritone.utils;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;

/**
 * Minecraft 1.21 replaced the shared Tessellator buffer: {@code Tessellator.begin} now returns the
 * buffer and drawing takes the built result. The debug renderers begin in one method and draw in
 * another, so this keeps the buffer that is being filled.
 */
public final class RenderBuffer {

    private static BufferBuilder current;

    private RenderBuffer() {
    }

    public static void begin(VertexFormat.DrawMode mode, VertexFormat format) {
        current = Tessellator.getInstance().begin(mode, format);
    }

    public static VertexConsumer vertex(float x, float y, float z) {
        return current.vertex(x, y, z);
    }

    public static void draw() {
        if (current == null) return;
        final BuiltBuffer built = current.endNullable();
        current = null;
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
    }
}
