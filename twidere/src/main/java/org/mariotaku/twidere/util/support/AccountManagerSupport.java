package org.mariotaku.twidere.util.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by mariotaku on 2016/12/2.
 */

public class AccountManagerSupport {
    public static AccountManagerFuture<Bundle> removeAccount(AccountManager am, Account account,
                                                             Activity activity,
                                                             final AccountManagerCallback<Bundle> callback,
                                                             Handler handler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return AccountManagerSupportL.removeAccount(am, account, activity, callback, handler);
        }
        //noinspection deprecation
        final AccountManagerFuture<Boolean> future = am.removeAccount(account, new AccountManagerCallback<Boolean>() {
            @Override
            public void run(AccountManagerFuture<Boolean> future) {
                callback.run(new BooleanToBundleAccountManagerFuture(future));
            }
        }, handler);
        return new BooleanToBundleAccountManagerFuture(future);
    }

    private static class AccountManagerSupportL {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
        static AccountManagerFuture<Bundle> removeAccount(AccountManager am, Account account,
                                                          Activity activity,
                                                          AccountManagerCallback<Bundle> callback,
                                                          Handler handler) {
            return am.removeAccount(account, activity, callback, handler);
        }
    }

    private static class BooleanToBundleAccountManagerFuture implements AccountManagerFuture<Bundle> {

        private final AccountManagerFuture<Boolean> future;

        BooleanToBundleAccountManagerFuture(AccountManagerFuture<Boolean> future) {
            this.future = future;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public Bundle getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, future.getResult());
            return result;
        }

        @Override
        public Bundle getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, future.getResult(timeout, unit));
            return result;
        }
    }
}
