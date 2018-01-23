package com.lfdb.zuptecnico;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.InventoryItemImage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by igorlira on 5/7/14.
 */
public class FullScreenImageActivity extends AppCompatActivity
    implements ViewPager.OnPageChangeListener {
  ImageFragment[] fragments;
  private static int TAP_INTERVAL = 300;
  private TapDetector tapDetector;

  public static class ImageFragment extends Fragment {
    InventoryItemImage image;
    PhotoViewAttacher attacher;
    boolean loadOnCreate = false;
    boolean isLoading = false;

    public ImageFragment setImage(InventoryItemImage image) {
      this.image = image;
      return this;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_fullscreen_image, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      if (loadOnCreate) load(true);
    }

    public void load(boolean bypassLoadCheck) {
      if (!bypassLoadCheck && isLoading) return;

      isLoading = true;
      if (getView() == null) {
        loadOnCreate = true;
        return;
      }
      final ImageView imgDisplay = (ImageView) getView().findViewById(R.id.imgDisplay);

      if (image.content != null) {
        Bitmap bitmap = BitmapFactory.decodeFile(image.content);
        if (bitmap != null) {
          imgDisplay.setImageBitmap(bitmap);
          imgDisplay.setVisibility(View.VISIBLE);
          attacher = new PhotoViewAttacher(imgDisplay);
        }
      } else {
        Picasso.with(getActivity()).load(image.versions.high).into(imgDisplay, new Callback() {
          @Override public void onSuccess() {
            imgDisplay.setVisibility(View.VISIBLE);
            attacher = new PhotoViewAttacher(imgDisplay);
          }

          @Override public void onError() {

          }
        });
      }
    }
  }

  class ImagePagerAdapter extends FragmentStatePagerAdapter {
    public ImagePagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
      super(fragmentManager);
    }

    @Override public int getCount() {
      return fragments.length;
    }

    @Override public Fragment getItem(int position) {
      return fragments[position];
    }
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

  }

  @Override public void onPageSelected(int position) {
    fragments[position].load(false);
  }

  @Override public void onPageScrollStateChanged(int state) {

  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_fullscreen_view);

    Zup.getInstance().initStorage(getApplicationContext());

    ViewPager pager = (ViewPager) findViewById(R.id.pager);
    ImagePagerAdapter adapter = new ImagePagerAdapter(getSupportFragmentManager());

    Parcelable[] images = getIntent().getParcelableArrayExtra("images");
    InventoryItemImage image = getIntent().getParcelableExtra("image");

    int index = -1;
    fragments = new ImageFragment[images.length];
    for (int i = 0; i < images.length; i++) {
      InventoryItemImage img = (InventoryItemImage) images[i];

      if (image.equals(img)) index = i;

      fragments[i] = new ImageFragment().setImage(img);
    }

    pager.setAdapter(adapter);
    pager.addOnPageChangeListener(this);
    pager.setCurrentItem(index, false);

    if (index >= 0 && index < fragments.length) fragments[index].load(false);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) showHideActionBar();

    return true;
  }

  void showHideActionBar() {
    final View container = findViewById(R.id.back_container);
    AlphaAnimation animation;
    if (container.getVisibility() == View.VISIBLE) {
      animation = new AlphaAnimation(1, 0);
      animation.setDuration(350);
      animation.setAnimationListener(new Animation.AnimationListener() {
        @Override public void onAnimationStart(Animation animation) {
          container.setVisibility(View.GONE);
        }

        @Override public void onAnimationEnd(Animation animation) {

        }

        @Override public void onAnimationRepeat(Animation animation) {

        }
      });
    } else {
      animation = new AlphaAnimation(1, 0);
      animation.setDuration(10);
      container.startAnimation(animation);

      container.setVisibility(View.VISIBLE);
      animation = new AlphaAnimation(0, 1);
      animation.setDuration(350);
    }

    container.startAnimation(animation);
  }

  public void back(View view) {
    finish();
  }

  @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    if (tapDetector != null && ev.getAction() != MotionEvent.ACTION_UP) {
      tapDetector.cancel(true);
      tapDetector = null;
    } else if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      tapDetector = new TapDetector();
      tapDetector.execute();
    }

    return super.dispatchTouchEvent(ev);
  }

  class TapDetector extends AsyncTask<Void, Void, Boolean> {
    @Override protected Boolean doInBackground(Void... voids) {
      try {
        Thread.sleep(TAP_INTERVAL);
        return true;
      } catch (InterruptedException e) {

      }

      return false;
    }

    @Override protected void onPostExecute(Boolean aBoolean) {
      if (aBoolean) showHideActionBar();

      tapDetector = null;
    }
  }
}