package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.DetailPresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ChapterAdapter;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends BaseActivity implements DetailView {

    @BindView(R.id.detail_chapter_list) RecyclerView mRecyclerView;
    @BindView(R.id.detail_coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.detail_star_btn) FloatingActionButton mStarButton;
    @BindView(R.id.detail_progress_bar) ProgressBar mProgressBar;

    private ChapterAdapter mChapterAdapter;
    private DetailPresenter mPresenter;

    @OnClick(R.id.detail_star_btn) void onClick() {
        if (mPresenter.isComicFavorite()) {
            mPresenter.unfavoriteComic();
            mStarButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            showSnackbar(R.string.detail_unfavorite);
        } else {
            mPresenter.favoriteComic();
            mStarButton.setImageResource(R.drawable.ic_favorite_white_24dp);
            showSnackbar(R.string.detail_favorite);
        }
    }

    @Override
    protected void initView() {
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        String cid = getIntent().getStringExtra(EXTRA_CID);
        mPresenter.load(id, cid);
    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void initPresenter() {
        int source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        mPresenter = new DetailPresenter(source);
        mPresenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        mPresenter.updateComic();
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_detail;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.detail);
    }

    @Override
    protected View getLayoutView() {
        return mCoordinatorLayout;
    }

    @Override
    public void onChapterChange(String last) {
        mChapterAdapter.setLast(last);
    }

    @Override
    public void onComicLoad(Comic comic) {
        mChapterAdapter = new ChapterAdapter(this, new LinkedList<Chapter>());
        mChapterAdapter.setInfo(comic.getSource(), comic.getCover(), comic.getTitle(), comic.getAuthor(),
                comic.getIntro(), comic.getStatus(), comic.getUpdate(), comic.getLast());
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mRecyclerView.setAdapter(mChapterAdapter);
        mRecyclerView.addItemDecoration(mChapterAdapter.getItemDecoration());

        if (comic.getTitle() != null && comic.getCover() != null && comic.getUpdate() != null) {
            if (comic.getFavorite() != null) {
                mStarButton.setImageResource(R.drawable.ic_favorite_white_24dp);
            } else {
                mStarButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            }
            mStarButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChapterLoad(List<Chapter> list) {
        mChapterAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position != 0) {
                    Intent intent = ReaderActivity.createIntent(DetailActivity.this, mPresenter.getComic(),
                            mChapterAdapter.getDateSet(), position - 1);
                    startActivity(intent);
                }
            }
        });
        mChapterAdapter.setData(list);
    }

    @Override
    public void showLayout() {
        if (mProgressBar.isShown() || !mCoordinatorLayout.isShown()) {
            mProgressBar.setVisibility(View.GONE);
            mCoordinatorLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNetworkError() {
        showSnackbar(R.string.common_network_error);
    }

    @Override
    public void onParseError() {
        showSnackbar(R.string.common_parse_error);
    }

    public static final String EXTRA_ID = "a";
    public static final String EXTRA_SOURCE = "b";
    public static final String EXTRA_CID = "c";

    public static Intent createIntent(Context context, Long id, int source, String cid) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_CID, cid);
        return intent;
    }

}
