/*
 * This file is a part of project QuickShop, the name is ListOfLanguagesForOTA.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Util.OneSkyAppPlatform;

import java.util.List;

public class ListOfLanguagesForOTA {

    /**
     * meta : {"status":200,"record_count":3}
     * data : [{"code":"en-US","english_name":"English (United States)","local_name":"English (United States)","locale":"en","region":"US","is_base_language":true,"is_ready_to_publish":true,"translation_progress":"100%","uploaded_at":"2013-10-07T15:27:10+0000","uploaded_at_timestamp":1381159630},{"code":"ja-JP","english_name":"Japanese","local_name":"日本語","locale":"ja","region":"JP","is_base_language":false,"is_ready_to_publish":true,"translation_progress":"98%","uploaded_at":"2013-10-07T15:27:10+0000","uploaded_at_timestamp":1381159630},{"code":"ko-KR","english_name":"Korean","local_name":"한국어","locale":"ko","region":"KR","is_base_language":false,"is_ready_to_publish":true,"translation_progress":"56%","uploaded_at":"2013-10-07T15:27:10+0000","uploaded_at_timestamp":1381159630},"..."]
     */

    private MetaBean meta;
    private List<DataBean> data;

    public MetaBean getMeta() {
        return meta;
    }

    public void setMeta(MetaBean meta) {
        this.meta = meta;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class MetaBean {
        /**
         * status : 200
         * record_count : 3
         */

        private int status;
        private int record_count;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getRecord_count() {
            return record_count;
        }

        public void setRecord_count(int record_count) {
            this.record_count = record_count;
        }
    }

    public static class DataBean {
        /**
         * code : en-US
         * english_name : English (United States)
         * local_name : English (United States)
         * locale : en
         * region : US
         * is_base_language : true
         * is_ready_to_publish : true
         * translation_progress : 100%
         * uploaded_at : 2013-10-07T15:27:10+0000
         * uploaded_at_timestamp : 1381159630
         */

        private String code;
        private String english_name;
        private String local_name;
        private String locale;
        private String region;
        private boolean is_base_language;
        private boolean is_ready_to_publish;
        private String translation_progress;
        private String uploaded_at;
        private int uploaded_at_timestamp;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getEnglish_name() {
            return english_name;
        }

        public void setEnglish_name(String english_name) {
            this.english_name = english_name;
        }

        public String getLocal_name() {
            return local_name;
        }

        public void setLocal_name(String local_name) {
            this.local_name = local_name;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public boolean isIs_base_language() {
            return is_base_language;
        }

        public void setIs_base_language(boolean is_base_language) {
            this.is_base_language = is_base_language;
        }

        public boolean isIs_ready_to_publish() {
            return is_ready_to_publish;
        }

        public void setIs_ready_to_publish(boolean is_ready_to_publish) {
            this.is_ready_to_publish = is_ready_to_publish;
        }

        public String getTranslation_progress() {
            return translation_progress;
        }

        public void setTranslation_progress(String translation_progress) {
            this.translation_progress = translation_progress;
        }

        public String getUploaded_at() {
            return uploaded_at;
        }

        public void setUploaded_at(String uploaded_at) {
            this.uploaded_at = uploaded_at;
        }

        public int getUploaded_at_timestamp() {
            return uploaded_at_timestamp;
        }

        public void setUploaded_at_timestamp(int uploaded_at_timestamp) {
            this.uploaded_at_timestamp = uploaded_at_timestamp;
        }
    }
}
