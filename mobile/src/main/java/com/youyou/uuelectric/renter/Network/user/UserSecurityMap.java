package com.youyou.uuelectric.renter.Network.user;

import com.youyou.uuelectric.renter.Network.NetworkTask;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by taurusxi on 14-9-6.
 */
public class UserSecurityMap {


    public static final ConcurrentHashMap<Integer, SecurityItem> requestSecurityMap = new ConcurrentHashMap<>();


    public static void put(int seq, SecurityItem item) {

        requestSecurityMap.put(seq, item);
    }

    public static SecurityItem get(int seq) {
        return requestSecurityMap.get(seq);
    }

    public static void remove(int seq) {
        if (requestSecurityMap.contains(seq)) {
            requestSecurityMap.remove(seq);
        }
    }

    public static class SecurityItem {
        public NetworkTask networkItem;
        public boolean isPublic;
        public byte[] b3KeyItem;
        public byte[] b2Item;
        public byte[] b3Item;
        public byte[] sessionKeyItem;


        public SecurityItem(final NetworkTask networkItem, boolean isPublic, byte[] b3KeyItem, byte[] b2Item, byte[] b3Item, byte[] sessionKeyItem) {
            this.networkItem = networkItem;
            this.isPublic = isPublic;
            this.b3KeyItem = b3KeyItem;
            this.b2Item = b2Item;
            this.b3Item = b3Item;
            this.sessionKeyItem = sessionKeyItem;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("isPublic:").append(isPublic).append("_");
            stringBuilder.append("sessionKeyItem:").append(sessionKeyItem).append("_");
            stringBuilder.append("networkItem:").append(networkItem.toString()).append("_");
            return stringBuilder.toString();
        }

    }


}
