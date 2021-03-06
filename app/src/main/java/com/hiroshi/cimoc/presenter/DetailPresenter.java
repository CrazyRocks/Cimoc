package com.hiroshi.cimoc.presenter;

import android.util.Log;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class DetailPresenter extends BasePresenter<DetailView> {

    private ComicManager mComicManager;
    private Comic mComic;
    private int source;

    public DetailPresenter(int source) {
        this.source = source;
        this.mComicManager = ComicManager.getInstance();
    }

    @Override
    public void attachView(DetailView mBaseView) {
        super.attachView(mBaseView);
        initSubscription();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.COMIC_CHAPTER_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                String last = (String) rxEvent.getData();
                int page = (int) rxEvent.getData(1);
                mComic.setHistory(System.currentTimeMillis());
                mComic.setLast(last);
                mComic.setPage(page);
                if (mComic.getId() == null) {
                    long id = mComicManager.insert(mComic);
                    mComic.setId(id);
                } else {
                    mComicManager.update(mComic);
                }
                RxBus.getInstance().post(new RxEvent(RxEvent.HISTORY_COMIC, new MiniComic(mComic)));
                mBaseView.onChapterChange(last);
            }
        });
        addSubscription(RxEvent.COMIC_PAGE_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mComic.setPage((Integer) rxEvent.getData());
                if (mComic.getId() != null) {
                    mComicManager.update(mComic);
                }
            }
        });
    }

    public void load(long id, final String cid) {
        Observable<Comic> observable =
                id == -1 ? mComicManager.loadInRx(source, cid) : mComicManager.loadInRx(id);
        observable.flatMap(new Func1<Comic, Observable<List<Chapter>>>() {
            @Override
            public Observable<List<Chapter>> call(Comic comic) {
                if (comic == null) {
                    comic = new Comic(source, cid);
                } if (comic.getFavorite() != null && comic.getHighlight()) {
                    comic.setHighlight(false);
                    comic.setFavorite(System.currentTimeMillis());
                }
                mComic = comic;
                return Manga.info(SourceManager.getParser(source), comic);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Chapter>>() {
                    @Override
                    public void call(List<Chapter> list) {
                        mBaseView.showLayout();
                        mBaseView.onComicLoad(mComic);
                        mBaseView.onChapterLoad(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.showLayout();
                        if (throwable instanceof Manga.NetworkErrorException) {
                            mBaseView.onNetworkError();
                        } else {
                            mBaseView.onComicLoad(mComic);
                            mBaseView.onParseError();
                        }
                    }
                });
    }

    public void updateComic() {
        if (mComic.getId() != null) {
            mComicManager.update(mComic);
        }
    }

    public Comic getComic() {
        return mComic;
    }

    public boolean isComicFavorite() {
        return mComic.getFavorite() != null;
    }

    public void favoriteComic() {
        mComic.setFavorite(System.currentTimeMillis());
        if (mComic.getId() == null) {
            long id = mComicManager.insert(mComic);
            mComic.setId(id);
        }
        RxBus.getInstance().post(new RxEvent(RxEvent.FAVORITE_COMIC, new MiniComic(mComic)));
    }

    public void unfavoriteComic() {
        long id = mComic.getId();
        mComic.setFavorite(null);
        if (mComic.getHistory() == null) {
            mComicManager.deleteByKey(id);
            mComic.setId(null);
        }
        RxBus.getInstance().post(new RxEvent(RxEvent.UN_FAVORITE_COMIC, id));
    }

}
