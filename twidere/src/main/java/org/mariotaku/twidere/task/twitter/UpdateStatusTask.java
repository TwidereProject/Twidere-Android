package org.mariotaku.twidere.task.twitter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Pair;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.fanfou.model.PhotoStatusUpdate;
import org.mariotaku.microblog.library.twitter.TwitterUpload;
import org.mariotaku.microblog.library.twitter.model.ErrorInfo;
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.StatusUpdate;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.restfu.http.mime.FileBody;
import org.mariotaku.restfu.http.mime.SimpleBody;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Draft;
import org.mariotaku.twidere.model.DraftValuesCreator;
import org.mariotaku.twidere.model.MediaUploadResult;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.StatusShortenResult;
import org.mariotaku.twidere.model.UploaderMediaItem;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtra;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.model.util.ParcelableLocationUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.preference.ServicePickerPreference;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.util.AbsServiceInterface;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.CollectionUtils;
import org.mariotaku.twidere.util.MediaUploaderInterface;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.StatusShortenerInterface;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.util.io.ContentLengthInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Created by mariotaku on 16/5/22.
 */
public class UpdateStatusTask extends AbstractTask<Pair<String, ParcelableStatusUpdate>,
        UpdateStatusTask.UpdateStatusResult, Context> implements Constants {

    private static final int BULK_SIZE = 256 * 1024;// 128 Kib

    final Context context;
    final StateCallback stateCallback;

    @Inject
    AsyncTwitterWrapper mTwitterWrapper;
    @Inject
    SharedPreferencesWrapper mPreferences;

    public UpdateStatusTask(Context context, StateCallback stateCallback) {
        GeneralComponentHelper.build(context).inject(this);
        this.context = context;
        this.stateCallback = stateCallback;
    }

    @Override
    protected UpdateStatusResult doLongOperation(Pair<String, ParcelableStatusUpdate> params) {
        final long draftId = saveDraft(params.first, params.second);
        mTwitterWrapper.addSendingDraftId(draftId);
        try {
            final UpdateStatusResult result = doUpdateStatus(params.second);
            deleteOrUpdateDraft(params.second, result, draftId);
            return result;
        } catch (UpdateStatusException e) {
            return new UpdateStatusResult(e);
        } finally {
            mTwitterWrapper.removeSendingDraftId(draftId);
        }
    }

    @Override
    protected void beforeExecute() {
        stateCallback.beforeExecute();
    }

    @Override
    protected void afterExecute(Context handler, UpdateStatusResult result) {
        stateCallback.afterExecute(handler, result);
    }

    @NonNull
    private UpdateStatusResult doUpdateStatus(ParcelableStatusUpdate update) throws UpdateStatusException {
        final TwidereApplication app = TwidereApplication.getInstance(context);
        final MediaUploaderInterface uploader = getMediaUploader(app);
        final StatusShortenerInterface shortener = getStatusShortener(app);

        final PendingStatusUpdate pendingUpdate = PendingStatusUpdate.from(update);


        uploadMedia(uploader, update, pendingUpdate);
        shortenStatus(shortener, update, pendingUpdate);

        final UpdateStatusResult result;
        try {
            result = requestUpdateStatus(update, pendingUpdate);
        } catch (IOException e) {
            return new UpdateStatusResult(new UpdateStatusException(e));
        }

        mediaUploadCallback(uploader, pendingUpdate, result);
        statusShortenCallback(shortener, pendingUpdate, result);
        return result;
    }

    private void deleteOrUpdateDraft(ParcelableStatusUpdate update, UpdateStatusResult result, long draftId) {
        final String where = Expression.equalsArgs(Drafts._ID).getSQL();
        final String[] whereArgs = {String.valueOf(draftId)};
        boolean hasError = false;
        List<UserKey> failedAccounts = new ArrayList<>();
        for (int i = 0; i < update.accounts.length; i++) {
            Exception exception = result.exceptions[i];
            if (exception != null && !isDuplicate(exception)) {
                hasError = true;
                failedAccounts.add(update.accounts[i].account_key);
            }
        }
        final ContentResolver cr = context.getContentResolver();
        if (hasError) {
            ContentValues values = new ContentValues();
            values.put(Drafts.ACCOUNT_KEYS, CollectionUtils.toString(failedAccounts, ',', false));
            cr.update(Drafts.CONTENT_URI, values, where, whereArgs);
            // TODO show error message
        } else {
            cr.delete(Drafts.CONTENT_URI, where, whereArgs);
        }
    }

    private void uploadMedia(@Nullable MediaUploaderInterface uploader,
                             @NonNull ParcelableStatusUpdate update,
                             @NonNull PendingStatusUpdate pendingUpdate) throws UploadException {
        stateCallback.onStartUploadingMedia();
        if (uploader == null) {
            uploadMediaWithDefaultProvider(update, pendingUpdate);
        } else {
            uploadMediaWithExtension(uploader, update, pendingUpdate);
        }
    }

    private void uploadMediaWithExtension(@NonNull MediaUploaderInterface uploader,
                                          @NonNull ParcelableStatusUpdate update,
                                          @NonNull PendingStatusUpdate pending) throws UploadException {
        final UploaderMediaItem[] media;
        try {
            media = UploaderMediaItem.getFromStatusUpdate(context, update);
        } catch (FileNotFoundException e) {
            throw new UploadException(e);
        }
        Map<UserKey, MediaUploadResult> sharedMedia = new HashMap<>();
        for (int i = 0; i < pending.length; i++) {
            ParcelableAccount account = update.accounts[i];
            // Skip upload if shared media found
            final UserKey accountKey = account.account_key;
            MediaUploadResult uploadResult = sharedMedia.get(accountKey);
            if (uploadResult == null) {
                uploadResult = uploader.upload(update, accountKey, media);
                if (uploadResult == null) {
                    // TODO error handling
                    continue;
                }
                pending.mediaUploadResults[i] = uploadResult;
                if (uploadResult.shared_owners != null) {
                    for (UserKey sharedOwner : uploadResult.shared_owners) {
                        sharedMedia.put(sharedOwner, uploadResult);
                    }
                }
            }
            // Override status text
            pending.overrideTexts[i] = Utils.getMediaUploadStatus(context,
                    uploadResult.media_uris, pending.overrideTexts[i]);
        }
    }

    private void shortenStatus(@Nullable StatusShortenerInterface shortener,
                               ParcelableStatusUpdate update,
                               PendingStatusUpdate pending) {
        if (shortener == null) return;
        stateCallback.onShorteningStatus();
        Map<UserKey, StatusShortenResult> sharedShortened = new HashMap<>();
        for (int i = 0; i < pending.length; i++) {
            ParcelableAccount account = update.accounts[i];
            // Skip upload if this shared media found
            final UserKey accountKey = account.account_key;
            StatusShortenResult shortenResult = sharedShortened.get(accountKey);
            if (shortenResult == null) {
                shortenResult = shortener.shorten(update, accountKey, pending.overrideTexts[i]);
                if (shortenResult == null) {
                    // TODO error handling
                    continue;
                }
                pending.statusShortenResults[i] = shortenResult;
                if (shortenResult.shared_owners != null) {
                    for (UserKey sharedOwner : shortenResult.shared_owners) {
                        sharedShortened.put(sharedOwner, shortenResult);
                    }
                }
            }
            // Override status text
            pending.overrideTexts[i] = shortenResult.shortened;
        }
    }

    @NonNull
    private UpdateStatusResult requestUpdateStatus(ParcelableStatusUpdate statusUpdate, PendingStatusUpdate pendingUpdate) throws IOException {

        stateCallback.onUpdatingStatus();

        UpdateStatusResult result = new UpdateStatusResult(new ParcelableStatus[pendingUpdate.length],
                new MicroBlogException[pendingUpdate.length]);

        for (int i = 0; i < pendingUpdate.length; i++) {
            final ParcelableAccount account = statusUpdate.accounts[i];
            MicroBlog microBlog = MicroBlogAPIFactory.getInstance(context, account.account_key, true);
            Body body = null;
            try {
                switch (ParcelableAccountUtils.getAccountType(account)) {
                    case ParcelableAccount.Type.FANFOU: {
                        // Call uploadPhoto if media present
                        if (!ArrayUtils.isEmpty(statusUpdate.media)) {
                            // Fanfou only allow one photo
                            if (statusUpdate.media.length > 1) {
                                result.exceptions[i] = new MicroBlogException(
                                        context.getString(R.string.error_too_many_photos_fanfou));
                                break;
                            }
                            body = getBodyFromMedia(context.getContentResolver(),
                                    Uri.parse(statusUpdate.media[0].uri), new ContentLengthInputStream.ReadListener() {
                                        @Override
                                        public void onRead(long length, long position) {
                                            stateCallback.onUploadingProgressChanged(-1, position, length);
                                        }
                                    });
                            PhotoStatusUpdate photoUpdate = new PhotoStatusUpdate(body,
                                    pendingUpdate.overrideTexts[i]);
                            final Status requestResult = microBlog.uploadPhoto(photoUpdate);

                            result.statuses[i] = ParcelableStatusUtils.fromStatus(requestResult,
                                    account.account_key, false);
                        } else {
                            final Status requestResult = twitterUpdateStatus(microBlog,
                                    statusUpdate, pendingUpdate, pendingUpdate.overrideTexts[i], i);

                            result.statuses[i] = ParcelableStatusUtils.fromStatus(requestResult,
                                    account.account_key, false);
                        }
                        break;
                    }
                    default: {
                        final Status requestResult = twitterUpdateStatus(microBlog, statusUpdate,
                                pendingUpdate, pendingUpdate.overrideTexts[i], i);

                        result.statuses[i] = ParcelableStatusUtils.fromStatus(requestResult,
                                account.account_key, false);
                        break;
                    }
                }
            } catch (MicroBlogException e) {
                result.exceptions[i] = e;
            } finally {
                Utils.closeSilently(body);
            }
        }
        return result;
    }

    /**
     * Calling Twitter's upload method. This method sets multiple owner for bandwidth saving
     */
    private void uploadMediaWithDefaultProvider(ParcelableStatusUpdate update, PendingStatusUpdate pendingUpdate)
            throws UploadException {
        // Return empty array if no media attached
        if (ArrayUtils.isEmpty(update.media)) return;
        List<UserKey> ownersList = new ArrayList<>();
        List<String> ownerIdsList = new ArrayList<>();
        for (ParcelableAccount item : update.accounts) {
            if (ParcelableAccount.Type.TWITTER.equals(ParcelableAccountUtils.getAccountType(item))) {
                // Add to owners list
                ownersList.add(item.account_key);
                ownerIdsList.add(item.account_key.getId());
            }
        }
        String[] ownerIds = ownerIdsList.toArray(new String[ownerIdsList.size()]);
        for (int i = 0; i < pendingUpdate.length; i++) {
            final ParcelableAccount account = update.accounts[i];
            String[] mediaIds;
            switch (ParcelableAccountUtils.getAccountType(account)) {
                case ParcelableAccount.Type.TWITTER: {
                    final TwitterUpload upload = MicroBlogAPIFactory.getInstance(context,
                            account.account_key, true, true, TwitterUpload.class);
                    if (pendingUpdate.sharedMediaIds != null) {
                        mediaIds = pendingUpdate.sharedMediaIds;
                    } else {
                        mediaIds = uploadAllMediaShared(upload, update, ownerIds, true);
                        pendingUpdate.sharedMediaIds = mediaIds;
                    }
                    break;
                }
                case ParcelableAccount.Type.FANFOU: {
                    // Nope, fanfou uses photo uploading API
                    mediaIds = null;
                    break;
                }
                case ParcelableAccount.Type.STATUSNET: {
                    // TODO use their native API
                    final TwitterUpload upload = MicroBlogAPIFactory.getInstance(context,
                            account.account_key, true, true, TwitterUpload.class);
                    mediaIds = uploadAllMediaShared(upload, update, ownerIds, false);
                    break;
                }
                default: {
                    mediaIds = null;
                    break;
                }
            }
            pendingUpdate.mediaIds[i] = mediaIds;
        }
        pendingUpdate.sharedMediaOwners = ownersList.toArray(new UserKey[ownersList.size()]);
    }

    private Status twitterUpdateStatus(MicroBlog microBlog, ParcelableStatusUpdate statusUpdate,
                                       PendingStatusUpdate pendingUpdate, String overrideText,
                                       int index) throws MicroBlogException {
        final StatusUpdate status = new StatusUpdate(overrideText);
        if (statusUpdate.in_reply_to_status != null) {
            status.inReplyToStatusId(statusUpdate.in_reply_to_status.id);
        }
        if (statusUpdate.repost_status_id != null) {
            status.setRepostStatusId(statusUpdate.repost_status_id);
        }
        if (statusUpdate.attachment_url != null) {
            status.setAttachmentUrl(statusUpdate.attachment_url);
        }
        if (statusUpdate.location != null) {
            status.location(ParcelableLocationUtils.toGeoLocation(statusUpdate.location));
            status.displayCoordinates(statusUpdate.display_coordinates);
        }
        final String[] mediaIds = pendingUpdate.mediaIds[index];
        if (mediaIds != null) {
            status.mediaIds(mediaIds);
        }
        status.possiblySensitive(statusUpdate.is_possibly_sensitive);
        return microBlog.updateStatus(status);
    }

    private void statusShortenCallback(StatusShortenerInterface shortener, PendingStatusUpdate pendingUpdate, UpdateStatusResult updateResult) {
        for (int i = 0; i < pendingUpdate.length; i++) {
            final StatusShortenResult shortenResult = pendingUpdate.statusShortenResults[i];
            final ParcelableStatus status = updateResult.statuses[i];
            if (shortenResult == null || status == null) continue;
            shortener.callback(shortenResult, status);
        }
    }

    private void mediaUploadCallback(MediaUploaderInterface uploader, PendingStatusUpdate pendingUpdate, UpdateStatusResult updateResult) {
        for (int i = 0; i < pendingUpdate.length; i++) {
            final MediaUploadResult uploadResult = pendingUpdate.mediaUploadResults[i];
            final ParcelableStatus status = updateResult.statuses[i];
            if (uploadResult == null || status == null) continue;
            uploader.callback(uploadResult, status);
        }
    }

    @Nullable
    private StatusShortenerInterface getStatusShortener(TwidereApplication app) throws UploaderNotFoundException, UploadException, ShortenerNotFoundException, ShortenException {
        final String shortenerComponent = mPreferences.getString(KEY_STATUS_SHORTENER, null);
        if (ServicePickerPreference.isNoneValue(shortenerComponent)) return null;

        final StatusShortenerInterface shortener = StatusShortenerInterface.getInstance(app, shortenerComponent);
        if (shortener == null) throw new ShortenerNotFoundException();
        try {
            shortener.checkService(new AbsServiceInterface.CheckServiceAction() {
                @Override
                public void check(@Nullable Bundle metaData) throws AbsServiceInterface.CheckServiceException {
                    if (metaData == null) throw new ExtensionVersionMismatchException();
                    final String extensionVersion = metaData.getString(METADATA_KEY_EXTENSION_VERSION_STATUS_SHORTENER);
                    if (!TextUtils.equals(extensionVersion, context.getString(R.string.status_shortener_service_interface_version))) {
                        throw new ExtensionVersionMismatchException();
                    }
                }
            });
        } catch (AbsServiceInterface.CheckServiceException e) {
            if (e instanceof ExtensionVersionMismatchException) {
                throw new ShortenException(context.getString(R.string.shortener_version_incompatible));
            }
            throw new ShortenException(e);
        }
        return shortener;
    }

    @Nullable
    private MediaUploaderInterface getMediaUploader(TwidereApplication app) throws UploaderNotFoundException, UploadException {
        final String uploaderComponent = mPreferences.getString(KEY_MEDIA_UPLOADER, null);
        if (ServicePickerPreference.isNoneValue(uploaderComponent)) return null;
        final MediaUploaderInterface uploader = MediaUploaderInterface.getInstance(app, uploaderComponent);
        if (uploader == null) {
            throw new UploaderNotFoundException(context.getString(R.string.error_message_media_uploader_not_found));
        }
        try {
            uploader.checkService(new AbsServiceInterface.CheckServiceAction() {
                @Override
                public void check(@Nullable Bundle metaData) throws AbsServiceInterface.CheckServiceException {
                    if (metaData == null) throw new ExtensionVersionMismatchException();
                    final String extensionVersion = metaData.getString(METADATA_KEY_EXTENSION_VERSION_MEDIA_UPLOADER);
                    if (!TextUtils.equals(extensionVersion, context.getString(R.string.media_uploader_service_interface_version))) {
                        throw new ExtensionVersionMismatchException();
                    }
                }
            });
        } catch (AbsServiceInterface.CheckServiceException e) {
            if (e instanceof ExtensionVersionMismatchException) {
                throw new UploadException(context.getString(R.string.uploader_version_incompatible));
            }
            throw new UploadException(e);
        }
        return uploader;
    }

    @NonNull
    private String[] uploadAllMediaShared(TwitterUpload upload, ParcelableStatusUpdate update,
                                          String[] ownerIds, boolean chucked) throws UploadException {
        String[] mediaIds = new String[update.media.length];
        for (int i = 0; i < update.media.length; i++) {
            ParcelableMediaUpdate media = update.media[i];
            final MediaUploadResponse resp;
            //noinspection TryWithIdenticalCatches
            Body body = null;
            try {
                final int index = i;
                body = getBodyFromMedia(context.getContentResolver(), Uri.parse(media.uri), new ContentLengthInputStream.ReadListener() {
                    @Override
                    public void onRead(long length, long position) {
                        stateCallback.onUploadingProgressChanged(index, position, length);
                    }
                });
                if (chucked) {
                    resp = uploadMediaChucked(upload, body, ownerIds);
                } else {
                    resp = upload.uploadMedia(body, ownerIds);
                }
            } catch (IOException e) {
                throw new UploadException(e);
            } catch (MicroBlogException e) {
                throw new UploadException(e);
            } finally {
                Utils.closeSilently(body);
            }
            mediaIds[i] = resp.getId();
        }
        return mediaIds;
    }


    private MediaUploadResponse uploadMediaChucked(final TwitterUpload upload, Body body,
                                                   String[] ownerIds) throws IOException, MicroBlogException {
        final String mediaType = body.contentType().getContentType();
        final long length = body.length();
        final InputStream stream = body.stream();
        MediaUploadResponse response = upload.initUploadMedia(mediaType, length, ownerIds);
        final int segments = length == 0 ? 0 : (int) (length / BULK_SIZE + 1);
        for (int segmentIndex = 0; segmentIndex < segments; segmentIndex++) {
            final int currentBulkSize = (int) Math.min(BULK_SIZE, length - segmentIndex * BULK_SIZE);
            final SimpleBody bulk = new SimpleBody(ContentType.OCTET_STREAM, null, currentBulkSize,
                    stream);
            upload.appendUploadMedia(response.getId(), segmentIndex, bulk);
        }
        response = upload.finalizeUploadMedia(response.getId());
        for (MediaUploadResponse.ProcessingInfo info = response.getProcessingInfo(); shouldWaitForProcess(info); info = response.getProcessingInfo()) {
            final long checkAfterSecs = info.getCheckAfterSecs();
            if (checkAfterSecs <= 0) {
                break;
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(checkAfterSecs));
            } catch (InterruptedException e) {
                break;
            }
            response = upload.getUploadMediaStatus(response.getId());
        }
        MediaUploadResponse.ProcessingInfo info = response.getProcessingInfo();
        if (info != null && MediaUploadResponse.ProcessingInfo.State.FAILED.equals(info.getState())) {
            final MicroBlogException exception = new MicroBlogException();
            ErrorInfo errorInfo = info.getError();
            if (errorInfo != null) {
                exception.setErrors(new ErrorInfo[]{errorInfo});
            }
            throw exception;
        }
        return response;
    }


    public static FileBody getBodyFromMedia(@NonNull final ContentResolver resolver,
                                            @NonNull final Uri mediaUri,
                                            @NonNull final ContentLengthInputStream.ReadListener
                                                    readListener) throws IOException {
        final String mediaType = resolver.getType(mediaUri);
        final InputStream is = resolver.openInputStream(mediaUri);
        if (is == null) {
            throw new FileNotFoundException(mediaUri.toString());
        }
        final long length = is.available();
        final ContentLengthInputStream cis = new ContentLengthInputStream(is, length);
        cis.setReadListener(readListener);
        final ContentType contentType;
        if (TextUtils.isEmpty(mediaType)) {
            contentType = ContentType.parse("application/octet-stream");
        } else {
            contentType = ContentType.parse(mediaType);
        }
        return new FileBody(cis, "attachment", length, contentType);
    }

    private boolean isDuplicate(Exception exception) {
        return exception instanceof MicroBlogException
                && ((MicroBlogException) exception).getErrorCode() == ErrorInfo.STATUS_IS_DUPLICATE;
    }

    private boolean shouldWaitForProcess(MediaUploadResponse.ProcessingInfo info) {
        if (info == null) return false;
        switch (info.getState()) {
            case MediaUploadResponse.ProcessingInfo.State.PENDING:
            case MediaUploadResponse.ProcessingInfo.State.IN_PROGRESS:
                return true;
            default:
                return false;
        }
    }


    private long saveDraft(String draftAction, ParcelableStatusUpdate statusUpdate) {
        final Draft draft = new Draft();
        draft.account_keys = ParcelableAccountUtils.getAccountKeys(statusUpdate.accounts);
        if (draftAction != null) {
            draft.action_type = draftAction;
        } else {
            draft.action_type = Draft.Action.UPDATE_STATUS;
        }
        draft.text = statusUpdate.text;
        draft.location = statusUpdate.location;
        draft.media = statusUpdate.media;
        final UpdateStatusActionExtra extra = new UpdateStatusActionExtra();
        extra.setInReplyToStatus(statusUpdate.in_reply_to_status);
        extra.setIsPossiblySensitive(statusUpdate.is_possibly_sensitive);
        extra.setRepostStatusId(statusUpdate.repost_status_id);
        extra.setDisplayCoordinates(statusUpdate.display_coordinates);
        draft.action_extras = extra;
        final ContentResolver resolver = context.getContentResolver();
        final Uri draftUri = resolver.insert(Drafts.CONTENT_URI, DraftValuesCreator.create(draft));
        if (draftUri == null) return -1;
        return NumberUtils.toLong(draftUri.getLastPathSegment(), -1);
    }

    static class PendingStatusUpdate {

        @Nullable
        String[] sharedMediaIds;
        UserKey[] sharedMediaOwners;

        final int length;

        @NonNull
        final String[] overrideTexts;
        @NonNull
        final String[][] mediaIds;

        @NonNull
        final MediaUploadResult[] mediaUploadResults;
        @NonNull
        final StatusShortenResult[] statusShortenResults;

        PendingStatusUpdate(int length, String defaultText) {
            this.length = length;
            overrideTexts = new String[length];
            mediaUploadResults = new MediaUploadResult[length];
            statusShortenResults = new StatusShortenResult[length];
            mediaIds = new String[length][];
            Arrays.fill(overrideTexts, defaultText);
        }

        static PendingStatusUpdate from(ParcelableStatusUpdate statusUpdate) {
            return new PendingStatusUpdate(statusUpdate.accounts.length,
                    statusUpdate.text);
        }
    }

    public static class UpdateStatusResult {
        @NonNull
        public final ParcelableStatus[] statuses;
        @NonNull
        public final MicroBlogException[] exceptions;

        public final UpdateStatusException exception;

        public UpdateStatusResult(@NonNull ParcelableStatus[] statuses, @NonNull MicroBlogException[] exceptions) {
            this.statuses = statuses;
            this.exceptions = exceptions;
            this.exception = null;
        }

        public UpdateStatusResult(UpdateStatusException exception) {
            this.exception = exception;
            this.statuses = new ParcelableStatus[0];
            this.exceptions = new MicroBlogException[0];
        }
    }


    public static class UpdateStatusException extends Exception {
        public UpdateStatusException() {
            super();
        }

        public UpdateStatusException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public UpdateStatusException(Throwable throwable) {
            super(throwable);
        }

        public UpdateStatusException(final String message) {
            super(message);
        }
    }

    public static class UploaderNotFoundException extends UpdateStatusException {

        public UploaderNotFoundException() {
            super();
        }

        public UploaderNotFoundException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public UploaderNotFoundException(Throwable throwable) {
            super(throwable);
        }

        public UploaderNotFoundException(String message) {
            super(message);
        }
    }

    public static class UploadException extends UpdateStatusException {

        public UploadException() {
            super();
        }

        public UploadException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public UploadException(Throwable throwable) {
            super(throwable);
        }

        public UploadException(String message) {
            super(message);
        }
    }

    public static class ExtensionVersionMismatchException extends AbsServiceInterface.CheckServiceException {

    }

    public static class ShortenerNotFoundException extends UpdateStatusException {
    }

    public static class ShortenException extends UpdateStatusException {

        public ShortenException() {
            super();
        }

        public ShortenException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public ShortenException(Throwable throwable) {
            super(throwable);
        }

        public ShortenException(final String message) {
            super(message);
        }
    }

    public interface StateCallback {
        @WorkerThread
        void onStartUploadingMedia();

        @WorkerThread
        void onUploadingProgressChanged(int index, long current, long total);

        @WorkerThread
        void onShorteningStatus();

        @WorkerThread
        void onUpdatingStatus();

        @UiThread
        void afterExecute(Context handler, UpdateStatusResult result);

        @UiThread
        void beforeExecute();
    }
}
