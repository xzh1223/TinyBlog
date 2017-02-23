package com.tinyblog.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.utils.NetworkUtils;
import com.google.gson.Gson;
import com.tinyblog.R;
import com.tinyblog.base.BaseActivity;
import com.tinyblog.bean.PostDetailsBean;
import com.tinyblog.sys.Constants;
import com.tinyblog.sys.Url;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zzhoujay.richtext.RichText;

import java.util.ArrayList;
import java.util.List;

import me.next.tagview.TagCloudView;
import okhttp3.Call;

/**
 * @author xiarui
 * @date 2017/1/14 19:19
 * @desc 文章详情页
 * @remark
 */

public class PostDetailsActivity extends BaseActivity {
    private Toolbar mPostDetailsTBar;
    private TextView mPostTitleText;
    private TextView mPostTimeText;
    private ProgressBar mPostPBar;
    private TextView mPostContentText;
    private TagCloudView mPostCategoriesTCView;
    private TagCloudView mPostLabelsTCView;
    private PostDetailsBean.PostBean postBean;
    private LinearLayout mPostCardsLLayout;

    //停止刷新操作
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == Constants.REFRESH_SUCCESS) {
                //刷新完成更新UI
                showUIAtRefreshDown();
            }
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_post_details;
    }

    @Override
    public void initView() {
        mPostDetailsTBar = (Toolbar) findViewById(R.id.tb_post_details);
        setSupportActionBar(mPostDetailsTBar);
        //隐藏默认标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPostTitleText = (TextView) findViewById(R.id.tv_post_details_title);
        mPostTimeText = (TextView) findViewById(R.id.tv_post_details_time);
        mPostPBar = (ProgressBar) findViewById(R.id.pb_post_details);
        mPostContentText = (TextView) findViewById(R.id.tv_post_details_content);
        mPostCategoriesTCView = (TagCloudView) findViewById(R.id.tcv_post_category);
        mPostLabelsTCView = (TagCloudView) findViewById(R.id.tcv_post_label);
        mPostCardsLLayout = (LinearLayout) findViewById(R.id.ll_post_details_cards);
    }

    @Override
    public void initData() {
        OkHttpUtils
                .get()
                .url(Url.GET_POST_DETAILS + getIntent().getStringExtra(Constants.POST_DETAILS_ID))
                .build()
                .execute(new PostDetailsCallBack());
    }

    /**
     * 获取当前文章详情
     */
    public class PostDetailsCallBack extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {
            if (!NetworkUtils.isConnected()) {
                showBaseToast("获取失败，请检查网络连接");
            }
        }

        @Override
        public void onResponse(String response, int id) {
            PostDetailsBean postDetailsBean = new Gson().fromJson(response, PostDetailsBean.class);
            if (postDetailsBean.getStatus().equals("ok")) {
                mHandler.sendEmptyMessage(Constants.REFRESH_SUCCESS);
                updatePostUIFromNet(postDetailsBean);
            } else {
                showBaseToast("数据异常，请重新刷新");
            }
        }

        /**
         * 从网络获取数据更新文章页UI
         *
         * @param postDetailsBean 文章详情
         */
        private void updatePostUIFromNet(PostDetailsBean postDetailsBean) {
            postBean = postDetailsBean.getPost();
            mPostTitleText.setText(postBean.getTitle());
            mPostTimeText.setText(postBean.getDate());
            //TODO: 代码块无法分行 GIF无法播放
            RichText.from(postBean.getContent()).into(mPostContentText);
            //添加文章分类
            addPostCategory(postBean);
            //添加文章标签
            addPostLabel(postBean);
        }

        /**
         * 添加文章分类
         *
         * @param postBean 文章Bean对象
         */
        private void addPostCategory(PostDetailsBean.PostBean postBean) {
            List<PostDetailsBean.PostBean.CategoriesBean> categoriesBean = postBean.getCategories();
            List<String> categoriesTags = new ArrayList<>();
            for (int i = 0; i < categoriesBean.size(); i++) {
                categoriesTags.add(categoriesBean.get(i).getTitle() + "(" + categoriesBean.get(i).getPost_count() + ")");
            }
            mPostCategoriesTCView.setTags(categoriesTags);
        }

        /**
         * 添加文章标签
         *
         * @param postBean 文章Bean对象
         */
        private void addPostLabel(PostDetailsBean.PostBean postBean) {
            List<PostDetailsBean.PostBean.TagsBean> labelBean = postBean.getTags();
            List<String> labelTags = new ArrayList<>();
            for (int i = 0; i < labelBean.size(); i++) {
                labelTags.add(labelBean.get(i).getTitle() + "(" + labelBean.get(i).getPost_count() + ")");
            }
            mPostLabelsTCView.setTags(labelTags);
        }
    }

    @Override
    public void initEvents() {
        mPostCategoriesTCView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
            @Override
            public void onTagClick(int position) {
                showBaseToast(" ID = " + postBean.getCategories().get(position).getId());
            }
        });

        mPostLabelsTCView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
            @Override
            public void onTagClick(int position) {
                showBaseToast(" ID = " + postBean.getTags().get(position).getId());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_item_refresh:
                hideUIAtRefreshing();
                initData();
                return true;
            case R.id.menu_item_share:
                showBaseToast("分享");
                return true;
            case R.id.menu_item_comment:
                showBaseToast("评论");
                return true;
        }
        return true;
    }

    /**
     * 在刷新的时候隐藏布局
     */
    private void hideUIAtRefreshing() {
        mPostPBar.setVisibility(View.VISIBLE);
        mPostContentText.setVisibility(View.INVISIBLE);
        mPostCardsLLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * 在刷新完成的时候显示布局
     */
    private void showUIAtRefreshDown() {
        mPostPBar.setVisibility(View.INVISIBLE);
        mPostContentText.setVisibility(View.VISIBLE);
        mPostCardsLLayout.setVisibility(View.VISIBLE);
    }
}
