

package twitter4j;

import java.io.Serializable;
import java.util.Map;


public interface MediaEntity extends UrlEntity {

    long getId();


    Map<String, Feature> getFeatures();

    String getMediaUrl();


    String getMediaUrlHttps();


    Map<String, Size> getSizes();


    Type getType();

    enum Type {
        PHOTO, VIDEO, ANIMATED_GIF, UNKNOWN;

        public static Type parse(String typeString) {
            if ("photo".equalsIgnoreCase(typeString)) {
                return PHOTO;
            } else if ("video".equalsIgnoreCase(typeString)) {
                return VIDEO;
            } else if ("animated_gif".equalsIgnoreCase(typeString)) {
                return ANIMATED_GIF;
            }
            return UNKNOWN;
        }
    }

    VideoInfo getVideoInfo();

    interface VideoInfo {

        Variant[] getVariants();

        long[] getAspectRatio();

        long getDuration();

        interface Variant {

            String getContentType();

            String getUrl();

            long getBitrate();
        }

    }

    interface Size {
        String THUMB = "thumb";
        String SMALL = "small";
        String MEDIUM = "medium";
        String LARGE = "large";
        int FIT = 100;
        int CROP = 101;

        int getHeight();

        String getResize();

        int getWidth();
    }

    /**
     * Created by mariotaku on 15/3/31.
     */
    interface Feature {

        interface Face {

            int getX();

            int getY();

            int getHeight();

            int getWidth();
        }
    }
}
