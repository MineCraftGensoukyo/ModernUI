/*
 * Modern UI.
 * Copyright (C) 2019-2022 BloCamLimb. All rights reserved.
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

package icyllis.modernui.graphics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a rectangle.
 */
@SuppressWarnings("unused")
public class Rect {

    public int left;
    public int top;
    public int right;
    public int bottom;

    /**
     * Create a new Rect with all coordinates initialized to 0.
     */
    public Rect() {
    }

    /**
     * Create a new rectangle with the specified coordinates. Note: no range
     * checking is performed, so the caller must ensure that left <= right and
     * top <= bottom.
     *
     * @param left   The X coordinate of the left side of the rectangle
     * @param top    The Y coordinate of the top of the rectangle
     * @param right  The X coordinate of the right side of the rectangle
     * @param bottom The Y coordinate of the bottom of the rectangle
     */
    public Rect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    /**
     * Create a new rectangle, initialized with the values in the specified
     * rectangle (which is left unmodified).
     *
     * @param r The rectangle whose coordinates are copied into the new
     *          rectangle.
     */
    public Rect(@Nullable Rect r) {
        if (r != null) {
            left = r.left;
            top = r.top;
            right = r.right;
            bottom = r.bottom;
        }
    }

    /**
     * Returns a copy of {@code r} if it is not {@code null}, or
     * an empty Rect otherwise.
     *
     * @param r the rect to copy from
     */
    @Nonnull
    public static Rect copy(@Nullable Rect r) {
        return r == null ? new Rect() : r.copy();
    }

    /**
     * Returns true if the rectangle is empty (left >= right or top >= bottom)
     */
    public final boolean isEmpty() {
        return left >= right || top >= bottom;
    }

    /**
     * Returns the rectangle's left.
     */
    public final int x() {
        return left;
    }

    /**
     * Return the rectangle's top.
     */
    public final int y() {
        return top;
    }

    /**
     * @return the rectangle's width. This does not check for a valid rectangle
     * (i.e. left <= right) so the result may be negative.
     */
    public final int width() {
        return right - left;
    }

    /**
     * @return the rectangle's height. This does not check for a valid rectangle
     * (i.e. top <= bottom) so the result may be negative.
     */
    public final int height() {
        return bottom - top;
    }

    /**
     * @return the horizontal center of the rectangle. If the computed value
     * is fractional, this method returns the largest integer that is
     * less than the computed value.
     */
    public final int centerX() {
        return (left + right) >> 1;
    }

    /**
     * @return the vertical center of the rectangle. If the computed value
     * is fractional, this method returns the largest integer that is
     * less than the computed value.
     */
    public final int centerY() {
        return (top + bottom) >> 1;
    }

    /**
     * @return the exact horizontal center of the rectangle as a float.
     */
    public final float exactCenterX() {
        return (left + right) * 0.5f;
    }

    /**
     * @return the exact vertical center of the rectangle as a float.
     */
    public final float exactCenterY() {
        return (top + bottom) * 0.5f;
    }

    /**
     * Set the rectangle to (0,0,0,0)
     */
    public void setEmpty() {
        left = right = top = bottom = 0;
    }

