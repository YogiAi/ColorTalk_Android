package navyblue.top.colortalk.ui.quote;

import android.os.Bundle;

import navyblue.top.colortalk.R;
import navyblue.top.colortalk.ui.base.DrawerActivity;

/**
 * Simple wrapper for {@link ArticleDetailFragment}
 * This wrapper is only used in single pan mode (= on smartphones)
 * Created by Andreas Schrade on 14.12.2015.
 */
public class ArticleDetailActivity extends DrawerActivity {

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show the Up button in the action bar.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ArticleDetailFragment fragment =  ArticleDetailFragment.newInstance(getIntent().getStringExtra(ArticleDetailFragment.ARG_ITEM_ID));
        getSupportFragmentManager().beginTransaction().replace(R.id.article_detail_container, fragment).commit();
    }

}
