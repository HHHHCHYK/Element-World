package com.elementworld;

import com.elementworld.gui.ElementRender;
import com.elementworld.gui.FloatingTextRenderer;
import net.fabricmc.api.ClientModInitializer;

public class ElementWorldClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ElementRender.renderElement();
		FloatingTextRenderer.register();
	}
}
