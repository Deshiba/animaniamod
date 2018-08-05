package com.animania.common.entities.chickens.ai;

import java.util.Random;

import com.animania.common.entities.chickens.EntityAnimaniaChicken;
import com.animania.common.handler.BlockHandler;
import com.animania.common.tileentities.TileEntityTrough;
import com.animania.config.AnimaniaConfig;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fluids.FluidRegistry;

public class EntityAIFindWater extends EntityAIBase
{
	private final EntityAnimaniaChicken temptedEntity;
	private final double         speed;
	private double               targetX;
	private double               targetY;
	private double               targetZ;
	private double               pitch;
	private double               yaw;
	private EntityPlayer         temptingPlayer;
	private int                  delayTemptCounter;
	private boolean              isRunning;

	public EntityAIFindWater(EntityAnimaniaChicken temptedEntityIn, double speedIn) {
		this.temptedEntity = temptedEntityIn;
		this.speed = speedIn;
		this.setMutexBits(3);
		this.delayTemptCounter = 0;
	}

	@Override
	public boolean shouldExecute() {

		delayTemptCounter++;
		if (this.delayTemptCounter < AnimaniaConfig.gameRules.ticksBetweenAIFirings) {
			return false;
		} else if (delayTemptCounter > AnimaniaConfig.gameRules.ticksBetweenAIFirings) {

			if (temptedEntity.getSleeping()) {
				this.delayTemptCounter = 0;
				return false;
			}

			if (this.temptedEntity instanceof EntityAnimaniaChicken) {
				EntityAnimaniaChicken entity = (EntityAnimaniaChicken) temptedEntity;
				if (entity.getWatered()) {
					this.delayTemptCounter = 0;
					return false;
				}
			} 

			if (this.temptedEntity.getRNG().nextInt(100) == 0)
			{
				Vec3d vec3d = RandomPositionGenerator.findRandomTarget(this.temptedEntity, 20, 4);
				if (vec3d != null) {
					this.delayTemptCounter = 0;
					this.resetTask();
					this.temptedEntity.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, this.speed);
					this.temptedEntity.getLookHelper().setLookPosition(vec3d.x, vec3d.y, vec3d.z, 0.0F, 0.0F);
				}
				return false;
			}

			BlockPos currentpos = new BlockPos(this.temptedEntity.posX, this.temptedEntity.posY, this.temptedEntity.posZ);
			BlockPos currentposlower = new BlockPos(this.temptedEntity.posX, this.temptedEntity.posY-1, this.temptedEntity.posZ);
			Block poschk = this.temptedEntity.world.getBlockState(currentpos).getBlock();
			Block poschk1 = this.temptedEntity.world.getBlockState(currentposlower).getBlock();

			Biome biomegenbase = this.temptedEntity.world
					.getBiome(new BlockPos(this.temptedEntity.posX, this.temptedEntity.posY, this.temptedEntity.posZ));

			if ((poschk == Blocks.WATER || poschk1 == Blocks.WATER) && !BiomeDictionary.hasType(biomegenbase, Type.OCEAN) && !BiomeDictionary.hasType(biomegenbase, Type.BEACH)) {

				if (this.temptedEntity instanceof EntityAnimaniaChicken) {
					EntityAnimaniaChicken entity = (EntityAnimaniaChicken) this.temptedEntity;
					entity.setWatered(true);
				}
				this.delayTemptCounter = 0;
				return false;

			}
			else if (poschk == BlockHandler.blockTrough) {

				TileEntityTrough te = (TileEntityTrough) this.temptedEntity.world.getTileEntity(currentpos);

				if (te != null && (te.canConsume(null, FluidRegistry.WATER))) {
					if (this.temptedEntity instanceof EntityAnimaniaChicken) {
						EntityAnimaniaChicken entity = (EntityAnimaniaChicken) this.temptedEntity;
						entity.setWatered(true);
						te.consumeLiquid(50);
					}
					this.delayTemptCounter = 0;
					return false;
				}
			}

			double x = this.temptedEntity.posX;
			double y = this.temptedEntity.posY;
			double z = this.temptedEntity.posZ;

			boolean waterFound = false;
			Random rand = new Random();

			BlockPos pos = new BlockPos(x, y, z);

			for (int i = -10; i < 10; i++)
				for (int j = -3; j < 3; j++)
					for (int k = -10; k < 10; k++) {

						pos = new BlockPos(x + i, y + j, z + k);
						Block blockchk = this.temptedEntity.world.getBlockState(pos).getBlock();
						if (blockchk == Blocks.WATER) {
							waterFound = true;

							if (rand.nextInt(200) == 0) {
								this.delayTemptCounter = 0;
								return false;
							}
							else if (this.temptedEntity.collidedHorizontally && this.temptedEntity.motionX == 0
									&& this.temptedEntity.motionZ == 0) {
								this.delayTemptCounter = 0;
								return false;
							}
							else
								return true;

						}
						else if (blockchk == BlockHandler.blockTrough) {
							TileEntityTrough te = (TileEntityTrough) this.temptedEntity.world.getTileEntity(pos);
							if (te != null && (te.canConsume(null, FluidRegistry.WATER))) {
								waterFound = true;

								if (rand.nextInt(20) == 0) {
									this.delayTemptCounter = 0;
									return false;
								}
								else if (this.temptedEntity.collidedHorizontally && this.temptedEntity.motionX == 0
										&& this.temptedEntity.motionZ == 0) {
									this.delayTemptCounter = 0;
									return false;
								}
								else
									return true;
							}
						}

					}

			if (!waterFound) {
				this.delayTemptCounter = 0;
				return false;
			}
		}

