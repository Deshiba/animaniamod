package com.animania.common.entities.rodents.ai;

import java.util.Set;

import javax.annotation.Nullable;

import com.animania.common.entities.rodents.EntityFerretBase;
import com.animania.common.entities.rodents.EntityHamster;
import com.animania.common.entities.rodents.EntityHedgehogBase;
import com.google.common.collect.Sets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;

public class EntityAITemptRodents extends EntityAIBase
{
	private final EntityTameable temptedEntity;
	private final double speed;
	private double targetX;
	private double targetY;
	private double targetZ;
	private double pitch;
	private double yaw;
	private EntityPlayer temptingPlayer;
	private int delayTemptCounter;
	private boolean isRunning;
	private final Set<Item> temptItem;
	private final boolean scaredByPlayerMovement;

	public EntityAITemptRodents(EntityTameable temptedEntityIn, double speedIn, Item temptItemIn, boolean scaredByPlayerMovementIn)
	{
		this(temptedEntityIn, speedIn, scaredByPlayerMovementIn, Sets.newHashSet(new Item[] {temptItemIn}));
	}

	public EntityAITemptRodents(EntityTameable temptedEntityIn, double speedIn, boolean scaredByPlayerMovementIn, Set<Item> temptItemIn)
	{
		this.temptedEntity = temptedEntityIn;
		this.speed = speedIn;
		this.temptItem = temptItemIn;
		this.scaredByPlayerMovement = scaredByPlayerMovementIn;
		this.setMutexBits(3);

		if (!(temptedEntityIn.getNavigator() instanceof PathNavigateGround))
		{
			throw new IllegalArgumentException("Unsupported mob type for TemptGoal");
		}
	}

	public boolean shouldExecute()
	{
		if (this.delayTemptCounter > 0)
		{
			--this.delayTemptCounter;
			return false;
		}
		else
		{
			if (this.temptedEntity instanceof EntityHamster) {
				EntityHamster er = (EntityHamster) this.temptedEntity;
				if (er.getSleeping()) {
					return false;
				}
			}

			if (this.temptedEntity instanceof EntityFerretBase) {
				EntityFerretBase er = (EntityFerretBase) this.temptedEntity;
				if (er.getSleeping()) {
					return false;
				}
			}

			if (this.temptedEntity instanceof EntityHedgehogBase) {
				EntityHedgehogBase er = (EntityHedgehogBase) this.temptedEntity;
				if (er.getSleeping()) {
					return false;
				}
			}

			this.temptingPlayer = this.temptedEntity.world.getClosestPlayerToEntity(this.temptedEntity, 10.0D);
			return this.temptingPlayer == null ? false : this.isTempting(this.temptingPlayer.getHeldItemMainhand()) || this.isTempting(this.temptingPlayer.getHeldItemOffhand());
		}
	}

	protected boolean isTempting(@Nullable ItemStack stack)
	{
		return stack == null ? false : this.temptItem.contains(stack.getItem());
	}

	public boolean shouldContinueExecuting()
	{
		if (this.scaredByPlayerMovement)
		{
			if (this.temptedEntity.getDistanceSq(this.temptingPlayer) < 36.0D)
			{
				if (this.temptingPlayer.getDistanceSq(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002D)
				{
					return false;
				}

				if (Math.abs((double)this.temptingPlayer.rotationPitch - this.pitch) > 5.0D || Math.abs((double)this.temptingPlayer.rotationYaw - this.yaw) > 5.0D)
				{
					return false;
				}
			}
			else
			{
				this.targetX = this.temptingPlayer.posX;
				this.targetY = this.temptingPlayer.posY;
				this.targetZ = this.temptingPlayer.posZ;
			}

			this.pitch = (double)this.temptingPlayer.rotationPitch;
			this.yaw = (double)this.temptingPlayer.rotationYaw;
		}

		return this.shouldExecute();
	}

	public void startExecuting()
	{
		this.targetX = this.temptingPlayer.posX;
		this.targetY = this.temptingPlayer.posY;
		this.targetZ = this.temptingPlayer.posZ;
		this.isRunning = true;
	}

	public void resetTask()
	{
		this.temptingPlayer = null;
		this.temptedEntity.getNavigator().clearPath();
		this.delayTemptCounter = 100;
		this.isRunning = false;

	}

	public void updateTask()
	{
		this.temptedEntity.getLookHelper().setLookPositionWithEntity(this.temptingPlayer, (float)(this.temptedEntity.getHorizontalFaceSpeed() + 20), (float)this.temptedEntity.getVerticalFaceSpeed());

		if (this.temptedEntity.getDistanceSq(this.temptingPlayer) < 6.25D)
		{
			this.temptedEntity.getNavigator().clearPath();
		}
		else
		{

			if (!this.temptedEntity.isSitting()) {
				this.temptedEntity.getNavigator().tryMoveToEntityLiving(this.temptingPlayer, this.speed);
			}
		}

	}

	public boolean isRunning()
	{
		return this.isRunning;
	}
}