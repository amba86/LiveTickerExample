/*
 * HorizontalListView.java v1.5
 *
 * 
 * The MIT License
 * Copyright (c) 2011 Paul Soucy (paul@dev-smart.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package amba.livetickerexample.view;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class HorizontalListView extends AdapterView<ListAdapter> {

	public boolean mAlwaysOverrideTouch = true;

	protected ListAdapter mAdapter;
	protected Scroller mScroller;
	protected int mCurrentX;
	protected int mNextX;

	private int mLeftViewIndex = -1;
	private int mRightViewIndex = 0;
	private int mMaxX = Integer.MAX_VALUE;
	private int mDisplayOffset = 0;
	private GestureDetector mGesture;
	private Queue<View> mRemovedViewQueue = new LinkedList<View>();
	private OnItemSelectedListener mOnItemSelected;
	private OnItemClickListener mOnItemClicked;
	private OnItemLongClickListener mOnItemLongClicked;
	private boolean mDataChanged = false;

	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			synchronized (HorizontalListView.this) {
				HorizontalListView.this.mDataChanged = true;
			}
			HorizontalListView.this.invalidate();
			HorizontalListView.this.requestLayout();
		}

		@Override
		public void onInvalidated() {
			HorizontalListView.this.reset();
			HorizontalListView.this.invalidate();
			HorizontalListView.this.requestLayout();
		}

	};

	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

		private boolean isEventWithinView(MotionEvent e, View child) {
			Rect viewRect = new Rect();
			int[] childPosition = new int[2];
			child.getLocationOnScreen(childPosition);
			int left = childPosition[0];
			int right = left + child.getWidth();
			int top = childPosition[1];
			int bottom = top + child.getHeight();
			viewRect.set(left, top, right, bottom);
			return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return HorizontalListView.this.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return HorizontalListView.this
					.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public void onLongPress(MotionEvent e) {
			int childCount = HorizontalListView.this.getChildCount();
			for (int i = 0; i < childCount; i++) {
				View child = HorizontalListView.this.getChildAt(i);
				if (this.isEventWithinView(e, child)) {
					if (HorizontalListView.this.mOnItemLongClicked != null) {
						HorizontalListView.this.mOnItemLongClicked
								.onItemLongClick(
										HorizontalListView.this,
										child,
										HorizontalListView.this.mLeftViewIndex
												+ 1 + i,
										HorizontalListView.this.mAdapter
												.getItemId(HorizontalListView.this.mLeftViewIndex
														+ 1 + i));
					}
					break;
				}

			}
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			synchronized (HorizontalListView.this) {
				HorizontalListView.this.mNextX += (int) distanceX;
			}
			HorizontalListView.this.requestLayout();

			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			for (int i = 0; i < HorizontalListView.this.getChildCount(); i++) {
				View child = HorizontalListView.this.getChildAt(i);
				if (this.isEventWithinView(e, child)) {
					if (HorizontalListView.this.mOnItemClicked != null) {
						HorizontalListView.this.mOnItemClicked
								.onItemClick(
										HorizontalListView.this,
										child,
										HorizontalListView.this.mLeftViewIndex
												+ 1 + i,
										HorizontalListView.this.mAdapter
												.getItemId(HorizontalListView.this.mLeftViewIndex
														+ 1 + i));
					}
					if (HorizontalListView.this.mOnItemSelected != null) {
						HorizontalListView.this.mOnItemSelected
								.onItemSelected(
										HorizontalListView.this,
										child,
										HorizontalListView.this.mLeftViewIndex
												+ 1 + i,
										HorizontalListView.this.mAdapter
												.getItemId(HorizontalListView.this.mLeftViewIndex
														+ 1 + i));
					}
					break;
				}

			}
			return true;
		}
	};

	final Runnable mLayoutRunnable = new Runnable() {
		@Override
		public void run() {
			HorizontalListView.this.requestLayout();
		}
	};

	public HorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initView();
	}

	private void addAndMeasureChild(final View child, int viewPos) {
		LayoutParams params = child.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
		}

		this.addViewInLayout(child, viewPos, params, true);
		child.measure(MeasureSpec.makeMeasureSpec(this.getWidth(),
				MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(
				this.getHeight(), MeasureSpec.AT_MOST));
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean handled = super.dispatchTouchEvent(ev);
		handled |= this.mGesture.onTouchEvent(ev);
		return handled;
	}

	private void fillList(final int dx) {
		int edge = 0;
		View child = this.getChildAt(this.getChildCount() - 1);
		if (child != null) {
			edge = child.getRight();
		}
		this.fillListRight(edge, dx);

		edge = 0;
		child = this.getChildAt(0);
		if (child != null) {
			edge = child.getLeft();
		}
		this.fillListLeft(edge, dx);

	}

	private void fillListLeft(int leftEdge, final int dx) {
		while (((leftEdge + dx) > 0) && (this.mLeftViewIndex >= 0)) {
			View child = this.mAdapter.getView(this.mLeftViewIndex,
					this.mRemovedViewQueue.poll(), this);
			this.addAndMeasureChild(child, 0);
			leftEdge -= child.getMeasuredWidth();
			this.mLeftViewIndex--;
			this.mDisplayOffset -= child.getMeasuredWidth();
		}
	}

	private void fillListRight(int rightEdge, final int dx) {
		while (((rightEdge + dx) < this.getWidth())
				&& (this.mRightViewIndex < this.mAdapter.getCount())) {

			View child = this.mAdapter.getView(this.mRightViewIndex,
					this.mRemovedViewQueue.poll(), this);
			this.addAndMeasureChild(child, -1);
			rightEdge += child.getMeasuredWidth();

			if (this.mRightViewIndex == (this.mAdapter.getCount() - 1)) {
				this.mMaxX = (this.mCurrentX + rightEdge) - this.getWidth();
			}

			if (this.mMaxX < 0) {
				this.mMaxX = 0;
			}
			this.mRightViewIndex++;
		}

	}

	@Override
	public ListAdapter getAdapter() {
		return this.mAdapter;
	}

	@Override
	public View getSelectedView() {
		// TODO: implement
		return null;
	}

	private synchronized void initView() {
		this.mLeftViewIndex = -1;
		this.mRightViewIndex = 0;
		this.mDisplayOffset = 0;
		this.mCurrentX = 0;
		this.mNextX = 0;
		this.mMaxX = Integer.MAX_VALUE;
		this.mScroller = new Scroller(this.getContext());
		this.mGesture = new GestureDetector(this.getContext(), this.mOnGesture);
	}

	protected boolean onDown(MotionEvent e) {
		this.mScroller.forceFinished(true);
		return true;
	}

	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		synchronized (HorizontalListView.this) {
			this.mScroller.fling(this.mNextX, 0, (int) -velocityX, 0, 0,
					this.mMaxX, 0, 0);
		}
		this.requestLayout();

		return true;
	}

	@Override
	protected synchronized void onLayout(boolean changed, int left, int top,
			int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (this.mAdapter == null) {
			return;
		}

		if (this.mDataChanged) {
			int oldCurrentX = this.mCurrentX;
			this.initView();
			this.removeAllViewsInLayout();
			this.mNextX = oldCurrentX;
			this.mDataChanged = false;
		}

		if (this.mScroller.computeScrollOffset()) {
			int scrollx = this.mScroller.getCurrX();
			this.mNextX = scrollx;
		}

		if (this.mNextX <= 0) {
			this.mNextX = 0;
			this.mScroller.forceFinished(true);
		}
		if (this.mNextX >= this.mMaxX) {
			this.mNextX = this.mMaxX;
			this.mScroller.forceFinished(true);
		}

		int dx = this.mCurrentX - this.mNextX;

		this.removeNonVisibleItems(dx);
		this.fillList(dx);
		this.positionItems(dx);

		this.mCurrentX = this.mNextX;

		if (!this.mScroller.isFinished()) {
			this.post(this.mLayoutRunnable);

		}
	}

	private void positionItems(final int dx) {
		if (this.getChildCount() > 0) {
			this.mDisplayOffset += dx;
			int left = this.mDisplayOffset;
			for (int i = 0; i < this.getChildCount(); i++) {
				View child = this.getChildAt(i);
				int childWidth = child.getMeasuredWidth();
				child.layout(left, 0, left + childWidth,
						child.getMeasuredHeight());
				left += childWidth + child.getPaddingRight();
			}
		}
	}

	private void removeNonVisibleItems(final int dx) {
		View child = this.getChildAt(0);
		while ((child != null) && ((child.getRight() + dx) <= 0)) {
			this.mDisplayOffset += child.getMeasuredWidth();
			this.mRemovedViewQueue.offer(child);
			this.removeViewInLayout(child);
			this.mLeftViewIndex++;
			child = this.getChildAt(0);

		}

		child = this.getChildAt(this.getChildCount() - 1);
		while ((child != null) && ((child.getLeft() + dx) >= this.getWidth())) {
			this.mRemovedViewQueue.offer(child);
			this.removeViewInLayout(child);
			this.mRightViewIndex--;
			child = this.getChildAt(this.getChildCount() - 1);
		}
	}

	private synchronized void reset() {
		this.initView();
		this.removeAllViewsInLayout();
		this.requestLayout();
	}

	public synchronized void scrollTo(int x) {
		this.mScroller.startScroll(this.mNextX, 0, x - this.mNextX, 0);
		this.requestLayout();
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (this.mAdapter != null) {
			this.mAdapter.unregisterDataSetObserver(this.mDataObserver);
		}
		this.mAdapter = adapter;
		this.mAdapter.registerDataSetObserver(this.mDataObserver);
		this.reset();
	}

	@Override
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
		this.mOnItemClicked = listener;
	}

	@Override
	public void setOnItemLongClickListener(
			AdapterView.OnItemLongClickListener listener) {
		this.mOnItemLongClicked = listener;
	}

	@Override
	public void setOnItemSelectedListener(
			AdapterView.OnItemSelectedListener listener) {
		this.mOnItemSelected = listener;
	}

	@Override
	public void setSelection(int position) {
		// TODO: implement
	}
}
