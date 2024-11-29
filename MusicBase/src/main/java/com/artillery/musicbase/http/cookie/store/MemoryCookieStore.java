package com.artillery.musicbase.http.cookie.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class MemoryCookieStore implements CookieStore {

    private final HashMap<String, List<Cookie>> memoryCookies = new HashMap<>();

    @Override
    public synchronized void saveCookie(HttpUrl url, List<Cookie> cookies) {
        List<Cookie> oldCookies = memoryCookies.get(url.host());
        List<Cookie> needRemove = new ArrayList<>();
        if (oldCookies == null) {
            return;
        }
        for (Cookie newCookie : cookies) {
            for (Cookie oldCookie : oldCookies) {
                if (newCookie.name().equals(oldCookie.name())) {
                    needRemove.add(oldCookie);
                }
            }
        }
        oldCookies.removeAll(needRemove);
        oldCookies.addAll(cookies);
    }

    @Override
    public synchronized void saveCookie(HttpUrl url, Cookie cookie) {
        List<Cookie> cookies = memoryCookies.get(url.host());
        List<Cookie> needRemove = new ArrayList<>();
        if (cookies == null) {
            return;
        }
        for (Cookie item : cookies) {
            if (cookie.name().equals(item.name())) {
                needRemove.add(item);
            }
        }
        cookies.removeAll(needRemove);
        cookies.add(cookie);
    }

    @Override
    public synchronized List<Cookie> loadCookie(HttpUrl url) {
        return memoryCookies.computeIfAbsent(url.host(), k -> new ArrayList<>());
    }

    @Override
    public synchronized List<Cookie> getAllCookie() {
        List<Cookie> cookies = new ArrayList<>();
        Set<String> httpUrls = memoryCookies.keySet();
        for (String url : httpUrls) {
            cookies.addAll(memoryCookies.get(url));
        }
        return cookies;
    }

    @Override
    public List<Cookie> getCookie(HttpUrl url) {
        List<Cookie> cookies = new ArrayList<>();
        List<Cookie> urlCookies = memoryCookies.get(url.host());
        if (urlCookies != null) cookies.addAll(urlCookies);
        return cookies;
    }

    @Override
    public synchronized boolean removeCookie(HttpUrl url, Cookie cookie) {
        List<Cookie> cookies = memoryCookies.get(url.host());
        if (cookies == null) {
            return false;
        }
        return cookie != null && cookies.remove(cookie);
    }

    @Override
    public synchronized boolean removeCookie(HttpUrl url) {
        return memoryCookies.remove(url.host()) != null;
    }

    @Override
    public synchronized boolean removeAllCookie() {
        memoryCookies.clear();
        return true;
    }
}
