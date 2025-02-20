/*
 * Modern UI.
 * Copyright (C) 2019-2023 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.arc3d.engine.geom;

import icyllis.arc3d.engine.*;
import icyllis.arc3d.engine.shading.*;

import javax.annotation.Nonnull;

import static icyllis.arc3d.engine.Engine.*;

/**
 * Unlike {@link CircleProcessor}, this processor uses SDF and supports over-stroking.
 * The stroke direction is CENTER. This processor uses instance rendering and static
 * vertex data.
 */
public class SDFRoundRectGeoProc extends GeometryProcessor {

    /**
     * Per-vertex attributes.
     */
    // {(-1,-1), (-1, 1), (1, -1), (1, 1)}
    public static final Attribute
            POSITION = new Attribute("Position", VertexAttribType.kFloat2, SLDataType.kFloat2);
    /**
     * Per-instance attributes.
     */
    // per-multiplied color
    public static final Attribute
            COLOR = new Attribute("Color", VertexAttribType.kFloat4, SLDataType.kFloat4);
    // scale x, translate x, scale y, translate y
    public static final Attribute
            LOCAL_RECT = new Attribute("LocalRect", VertexAttribType.kFloat4, SLDataType.kFloat4);
    // radius, stroke radius (if stroke, or 0)
    public static final Attribute
            RADII = new Attribute("Radii", VertexAttribType.kFloat2, SLDataType.kFloat2);
    public static final Attribute
            MODEL_VIEW = new Attribute("ModelView", VertexAttribType.kFloat3, SLDataType.kFloat3x3);

    public static final AttributeSet VERTEX_ATTRIBS = AttributeSet.makeImplicit(
            POSITION);
    public static final AttributeSet INSTANCE_ATTRIBS = AttributeSet.makeImplicit(
            COLOR, LOCAL_RECT, RADII, MODEL_VIEW);

    private final boolean mStroke;

    public SDFRoundRectGeoProc(boolean stroke) {
        super(RoundRect_GeoProc_ClassID);
        mStroke = stroke;
        setVertexAttributes(VERTEX_ATTRIBS, 0x1);
        setInstanceAttributes(INSTANCE_ATTRIBS, 0xF);
    }

    @Nonnull
    @Override
    public String name() {
        return "SDFRoundRect_GeomProc";
    }

    @Override
    public byte primitiveType() {
        return PrimitiveType.TriangleStrip;
    }

    @Override
    public void addToKey(Key.Builder b) {
        b.addBool(mStroke, "stroke");
    }

    @Nonnull
    @Override
    public ProgramImpl makeProgramImpl(ShaderCaps caps) {
        return new Impl();
    }

    private static class Impl extends ProgramImpl {

        @Override
        public void setData(UniformDataManager manager,
                            GeometryProcessor geomProc) {
        }

        @Override
        protected void onEmitCode(VertexGeomBuilder vertBuilder,
                                  FPFragmentBuilder fragBuilder,
                                  VaryingHandler varyingHandler,
                                  UniformHandler uniformHandler,
                                  ShaderCaps shaderCaps,
                                  GeometryProcessor geomProc,
                                  String outputColor,
                                  String outputCoverage,
                                  int[] texSamplers,
                                  ShaderVar localPos,
                                  ShaderVar worldPos) {
            final boolean stroke = ((SDFRoundRectGeoProc) geomProc).mStroke;

            // emit attributes
            vertBuilder.emitAttributes(geomProc);

            Varying rectEdge = new Varying(SLDataType.kFloat2);
            varyingHandler.addVarying("RectEdge", rectEdge);
            // add stroke radius and a full pixel bloat
            vertBuilder.codeAppendf("""
                    vec2 rectEdge = (%s.xz + %s.y + 1.0) * %s;
                    %s = rectEdge;
                    """, LOCAL_RECT.name(), RADII.name(), POSITION.name(), rectEdge.vsOut());
            fragBuilder.codeAppendf("""
                    vec2 rectEdge = %s;
                    """, rectEdge.fsIn());

            // setup pass through color
            fragBuilder.codeAppendf("""
                    vec4 %s;
                    """, outputColor);
            varyingHandler.addPassThroughAttribute(COLOR, outputColor,
                    VaryingHandler.INTERPOLATION_CAN_BE_FLAT);

            Varying sizeAndRadii = new Varying(SLDataType.kFloat4);
            varyingHandler.addVarying("SizeAndRadii", sizeAndRadii,
                    VaryingHandler.INTERPOLATION_CAN_BE_FLAT);
            vertBuilder.codeAppendf("""
                    %s = vec4(%s.xz, %s);
                    """, sizeAndRadii.vsOut(), LOCAL_RECT.name(), RADII.name());
            fragBuilder.codeAppendf("""
                    vec4 sizeAndRadii = %s;
                    """, sizeAndRadii.fsIn());

            // setup position
            vertBuilder.codeAppendf("""
                    vec2 localPos = rectEdge + %s.yw;
                    """, LOCAL_RECT.name());
            localPos.set("localPos", SLDataType.kFloat2);
            writeWorldPosition(vertBuilder, localPos, MODEL_VIEW.name(), worldPos);

            if (stroke) {
                fragBuilder.codeAppend("""
                        vec2 q = abs(rectEdge) - sizeAndRadii.xy + sizeAndRadii.z;
                        float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - sizeAndRadii.z;
                        """);
            } else {
                // simplified version
                fragBuilder.codeAppend("""
                        vec2 q = abs(rectEdge) - sizeAndRadii.xy + sizeAndRadii.z;
                        float d = length(max(q, 0.0)) - sizeAndRadii.z;
                        """);
            }
            if (stroke) {
                fragBuilder.codeAppend("""
                        d = abs(d) - sizeAndRadii.w;
                        """);
            }
            // use L2-norm of grad SDF
            fragBuilder.codeAppend("""
                        float afwidth = length(vec2(dFdx(d),dFdy(d)))*0.7;
                        float edgeAlpha = 1.0 - smoothstep(-afwidth,afwidth,d);
                        """);
            fragBuilder.codeAppendf("""
                    vec4 %s = vec4(edgeAlpha);
                    """, outputCoverage);
        }
    }
}
