package com.elementworld;

import com.elementworld.command.CommandsRegister;
import com.elementworld.command.Hello;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElementWorld implements ModInitializer {
	public static final String MOD_ID = "elementworld";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");


		/*
		下面是进行将命令注册到命令分发器的操作，与注册命令的操作区分（消歧义）
		 */
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			/*
			   将命令注册到命令分发器
			 */
			Hello.register(dispatcher);
			CommandsRegister.register(dispatcher);
		});
	}
}