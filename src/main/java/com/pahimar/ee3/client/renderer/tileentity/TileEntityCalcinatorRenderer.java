package com.pahimar.ee3.client.renderer.tileentity;

import com.pahimar.ee3.client.helper.ColourUtils;
import com.pahimar.ee3.client.model.ModelCalcinator;
import com.pahimar.ee3.lib.Colours;
import com.pahimar.ee3.lib.Textures;
import com.pahimar.ee3.tileentity.TileCalcinator;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * Equivalent-Exchange-3
 * <p/>
 * TileEntityCalcinatorRenderer
 *
 * @author pahimar
 */
@SideOnly(Side.CLIENT)
public class TileEntityCalcinatorRenderer extends TileEntitySpecialRenderer
{
    // TODO Show the firepot as being "lit"
    // TODO Packet for display dust size/colour and calcinator state

    private final ModelCalcinator modelCalcinator = new ModelCalcinator();

    @Override
    // TODO Fix bug: https://github.com/pahimar/Equivalent-Exchange-3/issues/573
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float tick)
    {
        if (tileEntity instanceof TileCalcinator)
        {
            TileCalcinator tileCalcinator = (TileCalcinator) tileEntity;

            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_LIGHTING);

            // Scale, Translate, Rotate
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            GL11.glTranslatef((float) x + 0.5F, (float) y + 0.0F, (float) z + 1.2F);
            GL11.glRotatef(45F, 0F, 1F, 0F);
            GL11.glRotatef(-90F, 1F, 0F, 0F);

            // Bind texture
            if (tileCalcinator.getState() == 1)
            {
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(Textures.MODEL_CALCINATOR_ACTIVE);
            }
            else
            {
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(Textures.MODEL_CALCINATOR_IDLE);
            }

            // Render
            modelCalcinator.renderPart("Calcinator");

            int dustStackSize = tileCalcinator.leftStackSize + tileCalcinator.rightStackSize;

            if (dustStackSize > 0)
            {
                GL11.glPushMatrix();

                // Reverse previous rotation to get back into a workable frame of reference
                GL11.glRotatef(90F, 1F, 0F, 0F);
                GL11.glRotatef(-45F, 0F, 1F, 0F);

                float[] dustColour = getBlendedDustColour(tileCalcinator.leftStackSize, tileCalcinator.leftStackMeta, tileCalcinator.rightStackSize, tileCalcinator.rightStackMeta);

                GL11.glColor4f(dustColour[0], dustColour[1], dustColour[2], 1F);

                if (dustStackSize <= 32)
                {
                    GL11.glScalef(0.25F, 0.25F, 0.25F);
                    GL11.glTranslatef(0.0F, 2.20F, -2.1125F);
                }
                else if (dustStackSize <= 64)
                {
                    GL11.glScalef(0.5F, 0.5F, 0.5F);
                    GL11.glTranslatef(-0.0125F, 0.75F, -0.7125F);
                }

                // Reapply previous rotation to get it back to a viewable state
                GL11.glRotatef(45F, 0F, 1F, 0F);
                GL11.glRotatef(-90F, 1F, 0F, 0F);

                // Render the dust in the Calcinator bowl
                modelCalcinator.renderPart("Dust");
                GL11.glPopMatrix();
            }

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }

    private static float[] getBlendedDustColour(int leftStackSize, int leftStackMeta, int rightStackSize, int rightStackMeta)
    {
        int totalDustStacksSize = leftStackSize + rightStackSize;

        if (totalDustStacksSize > 0)
        {
            int leftStackColour = Integer.parseInt(Colours.DUST_COLOURS[MathHelper.clamp_int(leftStackMeta, 0, Colours.DUST_COLOURS.length)], 16);
            int rightStackColour = Integer.parseInt(Colours.DUST_COLOURS[MathHelper.clamp_int(rightStackMeta, 0, Colours.DUST_COLOURS.length)], 16);

            float leftStackRatio = leftStackSize * 1f / totalDustStacksSize;
            float rightStackRatio = rightStackSize * 1f / totalDustStacksSize;

            float[][] blendedColours = ColourUtils.getFloatBlendedColours(leftStackColour, rightStackColour, 32);

            if (blendedColours.length > 0)
            {
                if (Float.compare(leftStackRatio, rightStackRatio) > 0)
                {
                    return blendedColours[Math.round((1 - leftStackRatio) * (blendedColours.length - 1))];
                }
                else
                {
                    return blendedColours[Math.round(rightStackRatio * (blendedColours.length - 1))];
                }
            }
        }

        return new float[]{1F, 1F, 1F};
    }
}
