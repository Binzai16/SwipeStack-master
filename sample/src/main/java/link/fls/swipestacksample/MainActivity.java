/*
 * Copyright (C) 2016 Frederik Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package link.fls.swipestacksample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import link.fls.swipestack.SwipeStack;
import link.fls.swipestack.util.SwipBaseAdapter;

public class MainActivity extends AppCompatActivity implements SwipeStack.SwipeStackListener, View.OnClickListener {

    private Button mButtonLeft, mButtonRight;
    private FloatingActionButton mFab;

    private List<Object> mData;
    private SwipeStack mSwipeStack;
    private SwipBaseAdapter mAdapter;
    private int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeStack = (SwipeStack) findViewById(R.id.swipeStack);
        mButtonLeft = (Button) findViewById(R.id.buttonSwipeLeft);
        mButtonRight = (Button) findViewById(R.id.buttonSwipeRight);
        mFab = (FloatingActionButton) findViewById(R.id.fabAdd);

        mButtonLeft.setOnClickListener(this);
        mButtonRight.setOnClickListener(this);
        mFab.setOnClickListener(this);

        mData = new ArrayList<>();
        fillWithTestData();
        mAdapter = new SwipeStackAdapter(mData, this);
        mSwipeStack.setAdapter(mAdapter);
        mSwipeStack.setListener(this);
        mAdapter.setOnClickListener(new SwipBaseAdapter.OnClickListener() {
            @Override
            public void onLCick(Object object) {
//                String  o = (String) mData.get(mSwipeStack.getCurrentPosition());
                mData.clear();
                fillWithTestData3();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void fillWithTestData() {
        for (int x = 0; x < 5; x++) {
            mData.add(getString(R.string.dummy_text) + " " + (x + 1));
        }
    }

    private void fillWithTestData3() {
        for (int x = 0; x < 5; x++) {
            if (x == 0) {
                mData.add(getString(R.string.dummy_text) + "--- " + (x + 1));
            } else {
                mData.add(getString(R.string.dummy_text) + " " + (x + 1));
            }
        }
    }


    private void fillWithTestData2() {
        for (int x = 0; x < 2; x++) {
            mData.add(getString(R.string.dummy_text) + " num- " + num + " xxxx " + (x + 1));
        }
    }


    @Override
    public void onClick(View v) {
        if (v.equals(mButtonLeft)) {
            mSwipeStack.swipeTopViewToLeft();
        } else if (v.equals(mButtonRight)) {
            mSwipeStack.swipeTopViewToRight();
        } else if (v.equals(mFab)) {
            mData.add(getString(R.string.dummy_fab));
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuReset:
                mSwipeStack.resetStack();
                Snackbar.make(mFab, R.string.stack_reset, Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.menuGitHub:
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://github.com/flschweiger/SwipeStack"));
                startActivity(browserIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewSwipedToRight(int position) {
//        String swipedElement = mAdapter.getItem(position);
//        Toast.makeText(this, getString(R.string.view_swiped_right, swipedElement),
//                Toast.LENGTH_SHORT).show();
//        mData.remove(position);
//        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onViewSwipedToTop(int position) {
//        String swipedElement = mAdapter.getItem(position);
//        Toast.makeText(this, getString(R.string.view_swiped_left, swipedElement),
//                Toast.LENGTH_SHORT).show();
//        mData.remove(position);
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onViewSwipedToBottom(int position) {
//        mData.remove(position);
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onViewSwipedToLeft(int position) {
        Log.e("ccc", " onViewSwipedToLeft  position " + position);
//        String swipedElement = mAdapter.getItem(position);
//        Toast.makeText(this, getString(R.string.view_swiped_left, swipedElement),
//                Toast.LENGTH_SHORT).show();
//        mData.remove(position);
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStackEmpty() {
        Toast.makeText(this, R.string.stack_empty, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLastThreeData(int position) {
        Toast.makeText(this, "第三条", Toast.LENGTH_SHORT).show();
        num++;
//        if (num < 2) {
//            fillWithTestData2();
//        }
        Log.e("ccc", "onLastThreeData   position  " + position);
    }

    @Override
    public void onClickListener(int position) {
        Toast.makeText(this, (String) mAdapter.getItem(position), Toast.LENGTH_SHORT).show();

    }

    public class SwipeStackAdapter extends SwipBaseAdapter {

        private MainActivity context;

        public void setContext(MainActivity context) {
            this.context = context;
        }

        public SwipeStackAdapter(List<Object> data, MainActivity context) {
            list = data;
            this.context = context;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.card, parent, false);
            }

            final TextView textViewCard = (TextView) convertView.findViewById(R.id.textViewCard);
            final ImageView iv_ = (ImageView) convertView.findViewById(R.id.iv_);
            textViewCard.setText((String) mData.get(position));
            textViewCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//
//                    iv_.setVisibility(View.VISIBLE);
//                    textViewCard.setText("30% 哈哈哈哈哈哈");
//                    mSwipeStack.resetStack2Position();
                    mOnClickListener.onLCick(mData.get(position));
//                    Log.w("www", "setOnClickListener   item   textViewCard  getMeasuredWidth    " + textViewCard.getMeasuredWidth() + " textViewCard  width   " + textViewCard.getWidth());

                }
            });
            return convertView;
        }
    }

}