    /**
     * Set the rectangle's coordinates to the specified values. Note: no range
     * checking is performed, so it is up to the caller to ensure that
     * left <= right and top <= bottom.
     *
     * @param left   The X coordinate of the left side of the rectangle
     * @param top    The Y coordinate of the top of the rectangle
     * @param right  The X coordinate of the right side of the rectangle
     * @param bottom The Y coordinate of the bottom of the rectangle
     */
    public void set(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    /**
     * Copy the coordinates from src into this rectangle.
     *
     * @param src The rectangle whose coordinates are copied into this
     *            rectangle.
     */
    public void set(@Nonnull Rect src) {
        this.left = src.left;
        this.top = src.top;
        this.right = src.right;
        this.bottom = src.bottom;
    }

    /**
     * Offset the rectangle by adding dx to its left and right coordinates, and
     * adding dy to its top and bottom coordinates.
     *
     * @param dx The amount to add to the rectangle's left and right coordinates
     * @param dy The amount to add to the rectangle's top and bottom coordinates
     */
    public void offset(int dx, int dy) {
        left += dx;
        top += dy;
        right += dx;
        bottom += dy;
    }

    /**
     * Offset the rectangle to a specific (left, top) position,
     * keeping its width and height the same.
     *
     * @param newLeft The new "left" coordinate for the rectangle
     * @param newTop  The new "top" coordinate for the rectangle
     */
    public void offsetTo(int newLeft, int newTop) {
        right += newLeft - left;
        bottom += newTop - top;
        left = newLeft;
        top = newTop;
    }

    /**
     * Inset the rectangle by (dx,dy). If dx is positive, then the sides are
     * moved inwards, making the rectangle narrower. If dx is negative, then the
     * sides are moved outwards, making the rectangle wider. The same holds true
     * for dy and the top and bottom.
     *
     * @param dx The amount to add(subtract) from the rectangle's left(right)
     * @param dy The amount to add(subtract) from the rectangle's top(bottom)
     */
    public void inset(int dx, int dy) {
        left += dx;
        top += dy;
        right -= dx;
        bottom -= dy;
    }

    /**
     * Insets the rectangle on all sides specified by the dimensions of the {@code insets}
     * rectangle.
     *
     * @param insets The rectangle specifying the insets on all side.
     */
    public void inset(@Nonnull Rect insets) {
        left += insets.left;
        top += insets.top;
        right -= insets.right;
        bottom -= insets.bottom;
    }

    /**
     * Insets the rectangle on all sides specified by the insets.
     *
     * @param left   The amount to add from the rectangle's left
     * @param top    The amount to add from the rectangle's top
     * @param right  The amount to subtract from the rectangle's right
     * @param bottom The amount to subtract from the rectangle's bottom
     */
    public void inset(int left, int top, int right, int bottom) {
        this.left += left;
        this.top += top;
        this.right -= right;
        this.bottom -= bottom;
    }

    /**
     * Returns true if (x,y) is inside the rectangle. The left and top are
     * considered to be inside, while the right and bottom are not. This means
     * that for a (x,y) to be contained: left <= x < right and top <= y < bottom.
     * An empty rectangle never contains any point.
     *
     * @param x The X coordinate of the point being tested for containment
     * @param y The Y coordinate of the point being tested for containment
     * @return true if (x,y) are contained by the rectangle, where containment
     * means left <= x < right and top <= y < bottom
     */
    public boolean contains(int x, int y) {
        return left < right && top < bottom  // check for empty first
                && x >= left && x < right && y >= top && y < bottom;
    }

    /**
     * Returns true if the 4 specified sides of a rectangle are inside or equal
     * to this rectangle. i.e. is this rectangle a superset of the specified
     * rectangle. An empty rectangle never contains another rectangle.
     *
     * @param left   The left side of the rectangle being tested for containment
     * @param top    The top of the rectangle being tested for containment
     * @param right  The right side of the rectangle being tested for containment
     * @param bottom The bottom of the rectangle being tested for containment
     * @return true if the 4 specified sides of a rectangle are inside or
     * equal to this rectangle
     */
    public boolean contains(int left, int top, int right, int bottom) {
        // check for empty first
        return this.left < this.right && this.top < this.bottom
                // now check for containment
                && this.left <= left && this.top <= top
                && this.right >= right && this.bottom >= bottom;
    }

    /**
     * Returns true if the specified rectangle r is inside or equal to this
     * rectangle. An empty rectangle never contains another rectangle.
     *
     * @param r The rectangle being tested for containment.
     * @return true if the specified rectangle r is inside or equal to this
     * rectangle
     */
    public boolean contains(@Nonnull Rect r) {
        // check for empty first
        return this.left < this.right && this.top < this.bottom
                // now check for containment
                && left <= r.left && top <= r.top && right >= r.right && bottom >= r.bottom;
    }

    /**
     * If the rectangle specified by left,top,right,bottom intersects this
     * rectangle, return true and set this rectangle to that intersection,
     * otherwise return false and do not change this rectangle. No check is
     * performed to see if either rectangle is empty. Note: To just test for
     * intersection, use {@link #intersects(Rect, Rect)}.
     *
     * @param left   The left side of the rectangle being intersected with this
     *               rectangle
     * @param top    The top of the rectangle being intersected with this rectangle
     * @param right  The right side of the rectangle being intersected with this
     *               rectangle.
     * @param bottom The bottom of the rectangle being intersected with this
     *               rectangle.
     * @return true if the specified rectangle and this rectangle intersect
     * (and this rectangle is then set to that intersection) else
     * return false and do not change this rectangle.
     */
    public boolean intersect(int left, int top, int right, int bottom) {
        if (this.left < right && left < this.right && this.top < bottom && top < this.bottom) {
            if (this.left < left) this.left = left;
            if (this.top < top) this.top = top;
            if (this.right > right) this.right = right;
            if (this.bottom > bottom) this.bottom = bottom;
            return true;
        }
        return false;
    }

    /**
     * If the specified rectangle intersects this rectangle, return true and set
     * this rectangle to that intersection, otherwise return false and do not
     * change this rectangle. No check is performed to see if either rectangle
     * is empty. To just test for intersection, use intersects()
     *
     * @param r The rectangle being intersected with this rectangle.
     * @return true if the specified rectangle and this rectangle intersect
     * (and this rectangle is then set to that intersection) else
     * return false and do not change this rectangle.
     */
    public boolean intersect(@Nonnull Rect r) {
        return intersect(r.left, r.top, r.right, r.bottom);
    }

    /**
     * If the specified rectangle intersects this rectangle, set this rectangle to that
     * intersection, otherwise set this rectangle to the empty rectangle.
     *
     * @see #inset(int, int, int, int) but without checking if the rects overlap.
     */
    public void intersectUnchecked(@Nonnull Rect other) {
        left = Math.max(left, other.left);
        top = Math.max(top, other.top);
        right = Math.min(right, other.right);
        bottom = Math.min(bottom, other.bottom);
    }

    /**
     * If rectangles a and b intersect, return true and set this rectangle to
     * that intersection, otherwise return false and do not change this
     * rectangle. No check is performed to see if either rectangle is empty.
     * To just test for intersection, use intersects()
     *
     * @param a The first rectangle being intersected with
     * @param b The second rectangle being intersected with
     * @return true if the two specified rectangles intersect. If they do, set
     * this rectangle to that intersection. If they do not, return
     * false and do not change this rectangle.
     */
    public boolean setIntersect(@Nonnull Rect a, @Nonnull Rect b) {
        if (a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom) {
            left = Math.max(a.left, b.left);
            top = Math.max(a.top, b.top);
            right = Math.min(a.right, b.right);
            bottom = Math.min(a.bottom, b.bottom);
            return true;
        }
        return false;
    }

    /**
     * Returns true if this rectangle intersects the specified rectangle.
     * In no event is this rectangle modified. No check is performed to see
     * if either rectangle is empty. To record the intersection, use intersect()
     * or setIntersect().
     *
     * @param left   The left side of the rectangle being tested for intersection
     * @param top    The top of the rectangle being tested for intersection
     * @param right  The right side of the rectangle being tested for
     *               intersection
     * @param bottom The bottom of the rectangle being tested for intersection
     * @return true if the specified rectangle intersects this rectangle. In
     * no event is this rectangle modified.
     */
    public boolean intersects(int left, int top, int right, int bottom) {
        return this.left < right && left < this.right && this.top < bottom && top < this.bottom;
    }

    /**
     * Returns true if the two specified rectangles intersect. In no event are
     * either of the rectangles modified. To record the intersection,
     * use {@link #intersect(Rect)} or {@link #setIntersect(Rect, Rect)}.
     *
     * @param a The first rectangle being tested for intersection
     * @param b The second rectangle being tested for intersection
     * @return true if the two specified rectangles intersect. In no event are
     * either of the rectangles modified.
     */
    public static boolean intersects(@Nonnull Rect a, @Nonnull Rect b) {
        return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom;
    }

    /**
     * Update this Rect to enclose itself and the specified rectangle. If the
     * specified rectangle is empty, nothing is done. If this rectangle is empty
     * it is set to the specified rectangle.
     *
     * @param left   the left edge being unioned with this rectangle
     * @param top    the top edge being unioned with this rectangle
     * @param right  the right edge being unioned with this rectangle
     * @param bottom the bottom edge being unioned with this rectangle
     */
    public final void join(int left, int top, int right, int bottom) {
        // do nothing if the params are empty
        if (left >= right || top >= bottom) {
            return;
        }
        if (this.left < this.right && this.top < this.bottom) {
            if (this.left > left) this.left = left;
            if (this.top > top) this.top = top;
            if (this.right < right) this.right = right;
            if (this.bottom < bottom) this.bottom = bottom;
        } else {
            // if we are empty, just assign
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

    /**
     * Update this Rect to enclose itself and the specified rectangle. If the
     * specified rectangle is empty, nothing is done. If this rectangle is empty
     * it is set to the specified rectangle.
     *
     * @param left   The left edge being unioned with this rectangle
     * @param top    The top edge being unioned with this rectangle
     * @param right  The right edge being unioned with this rectangle
     * @param bottom The bottom edge being unioned with this rectangle
     */
    public void union(int left, int top, int right, int bottom) {
        if ((left < right) && (top < bottom)) {
            if ((this.left < this.right) && (this.top < this.bottom)) {
                if (this.left > left) this.left = left;
                if (this.top > top) this.top = top;
                if (this.right < right) this.right = right;
                if (this.bottom < bottom) this.bottom = bottom;
            } else {
                this.left = left;
                this.top = top;
                this.right = right;
                this.bottom = bottom;
            }
        }
    }

    /**
     * Update this Rect to enclose itself and the specified rectangle. If the
     * specified rectangle is empty, nothing is done. If this rectangle is empty
     * it is set to the specified rectangle.
     *
     * @param r The rectangle being unioned with this rectangle
     */
    public void union(@Nonnull Rect r) {
        union(r.left, r.top, r.right, r.bottom);
    }

    /**
     * Update this Rect to enclose itself and the [x,y] coordinate. There is no
     * check to see that this rectangle is non-empty.
     *
     * @param x The x coordinate of the point to add to the rectangle
     * @param y The y coordinate of the point to add to the rectangle
     */
    public void union(int x, int y) {
        if (x < left) {
            left = x;
        } else if (x > right) {
            right = x;
        }
        if (y < top) {
            top = y;
        } else if (y > bottom) {
            bottom = y;
        }
    }

    /**
     * Swap top/bottom or left/right if there are flipped (i.e. left > right
     * and/or top > bottom). This can be called if
     * the edges are computed separately, and may have crossed over each other.
     * If the edges are already correct (i.e. left <= right and top <= bottom)
     * then nothing is done.
     */
    public void sort() {
        if (left > right) {
            int temp = left;
            left = right;
            right = temp;
        }
        if (top > bottom) {
            int temp = top;
            top = bottom;
            bottom = temp;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rect rect = (Rect) o;
        return left == rect.left && top == rect.top && right == rect.right && bottom == rect.bottom;
    }

    @Override
    public int hashCode() {
        int result = left;
        result = 31 * result + top;
        result = 31 * result + right;
        result = 31 * result + bottom;
        return result;
    }

    @Override
    public String toString() {
        return "Rect(" + left + ", " +
                top + " - " + right +
                ", " + bottom + ")";
    }

    /**
     * Return a string representation of the rectangle in a compact form.
     *
     * @hide
     */
    @Nonnull
    public String toShortString() {
        return "[" + left + ',' +
                top + "][" + right +
                ',' + bottom + ']';
    }

    @Nonnull
    public Rect copy() {
        return new Rect(left, top, right, bottom);
    }
}
