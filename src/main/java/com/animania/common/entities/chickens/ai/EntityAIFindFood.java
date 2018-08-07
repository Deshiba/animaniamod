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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntityAIFindFood extends EntityAIBase 
{
	private final EntityAnimaniaChicken entityIn;
	private final double speed;
	private double targetX;
	private double targetY;
	private double targetZ;
	private double pitch;
	private double yaw;
	private EntityPlayer temptingPlayer;
	private boolean isRunning;
	private int delayTemptCounter;

	public EntityAIFindFood(EntityAnimaniaChicken temptedEntityIn, double speedIn)
	{
		this.entityIn = temptedEntityIn;
		this.speed = speedIn;
		this.setMutexBits(3);
		this.delayTemptCounter = 0;
	}

	public boolean shouldExecute()
	{

		delayTemptCounter++;

		if (this.delayTemptCounter <= AnimaniaConfig.gameRules.ticksBetweenAIFirings) {
			return false;
		} else if (delayTemptCounter > AnimaniaConfig.gameRules.ticksBetweenAIFirings) {
			if (entityIn instanceof EntityAnimaniaChicken) {
				EntityAnimaniaChicken ech = (EntityAnimaniaChicken)entityIn;
				if (ech.getFed()) {
					this.delayTemptCounter = 0;
					return false;		
				}
			} 

			if (!entityIn.world.isDaytime() || entityIn.getSleeping()) {
				this.delayTemptCounter = 0;
				return false;
			}

			if (this.entityIn.getRNG().nextInt(100) == 0)
			{
				Vec3d vec3d = RandomPositionGenerator.findRandomTarget(this.entityIn, 20, 4);
				if (vec3d != null) {
					this.delayTemptCounter = 0;
					this.resetTask();
					this.entityIn.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, this.speed);
					this.entityIn.getLookHelper().setLookPosition(vec3d.x, vec3d.y, vec3d.z, 0.0F, 0.0F);
				}
				return false;
			}

			BlockPos currentpos = new BlockPos(entityIn.posX, entityIn.posY, entityIn.posZ);
			BlockPos trypos1 = new BlockPos(entityIn.posX + 1, entityIn.posY, entityIn.posZ);
			BlockPos trypos2 = new BlockPos(entityIn.posX - 1, entityIn.posY, entityIn.posZ);
			BlockPos trypos3 = new BlockPos(entityIn.posX, entityIn.posY, entityIn.posZ + 1);
			BlockPos trypos4 = new BlockPos(entityIn.posX, entityIn.posY, entityIn.posZ - 1);
			BlockPos trypos5 = new BlockPos(entityIn.posX + 1, entityIn.posY, entityIn.posZ + 1);
			BlockPos trypos6 = new BlockPos(entityIn.posX - 1, entityIn.posY, entityIn.posZ - 1);
			BlockPos trypos7 = new BlockPos(entityIn.posX - 1, entityIn.posY, entityIn.posZ + 1);
			BlockPos trypos8 = new BlockPos(entityIn.posX + 1, entityIn.posY, entityIn.posZ - 1);
			Block poschk = entityIn.world.getBlockState(currentpos).getBlock();
			Block poschk1 = entityIn.world.getBlockState(trypos1).getBlock();
			Block poschk2 = entityIn.world.getBlockState(trypos2).getBlock();
			Block poschk3 = entityIn.world.getBlockState(trypos3).getBlock();
			Block poschk4 = entityIn.world.getBlockState(trypos4).getBlock();
			Block poschk5 = entityIn.world.getBlockState(trypos5).getBlock();
			Block poschk6 = entityIn.world.getBlockState(trypos6).getBlock();
			Block poschk7 = entityIn.world.getBlockState(trypos7).getBlock();
			Block poschk8 = entityIn.world.getBlockState(trypos8).getBlock();

			if (poschk == BlockHandler.blockTrough) {
				//do nothing
			} else if (poschk1 == BlockHandler.blockTrough) {
				currentpos = trypos1;
			} else if (poschk2 == BlockHandler.blockTrough) {
				currentpos = trypos2;
			} else if (poschk3 == BlockHandler.blockTrough) {
				currentpos = trypos3;
			} else if (poschk4 == BlockHandler.blockTrough) {
				currentpos = trypos4;
			} else if (poschk5 == BlockHandler.blockTrough) {
				currentpos = trypos5;
			} else if (poschk6 == BlockHandler.blockTrough) {
				currentpos = trypos6;
			} else if (poschk7 == BlockHandler.blockTrough) {
				currentpos = trypos7;
			} else if (poschk8 == BlockHandler.blockTrough) {
				currentpos = trypos8;
			}

			if (poschk == BlockHandler.blockTrough || poschk1 == BlockHandler.blockTrough || poschk2 == BlockHandler.blockTrough || poschk3 == BlockHandler.blockTrough || poschk4 == BlockHandler.blockTrough || poschk5 == BlockHandler.blockTrough || poschk6 == BlockHandler.blockTrough || poschk7 == BlockHandler.blockTrough || poschk8 == BlockHandler.blockTrough) {
				TileEntityTrough te = (TileEntityTrough) entityIn.world.getTileEntity(currentpos);
				if (te != null && te.canConsume(EntityAnimaniaChicken.TEMPTATION_ITEMS, null)) {
					te.consumeSolid(1);

					if (entityIn instanceof EntityAnimaniaChicken) {
						EntityAnimaniaChicken ech = (EntityAnimaniaChicken)entityIn;
						ech.setFed(true);
						ech.setHandFed(true);

					} 
					this.delayTemptCounter = 0;
					return false;

				} 
			}

			if (poschk == BlockHandler.blockSeeds) {

				if (entityIn instanceof EntityAnimaniaChicken) {
					EntityAnimaniaChicken ech = (EntityAnimaniaChicken)entityIn;
					ech.setFed(true);
					ech.setHandFed(true);

				} 

				if (AnimaniaConfig.gameRules.plantsRemovedAfterEating) {
					Block destchk = entityIn.world.getBlockState(currentpos).getBlock();
					if (destchk != BlockHandler.blockTrough) {
						//entityIn.world.destroyBlock(currentpos, false);
						entityIn.world.setBlockToAir(currentpos);
					}
				}
				this.delayTemptCounter = 0;
				return false;
			}

			double x = this.entityIn.posX;
			double y = this.entityIn.posY;
			double z = this.entityIn.posZ;

			boolean foodFound = false;
			boolean prefFoodFound = false;
			Random rand = new Random();

			BlockPos pos = new BlockPos(x, y, z);

			for (int i = -16; i < 16; i++) {
				for (int j = -3; j < 3; j++) {
					for (int k = -16; k < 16; k++) {

						pos = new BlockPos(x + i, y + j, z + k);

						Block blockchk = entityIn.world.getBlockState(pos).getBlock();

						if (blockchk == BlockHandler.blockTrough) {
							TileEntityTrough te = (TileEntityTrough) entityIn.world.getTileEntity(pos);
							if (te != null && te.canConsume(EntityAnimaniaChicken.TEMPTATION_ITEMS, null)) {
								foodFound = true;
								prefFoodFound = true;
								if (rand.nextInt(200) == 0) {
									this.delayTemptCounter = 0;
									return false;
								} else if (this.entityIn.collidedHorizontally && this.entityIn.motionX == 0 && this.entityIn.motionZ == 0 ) {
									this.delayTemptCounter = 0;
									return false;
								} else {
									return true;
								}
							}
						}

						if (!prefFoodFound && blockchk == BlockHandler.blockSeeds) {

							foodFound = true;
							if (rand.nextInt(200) == 0) {
								this.delayTemptCounter = 0;
								return false;
							} else if (this.entityIn.collidedHorizontally && this.entityIn.motionX == 0 && this.entityIn.motionZ == 0 ) {
								this.delayTemptCounter = 0;
								return false;
							} else {
								return true;
							}
						}

					}

				}
			}

			if (!foodFound) {
				this.delayTemptCounter = 0;
				return false;
			}
		}

		return false;
	}

	public boolean shouldContinueExecuting()
	{
		return !this.entityIn.getNavigator().noPath();
	}


	public void resetTask()
	{
		this.temptingPlayer = null;
		this.entityIn.getNavigator().clearPath();
		this.isRunning = false;
	}


	public void startExecuting()
	{

		double x = this.entityIn.posX;
		double y = this.entityIn.posY;
		double z = this.entityIn.posZ;

		boolean foodFound = false;
		boolean prefFoodFound = false;
		int loc = 24;
		int newloc = 24;
		BlockPos pos = new BlockPos(x, y, z);
		BlockPos foodPos = new BlockPos(x, y, z);

		for (int i = -16; i < 16; i++) {
			for (int j = -3; j < 3; j++) {
				for (int k = -16; k < 16; k++) {

					pos = new BlockPos(x + i, y + j, z + k);
					Block blockchk = entityIn.world.getBlockState(pos).getBlock();

					if (blockchk == BlockHandler.blockTrough) {

						TileEntityTrough te = (TileEntityTrough) entityIn.world.getTileEntity(pos);

						if (te != null && te.canConsume(EntityAnimaniaChicken.TEMPTATION_ITEMS, null)) {

							foodFound = true;
							prefFoodFound = true;
							newloc = Math.abs(i)  +  Math.abs(j) +  Math.abs(k);

							if (newloc < loc) {

								loc = newloc;

								if (entityIn.posX < foodPos.getX()) {
									BlockPos foodPoschk = new BlockPos(x + i + 1, y + j, z + k);
									Block mudBlockchk = entityIn.world.getBlockState(foodPoschk).getBlock();
									i = i + 1;
								} 

								if (entityIn.posZ < foodPos.getZ()) {
									BlockPos foodPoschk = new BlockPos(x + i, y + j, z + k + 1);
									Block mudBlockchk = entityIn.world.getBlockState(foodPoschk).getBlock();
									k = k + 1;
								}

								foodPos = new BlockPos(x + i, y + j, z + k);

							}
						}
					} 
				}
			}
		}

		if (!prefFoodFound && !foodFound && !this.entityIn.hasPath()) {

			for (int i = -16; i < 16; i++) {
				for (int j = -3; j < 3; j++) {
					for (int k = -16; k < 16; k++) {	

						pos = new BlockPos(x + i, y + j, z + k);
						Block blockchk = entityIn.world.getBlockState(pos).getBlock();

						if (blockchk == BlockHandler.blockSeeds) {

							foodFound = true;

							newloc = Math.abs(i)  +  Math.abs(j) +  Math.abs(k);

							if (newloc < loc) {

								loc = newloc;

								if (entityIn.posX < foodPos.getX()) {
									BlockPos foodPoschk = new BlockPos(x + i + 1, y + j, z + k);
									Block mudBlockchk = entityIn.world.getBlockState(foodPoschk).getBlock();
									if (mudBlockchk == BlockHandler.blockSeeds) {
										i = i + 1;
									}
								} 

								if (entityIn.posZ < foodPos.getZ()) {
									BlockPos foodPoschk = new BlockPos(x + i, y + j, z + k + 1);
									Block mudBlockchk = entityIn.world.getBlockState(foodPoschk).getBlock();
									if (mudBlockchk == BlockHandler.blockSeeds) {
										k = k + 1;
									} 
								}

								foodPos = new BlockPos(x + i, y + j, z + k);
							}
						} 
					}
				}
			}
		}


		if (foodFound) {

			Block blockchk = entityIn.world.getBlockState(foodPos).getBlock();
			if ((blockchk == BlockHandler.blockTrough || blockchk == BlockHandler.blockSeeds) && !this.entityIn.hasPath()) {
				if(this.entityIn.getNavigator().tryMoveToXYZ(foodPos.getX(), foodPos.getY(), foodPos.getZ(), this.speed) == false) {
					this.entityIn.getLookHelper().setLookPosition(foodPos.getX(), foodPos.getY(), foodPos.getZ(), 0.0F, 0.0F);
					this.resetTask();
				} else {
					this.entityIn.getNavigator().tryMoveToXYZ(foodPos.getX(), foodPos.getY(), foodPos.getZ(), this.speed);
					this.entityIn.getLookHelper().setLookPosition(foodPos.getX(), foodPos.getY(), foodPos.getZ(), 0.0F, 0.0F);
				}
			} 
		} else {
			delayTemptCounter = 0;
		}
	}


	public boolean isRunning()
	{
		return this.isRunning;
	}
}