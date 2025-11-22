    /*
     * Copyright 2020 Google LLC. All rights reserved.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

    package com.fit.fitform.util; // Make sure this matches your package name

    import android.content.Context;
    import android.graphics.Canvas;
    import android.graphics.Matrix;
    import android.util.AttributeSet;
    import android.view.View;
    import com.google.mlkit.vision.interfaces.Detector;
    import java.util.ArrayList;
    import java.util.List;

    /**
     * A view which renders a series of custom graphics to be overlaid on top of an associated preview
     * (i.e., the camera preview). The creator can add graphics objects, update the objects, and remove
     * them, triggering the appropriate drawing and invalidation within the view.
     *
     * <p>Supports scaling and mirroring of the graphics relative to the camera's preview properties.
     * The idea is that detection results are expressed in terms of a preview size, but need to be
     * scaled up to the full view size, and also mirrored in the case of a front-facing camera.
     *
     * <p>Associated {@link Graphic} items should use the following methods to convert to view
     * coordinates for the graphics that are drawn:
     *
     * <ol>
     *   <li>{@link Graphic#scale(float)} and {@link Graphic#scaleY(float)} adjust the size of the
     *       detected object from the preview scale to the view scale.
     *   <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
     *       coordinate from the preview's coordinate system to the view coordinate system.
     * </ol>
     */
    public class GraphicOverlay extends View {
        private final Object lock = new Object();
        private final List<Graphic> graphics = new ArrayList<>();
        // Matrix for transforming from image coordinates to view coordinates.
        private final Matrix transformMatrix = new Matrix();

        private int imageWidth;
        private int imageHeight;
        // The factor of overlay View to image view.
        private float scaleFactor = 1.0f;
        // The number of horizontal pixels to shift translation to match image display.
        private float postScaleWidthOffset;
        // The number of vertical pixels to shift translation to match image display.
        private float postScaleHeightOffset;
        private boolean isImageFlipped;
        private boolean needUpdateTransformation = true;

        /**
         * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass
         * this and implement the {@link #draw(Canvas)} method to define the graphics logic. Add
         * instances to the overlay using {@link GraphicOverlay#add(Graphic)}.
         */
        public abstract static class Graphic {
            private GraphicOverlay overlay;

            public Graphic(GraphicOverlay overlay) {
                this.overlay = overlay;
            }

            /**
             * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
             * to view coordinates for the graphics that are drawn:
             *
             * <ol>
             *   <li>{@link Graphic#scale(float)} and {@link Graphic#scaleY(float)} adjust the size of the
             *       detected object from the preview scale to the view scale.
             *   <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
             *       coordinate from the preview's coordinate system to the view coordinate system.
             * </ol>
             *
             * @param canvas The canvas to draw on for this graphic.
             */
            public abstract void draw(Canvas canvas);

            /** Adjusts a horizontal value of the supplied value from the image scale to the view scale. */
            public float scale(float imagePixel) {
                return imagePixel * overlay.scaleFactor;
            }

            /** Adjusts a vertical value of the supplied value from the image scale to the view scale. */
            public float scaleY(float imagePixel) {
                return imagePixel * overlay.scaleFactor;
            }

            /** Returns the application context. */
            public Context getApplicationContext() {
                return overlay.getContext().getApplicationContext();
            }

            /**
             * Adjusts the x coordinate from the image's coordinate system to the view coordinate system.
             */
            public float translateX(float x) {
                if (overlay.isImageFlipped) {
                    return overlay.getWidth() - (scale(x) + overlay.postScaleWidthOffset);
                } else {
                    return scale(x) + overlay.postScaleWidthOffset;
                }
            }

            /**
             * Adjusts the y coordinate from the image's coordinate system to the view coordinate system.
             */
            public float translateY(float y) {
                return scale(y) + overlay.postScaleHeightOffset;
            }

            /**
             * Returns a Matrix for transforming from image coordinates to view coordinates.
             */
            public Matrix getTransformationMatrix() {
                return overlay.transformMatrix;
            }

            public void postInvalidate() {
                overlay.postInvalidate();
            }

            /**
             * Given a value in the image coordinate system, returns the associated value in the view
             * coordinate system.
             */
            public float B(float imagePixel) {
                return imagePixel * overlay.scaleFactor;
            }
        }

        public GraphicOverlay(Context context, AttributeSet attrs) {
            super(context, attrs);
            addOnLayoutChangeListener(
                    (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                            needUpdateTransformation = true);
        }

        /** Removes all graphics from the overlay. */
        public void clear() {
            synchronized (lock) {
                graphics.clear();
            }
            postInvalidate();
        }

        /** Adds a graphic to the overlay. */
        public void add(Graphic graphic) {
            synchronized (lock) {
                graphics.add(graphic);
            }
            postInvalidate();
        }

        /** Removes a graphic from the overlay. */
        public void remove(Graphic graphic) {
            synchronized (lock) {
                graphics.remove(graphic);
            }
            postInvalidate();
        }

        /**
         * Sets the source information of the image being processed by detectors, including size and
         * whether it is flipped, which informs how to transform image coordinates later.
         *
         * @param imageWidth the width of the image sent to ML Kit.
         * @param imageHeight the height of the image sent to ML Kit.
         * @param isFlipped whether the image is flipped. Should be true when the front camera is used.
         */
        public void setImageSourceInfo(int imageWidth, int imageHeight, boolean isFlipped) {
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            this.isImageFlipped = isFlipped;
            needUpdateTransformation = true;
            postInvalidate();
        }

        private void updateTransformation() {
            if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0) {
                return;
            }

            float viewAspectRatio = (float) getWidth() / getHeight();
            float imageAspectRatio = (float) imageWidth / imageHeight;
            postScaleWidthOffset = 0;
            postScaleHeightOffset = 0;

            if (viewAspectRatio > imageAspectRatio) {
                // The image fills the height of the view. Crop the sides of the image.
                scaleFactor = (float) getHeight() / imageHeight;
                postScaleWidthOffset = ((float) getWidth() - (imageWidth * scaleFactor)) / 2;
            } else {
                // The image fills the width of the view. Crop the top and bottom of the image.
                scaleFactor = (float) getWidth() / imageWidth;
                postScaleHeightOffset = ((float) getHeight() - (imageHeight * scaleFactor)) / 2;
            }

            transformMatrix.reset();
            transformMatrix.postScale(scaleFactor, scaleFactor);
            transformMatrix.postTranslate(postScaleWidthOffset, postScaleHeightOffset);

            if (isImageFlipped) {
                transformMatrix.postScale(-1f, 1f, getWidth() / 2f, getHeight() / 2f);
            }

            needUpdateTransformation = false;
        }

        /** Draws the overlay with its associated graphic objects. */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            synchronized (lock) {
                updateTransformation();

                for (Graphic graphic : graphics) {
                    graphic.draw(canvas);
                }
            }
        }
    }
    