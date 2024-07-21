
package com.waterphage.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ModKeys {
    @Environment(EnvType.CLIENT)
    public static KeyBinding FbBuildMode;
    @Environment(EnvType.CLIENT)
    public static void registerModKeys(){
        FbBuildMode = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fbased.bmode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_CONTROL,
                "key.categories.gameplay"
        )
        );
    }
}
