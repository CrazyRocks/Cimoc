package com.hiroshi.cimoc.core.manager;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.ComicDao.Properties;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;

/**
 * Created by Hiroshi on 2016/7/9.
 */
public class ComicManager {

    private static ComicManager mComicManager;

    private ComicDao mComicDao;

    private ComicManager() {
        mComicDao = CimocApplication.getDaoSession().getComicDao();
    }

    public <T> Observable<T> callInTx(Callable<T> callable) {
        return mComicDao.getSession()
                .rxTx()
                .call(callable);
    }

    public Observable<List<Comic>> listSource(int source) {
        return mComicDao.queryBuilder()
                .where(Properties.Source.eq(source))
                .rx()
                .list();
    }

    public Observable<List<Comic>> listFavorite() {
        return mComicDao.queryBuilder()
                .where(Properties.Favorite.isNotNull())
                .orderDesc(Properties.Highlight, Properties.Favorite)
                .rx()
                .list();
    }

    public Observable<List<Comic>> listHistory() {
        return mComicDao.queryBuilder()
                .where(Properties.History.isNotNull())
                .orderDesc(Properties.History)
                .rx()
                .list();
    }

    public Observable<Comic> loadInRx(int source, String cid) {
        return mComicDao.queryBuilder()
                .where(Properties.Source.eq(source), Properties.Cid.eq(cid))
                .rx()
                .unique();
    }

    public Observable<Comic> loadInRx(long id) {
        return mComicDao.rx().load(id);
    }

    public Comic load(int source, String cid) {
        return mComicDao.queryBuilder()
                .where(Properties.Source.eq(source), Properties.Cid.eq(cid))
                .unique();
    }

    public void update(Comic comic) {
        mComicDao.update(comic);
    }

    public void delete(Comic comic) {
        mComicDao.delete(comic);
    }

    public void deleteByKey(long id) {
        mComicDao.deleteByKey(id);
    }

    public void deleteInTx(List<Comic> list) {
        mComicDao.deleteInTx(list);
    }

    public long insert(Comic comic) {
        return mComicDao.insert(comic);
    }

    public static ComicManager getInstance() {
        if (mComicManager == null) {
            mComicManager = new ComicManager();
        }
        return mComicManager;
    }

}
