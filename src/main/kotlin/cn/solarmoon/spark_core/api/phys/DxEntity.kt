package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.phys.thread.getPhysWorld
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import kotlinx.coroutines.flow.asFlow
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.entity.EntityInLevelCallback
import org.ode4j.math.DVector3
import org.ode4j.ode.DBody
import org.ode4j.ode.DContactBuffer
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper
import java.util.concurrent.atomic.AtomicInteger

abstract class DxEntity(
    entityType: EntityType<*>,
    level: Level
): Entity(entityType, level) {

    companion object {
        @JvmStatic
        val ENTITY_OWNER = SynchedEntityData.defineId(DxEntity::class.java, EntityDataSerializers.INT)
        @JvmStatic
        val BODY_NAME = SynchedEntityData.defineId(DxEntity::class.java, EntityDataSerializers.STRING)
    }

    val body = OdeHelper.createBody(level.getPhysWorld().world)
    var bodyName
        get() = entityData[BODY_NAME]
        set(value) { entityData[BODY_NAME] = value }

    init {
        body.entity = this
        body.gravityMode = false
    }

    abstract fun onCollide(o2: DGeom, buffer: DContactBuffer)

    abstract fun getOwner(): Any?

    fun setEntityOwner(entity: Entity) {
        entityData[ENTITY_OWNER] = entity.id
    }

    /**
     * 两个碰撞体碰撞以后，哪个碰撞体的entity的此值为true，则单方面不调用另一个碰撞体的[onCollide]方法
     */
    open fun passFromCollide(): Boolean = false

    /**
     * 当此方法返回true时，将不会检测与[e2]间的相互碰撞
     *
     * 可以应用于避免比如绑定在同一个实体上的entity的相互碰撞
     *
     * 此方法与[passFromCollide]不同，一旦返回true将直接不会判断碰撞
     *
     * @return 默认情况下只在两者owner一致时不检测碰撞，不一致或任意一者owner为null时都会检测碰撞
     */
    fun pass(e2: DxEntity): Boolean {
        if (getOwner() == e2.getOwner()) return true
        if (getOwner() == null || e2.getOwner() == null) return false
        return false
    }

    override fun setPos(x: Double, y: Double, z: Double) {
        super.setPos(x, y, z)
        body?.position = DVector3(x, y, z)
    }

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean {
        return true
    }

    override fun onRemovedFromLevel() {
        super.onRemovedFromLevel()
        body.destroy()
    }

    open fun shouldRemove(): Boolean {
        val ownerRemoved = (getOwner() as? Entity)?.isRemoved == true
        val noOwner = getOwner() == null
        return ownerRemoved || noOwner
    }

    override fun tick() {
        if (firstTick) {
            body.geomIterator.forEach { level().getPhysWorld().space.add(it) }
        }
        super.tick()
        if (shouldRemove()) {
            remove(RemovalReason.UNLOADED_TO_CHUNK)
        }
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        builder.define(ENTITY_OWNER, -1)
        builder.define(BODY_NAME, "body")
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {}

    override fun addAdditionalSaveData(compound: CompoundTag) {}

}