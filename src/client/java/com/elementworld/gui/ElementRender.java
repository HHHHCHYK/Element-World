package com.elementworld.gui;

import com.elementworld.ElementWorld;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.ArrayList;


public class ElementRender {

    public static ArrayList<String> elements = new ArrayList<>();
    private static int perSecond = 0;

    public static void renderElement() {
        System.out.println("Rendering Element");
        HudRenderCallback.EVENT.register(((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if(client.player == null) return;//判断客户端的玩家是否为空（意外判断）
            //-----------------渲染HUD-----------------

            ClientPlayNetworking.registerGlobalReceiver(
                    new Identifier(ElementWorld.MOD_ID,"element_types"),
                    (client1, handler, buf, responseSender) -> {
                        System.out.println("Received element types");
                        if(client1.player != null){
                            int size = buf.readInt();
                            for(int i = 0; i < size; i++){
                                elements.add(buf.readString());
                            }
                        }
                    }
            );
            //上面接受服务端数据包以渲染HUD

            if((perSecond++) % 10 == 0){
                if(elements.isEmpty()) return;//如果没有元素就不渲染
                int size = elements.size();

                int screenWidth = client.getWindow().getScaledWidth();
                int screenHeight = client.getWindow().getScaledHeight();
                int centerX = screenWidth / 2;
                int y = screenHeight - 54;
                int pos = centerX - (size * 8);
                for(int i = 0; i < size; i++){
                    Identifier elementTexture;
                    switch (elements.get(i)){
                        case "Anemo":{
                            elementTexture = new Identifier(ElementWorld.MOD_ID,"textures/element/anemo.png");
                            break;
                        }
                        case "Geo": {
                            elementTexture = new Identifier(ElementWorld.MOD_ID, "textures/element/geo.png");
                            break;
                        }
                        case "Electro": {
                            elementTexture = new Identifier(ElementWorld.MOD_ID, "textures/element/electro.png");
                            break;
                        }
                        case "Hydro": {
                            elementTexture = new Identifier(ElementWorld.MOD_ID, "textures/element/hydro.png");
                            break;
                        }
                        case "Pyro": {
                            elementTexture = new Identifier(ElementWorld.MOD_ID, "textures/element/pyro.png");
                            break;
                        }
                        case "Cryo": {
                            elementTexture = new Identifier(ElementWorld.MOD_ID, "textures/element/cryo.png");
                            break;
                        }
                        case "Dendro": {
                            elementTexture = new Identifier(ElementWorld.MOD_ID, "textures/element/dendro.png");
                            break;
                        }
                        default:
                            elementTexture = new Identifier(ElementWorld.MOD_ID,"none");
                            break;
                    }
                    RenderSystem.setShaderTexture(0, elementTexture);
                    drawContext.drawTexture(elementTexture,pos,y,0,0,16,16,16,16);
                    pos += 16;
                }
                elements.clear();
            }



            //上面为渲染HUD回调
        }));
    }
}