		return false;
	}

	public boolean shouldContinueExecuting()
	{
		return !this.temptedEntity.getNavigator().noPath();
	}

	@Override
	public void resetTask() {
		this.temptingPlayer = null;
		this.temptedEntity.getNavigator().clearPath();
		this.isRunning = false;
	}

	@Override
	public void startExecuting()
	{

		double x = this.temptedEntity.posX;
		double y = this.temptedEntity.posY;
		double z = this.temptedEntity.posZ;

		BlockPos currentpos = new BlockPos(x, y, z);
		Block poschk = this.temptedEntity.world.getBlockState(currentpos).getBlock();
		if (poschk != Blocks.WATER) {

			boolean waterFound = false;
			int loc = 24;
			int newloc = 24;
			BlockPos pos = new BlockPos(x, y, z);
			BlockPos waterPos = new BlockPos(x, y, z);

			for (int i = -10; i < 10; i++)
				for (int j = -3; j < 3; j++)
					for (int k = -10; k < 10; k++) {

						pos = new BlockPos(x + i, y + j, z + k);
						Block blockchk = this.temptedEntity.world.getBlockState(pos).getBlock();

						Biome biomegenbase = this.temptedEntity.world.getBiome(pos);

						if (blockchk == BlockHandler.blockTrough) {
							TileEntityTrough te = (TileEntityTrough) this.temptedEntity.world.getTileEntity(pos);
							if (te != null && (te.canConsume(null, FluidRegistry.WATER))) {
								waterFound = true;
								newloc = Math.abs(i) + Math.abs(j) + Math.abs(k);

								if (newloc < loc) {

									loc = newloc;

									if (this.temptedEntity.posX < waterPos.getX()) {
										BlockPos waterPoschk = new BlockPos(x + i + 1, y + j, z + k);
										Block waterBlockchk = this.temptedEntity.world.getBlockState(waterPoschk).getBlock();
										if (waterBlockchk == BlockHandler.blockTrough)
											i = i + 1;
									}

									if (this.temptedEntity.posZ < waterPos.getZ()) {
										BlockPos waterPoschk = new BlockPos(x + i, y + j, z + k + 1);
										Block waterBlockchk = this.temptedEntity.world.getBlockState(waterPoschk).getBlock();
										if (waterBlockchk == BlockHandler.blockTrough)
											k = k + 1;
									}

									waterPos = new BlockPos(x + i, y + j, z + k);

								}

							}
						}
					}

			if (!waterFound && !this.temptedEntity.hasPath()) {
				for (int i = -10; i < 10; i++)
					for (int j = -3; j < 3; j++)
						for (int k = -10; k < 10; k++) {

							pos = new BlockPos(x + i, y + j, z + k);
							Block blockchk = this.temptedEntity.world.getBlockState(pos).getBlock();
							Biome biomegenbase = this.temptedEntity.world.getBiome(pos);

							if (blockchk == Blocks.WATER && !BiomeDictionary.hasType(biomegenbase, Type.OCEAN) && !BiomeDictionary.hasType(biomegenbase, Type.BEACH) && !this.temptedEntity.hasPath()) {
								waterFound = true;
								newloc = Math.abs(i) + Math.abs(j) + Math.abs(k);

								if (newloc < loc) {

									loc = newloc;

									if (this.temptedEntity.posX < waterPos.getX()) {
										BlockPos waterPoschk = new BlockPos(x + i + 1, y + j, z + k);
										Block waterBlockchk = this.temptedEntity.world.getBlockState(waterPoschk).getBlock();
										if (waterBlockchk == Blocks.WATER)
											i = i + 1;
									}

									if (this.temptedEntity.posZ < waterPos.getZ()) {
										BlockPos waterPoschk = new BlockPos(x + i, y + j, z + k + 1);
										Block waterBlockchk = this.temptedEntity.world.getBlockState(waterPoschk).getBlock();
										if (waterBlockchk == Blocks.WATER)
											k = k + 1;
									}

									waterPos = new BlockPos(x + i, y + j, z + k);

								}

							}

						}

			}

			if (waterFound) {

				Block waterBlockchk = this.temptedEntity.world.getBlockState(waterPos).getBlock();
				Biome biomegenbase = this.temptedEntity.world.getBiome(waterPos);

				if (waterBlockchk == BlockHandler.blockTrough) {
					if (this.temptedEntity.getNavigator().tryMoveToXYZ(waterPos.getX(), waterPos.getY(), waterPos.getZ(), this.speed) == false) {
						this.delayTemptCounter = 0;
						this.temptedEntity.getLookHelper().setLookPosition(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 0.0F, 0.0F);
					} else {
						this.temptedEntity.getNavigator().tryMoveToXYZ(waterPos.getX(), waterPos.getY(), waterPos.getZ(), this.speed); 
						this.temptedEntity.getLookHelper().setLookPosition(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 0.0F, 0.0F);
					}

				} else if (waterBlockchk == Blocks.WATER && !BiomeDictionary.hasType(biomegenbase, Type.OCEAN) && !BiomeDictionary.hasType(biomegenbase, Type.BEACH)) {
					if (this.temptedEntity.getNavigator().tryMoveToXYZ(waterPos.getX(), waterPos.getY(), waterPos.getZ(), this.speed) == false) {
						this.delayTemptCounter = 0;
						this.temptedEntity.getLookHelper().setLookPosition(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 0.0F, 0.0F);

					} else {
						this.temptedEntity.getNavigator().tryMoveToXYZ(waterPos.getX(), waterPos.getY(), waterPos.getZ(), this.speed);
						this.temptedEntity.getLookHelper().setLookPosition(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 0.0F, 0.0F);
					}
				} else {
					this.delayTemptCounter = 0;
				}
			}
		}

	}

	public boolean isRunning() {
		return this.isRunning;
	}
}