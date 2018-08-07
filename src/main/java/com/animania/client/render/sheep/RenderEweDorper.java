package com.animania.client.render.sheep;

import org.lwjgl.opengl.GL11;

import com.animania.client.models.sheep.ModelDorperSheep;
import com.animania.common.entities.sheep.EntityAnimaniaSheep;
import com.animania.common.entities.sheep.EntityEweDorper;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEweDorper<T extends EntityEweDorper> extends RenderLiving<T>
{
	public static final Factory           FACTORY          = new Factory();
	private static final String             modid          = "animania", SheepBaseDir = "textures/entity/sheep/";

	private static final ResourceLocation[] SHEEP_TEXTURES = new ResourceLocation[] { 
			new ResourceLocation(RenderEweDorper.modid, RenderEweDorper.SheepBaseDir +"sheep_dorper.png")}; 

	private static final ResourceLocation[] SHEEP_TEXTURES_BLINK = new ResourceLocation[] { 
			new ResourceLocation(RenderEweDorper.modid, RenderEweDorper.SheepBaseDir +"sheep_dorper_blink.png")}; 

	private static final ResourceLocation[] SHEEP_TEXTURES_SHEARED = new ResourceLocation[] { 
			new ResourceLocation(RenderEweDorper.modid, RenderEweDorper.SheepBaseDir +"sheep_dorper_sheared.png")}; 

	private static final ResourceLocation[] SHEEP_TEXTURES_SHEARED_BLINK = new ResourceLocation[] { 
			new ResourceLocation(RenderEweDorper.modid, RenderEweDorper.SheepBaseDir +"sheep_dorper_sheared_blink.png")};  


	public RenderEweDorper(RenderManager rm) {
		super(rm, new ModelDorperSheep(), 0.5F);
	}

	protected void preRenderScale(EntityEweDorper entity, float f) {
		GL11.glScalef(0.60F, 0.60F, 0.60F);
		GL11.glTranslatef(0f, 0f, -0.5f);
		boolean isSleeping = false;
		EntityAnimaniaSheep entitySheep = (EntityAnimaniaSheep) entity;
		if (entitySheep.getSleeping()) {
			isSleeping = true;
		}

		if (isSleeping) {
			this.shadowSize = 0;
			float sleepTimer = entitySheep.getSleepTimer();
			if (sleepTimer > - 0.55F) {
				sleepTimer = sleepTimer - 0.01F;
			}
			entity.setSleepTimer(sleepTimer);

			GlStateManager.translate(-0.25F, entity.height - .85F - sleepTimer, -0.25F);
			GlStateManager.rotate(6.0F, 0.0F, 0.0F, 1.0F);
		} else {
			this.shadowSize = 0.5F;
			entitySheep.setSleeping(false);
			entitySheep.setSleepTimer(0F);
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity) {
		int blinkTimer = entity.blinkTimer;
		long currentTime = entity.world.getWorldTime() % 23999;
		boolean isSleeping = false;

		EntityAnimaniaSheep entitySheep = (EntityAnimaniaSheep) entity;
		isSleeping = entitySheep.getSleeping();
		float sleepTimer = entitySheep.getSleepTimer();
		
		if (!entity.getSheared()) {
			if (isSleeping && sleepTimer <= -0.55F && currentTime < 23250) {
				return this.SHEEP_TEXTURES_BLINK[entity.getColorNumber()];
			} else if (blinkTimer < 7 && blinkTimer >= 0) {
				return this.SHEEP_TEXTURES_BLINK[entity.getColorNumber()];
			} else {
				return this.SHEEP_TEXTURES[entity.getColorNumber()];
			}
		} else {
			if (isSleeping && sleepTimer <= -0.55F && currentTime < 23250) {
				return this.SHEEP_TEXTURES_SHEARED_BLINK[entity.getColorNumber()];
			} else if (blinkTimer < 7 && blinkTimer >= 0) {
				return this.SHEEP_TEXTURES_SHEARED_BLINK[entity.getColorNumber()];
			} else {
				return this.SHEEP_TEXTURES_SHEARED[entity.getColorNumber()];
			}
		}

	}

	@Override
	protected void preRenderCallback(T entityliving, float f) {
		this.preRenderScale(entityliving, f);
	}

	static class Factory<T extends EntityEweDorper> implements IRenderFactory<T>
	{
		@Override
		public Render<? super T> createRenderFor(RenderManager manager) {
			return new RenderEweDorper(manager);
		}

	}
}
