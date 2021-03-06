package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.ResultPresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ResultAdapter;
import com.hiroshi.cimoc.ui.view.ResultView;
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class ResultActivity extends BaseActivity implements ResultView {

    @BindView(R.id.result_comic_list) RecyclerView mRecyclerView;
    @BindView(R.id.result_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.result_layout) LinearLayout mLinearLayout;

    private ResultAdapter mResultAdapter;
    private LinearLayoutManager mLayoutManager;
    private ResultPresenter mPresenter;

    private int source;

    @Override
    protected void initPresenter() {
        String keyword = getIntent().getStringExtra(EXTRA_KEYWORD);
        source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        mPresenter = new ResultPresenter(source, keyword);
        mPresenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
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
    protected void initView() {
        mLayoutManager = new LinearLayoutManager(this);
        mResultAdapter = new ResultAdapter(this, new LinkedList<Comic>());
        mResultAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Comic comic = mResultAdapter.getItem(position);
                Intent intent = DetailActivity.createIntent(ResultActivity.this, comic.getId(), comic.getSource(), comic.getCid());
                startActivity(intent);
            }
        });
        mResultAdapter.setControllerBuilder(ControllerBuilderFactory.getControllerBuilder(source, this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mResultAdapter);
        mRecyclerView.addItemDecoration(mResultAdapter.getItemDecoration());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mLayoutManager.findLastVisibleItemPosition() >= mResultAdapter.getItemCount() - 4 && dy > 0) {
                    mPresenter.load();
                }
            }
        });

        mPresenter.load();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activtiy_result;
    }

    @Override
    protected View getLayoutView() {
        return mLinearLayout;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.result);
    }

    @Override
    public void onLoadSuccess(List<Comic> list) {
        mResultAdapter.addAll(list);
    }

    @Override
    public void onNetworkError() {
        showLayout();
        showSnackbar(R.string.common_network_error);
    }

    @Override
    public void onParseError() {
        showLayout();
        showSnackbar(R.string.common_parse_error);
    }

    @Override
    public void onEmptyResult() {
        showLayout();
        showSnackbar(R.string.result_empty);
    }

    @Override
    public void showLayout() {
        if (mProgressBar.isShown() || !mRecyclerView.isShown()) {
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public static final String EXTRA_KEYWORD = "a";
    public static final String EXTRA_SOURCE = "b";

    public static Intent createIntent(Context context, String keyword, int sid) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(EXTRA_KEYWORD, keyword);
        intent.putExtra(EXTRA_SOURCE, sid);
        return intent;
    }

}
