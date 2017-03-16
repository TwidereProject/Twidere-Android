package org.mariotaku.bitpay.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Mariotaku on 2017/3/16.
 */
@JsonObject
public class BuyerInfo {
    @JsonField(name = "name")
    String name;
    @JsonField(name = "address1")
    String address1;
    @JsonField(name = "address2")
    String address2;
    @JsonField(name = "locality")
    String locality;
    @JsonField(name = "region")
    String region;
    @JsonField(name = "postalCode")
    String postalCode;
    @JsonField(name = "email")
    String email;
    @JsonField(name = "phone")
    String phone;

    public String getName() {
        return name;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getLocality() {
        return locality;
    }

    public String getRegion() {
        return region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
