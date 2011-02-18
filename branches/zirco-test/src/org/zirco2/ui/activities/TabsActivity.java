package org.zirco2.ui.activities;

import org.zirco2.ImageAdapter;
import org.zirco2.R;
import org.zirco2.TabsController;
import org.zirco2.ui.components.CustomWebView;
import org.zirco2.utils.ApplicationUtils;
import org.zirco2.utils.UrlUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class TabsActivity extends Activity {
	
	private Gallery mTabsGallery;
	private AutoCompleteTextView mUrl;
	private ImageButton mGo;
	private ImageButton mAddTab;
	private ImageButton mCloseTab;
		
	private CustomWebView mCurrentWebView;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
        setContentView(R.layout.tabs_activity);
        
        mUrl = (AutoCompleteTextView) findViewById(R.id.UrlText);
        
        mUrl.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				// Select all when focus gained.
                if (hasFocus) {
                	mUrl.setSelection(0, mUrl.getText().length());
                }
			}
		});
        
        mUrl.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					navigateToCurrentUrl();
					return true;
				}
				return false;
			}			
    	});
        
        mUrl.setCompoundDrawablePadding(5);
        
        mGo = (ImageButton) findViewById(R.id.GoBtn);
        
        mGo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				navigateToCurrentUrl();
			}
		});
        
        mTabsGallery = (Gallery) findViewById(R.id.TabsGallery);        

        mTabsGallery.setSpacing(5);
        mTabsGallery.setUnselectedAlpha(0.5f);
        
        mTabsGallery.setOnItemClickListener(new OnItemClickListener() {

        	@Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        		doFinish(position);
            }        	
        });
        
        mTabsGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				mCurrentWebView = TabsController.getInstance().getWebViewContainers().get(position).getWebView();
				
				String currentUrl = mCurrentWebView.getUrl();
				if (!currentUrl.equals(UrlUtils.URL_ABOUT_BLANK)) {
					mUrl.setText(currentUrl);
				} else {
					mUrl.setText(null);
				}
				mUrl.setCompoundDrawables(getNormalizedFavicon(),
						null,
						null,
						null);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub				
			}
		});               
        
        mAddTab = (ImageButton) findViewById(R.id.AddTabBtn);
        mAddTab.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				addTab();
			}
		});
        
        mCloseTab = (ImageButton) findViewById(R.id.CloseTabBtn);
        mCloseTab.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				removeTab();
			}
		});
        
        refreshTabsGallery(0);
        
        Bundle extras = getIntent().getExtras();
    	if (extras != null) {        	
    		mTabsGallery.setSelection(extras.getInt("CURRENT_VIEW_INDEX"));        	
        }
	}

	@Override
	public void onBackPressed() {
		doFinish(-1);
	}

	private void doFinish(int index) {
		if (index != -1) {
			Intent i = new Intent();
			i.putExtra("TAB_INDEX", index);
			setResult(RESULT_OK, i);
		}
		finish();
		overridePendingTransition(R.anim.browser_view_enter, R.anim.tab_view_exit);
	}
	
	private void hideKeyboard() {
    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(mUrl.getWindowToken(), 0);
	}
	
	private void navigateToCurrentUrl() {
		hideKeyboard();
		int selected = 	mTabsGallery.getSelectedItemPosition();
		TabsController.getInstance().getWebViewContainers().get(selected).getWebView().loadUrl(mUrl.getText().toString());
		doFinish(selected);
	}
	
	private void addTab() {
		int newIndex = mTabsGallery.getSelectedItemPosition() + 1;
		TabsController.getInstance().addTab(newIndex, UrlUtils.URL_ABOUT_BLANK);
		
		refreshTabsGallery(newIndex);
	}
	
	private void removeTab() {
		int removedIndex = mTabsGallery.getSelectedItemPosition();
		TabsController.getInstance().removeTab(removedIndex);
		
		removedIndex--;
		refreshTabsGallery(removedIndex);
	}
	
	private void refreshTabsGallery(int indexToShow) {		
		mTabsGallery.setAdapter(new ImageAdapter(this));
		
		if (mTabsGallery.getCount() > 1) {
			mCloseTab.setVisibility(View.VISIBLE);
		} else {
			mCloseTab.setVisibility(View.GONE);
		}
		
		if (indexToShow > 0) {
			mTabsGallery.setSelection(indexToShow);
		}
	}
	
	/**
	 * Get a Drawable of the current favicon, with its size normalized relative to current screen density.
	 * @return The normalized favicon.
	 */
	private BitmapDrawable getNormalizedFavicon() {		
		BitmapDrawable favIcon = new BitmapDrawable(mCurrentWebView.getFavicon());
		
		if (mCurrentWebView.getFavicon() != null) {
			int favIconSize = ApplicationUtils.getFaviconSize(this);
			favIcon.setBounds(0, 0, favIconSize, favIconSize);
		}
		
		return favIcon;
	}	
}