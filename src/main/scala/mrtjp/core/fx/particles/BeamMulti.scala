/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.fx.particles

import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Vector3
import mrtjp.core.fx.{TAlphaParticle, TColourParticle, TTextureParticle}
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.renderer.Tessellator
import net.minecraft.world.World
import org.lwjgl.opengl.GL11._

class BeamMulti(w: World)
    extends CoreParticle(w)
    with TAlphaParticle
    with TColourParticle
    with TTextureParticle {
  texture = "projectred:textures/particles/beam1.png"
  setSize(0.02f, 0.02f)

  var points = Seq.empty[Vector3]

  override def renderParticle(
      t: Tessellator,
      frame: Float,
      cosyaw: Float,
      cospitch: Float,
      sinyaw: Float,
      sinsinpitch: Float,
      cossinpitch: Float
  ) {
    super.renderParticle(
      t,
      frame,
      cosyaw,
      cospitch,
      sinyaw,
      sinsinpitch,
      cossinpitch
    )

    if (points.nonEmpty) {
      t.draw()
      CCRenderState.changeTexture(texture)
      for (i <- 1 until points.size)
        drawBeam(points(i), points(i - 1), frame)
      t.startDrawingQuads()
    }
  }

  def drawBeam(p1: Vector3, p2: Vector3, f: Float) {
    val var9 = 1.0f
    val slide = ticksExisted
    val size = 0.7f
    val dp = p1.copy.subtract(p2)
    val dptx = dp.x
    val dpty = dp.y
    val dptz = dp.z
    val length = math.sqrt(dptx * dptx + dpty * dpty + dptz * dptz)
    val rotationYaw = (math.atan2(dptx, dptz) * 180.0d / math.Pi).toFloat
    val rotationPitch = (math.atan2(
      dpty,
      math.sqrt(dptx * dptx + dptz * dptz)
    ) * 180.0d / math.Pi).toFloat

    glPushMatrix()
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, 10497.0f)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, 10497.0f)
    glDisable(GL_CULL_FACE)

    glBlendFunc(GL_SRC_ALPHA, GL_ONE)
    glDepthMask(false)

    val xx = p1.x - EntityFX.interpPosX
    val yy = p1.y - EntityFX.interpPosY
    val zz = p1.z - EntityFX.interpPosZ
    glTranslated(xx, yy, zz)

    glRotatef(90.0f, 1.0f, 0.0f, 0.0f)
    glRotatef(180.0f + rotationYaw, 0.0f, 0.0f, -1.0f)
    glRotatef(rotationPitch, 1.0f, 0.0f, 0.0f)

    val var11 = slide + f
    val var12 = -var11 * 0.2f - math.floor(-var11 * 0.1f)
    val var44 = -0.15d * size
    val var17 = 0.15d * size

    for (t <- 0 until 2) {
      val var29 = length * var9
      val var31 = 0.0d
      val var33 = 1.0d
      val var35 = -1.0f + var12 + t / 3.0f
      val var37 = length * var9 + var35

      glRotatef(90.0f, 0.0f, 1.0f, 0.0f)
      val tess = Tessellator.instance
      tess.startDrawingQuads()
      tess.setBrightness(200)
      tess.setColorRGBA_F(
        red.toFloat,
        green.toFloat,
        blue.toFloat,
        alpha.toFloat
      )
      tess.addVertexWithUV(var44, var29, 0.0d, var33, var37)
      tess.addVertexWithUV(var44, 0.0d, 0.0d, var33, var35)
      tess.addVertexWithUV(var17, 0.0d, 0.0d, var31, var35)
      tess.addVertexWithUV(var17, var29, 0.0d, var31, var37)
      tess.draw()
    }

    glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
    glDepthMask(true)
    glBlendFunc(GL_SRC_ALPHA, 771)
    glEnable(GL_CULL_FACE)
    glPopMatrix()
  }
}
