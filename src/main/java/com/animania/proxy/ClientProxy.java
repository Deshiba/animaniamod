package com.animania.proxy;

import com.animania.client.AnimaniaTextures;
import com.animania.client.handler.RenderHandler;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit() {
		super.preInit();
		RenderHandler.preInit();
	}

	@Override
	public void init() {
		super.init();
		RenderHandler.init();
		AnimaniaTextures.registerTextures();
	}

}