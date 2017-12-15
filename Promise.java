/*
 * Copyright (c) 2017 CRAWLINK NETWORKS PVT. LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 

package com.crawlink;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;

/**
 * The Promise object represents the eventual completion (or failure)
 * of an asynchronous operation, and its resulting value.
 * <p>
 * A Promise is a proxy for a value not necessarily known when
 * the promise is created. It allows you to associate handlers
 * with an asynchronous action's eventual success value or failure reason.
 * This lets asynchronous methods return values like synchronous methods:
 * instead of immediately returning the final value,
 * the asynchronous method returns a promise to supply the value
 * at some point in the future.
 * <p>
 * For more information on Javascript Promise
 * please visit the official Mozilla Promise documentation
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise">
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise</a>
 * <p>
 * This Android Promise Library slightly different than the native Javascript Promise.
 * This promise object has two imprtant method i.e. `resolve()` and `reject()`,
 * whenevey you done withe your process just call resolve or reject
 * function based on your state.
 * The resultant value will be automaticall passed as argument to the
 * followng `then()` or `error()` function.
 * <p>
 * You can write `n` numbers of `then()` chain.
 * <p>
 * It supports above JAVA 1.8
 */

public class Promise {

    private static final String TAG = "Promise";
    private Handler handler;
    private OnSuccessListener onSuccessListener;
    private OnErrorListener onErrorListener;
    private Promise child;
    private boolean isResolved;
    private Object resolvedObject;
    private Object tag;


    public Promise() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            this.handler = new Handler();
        }
    }

    public static Promise all(ArrayList<Promise> list) {
        Promise p = new Promise();
        if (list == null || list.size() <= 0) {
            Log.w(TAG, "Promise list should not be empty!");
            p.resolve(new ArrayList<Object>());
            return p;
        }

        if (list != null && list.size() > 0) {
            new Thread(new Runnable() {
                int completedCount = 0;
                ArrayList<Object> result = new ArrayList<Object>(list.size());

                @Override
                public void run() {
                    for (int i = 0; i < list.size(); i++) {
                        Promise promise = list.get(i);
                        promise.setTag(i);
                        promise.then(res -> {
                            result.set((int) promise.getTag(), res);
                            completed();
                            return true;
                        }).error(err -> {
                            completed();
                        });
                    }
                }

                private void completed() {
                    completedCount++;
                    if (completedCount == list.size()) {
                        p.resolve(result);
                    }
                }

            }).start();
        } else {
            Log.w(TAG, "Promises should not be empty!");
            p.resolve(new ArrayList<Object>());
        }

        return p;
    }

    public static Promise map(ArrayList<Object> list, OnSuccessListener listener) {
        Promise p = new Promise();
        Handler handler = null;
        if (list == null || listener == null || list.size() <= 0) {
            Log.e(TAG, "Arguments should not be NULL!");
            return null;
        }
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            handler = new Handler();
        }

        final Handler finalHandler = handler;
        new Thread(new Runnable() {
            int completedCount = 0;
            ArrayList<Object> result = new ArrayList<>(list.size());

            @Override
            public void run() {
                if (list != null && list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        if (finalHandler != null) {
                            int finalI = i;
                            finalHandler.post(() -> {
                                handleSuccess(finalI, list.get(finalI));
                            });
                        } else {
                            handleSuccess(i, list.get(i));
                        }
                    }
                } else {
                    p.resolve(result);
                }
            }

            private void handleSuccess(int index, Object object) {
                Object res = listener.onSuccess(object);
                result.add(index, res);
                if (res instanceof Promise) {
                    Promise pro = (Promise) res;
                    pro.setTag(index);
                    pro.then(r -> {
                        result.set((int) pro.getTag(), r);
                        completed();
                        return true;
                    }).error(err -> completed());
                } else {
                    completed();
                }

            }

            private void completed() {
                completedCount++;
                if (completedCount == list.size()) {
                    p.resolve(result);
                }
            }

        }).start();

        return p;
    }

    /**
     * Call this function with your resultant value, it will be available
     * in following `then()` function call.
     *
     * @param object your resultant value (any type of data you can pass as argument
     *               e.g. int, String, List, Map, any Java object)
     * @return This will return the resultant value you passed in the function call
     */
    public Object resolve(Object object) {
        isResolved = true;
        resolvedObject = object;
        if (handler != null) {
            handler.post(() -> handleSuccess(child, object));
        } else {
            handleSuccess(child, object);
        }
        return object;
    }

    /**
     * Call this function with your error value, it will be available
     * in following `error()` function call.
     *
     * @param object your error value (any type of data you can pass as argument
     *               e.g. int, String, List, Map, any Java object)
     * @return This will return the error value you passed in the function call
     */
    public Object reject(Object object) {
        if (handler != null) {
            handler.post(() -> {
                handleError(object);
            });
        } else {
            handleError(object);
        }

        return object;
    }

    /**
     * After executing asyncronous function the result will be available in the success listener
     * as argument.
     *
     * @param listener OnSuccessListener
     * @return It returns a promise for satisfying next chain call.
     */
    public Promise then(OnSuccessListener listener) {
        onSuccessListener = listener;
        child = new Promise();
        return child;
    }

    /**
     * This function must call at the end of the `then()` cain, any `reject()` occurs in
     * previous execution this function will be called.
     *
     * @param listener
     */
    public void error(OnErrorListener listener) {
        onErrorListener = listener;
    }

    private void handleSuccess(Promise child, Object object) {
        if (onSuccessListener != null) {
            Object res = onSuccessListener.onSuccess(object);
            if (res != null) {
                if (res instanceof Promise) {
                    if (child != null) {
                        Promise p = (Promise) res;
                        p.onSuccessListener = child.onSuccessListener;
                        p.onErrorListener = child.onErrorListener;
                        p.child = child.child;
                        child = p;
                    }
                } else if (child != null) {
                    child.resolve(res);
                }
            } else {
                if (child != null) {
                    child.resolve(res);
                }
            }
        }
    }

    private void handleError(Object object) {
        if (onErrorListener != null) {
            onErrorListener.onError(object);
        } else if (child != null) {
            child.reject(object);
        }
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }


    public interface OnSuccessListener {
        Object onSuccess(Object object);
    }

    public interface OnErrorListener {
        void onError(Object object);
    }

}
