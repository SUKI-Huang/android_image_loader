package com.holiestar.imageloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.IntRange;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.L;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tony1 on 12/30/2016.
 */

/**
 * Created by tony1 on 12/30/2016.
 */

public class ImageLoaderPro {
    private static String TAG = "ImageLoaderPro";
    private static Context context;
    private static DisplayImageOptions DISPLAY_IMAGE_OPTIONS;
    private static ImageLoader IMAGE_LOADER;
    private static List<String> DISPLAYED_IMAGES = Collections.synchronizedList(new LinkedList<String>());
    private static final String PATH_ASSETS = "assets://";
    private static final String PATH_DRAWABLE = "drawable://";
    private static final String PATH_FILE = "file://";
    private static final Options OPTION_NORMAL = new Options();
    private static final Options OPTION_BLUR = new Options().setBlur(25);

    private static final ImageLoaderProListener IMAGE_LOADER_PRO_LISTENER_NORMAL = new ImageLoaderProListener(0, false, 0, true, 500);
    private static final ImageLoaderProListener IMAGE_LOADER_PRO_LISTENER_BLUR = new ImageLoaderProListener(0, true, 25, true, 500);

    private ImageLoaderPro() {
    }

    public static void initialize(Context _context) {
        context = _context;
        IMAGE_LOADER = ImageLoader.getInstance();
        DISPLAY_IMAGE_OPTIONS = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new SimpleBitmapDisplayer())
                .build();
        if (!BuildConfig.DEBUG) {
            L.writeLogs(false);
        }
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(DISPLAY_IMAGE_OPTIONS).build();
        IMAGE_LOADER.init(config);
    }

    public static ImageLoader getImageLoader() {
        return IMAGE_LOADER;
    }

    public static final String getAssetsUri(String assetsPath) {
        return PATH_ASSETS + assetsPath;
    }

    public static final String getDrawableUri(int resourceId) {
        return PATH_DRAWABLE + resourceId;
    }

    public static final String getFileUri(String filePath) {
        return PATH_FILE + filePath;
    }

    public static final void deleteDiskCache(String imageUri){
        if(IMAGE_LOADER==null){
            return;
        }
        DiskCacheUtils.removeFromCache(imageUri,IMAGE_LOADER.getDiskCache());
    }

    public static final void deleteMemoryCache(String imageUri){
        if(IMAGE_LOADER==null){
            return;
        }
        MemoryCacheUtils.removeFromCache(imageUri, IMAGE_LOADER.getMemoryCache());
    }

    public static final void deleteCache(String imageUri){
        if(IMAGE_LOADER==null){
            return;
        }
        MemoryCacheUtils.removeFromCache(imageUri, IMAGE_LOADER.getMemoryCache());
        DiskCacheUtils.removeFromCache(imageUri,IMAGE_LOADER.getDiskCache());
    }

    public static void load(ImageView iv, String imageUri) {
        load(iv, imageUri, OPTION_NORMAL.getDefaultUri(), OPTION_NORMAL.getCacheExpiredMillisecond(), OPTION_NORMAL.isEnableBlur(), OPTION_NORMAL.getBlurFactor(), OPTION_NORMAL.isEnableFade(), OPTION_NORMAL.getFadeDuration(), IMAGE_LOADER_PRO_LISTENER_NORMAL);
    }

    public static void loadBlur(ImageView iv, String imageUri) {
        load(iv, imageUri, OPTION_BLUR.getDefaultUri(), OPTION_BLUR.getCacheExpiredMillisecond(), OPTION_BLUR.isEnableBlur(), OPTION_BLUR.getBlurFactor(), OPTION_BLUR.isEnableFade(), OPTION_BLUR.getFadeDuration(), IMAGE_LOADER_PRO_LISTENER_BLUR);
    }

    public static void loadBlur(ImageView iv, String imageUri, @IntRange(from = 0, to = 25) int blurFactor) {
        load(iv, imageUri, null, 0, false, 0, false, 0, ImageLoaderProListener.clone(IMAGE_LOADER_PRO_LISTENER_BLUR).setBlurFactor(blurFactor));
    }

    public static void load(ImageView iv, String imageUri, Options options) {
        if (options != null) {
            load(iv, imageUri, options.getDefaultUri(), options.getCacheExpiredMillisecond(), options.isEnableBlur(), options.getBlurFactor(), options.isEnableFade(), options.getFadeDuration(), null);
        } else {
            load(iv, imageUri);
        }
    }

    private static void load(final ImageView iv, final String imageUri, final String defaultUri, final long cacheExpiredDuration, final boolean enableBlur, final int blurFactor, final boolean enableFade, final int fadeDuration, ImageLoaderProListener imageLoaderProListener) {
        Log.i(TAG, "load");
        if (imageUri == null) {
            return;
        }
        if (imageLoaderProListener != null) {
            IMAGE_LOADER.displayImage(imageUri, iv, imageLoaderProListener);
            return;
        }
        File fileCache = IMAGE_LOADER.getDiskCache().get(imageUri);
        boolean hasNetwork = isNetworkAvailable();
        boolean isFile = imageUri.indexOf("file://") == 0;
        boolean isDrawable = imageUri.indexOf("drawable://") == 0;
        boolean isAssets = imageUri.indexOf("assets://") == 0;
        boolean hasFileCache=isExist(fileCache);
        if (hasNetwork && !isFile && !isDrawable && !isAssets) {
            if (hasFileCache) {
                if (isFileExpired(fileCache, cacheExpiredDuration)) {
                    DiskCacheUtils.removeFromCache(imageUri,IMAGE_LOADER.getDiskCache());
                }else{
                    load(iv, getFileUri(fileCache.getAbsolutePath()), defaultUri, cacheExpiredDuration, enableBlur, blurFactor, enableFade, fadeDuration, imageLoaderProListener);
                    return;
                }
            }
        }

        IMAGE_LOADER.displayImage(imageUri, iv, new ImageLoaderProListener(cacheExpiredDuration, enableBlur, blurFactor, enableFade, fadeDuration) {
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (defaultUri == null) {
                    return;
                }
                //Default
                IMAGE_LOADER.displayImage(defaultUri, iv, new ImageLoaderProListener(0, enableBlur, blurFactor, enableFade, fadeDuration) {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                    }
                });
            }
        });

    }

    private static boolean isExist(File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            if (file.length() == 0) {
                file.delete();
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    private static boolean isFileExpired(File file, long expiredDuration) {
        if (file == null) {
            return true;
        }
        if (file.exists()) {
            long lastModified = file.lastModified();
            if (System.currentTimeMillis() - lastModified > expiredDuration) {
                return true;
            } else {
                return false;
            }

        } else {
            return true;
        }

    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null) && activeNetworkInfo.isConnected();
    }

    public static void clearCache() {
        if (IMAGE_LOADER != null) {
            IMAGE_LOADER.clearDiskCache();
            IMAGE_LOADER.clearMemoryCache();
        }
    }

    private static class ImageLoaderProListener extends SimpleImageLoadingListener {
        private long cacheExpiredMillisecond;
        private boolean enableBlur;
        private int blurFactor;
        private boolean enableFade;
        private int fadeDuration;

        public long getCacheExpiredMillisecond() {
            return cacheExpiredMillisecond;
        }


        public boolean isEnableBlur() {
            return enableBlur;
        }

        public ImageLoaderProListener setEnableBlur(boolean enableBlur) {
            this.enableBlur = enableBlur;
            return this;
        }

        public int getBlurFactor() {
            return blurFactor;
        }

        public ImageLoaderProListener setBlurFactor(int blurFactor) {
            this.blurFactor = blurFactor;
            return this;
        }

        public boolean isEnableFade() {
            return enableFade;
        }

        public ImageLoaderProListener setEnableFade(boolean enableFade) {
            this.enableFade = enableFade;
            return this;
        }

        public int getFadeDuration() {
            return fadeDuration;
        }

        public ImageLoaderProListener setFadeDuration(int fadeDuration) {
            this.fadeDuration = fadeDuration;
            return this;
        }

        public ImageLoaderProListener(long cacheExpiredMillisecond, boolean enableBlur, int blurFactor, boolean enableFade, int fadeDuration) {
            this.cacheExpiredMillisecond = cacheExpiredMillisecond;
            this.enableBlur = enableBlur;
            this.blurFactor = blurFactor;
            this.enableFade = enableFade;
            this.fadeDuration = fadeDuration;
            if(fadeDuration!=0){
                this.enableFade=true;
            }
        }


        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {
            super.onLoadingComplete(imageUri, view, bitmap);

            boolean firstDisplay = !DISPLAYED_IMAGES.contains(imageUri);
            if (firstDisplay) {
                DISPLAYED_IMAGES.add(imageUri);
            }

            //process blur
            if (enableBlur && blurFactor != 0) {
                ((ImageView) view).setImageBitmap(Blur.fastblur(context, bitmap, blurFactor));
            }
            //process fade
            if (enableFade) {
                FadeInBitmapDisplayer.animate(view, fadeDuration);
            }
        }

        public static ImageLoaderProListener clone(ImageLoaderProListener imageLoaderProListener) {
            return new ImageLoaderProListener(
                    imageLoaderProListener.getCacheExpiredMillisecond(),
                    imageLoaderProListener.isEnableBlur(),
                    imageLoaderProListener.getBlurFactor(),
                    imageLoaderProListener.isEnableFade(),
                    imageLoaderProListener.getFadeDuration());
        }
    }

    public static class Options {
        private String defaultUri = null;
        private long cacheExpiredMillisecond = 0;
        private boolean enableBlur = false;
        private int blurFactor = 0;
        private boolean enableFade = true;
        private int fadeDuration = 500;

        public String getDefaultUri() {
            return defaultUri;
        }

        public Options setDefaultUri(String defaultUri) {
            this.defaultUri = defaultUri;
            return this;
        }


        public long getCacheExpiredMillisecond() {
            return cacheExpiredMillisecond;
        }

        @Deprecated
        public Options setCacheExpiredDuration(long cacheExpiredMillisecond) {
            this.cacheExpiredMillisecond = cacheExpiredMillisecond;
            return this;
        }

        public Options setCacheExpiredMillisecond(long cacheExpiredMillisecond) {
            this.cacheExpiredMillisecond = cacheExpiredMillisecond;
            return this;
        }

        public Options setCacheExpiredSeconds(long seconds) {
            this.cacheExpiredMillisecond = seconds*1000l;
            return this;
        }

        public Options setCacheExpiredMins(long mins) {
            this.cacheExpiredMillisecond = mins*60000l;
            return this;
        }

        public Options setCacheExpiredDays(long days) {
            this.cacheExpiredMillisecond = days*86400000l;
            return this;
        }

        public Options setCacheExpiredOneDay() {
            this.cacheExpiredMillisecond = 86400000l;
            return this;
        }

        public Options setCacheExpiredWeeks(long weeks) {
            this.cacheExpiredMillisecond = weeks*604800000l;
            return this;
        }

        public Options setCacheExpiredOneWeeks() {
            this.cacheExpiredMillisecond = 604800000l;
            return this;
        }

        public Options setCacheExpiredForever() {
            this.cacheExpiredMillisecond = Long.MAX_VALUE;
            return this;
        }

        public boolean isEnableBlur() {
            return enableBlur;
        }

        public Options setBlur(int blurFactor) {
            this.enableBlur = true;
            this.blurFactor = blurFactor;
            return this;
        }

        public int getBlurFactor() {
            return blurFactor;
        }

        public boolean isEnableFade() {
            return enableFade;
        }

        public Options setFade(int fadeDuration) {
            this.enableFade = true;
            this.fadeDuration = fadeDuration;
            return this;
        }

        public int getFadeDuration() {
            return fadeDuration;
        }
    }

    public static class Blur {

        private static final String TAG = "Blur";

        @SuppressLint("NewApi")
        public static Bitmap fastblur(Context context, Bitmap sentBitmap, int radius) {

            if (Build.VERSION.SDK_INT > 16) {
                Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

                final RenderScript rs = RenderScript.create(context);
                final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
                        Allocation.USAGE_SCRIPT);
                final Allocation output = Allocation.createTyped(rs, input.getType());
                final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
                script.setRadius(radius /* e.g. 3.f */);
                script.setInput(input);
                script.forEach(output);
                output.copyTo(bitmap);
                return bitmap;
            }

            // Stack Blur v1.0 from
            // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
            //
            // Java Author: Mario Klingemann <mario at quasimondo.com>
            // http://incubator.quasimondo.com
            // created Feburary 29, 2004
            // Android port : Yahel Bouaziz <yahel at kayenko.com>
            // http://www.kayenko.com
            // ported april 5th, 2012

            // This is a compromise between Gaussian Blur and Box blur
            // It creates much better looking blurs than Box Blur, but is
            // 7x faster than my Gaussian Blur implementation.
            //
            // I called it Stack Blur because this describes best how this
            // filter works internally: it creates a kind of moving stack
            // of colors whilst scanning through the image. Thereby it
            // just has to add one new block of color to the right side
            // of the stack and remove the leftmost color. The remaining
            // colors on the topmost layer of the stack are either added on
            // or reduced by one, depending on if they are on the right or
            // on the left side of the stack.
            //
            // If you are using this algorithm in your code please add
            // the following line:
            //
            // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

            Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

            if (radius < 1) {
                return (null);
            }

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int[] pix = new int[w * h];
            Log.e("pix", w + " " + h + " " + pix.length);
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int r[] = new int[wh];
            int g[] = new int[wh];
            int b[] = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int vmin[] = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int dv[] = new int[256 * divsum];
            for (i = 0; i < 256 * divsum; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;

            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {

                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }
                yw += w;
            }
            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                    pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi += w;
                }
            }

            Log.e("pix", w + " " + h + " " + pix.length);
            bitmap.setPixels(pix, 0, w, 0, 0, w, h);
            return (bitmap);
        }

    }
}

